package Util;

import com.alibaba.fastjson.JSONObject;
import org.pegdown.PegDownProcessor;

import java.io.*;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
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

    private static List<String> uaList = new ArrayList();
    static {
        uaList.add("Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/41.0.2227.0 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 6.3; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/60.0.3112.90 Safari/537.36");
        uaList.add("Mozilla/5.01 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.132 Safari/537.36");
        uaList.add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; Shuame)");
        uaList.add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; QQBrowser/7.3.8126.400)");
        uaList.add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; WOW64; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E; .NET CLR 1.1.4322)");
        uaList.add("Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Win64; x64; Trident/4.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; .NET4.0C; .NET4.0E; InfoPath.3)");
        uaList.add("Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 6.1; Win64; x64; Trident/4.0; .NET CLR 2.0.50727; SLCC2; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0E)");
        uaList.add("Mozilla/5.0 (compatible; MSIE 9.0; Windows NT 6.1; WOW64; Trident/5.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; .NET4.0C; .NET4.0");
        uaList.add("Mozilla/5.0 (Macintosh mips64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh mips64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3904.108 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.114 Safari/537.36 Edg/89.0.774.75");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.128 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 11_2_1) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.192 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.106 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.90 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/77.0.3865.90 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.100 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/75.0.3770.80 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_4) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_3) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.100 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_2) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_14_0) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/607.3.10 (KHTML, like Gecko) Version/12.1.2 Safari/607.3.10 Maxthon/5.1.60");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/605.1.15 (KHTML, like Gecko) MicroMessenger/6.8.0(0x16080000) MacWechat/3.0.1(0x13000110) NetType/WIFI WindowsWechat");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.87 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_13_6) AppleWebKit/13605.3.8 (KHTML, like Gecko) Version/9.1.1 Safari/13605.3.8");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_12_6) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/12.1.1 Safari/605.1.15");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.122 Safari/537.36");
        uaList.add("Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/69.0.3497.100 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; LCTE; Core/1.70.3676.400 QQBrowser/10.4.3469.400; rv:11.0) like Gecko");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; Core/1.70.3776.400 QQBrowser/10.6.4212.400; rv:11.0) like Gecko");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64; Trident/7.0; .NET4.0C; .NET4.0E; .NET CLR 2.0.50727; .NET CLR 3.0.30729; .NET CLR 3.5.30729; Core/1.63.6788.400 QQBrowser/10.3.2727.400; rv:11.0) like Gecko");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.89 Safari/537.36 SLBrowser/7.0.0.2261 SLBChan/12");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/84.0.4147.105 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36 XiaoBai/10.3.3217.1573 (XBCEF)");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/80.0.3987.149 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/79.0.3945.136 YaBrowser/20.2.4.143 Yowser/2.5 Yptp/1.23 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/76.0.3809.87 Safari/537.36 SLBrowser/6.0.1.12161 SLBChan/103");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.200.124 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/67.0.1762.3 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/48.0.2564.116 Safari/537.36 2345Explorer/10.15.0.21066");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/546.36 (KHTML, like Gecko) Chrome/89.0.4385.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/543.36 (KHTML, like Gecko) Chrome/87.0.32496.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/542.36 (KHTML, like Gecko) Chrome/89.0.5219.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/542.36 (KHTML, like Gecko) Chrome/86.0.36322.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/540.36 (KHTML, like Gecko) Chrome/86.0.33219.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/538.36 (KHTML, like Gecko) Chrome/87.0.48110.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4466.0 Safari/537.36 Edg/91.0.859.0");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4455.2 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.72 Safari/537.36 Edg/90.0.818.39");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/90.0.4430.11 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4389.128 Safari/537.36 Edg/89.0.774.77");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/89.0.4350.7 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.50 Safari/537.36 Edg/88.0.705.29");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/88.0.4324.182 Safari/537.36 Edg/88.0.705.74");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.88 Safari/537.36 Edg/87.0.664.66");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.4280.67 Safari/537.36 OPR/73.0.3856.260");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/87.0.42434.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.56 Safari/537.36 Edg/83.0.478.33");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/82.0.4077.0 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4044.129 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/81.0.4023.0 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/78.0.3872.0 Safari/537.36 Edg/78.0.244.0");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.104 Safari/537.36 Edge/13.18362");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/536.36 (KHTML, like Gecko) Chrome/86.0.10846.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/535.36 (KHTML, like Gecko) Chrome/89.0.33519.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/533.36 (KHTML, like Gecko) Chrome/87.0.34697.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/530.36 (KHTML, like Gecko) Chrome/87.0.27523.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/528.36 (KHTML, like Gecko) Chrome/86.0.49343.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/525.36 (KHTML, like Gecko) Chrome/89.0.43907.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/511.36 (KHTML, like Gecko) Chrome/89.0.9922.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/509.36 (KHTML, like Gecko) Chrome/89.0.42050.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/508.36 (KHTML, like Gecko) Chrome/86.0.16571.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/506.36 (KHTML, like Gecko) Chrome/88.0.46354.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/504.36 (KHTML, like Gecko) Chrome/88.0.48271.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/503.36 (KHTML, like Gecko) Chrome/89.0.14272.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/503.36 (KHTML, like Gecko) Chrome/86.0.27485.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/500.36 (KHTML, like Gecko) Chrome/88.0.48357.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/499.36 (KHTML, like Gecko) Chrome/89.0.48906.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/498.36 (KHTML, like Gecko) Chrome/87.0.48788.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/496.36 (KHTML, like Gecko) Chrome/89.0.34528.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/494.36 (KHTML, like Gecko) Chrome/87.0.40937.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/491.36 (KHTML, like Gecko) Chrome/88.0.35623.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/491.36 (KHTML, like Gecko) Chrome/86.0.11902.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/490.36 (KHTML, like Gecko) Chrome/87.0.7030.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/489.36 (KHTML, like Gecko) Chrome/87.0.7809.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/483.36 (KHTML, like Gecko) Chrome/87.0.44790.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/482.36 (KHTML, like Gecko) Chrome/88.0.9787.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/481.36 (KHTML, like Gecko) Chrome/87.0.28829.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/476.36 (KHTML, like Gecko) Chrome/89.0.45365.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/473.36 (KHTML, like Gecko) Chrome/89.0.20219.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/473.36 (KHTML, like Gecko) Chrome/87.0.37035.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/472.36 (KHTML, like Gecko) Chrome/86.0.26591.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/471.36 (KHTML, like Gecko) Chrome/86.0.5210.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/469.36 (KHTML, like Gecko) Chrome/87.0.17682.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/466.36 (KHTML, like Gecko) Chrome/88.0.40585.82 Safari/537.36");
        uaList.add("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/460.36 (KHTML, like Gecko) Chrome/88.0.30832.82 Safari/537.36");
    }

    /**
     * 当前软件版本
     * @return
     */
    public static String getCurrentVersion() {
        return "v2.1.0";
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
     * 获取一个 5 - 15 位的随机字符串
     * @return
     */
    public static String getRandomString() {
        String str = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        int length = (int)(5 + Math.random() * (15 - 5 + 1));
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
    public static String readFile(String path) throws Exception {
        StringBuilder result = new StringBuilder();
        File file = new File(path);
        //构造一个BufferedReader类来读取文件
        BufferedReader br = new BufferedReader(new FileReader(file));
        String s = null;
        //使用readLine方法，一次读一行
        while((s = br.readLine())!=null){
            result.append(s + "\n");
        }
        br.close();
        return result.toString();
    }

    /**
     * 写文件
     * @param path
     * @param contents
     * @throws Exception
     */
    public static void writeFile(String path,String contents) throws Exception {
        File file = new File(path);
        if(!file.getParentFile().exists()){//如果文件夹不存在则创建
            file.getParentFile().mkdir();
        }
        if (!file.exists()) {// 如果文件不存在则创建
            file.createNewFile();
        } else {
            file.delete();
        }
        // 获取该文件的缓冲输出流
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(file), "UTF-8"));
        // 写入信息
        bufferedWriter.write(contents);
        bufferedWriter.flush();// 清空缓冲区
        bufferedWriter.close();// 关闭输出流


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


    /*
     * 字节数组转16进制字符串
     */
    public static String bytes2HexString(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();

        for (int i = 0; i < bytes.length; i++) {
            String hex = Integer.toHexString(0xFF & bytes[i]);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }

        return hexString.toString().toUpperCase();
    }


    /**
     * 读取文件内容转 Byte 数组
     * @param filename
     * @return
     */

    public static byte[] toByteArray(String filename) {
        FileChannel fc = null;
        byte[] result = null;
        try {
            fc = new RandomAccessFile(filename, "r").getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).load();
            //System.out.println(byteBuffer.isLoaded());
            result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return result;
    }

    ///**
    // * 获取文件的 hex 值。
    // * @param path
    // * @return
    // * @throws Exception
    // */
    //public static String binToHexString(String path) {
    //    StringBuffer sb = new StringBuffer();
    //    DataInputStream input = null;
    //    try {
    //        input = new DataInputStream(new FileInputStream(path));
    //        while (input.available() > 0) {
    //            String hex = String.format("%02x", input.readByte() & 0xFF);
    //            sb.append(hex);
    //        }
    //    } catch (Exception e) {
    //        MessageUtil.showExceptionMessage(e,e.getMessage());
    //    }
    //    //System.out.println(sb.toString());
    //    return sb.toString();
    //}

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
            if(dname.contains(currentDriverName)){
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

    /**
     * 分割http头参数,遵循规范：参数冒号空格值。eg：Cookie: a=b
     * @param str
     * @return
     */
    public static Map<String, String> splitHeaders(String str){
        Map<String, String> res = new HashMap<>();

        String[] headers = str.split("\n");
        for (String header : headers){
            String[] keyv = header.split(": ");
            res.put(keyv[0],keyv[1]);
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
        Map<String, String> headers = new HashMap<>();
        //获取json数据
        String jsonStringData = OKHttpUtil.getBodyWithGet(url,30,headers,"UTF-8",null);
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

    /**
     * 解析 MarkDown 语法，转换成 HTML 语法
     * @param markdownString
     * @throws IOException
     */
    public static String generateHtml(String markdownString) throws IOException {
        InputStream is = new ByteArrayInputStream(markdownString.getBytes(StandardCharsets.UTF_8));
        BufferedReader br = new BufferedReader(new InputStreamReader(is));
        String line = null;
        String mdContent = "";
        while ((line = br.readLine()) != null) {
            mdContent += line + "\n";
        }
        PegDownProcessor pdp = new PegDownProcessor(Integer.MAX_VALUE);
        String htmlContent = pdp.markdownToHtml(mdContent);
        return htmlContent;
    }

    /**
     * 获取随机 UA 头
     * @return
     */
    public static String randomUserAgent(){
        Random rnd = new Random();
        return uaList.get(rnd.nextInt(uaList.size()));
    }

    public static void openBrowse(String url) throws Exception {
        String osName = System.getProperty("os.name", "");// 获取操作系统的名字

        if (osName.startsWith("Windows")) {// windows
            Runtime.getRuntime().exec("rundll32 url.dll,FileProtocolHandler " + url);
        } else if (osName.startsWith("Mac OS")) {// Mac
            Class fileMgr = Class.forName("com.apple.eio.FileManager");
            Method openURL = fileMgr.getDeclaredMethod("openURL", String.class);
            openURL.invoke(null, url);
        } else {// Unix or Linux
            String[] browsers = {"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape"};
            String browser = null;
            for (int count = 0; count < browsers.length && browser == null; count++) { // 执行代码，在brower有值后跳出，
                // 这里是如果进程创建成功了，==0是表示正常结束。
                if (Runtime.getRuntime().exec(new String[]{"which", browsers[count]}).waitFor() == 0) {
                    browser = browsers[count];
                }
            }
            if (browser == null) {
                throw new RuntimeException("未找到任何可用的浏览器");
            } else {// 这个值在上面已经成功的得到了一个进程。
                Runtime.getRuntime().exec(new String[]{browser, url});
            }
        }
    }

}
