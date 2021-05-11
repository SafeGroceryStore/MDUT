package Dao;


import Util.Utils;
import java.io.File;
import java.net.URLDecoder;
import java.sql.*;
import org.json.JSONArray;
import org.json.JSONObject;

/**
 * @author ch1ng
 */
public class ManagerDao {

    private String CLASS_NAME = "org.sqlite.JDBC";
    private String DB_PATH = "data.db";
    private String DB_URL;
    private Connection connection;

    public ManagerDao() throws Exception {
        DB_PATH = URLDecoder.decode(Utils.getSelfPath(), "UTF-8") + File.separator + DB_PATH;
        //System.out.println(DB_PATH);
        DB_URL = "jdbc:sqlite:" + DB_PATH;
        if (!(new File(DB_PATH)).exists()) {
            throw new Exception("数据库文件丢失，无法启动。");
        } else {
            Class.forName(CLASS_NAME);
            connection = DriverManager.getConnection(DB_URL);
            connection.setAutoCommit(true);
        }
    }

    /**
     * 关闭连接
     */
    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception var2) {
            var2.printStackTrace();
        }

    }

    /**
     * 获取用户所有的数据库连接
     * @return
     * @throws Exception
     */
    public JSONArray listDatabases() throws Exception {
        JSONArray result = new JSONArray();
        Statement statement = connection.createStatement();
        ResultSet rs = statement.executeQuery("select * from data");
        ResultSetMetaData rsmd = rs.getMetaData();
        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();

            for(int i = 1; i <= numColumns; ++i) {
                String columnName = rsmd.getColumnName(i);
                obj.put(columnName, rs.getObject(columnName));
            }
            result.put((Object)obj);
        }
        return result;
    }

    /**
     * 根据 ID 查找对应的数据
     * @param id
     * @return
     * @throws Exception
     */
    public JSONArray findDataByid(String id) throws Exception {
        JSONArray result = new JSONArray();
        Statement statement = connection.createStatement();
        String sql = String.format("select * from data where id = \"%s\"",id);
        ResultSet rs = statement.executeQuery(sql);
        ResultSetMetaData rsmd = rs.getMetaData();
        while(rs.next()) {
            int numColumns = rsmd.getColumnCount();
            JSONObject obj = new JSONObject();
            for(int i = 1; i <= numColumns; ++i) {
                String columnName = rsmd.getColumnName(i);
                obj.put(columnName, rs.getObject(columnName));
            }
            result.put((Object)obj);
        }
        return result;
    }

    /**
     * 添加数据到数据库
     * @param databasetype
     * @param ipaddress
     * @param port
     * @param username
     * @param password
     * @param database
     * @param timeout
     * @param memo
     * @param ishttp
     * @param url
     * @param encryptionkey
     * @param isproxy
     * @param proxytype
     * @param proxyaddress
     * @param proxyport
     * @param proxyusername
     * @param proxypassword
     * @param httpheaders
     * @param connecttype
     * @param addtime
     * @return
     * @throws Exception
     */
    public int addDatebase(String databasetype,String ipaddress,String port,String username,String password,String database,String timeout,String memo,String ishttp,String url,String encryptionkey,String isproxy,String proxytype,String proxyaddress,String proxyport,String proxyusername,String proxypassword,String httpheaders,String connecttype,String addtime) throws Exception {
        String sql = "INSERT INTO main.data(databasetype, ipaddress, port, username, password, database, timeout, memo, ishttp, url, encryptionkey, isproxy, proxytype, proxyaddress, proxyport, proxyusername, proxypassword, httpheaders, connecttype, addtime) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, databasetype);
        statement.setString(2, ipaddress);
        statement.setString(3, port);
        statement.setString(4, username);
        statement.setString(5, password);
        statement.setString(6, database);
        statement.setString(7, timeout);
        statement.setString(8, memo);
        statement.setString(9, ishttp);
        statement.setString(10, url);
        statement.setString(11, encryptionkey);
        statement.setString(12, isproxy);
        statement.setString(13, proxytype);
        statement.setString(14, proxyaddress);
        statement.setString(15, proxyport);
        statement.setString(16, proxyusername);
        statement.setString(17, proxypassword);
        statement.setString(18, httpheaders);
        statement.setString(19, connecttype);
        statement.setString(20, addtime);
        return statement.executeUpdate();

    }

    /**
     * 修改其中一条数据
     * @param databasetype
     * @param ipaddress
     * @param port
     * @param username
     * @param password
     * @param database
     * @param timeout
     * @param memo
     * @param ishttp
     * @param url
     * @param encryptionkey
     * @param isproxy
     * @param proxytype
     * @param proxyaddress
     * @param proxyport
     * @param proxyusername
     * @param proxypassword
     * @param httpheaders
     * @param connecttype
     * @param id
     * @return
     * @throws Exception
     */
    public int updateDatebase(String databasetype,String ipaddress,String port,String username,String password,String database,String timeout,String memo,String ishttp,String url,String encryptionkey,String isproxy,String proxytype,String proxyaddress,String proxyport,String proxyusername,String proxypassword,String httpheaders,String connecttype,String id) throws Exception {
        String sql = "UPDATE data SET databasetype = ?, ipaddress = ?, port = ?, username = ?, password = ?, database = ?, timeout = ?, memo = ?, ishttp = ?, url = ?, encryptionkey = ?, isproxy = ?, proxytype = ?, proxyaddress = ?, proxyport = ?, proxyusername = ?, proxypassword = ?, httpheaders = ?, connecttype = ? WHERE id = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, databasetype);
        statement.setString(2, ipaddress);
        statement.setString(3, port);
        statement.setString(4, username);
        statement.setString(5, password);
        statement.setString(6, database);
        statement.setString(7, timeout);
        statement.setString(8, memo);
        statement.setString(9, ishttp);
        statement.setString(10, url);
        statement.setString(11, encryptionkey);
        statement.setString(12, isproxy);
        statement.setString(13, proxytype);
        statement.setString(14, proxyaddress);
        statement.setString(15, proxyport);
        statement.setString(16, proxyusername);
        statement.setString(17, proxypassword);
        statement.setString(18, httpheaders);
        statement.setString(19, connecttype);
        statement.setString(20, id);
        return statement.executeUpdate();

    }

    /**
     * 根据 ID 删除对应的数据
     * @param id
     * @return
     * @throws Exception
     */
    public int delDatebaseById(String id) throws Exception {
        String sql = "DELETE FROM data WHERE ID = ?";
        PreparedStatement statement = connection.prepareStatement(sql);
        statement.setString(1, id);
        return statement.executeUpdate();
    }



}
