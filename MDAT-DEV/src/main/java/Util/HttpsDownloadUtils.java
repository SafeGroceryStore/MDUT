package Util;


import javax.net.ssl.*;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

/**
 * @author ch1ng
 * @date 2021/8/17
 */

public class HttpsDownloadUtils {

    /**
     * @param fileUrl   https 远程路径
     * @param fileLocal 本地文件存放路径,需要注意的是这里是要传一个已经存在的文件，否则会报拒绝访问的的错误
     * @throws Exception
     */
    public static void downloadFile(String fileUrl, String fileLocal) throws Exception {
        SSLContext sslcontext = SSLContext.getInstance("SSL", "SunJSSE");
        sslcontext.init(null, new TrustManager[]{new X509TrustUtiil()}, new java.security.SecureRandom());
        URL url = new URL(fileUrl);
        HostnameVerifier ignoreHostnameVerifier = new HostnameVerifier() {
            @Override
            public boolean verify(String s, SSLSession sslsession) {
                return true;
            }
        };
        HttpsURLConnection.setDefaultHostnameVerifier(ignoreHostnameVerifier);
        HttpsURLConnection.setDefaultSSLSocketFactory(sslcontext.getSocketFactory());
        HttpsURLConnection urlCon = (HttpsURLConnection) url.openConnection();
        urlCon.setConnectTimeout(6000);
        urlCon.setReadTimeout(6000);
        int code = urlCon.getResponseCode();
        if (code != HttpURLConnection.HTTP_OK) {
            throw new Exception("文件读取失败");
        }
        // 读文件流
        DataInputStream in = new DataInputStream(urlCon.getInputStream());
        DataOutputStream out = new DataOutputStream(new FileOutputStream(fileLocal));
        byte[] buffer = new byte[2048];
        int count = 0;
        while ((count = in.read(buffer)) > 0) {
            out.write(buffer, 0, count);
        }
        out.close();
        in.close();

    }

    /**
     * X509Trust
     */
    static class X509TrustUtiil implements X509TrustManager {

        @Override
        public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {

        }

        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return null;
        }

    }

    public static void main(String[] args) throws Exception {
        downloadFile("https://github.com/SafeGroceryStore/MDUT/releases/download/v2.0.5/MDUT.jar", "/Users/ch1ng" +
                "/Workspaces/Git/MDUT/MDAT-DEV/target/MDUT123.jar");
    }
}
