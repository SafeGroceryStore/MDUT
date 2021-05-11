package Util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;


public class HttpsClientUtil {
    private final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    private static void trustAllHosts() {
        // Create a trust manager that does not validate certificate chains
        TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new java.security.cert.X509Certificate[]{};
            }

            public void checkClientTrusted(X509Certificate[] chain, String authType) {
            }

            public void checkServerTrusted(X509Certificate[] chain, String authType) {
            }

            @Override
            public void checkClientTrusted(
                    java.security.cert.X509Certificate[] arg0, String arg1)
                    throws CertificateException {
                // TODO Auto-generated method stub

            }

            @Override
            public void checkServerTrusted(
                    java.security.cert.X509Certificate[] arg0, String arg1)
                    throws CertificateException {
                // TODO Auto-generated method stub

            }
        }};
        // Install the all-trusting trust manager
        try {
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static String sendHtpps(String url) {
        String result = "";
        OutputStreamWriter out = null;
        BufferedReader in = null;
        HttpURLConnection conn;
        try {
            trustAllHosts();
            URL realUrl = new URL(null,url,new sun.net.www.protocol.https.Handler());
            //通過請求地址判斷請求型別(http或者是https)
            if (realUrl.getProtocol().toLowerCase().equals("https")) {
                HttpsURLConnection https = (HttpsURLConnection) realUrl.openConnection();
                https.setHostnameVerifier(DO_NOT_VERIFY);
                conn = https;
            } else {
                conn = (HttpURLConnection) realUrl.openConnection();
            }
            // 設定通用的請求屬性
            conn.setRequestProperty("accept", "*/*");
            conn.setRequestProperty("connection", "Keep-Alive");
            conn.setRequestProperty("user-agent",
                    "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            conn.setRequestProperty("Content-Type","application/x-www-form-urlencoded; charset=UTF-8");
            // 傳送POST請求必須設定如下兩行
            conn.setDoOutput(true);
            conn.setDoInput(true);
            // 獲取URLConnection物件對應的輸出流
//            out = new OutputStreamWriter(conn.getOutputStream(), "UTF-8");
//            out.write(a);
//            //錯誤方式，這種方式容易出現亂碼
//            // PrintWriter out = new PrintWriter(connection.getOutputStream());
//            /*out.print(a);*/
//            // flush輸出流的緩衝
//            out.flush();
            // 定義BufferedReader輸入流來讀取URL的響應
            in = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
            String line;
            while ((line = in.readLine()) != null) {
                result += line;
            }
        } catch (Exception e) {
            result = "ERROR://" + e.getMessage();
        } finally {// 使用finally塊來關閉輸出流、輸入流
            try {
                if (out != null) {
                    out.close();
                }
                if (in != null) {
                    in.close();
                }
            } catch (IOException ex) {
                result = "ERROR://" + ex.getMessage();
            }
        }
        return result;
    }

}