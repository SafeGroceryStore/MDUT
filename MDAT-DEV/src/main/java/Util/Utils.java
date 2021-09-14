package Util;

import com.alibaba.fastjson.JSONObject;

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
     * 当前软件版本
     * @return
     */
    public static String getCurrentVersion() {
        return "v2.0.6";
    }

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
     * 获取当前时间
     * @return
     */
    public static String currentTime() {
        Date date = new Date();
        DateFormat df = new SimpleDateFormat("yyyyMMddHHmmss", Locale.US);
        String time = df.format(date);
        return time;
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

    /**
     * 分割盘符，在windows下时候需要分割盘符，linux则直接返回
     * @param str
     * @return
     */
    public static ArrayList<String> splitDisk(String str){
        ArrayList<String> res = new ArrayList<String>();
        str = str.replace("\n","");
        // 有 : 则代表是 windows，就需要分割盘符
        if(str.contains(":")){
            String[] disk = str.split(":");
            for (String i : disk){
                res.add(i);
            }
        }else {
            res.add(str);
        }
        return res;
    }

    public static ArrayList<String> splitFiles(String str){
        ArrayList<String> res = new ArrayList<String>();
        String[] files = str.split("\n");
        for (String file : files){
            res.add(file);
        }
        return res;
    }


    /**
     * 检测是否需要更新
     * @return
     */
    public static JSONObject checkVersion() throws Exception {
        String update = "false";
        String currentVersion = Utils.getCurrentVersion();
        String url = "https://api.github.com/repos/SafeGroceryStore/MDUT/releases/latest";
        //获取json数据
        String jsonStringData = HttpUtil.httpReuest(url,"GET","","","");
        //解析 json
        JSONObject jsonData = JSONObject.parseObject(jsonStringData);
        //获取当前最新版本
        String newVersion = jsonData.getString("tag_name");
        String downloadUrl = "";
        String name = "";
        String body = "";
        //检查版本号是否相同，如果等于 -1 就代表有版本更新，将下载链接解析回来
        //同时设置 update 为 true
        int verComp = compareVersion(currentVersion.replace("v",""),newVersion.replace("v",""));
        if(verComp == -1){
            String tempDate = jsonData.getJSONArray("assets").getString(0);
            downloadUrl =  JSONObject.parseObject(tempDate).getString("browser_download_url");
            name =  JSONObject.parseObject(tempDate).getString("name");
            body = jsonData.getString("body");
            update = "true";
        }
        // 将结果转成 JSONObject 格式方便解析
        JSONObject resultData = new JSONObject();
        resultData.put("isupdate",update);
        resultData.put("version",newVersion);
        resultData.put("name",name);
        resultData.put("body",body);
        resultData.put("downloadurl",downloadUrl);
        return resultData;
    }

    /**
     * 比较版本大小
     *
     * 说明：支n位基础版本号+1位子版本号
     * 示例：1.0.2>1.0.1 , 1.0.1.1>1.0.1
     * 来源：https://www.cnblogs.com/hdwang/p/8603061.html
     * @param curVer 当前版本
     * @param newVer 新版版本
     * @return 0:相同 1:curVer 大于 newVer -1:curVer 小于 newVer
     */
    public static int compareVersion(String curVer, String newVer) {
        if (curVer.equals(newVer)) {
            //版本相同
            return 0;
        }
        String[] v1Array = curVer.split("\\.");
        String[] v2Array = newVer.split("\\.");
        int v1Len = v1Array.length;
        int v2Len = v2Array.length;
        //基础版本号位数（取长度小的）
        int baseLen = 0;
        if(v1Len > v2Len){
            baseLen = v2Len;
        }else{
            baseLen = v1Len;
        }
        //基础版本号比较
        for(int i=0;i<baseLen;i++){
            //同位版本号相同
            if(v1Array[i].equals(v2Array[i])){
                //比较下一位
                continue;
            }else{
                return Integer.parseInt(v1Array[i])>Integer.parseInt(v2Array[i]) ? 1 : -1;
            }
        }
        //基础版本相同，再比较子版本号
        if(v1Len != v2Len){
            return v1Len > v2Len ? 1:-1;
        }else {
            //基础版本相同，无子版本号
            return 0;
        }
    }

}
