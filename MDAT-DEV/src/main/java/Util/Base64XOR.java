package Util;

/**
 * @author ch1ng
 * @date 2022/4/8
 */
import org.apache.commons.codec.binary.Base64;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

public class Base64XOR {

    /**
     * base64编码
     *
     * @param str
     * @param key
     * @return
     */

    public static String encode(String str, String key) {
        try {
            return base64Encode(xorWithKey(str.getBytes("UTF-8"), key.getBytes()),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * base64解码
     *
     * @param str
     * @param key
     * @return
     */
    public static String decode(String str, String key) {
        try {

            return new String(xorWithKey(base64Decode(str), key.getBytes()),"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * base64编码
     *
     * @param s
     * @param key
     * @return
     */

    public static String encode(String s, String key,String code) {
        try {
            return base64Encode(xorWithKey(s.getBytes(code), key.getBytes()),code);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * base64解码
     * @param s
     * @param key
     * @return
     */
    public static String decode(String s, String key,String code) {
        try {

            return new String(xorWithKey(base64Decode(s), key.getBytes()),code);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static byte[] base64Decode(String s) {
        return Base64.decodeBase64(s);
    }

    public static String base64Decode(String s,String code) {
        try {
            return new String(Base64.decodeBase64(s),code);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public static String base64Encode(byte[] bytes,String code) {
        byte[] encodeBase64 = Base64.encodeBase64(bytes);
        try {
            return new String(encodeBase64,code);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 两个字符串异或
     *
     * @param a
     * @param key
     * @return
     */
    public static byte[] xorWithKey(byte[] a, byte[] key) {
        byte[] out = new byte[a.length];
        for (int i = 0; i < a.length; i++) {
            out[i] = (byte) (a[i] ^ key[i % key.length]);
        }
        return out;
    }

}
