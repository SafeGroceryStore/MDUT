package Util;

import java.io.*;
import java.sql.Driver;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.sql.DriverManager;

/**
 * 工具类 - Exception 全部往上层抛
 * @author ch1ng
 */
public class Utils {

    /**
     * 获取当前软件路径
     * @return
     */
    public static String getSelfPath() throws IOException {
        File file = getFile();
        if(file==null) {
            return null;
        }
        return getFile().getParent();
    }


    private static File getFile() {
        //关键是这行...
        String path = Utils.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try{
            //转换处理中文及空格
            path = java.net.URLDecoder.decode(path, "UTF-8");
        }catch (java.io.UnsupportedEncodingException e){
            return null;
        }
        return new File(path);
    }

    /**
     * 获取当前时间并返回为 String 类型
     * @return
     */
    public static String getCurrentTimeToString() {
        String currentTime = null;
        Date date = new Date();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        currentTime = formatter.format(date);
        return currentTime;
    }

    /**
     * 获取一个 8 位的随机字符串
     * @return
     */
    public static String getRandomString() {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        int length = 8;
        for(int i = 0; i < length; ++i) {
            int number = random.nextInt(62);
            sb.append(str.charAt(number));
        }
        return sb.toString();
    }

    /**
     * byte数组转16进制字符串
     * @param bArray
     * @return
     */
    public static String bytesToHexString(byte[] bArray) {
        StringBuffer sb = new StringBuffer(bArray.length);
        String sTemp;
        for (int i = 0; i < bArray.length; i++) {
            sTemp = Integer.toHexString(0xFF & bArray[i]);
            if (sTemp.length() < 2) {
                sb.append(0);
            }
            sb.append(sTemp.toUpperCase());
        }
        return sb.toString();
    }

    /**
     * 日志格式化
     * @param info
     */
    public static String log(String info) {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
        String time = df.format(date);
        return "[*] " + time + " - " + trimN(info) + "\n";
    }

    /**
     * 忘了干嘛用了，好像是过滤最后一位换行符的
     * @param str
     * @return
     */
    private static String trimN(String str){
        int len = str.length();
        int st = 0;
        char[] val = str.toCharArray();

        while ((st < len) && (val[st] <= '\r')) {
            st++;
        }
        while ((st < len) && (val[len - 1] <= '\r')) {
            len--;
        }
        return ((st > 0) || (len < str.length())) ? str.substring(st, len) : str;
    }

    /**
     * 读取文件内容
     * @param path
     * @return
     */
    public static String readFile(String path){
        File file = new File(path);
        StringBuilder result = new StringBuilder();
        try{
            //构造一个BufferedReader类来读取文件
            BufferedReader br = new BufferedReader(new FileReader(file));
            String s = null;
            //使用readLine方法，一次读一行
            while((s = br.readLine())!=null){
                result.append(System.lineSeparator()+s);
            }
            br.close();
        }catch(Exception e){
            e.printStackTrace();
        }
        return result.toString();
    }


    public static List<String> getStrList(String inputString, int length) {
        int size = inputString.length() / length;
        if (inputString.length() % length != 0) {
            size += 1;
        }
        return getStrList(inputString, length, size);
    }

    public static List<String> getStrList(String inputString, int length,
                                          int size) {
        List<String> list = new ArrayList<String>();
        for (int index = 0; index < size; index++) {
            String childStr = substring(inputString, index * length,
                    (index + 1) * length);
            list.add(childStr);
        }
        return list;
    }

    public static String substring(String str, int f, int t) {
        if (f > str.length()) {
            return null;
        }
        if (t > str.length()) {
            return str.substring(f, str.length());
        } else {
            return str.substring(f, t);
        }
    }


    /**
     * 获取文件的 hex 值。
     * @param path
     * @return
     * @throws Exception
     */
    public static String binToHexString(String path) {
        StringBuffer sb = new StringBuffer();
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(path));
            while (input.available() > 0) {
                String hex = String.format("%02x", input.readByte() & 0xFF);
                sb.append(hex);
            }
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e,e.getMessage());
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }

    /**
     * 返回路径上一个目录
     * @param path
     * @return
     */
    public static String getBeforePath(String path) {
        StringBuffer newpath = new StringBuffer();
        ArrayList<String> arrpath = new ArrayList<String>();
        arrpath.addAll(Arrays.asList(path.split("/")));
        if (arrpath.size() == 1) {
            return path;
        }
        arrpath.remove(arrpath.size() - 1);
        for (String x : arrpath) {
            newpath.append(x + "/");
        }
        return newpath.toString();
    }

    /**
     * 向文件写入byte[]
     *
     * @param fileName 文件名
     * @param bytes    字节内容
     * @param append   是否追加
     * @throws IOException
     */
    public static void writeFileByBytes(String fileName, byte[] bytes, boolean append) {
        try(OutputStream out = new BufferedOutputStream(new FileOutputStream(fileName, append))){
            out.write(bytes);
        }catch (Exception e){
            MessageUtil.showExceptionMessage(e,e.getMessage());
        }
    }


    /**
     * hex 转 btye
     * @param hex
     * @return
     */
    public static byte[] hexToByte(String hex){
        int m = 0, n = 0;
        // 每两个字符描述一个字节
        int byteLen = hex.length() / 2;
        byte[] ret = new byte[byteLen];
        for (int i = 0; i < byteLen; i++) {
            m = i * 2 + 1;
            n = m + 1;
            int intVal = Integer.decode("0x" + hex.substring(i * 2, m) + hex.substring(m, n));
            ret[i] = Byte.valueOf((byte)intVal);
        }
        return ret;
    }

    /**
     * 重新排列 registeredDrivers 的值，使得当前使用的数据库驱动在第一位
     * 避免出现错误的错误提示
     * @param currentDriverName
     * @throws SQLException
     */
    public static void regroupDrivers(String currentDriverName) throws SQLException {
        // 获取所有的初始化后的Drivers
        Enumeration drivers = DriverManager.getDrivers();
        // 循环
        while (drivers.hasMoreElements()){
            // 获取每个 Driver
            Driver driver = (Driver) drivers.nextElement();
            // 获取每个 Driver 的 CLassName
            String dname = driver.toString().toLowerCase(Locale.ROOT);
            // 对当前需要使用的 Driver 不做处理
            if(dname.indexOf(currentDriverName) != -1){
                continue;
            }else {
                /**
                 * 这里目的就是为了让 registeredDrivers 把对当前需要使用的 Driver
                 * 放到第一位
                 */
                // 删除 Driver
                DriverManager.deregisterDriver(driver);
                // 重新注册 Driver
                DriverManager.registerDriver(driver);
            }
        }

    }

    public static void main(String[] args) throws IOException {

//        String s = binToHexString("/Users/ch1ng/Library/Containers/com.tencent.xinWeChat/Data/Library/Application Support/com.tencent.xinWeChat/2.0b4.0.9/770eda2c1d02f3ce849961e7c746a25d/Message/MessageTemp/874e2e874e9428053cd8c51569337e5d/File/udf.dll");
        System.out.println(getSelfPath());
    }
}
