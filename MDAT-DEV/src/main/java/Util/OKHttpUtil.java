package Util;


import com.ejlchina.okhttps.HTTP;

import javax.net.ssl.*;
import java.io.UnsupportedEncodingException;
import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.PasswordAuthentication;
import java.net.Proxy;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static Util.Utils.randomUserAgent;

/**
 * @author ch1ng
 * @date 2021/11/12
 */
public class OKHttpUtil {

    public static X509TrustManager myTrustManager = new X509TrustManager() {

        @Override
        public void checkClientTrusted(X509Certificate[] chain, String authType) throws CertificateException {}

        @Override
        public void checkServerTrusted(X509Certificate[] chain, String authType) throws CertificateException {}


        @Override
        public X509Certificate[] getAcceptedIssuers() {
            return new X509Certificate[0];
        }
    };

    public static HostnameVerifier myHostnameVerifier = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public static String getCodeWithPost(String url, Map<String, String> params) throws Exception {
        return getCodeWithPost(url,30,null,params,null);
    }

    /**
     * 通过 POST 请求状态码
     * @param url
     * @return
     */
    public static String getCodeWithPost(String url, int timeout, Map<String, String> headers,
                                         Map<String, String> params,Map currentProxy) throws Exception {
        String result = null;
        HTTP http = null;
        Proxy proxy = null;
        try {
            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, new TrustManager[] { myTrustManager }, new SecureRandom());
            SSLSocketFactory mySSLSocketFactory = sslCtx.getSocketFactory();
            if(currentProxy != null){
                proxy = (Proxy) currentProxy.get("proxy");
                if(currentProxy.get("username") !=null && !currentProxy.get("username").equals("")){
                    String proxyUser = (String) currentProxy.get("username");
                    String proxyPassword = (String) currentProxy.get("password");
                    Authenticator.setDefault(new Authenticator(){
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                        }
                    });
                }
            }
            Proxy finalProxy = proxy;
            http = HTTP.builder().config(builder -> {
                builder.sslSocketFactory(mySSLSocketFactory,myTrustManager);
                builder.hostnameVerifier(myHostnameVerifier);
                builder.connectTimeout(timeout, TimeUnit.SECONDS);
                builder.writeTimeout(timeout, TimeUnit.SECONDS);
                builder.readTimeout(timeout, TimeUnit.SECONDS);
                builder.proxy(finalProxy);
            }).build();
            int statusCode = http.sync(url).addHeader(headers).addBodyPara(params).post().getStatus();
            result = "" + statusCode;
        }finally {
            http.cancelAll();
        }
        return result;
    }

    /**
     * 通过 Get 请求状态码
     * @param url
     * @return
     */
    public static String getCodeWithGet(String url, int timeout, Map<String, String> headers,Map currentProxy) {
        String result = null;
        HTTP http = null;
        Proxy proxy = null;
        try {
            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, new TrustManager[] { myTrustManager }, new SecureRandom());
            SSLSocketFactory mySSLSocketFactory = sslCtx.getSocketFactory();
            if(currentProxy != null){
                proxy = (Proxy) currentProxy.get("proxy");
                if(currentProxy.get("username") !=null && !currentProxy.get("username").equals("")){
                    String proxyUser = (String) currentProxy.get("username");
                    String proxyPassword = (String) currentProxy.get("password");
                    Authenticator.setDefault(new Authenticator(){
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                        }
                    });
                }
            }
            Proxy finalProxy = proxy;
            http = HTTP.builder().config(builder -> {
                builder.sslSocketFactory(mySSLSocketFactory,myTrustManager);
                builder.hostnameVerifier(myHostnameVerifier);
                builder.connectTimeout(timeout, TimeUnit.SECONDS);
                builder.writeTimeout(timeout, TimeUnit.SECONDS);
                builder.readTimeout(timeout, TimeUnit.SECONDS);
                builder.proxy(finalProxy);
            }).build();

            //随机 UA 头
            if(!headers.containsKey("User-Agent")){
                headers.put("User-Agent",randomUserAgent());
            }
            int statusCode = http.sync(url).addHeader(headers).get().getStatus();
            result = "" + statusCode;
        } catch (KeyManagementException e) {
            result = e.toString();
        } catch (NoSuchAlgorithmException e) {
            result = e.toString();
        } catch (Exception e){
            result = e.toString();
        }finally {
            http.cancelAll();
        }
        return result;
    }

    /**
     * 通过 Get 请求获取返回内容
     * @param url
     * @return
     */
    public static String getBodyWithGet(String url, int timeout, Map<String, String> headers,String coding, Map currentProxy) {
        String result = null;
        HTTP http = null;
        Proxy proxy = null;
        try {
            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, new TrustManager[] { myTrustManager }, new SecureRandom());
            SSLSocketFactory mySSLSocketFactory = sslCtx.getSocketFactory();
            if(currentProxy != null){
                proxy = (Proxy) currentProxy.get("proxy");
                if(currentProxy.get("username") !=null && !currentProxy.get("username").equals("")){
                    String proxyUser = (String) currentProxy.get("username");
                    String proxyPassword = (String) currentProxy.get("password");
                    Authenticator.setDefault(new Authenticator(){
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                        }
                    });
                }
            }
            Proxy finalProxy = proxy;
            http = HTTP.builder().config(builder -> {
                builder.sslSocketFactory(mySSLSocketFactory,myTrustManager);
                builder.hostnameVerifier(myHostnameVerifier);
                builder.connectTimeout(timeout, TimeUnit.SECONDS);
                builder.writeTimeout(timeout, TimeUnit.SECONDS);
                builder.readTimeout(timeout, TimeUnit.SECONDS);
                builder.proxy(finalProxy);
            }).build();
            //随机 UA 头
            if(!headers.containsKey("User-Agent")){
                headers.put("User-Agent",randomUserAgent());
            }
            byte[] resultByte = http.sync(url).addHeader(headers).get().getBody().toBytes();
            result = new String(resultByte,coding);
        } catch (UnsupportedEncodingException e) {
            result = e.toString();
        } catch (KeyManagementException e) {
            result = e.toString();
        } catch (NoSuchAlgorithmException e) {
            result = e.toString();
        } catch (Exception e){
            result = e.toString();
        }finally {
            http.cancelAll();
        }
        return result;
    }

    /**
     * 通过 Post 请求获取返回内容
     * @param url
     * @param params  提交的参数为key=value&key1=value1的形式
     */
    public static String getBodyWithPost(String url, Map<String, String> params,Map<String, String> headers,
                                         int timeout, String coding,Map currentProxy) {
        String result = null;
        HTTP http = null;
        Proxy proxy = null;
        try {
            SSLContext sslCtx = SSLContext.getInstance("TLS");
            sslCtx.init(null, new TrustManager[] { myTrustManager }, new SecureRandom());
            SSLSocketFactory mySSLSocketFactory = sslCtx.getSocketFactory();
            if(currentProxy != null){
                proxy = (Proxy) currentProxy.get("proxy");
                if(currentProxy.get("username") !=null && !currentProxy.get("username").equals("")){
                    String proxyUser = (String) currentProxy.get("username");
                    String proxyPassword = (String) currentProxy.get("password");
                    Authenticator.setDefault(new Authenticator(){
                        public PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(proxyUser, proxyPassword.toCharArray());
                        }
                    });
                }
            }
            Proxy finalProxy = proxy;
            http = HTTP.builder().config(builder -> {
                builder.sslSocketFactory(mySSLSocketFactory,myTrustManager);
                builder.hostnameVerifier(myHostnameVerifier);
                builder.connectTimeout(timeout, TimeUnit.SECONDS);
                builder.writeTimeout(timeout, TimeUnit.SECONDS);
                builder.readTimeout(timeout, TimeUnit.SECONDS);
                builder.proxy(finalProxy);
            }).build();
            //随机 UA 头
            if(!headers.containsKey("User-Agent")){
                headers.put("User-Agent",randomUserAgent());
            }
            byte[] resultByte = http.sync(url).addHeader(headers).addBodyPara(params).post().getBody().toBytes();
            //http.sync(url).get().getBody().toFile("").start();
            //byte[] resultByte = http.sync(url).addHeader(headers).get().getBody().toBytes();
            result = new String(resultByte,coding);
        } catch (UnsupportedEncodingException e) {
            result = e.toString();
        } catch (KeyManagementException e) {
            result = e.toString();
        } catch (NoSuchAlgorithmException e) {
            result = e.toString();
        } catch (Exception e){
            result = e.toString();
        }finally {
            http.cancelAll();
        }
        return result;
    }


    //public static String getCodeWithGet(String url) {
    //    return getCodeWithGet(url, 30, null,"UTF-8",null);
    //}
    //
    //public static String getBodyWithGet(String url) {
    //    return getBodyWithGet(url, 30, null,"UTF-8",null);
    //}
    //
    //public static String getBodyWithPost(String url,Map<String, String> params){
    //    return getBodyWithPost(url,params,null,30,"UTF-8",null);
    //}

    public static void main(String[] args) throws NoSuchAlgorithmException, KeyManagementException {
        Map<String, String> headers = new HashMap<>();
        Map<String, String> params = new HashMap<>();
        Map currentProxy = new HashMap();
        Proxy proxy;
        String ip = "127.0.0.1";
        int port = 8080;
        params.put("key1","demo");
        params.put("key2","demo2");
        InetSocketAddress proxyAddr = new InetSocketAddress(ip,port);
        proxy = new Proxy(Proxy.Type.HTTP, proxyAddr);
        currentProxy.put("proxy", proxy);



        //String res = OKHttpUtil.getBodyWithGet("http://10.211.55.19:8080/1.jsp",30,null,"UTF-8",null);
        //System.out.println(base64Decode(new String(xorWithKey(base64Decode(res,"UTF-8").getBytes(StandardCharsets
        // .UTF_8),"key".getBytes(StandardCharsets.UTF_8))),"UTF-8"));
        //System.out.println(OKHttpUtil.getBodyWithPost("http://10.211.55.10/1.asp",params,headers,30,"UTF-8",currentProxy));

    }
}