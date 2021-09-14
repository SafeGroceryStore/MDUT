package Util;


import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;
import java.net.URLConnection;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.*;
import javax.net.ssl.*;


/**
 * @author ch1ng
 * @date 2021/8/18
 */
public class HttpUtil {
    private static final int Timeout = 5000;
    private static final String DefalutEncoding = "UTF-8";
    private static Map currentProxy = new HashMap();
    public static HostnameVerifier allHostsValid = new HostnameVerifier() {
        @Override
        public boolean verify(String hostname, SSLSession session) {
            return true;
        }
    };

    public HttpUtil() {
    }

    public static String httpRequest(String requestUrl, int timeOut, String requestMethod, String contentType, String postString, String encoding) throws Exception {
        if ("".equals(encoding) || encoding == null) {
            encoding = DefalutEncoding;
        }

        URLConnection httpUrlConn = null;
        HttpsURLConnection hsc = null;
        HttpURLConnection hc = null;
        InputStream inputStream = null;

        String result;
        try {
            try {
                URL url = new URL(requestUrl);
                if (requestUrl.startsWith("https")) {
                    SSLContext sslContext = SSLContext.getInstance("SSL");
                    TrustManager[] tm = new TrustManager[]{new X509TrustUtiil()};
                    sslContext.init((KeyManager[]) null, tm, new SecureRandom());
                    SSLSocketFactory ssf = sslContext.getSocketFactory();
                    hsc = (HttpsURLConnection) url.openConnection();
                    hsc.setSSLSocketFactory(ssf);
                    hsc.setHostnameVerifier(allHostsValid);
                    httpUrlConn = hsc;
                } else {
                    hc = (HttpURLConnection) url.openConnection();
                    hc.setRequestMethod(requestMethod);
                    hc.setInstanceFollowRedirects(false);
                    System.out.println(hc.getRequestProperties());
                    httpUrlConn = hc;
                }

                ((URLConnection) httpUrlConn).setConnectTimeout(timeOut);
                ((URLConnection) httpUrlConn).setReadTimeout(timeOut);
                if (contentType != null && !"".equals(contentType)) {
                    ((URLConnection) httpUrlConn).setRequestProperty("Content-Type", contentType);
                }

                //((URLConnection) httpUrlConn).setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)");
                //((URLConnection) httpUrlConn).setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                //((URLConnection) httpUrlConn).setRequestProperty("Accept-Encoding", "gzip, deflate");
                //((URLConnection) httpUrlConn).setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
                //((URLConnection) httpUrlConn).setRequestProperty("Connection", "close");
                ((URLConnection) httpUrlConn).setDoOutput(true);
                ((URLConnection) httpUrlConn).setDoInput(true);
                ((URLConnection) httpUrlConn).connect();
                if (null != postString && !"".equals(postString)) {
                    OutputStream outputStream = ((URLConnection) httpUrlConn).getOutputStream();
                    outputStream.write(postString.getBytes(encoding));
                    outputStream.flush();
                    outputStream.close();
                }

                inputStream = ((URLConnection) httpUrlConn).getInputStream();
                result = readString(inputStream, encoding);
                String var22 = result;
                return var22;
            } catch (IOException var18) {
                System.out.println(var18);
                if (hsc == null) {
                    if (hc != null) {
                        result = readString(hc.getErrorStream(), encoding);
                        return result;
                    }

                    result = "";
                    return result;
                }
            } catch (Exception var19) {
                System.out.println(var19);
                throw var19;
            }

            result = readString(hsc.getErrorStream(), encoding);
        } finally {
            if (hsc != null) {
                hsc.disconnect();
            }

            if (hc != null) {
                hc.disconnect();
            }

        }

        return result;
    }

    public static String readString(InputStream inputStream, String encoding) throws IOException {
        BufferedInputStream bis = null;
        ByteArrayOutputStream baos = null;

        try {
            bis = new BufferedInputStream(inputStream);
            baos = new ByteArrayOutputStream();
            byte[] arr = new byte[1];

            int len;
            while ((len = bis.read(arr)) != -1) {
                baos.write(arr, 0, len);
            }
        } catch (IOException var9) {
        } finally {
            if (baos != null) {
                baos.flush();
                baos.close();
            }

            if (bis != null) {
                bis.close();
            }

            if (inputStream != null) {
                inputStream.close();
            }

            return baos.toString(encoding);
        }
    }

    public static String httpRequestAddHeader(String requestUrl, int timeOut, String requestMethod, String contentType, String postString, String encoding, HashMap<String, String> headers) throws Exception {
        if ("".equals(encoding) || encoding == null) {
            encoding = DefalutEncoding;
        }

        URLConnection httpUrlConn = null;
        HttpsURLConnection hsc = null;
        HttpURLConnection hc = null;
        InputStream inputStream = null;
        BufferedInputStream bis = null;
        Object var12 = null;

        String result;
        try {
            URL url = new URL(requestUrl);
            if (requestUrl.startsWith("https")) {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManager[] tm = new TrustManager[]{new X509TrustUtiil()};
                sslContext.init((KeyManager[]) null, tm, new SecureRandom());
                SSLSocketFactory ssf = sslContext.getSocketFactory();
                Proxy proxy = (Proxy) currentProxy.get("proxy");
                if (proxy != null) {
                    hsc = (HttpsURLConnection) url.openConnection(proxy);
                } else {
                    hsc = (HttpsURLConnection) url.openConnection();
                }

                hsc.setSSLSocketFactory(ssf);
                hsc.setHostnameVerifier(allHostsValid);
                httpUrlConn = hsc;
            } else {
                Proxy proxy = (Proxy) currentProxy.get("proxy");
                if (proxy != null) {
                    hc = (HttpURLConnection) url.openConnection(proxy);
                } else {
                    hc = (HttpURLConnection) url.openConnection();
                }

                hc.setRequestMethod(requestMethod);
                hc.setInstanceFollowRedirects(false);
                httpUrlConn = hc;
            }

            ((URLConnection) httpUrlConn).setConnectTimeout(timeOut);
            ((URLConnection) httpUrlConn).setReadTimeout(timeOut);
            if (contentType != null && !"".equals(contentType)) {
                ((URLConnection) httpUrlConn).setRequestProperty("Content-Type", contentType);
            }

            //((URLConnection) httpUrlConn).setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)");
            //((URLConnection) httpUrlConn).setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            //((URLConnection) httpUrlConn).setRequestProperty("Accept-Encoding", "gzip, deflate");
            //((URLConnection) httpUrlConn).setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            //((URLConnection) httpUrlConn).setRequestProperty("Connection", "close");
            String key;
            if (headers != null) {
                Iterator var28 = headers.keySet().iterator();

                while (var28.hasNext()) {
                    key = (String) var28.next();
                    String val = (String) headers.get(key);
                    ((URLConnection) httpUrlConn).addRequestProperty(key, val);
                }
            }

            ((URLConnection) httpUrlConn).setDoOutput(true);
            ((URLConnection) httpUrlConn).setDoInput(true);
            ((URLConnection) httpUrlConn).connect();
            if (null != postString && !"".equals(postString)) {
                OutputStream outputStream = ((URLConnection) httpUrlConn).getOutputStream();
                outputStream.write(postString.getBytes(encoding));
                outputStream.close();
            }

            inputStream = ((URLConnection) httpUrlConn).getInputStream();
            result = readString(inputStream, encoding);
            key = result;
            return key;
        } catch (IOException var22) {
            System.out.println(var22);
            if (hsc != null) {
                //System.out.println("1");
                //System.out.println(hsc.getErrorStream());
                result = readString(hsc.getErrorStream(), encoding);
                return result;
            }

            if (hc != null) {
                //System.out.println("2");
                //System.out.println(hc.getErrorStream());
                result = readString(hc.getErrorStream(), encoding);
                return result;
            }

            result = "";
        } catch (Exception var23) {
            //System.out.println("3");
            //System.out.println(var23);
            throw var23;
        } finally {
            if (hsc != null) {
                hsc.disconnect();
            }

            if (hc != null) {
                hc.disconnect();
            }

        }

        return result;
    }

    public static int codeByHttpRequest(String requestUrl, int timeOut, String requestMethod, String contentType, String postString, String encoding) throws Exception {
        if ("".equals(encoding) || encoding == null) {
            encoding = DefalutEncoding;
        }

        URLConnection httpUrlConn = null;
        HttpsURLConnection hsc = null;
        HttpURLConnection hc = null;
        InputStream inputStream = null;
        InputStreamReader isr = null;
        Object br = null;

        int var24;
        try {
            URL url = new URL(requestUrl);
            if (requestUrl.startsWith("https")) {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManager[] tm = new TrustManager[]{new X509TrustUtiil()};
                sslContext.init((KeyManager[]) null, tm, new SecureRandom());
                SSLSocketFactory ssf = sslContext.getSocketFactory();
                hsc = (HttpsURLConnection) url.openConnection();
                hsc.setSSLSocketFactory(ssf);
                hsc.setHostnameVerifier(allHostsValid);
                httpUrlConn = hsc;
            } else {
                hc = (HttpURLConnection) url.openConnection();
                hc.setRequestMethod(requestMethod);
                httpUrlConn = hc;
            }

            ((URLConnection) httpUrlConn).setReadTimeout(timeOut);
            if (contentType != null && !"".equals(contentType)) {
                ((URLConnection) httpUrlConn).setRequestProperty("Content-Type", contentType);
            }

            //((URLConnection) httpUrlConn).setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)");
            //((URLConnection) httpUrlConn).setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            //((URLConnection) httpUrlConn).setRequestProperty("Accept-Encoding", "gzip, deflate");
            //((URLConnection) httpUrlConn).setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            //((URLConnection) httpUrlConn).setRequestProperty("Connection", "close");
            ((URLConnection) httpUrlConn).setDoOutput(true);
            ((URLConnection) httpUrlConn).setDoInput(true);
            ((URLConnection) httpUrlConn).setUseCaches(false);
            ((URLConnection) httpUrlConn).connect();
            if (null != postString && !"".equals(postString)) {
                OutputStream outputStream = ((URLConnection) httpUrlConn).getOutputStream();
                outputStream.write(postString.getBytes(encoding));
                outputStream.close();
            }

            if (hsc != null) {
                var24 = hsc.getResponseCode();
                return var24;
            }

            if (hc == null) {
                byte var25 = 0;
                return var25;
            }

            var24 = hc.getResponseCode();
        } catch (IOException var20) {
            throw var20;
        } catch (Exception var21) {
            throw var21;
        } finally {
            if (br != null) {
                ((BufferedReader) br).close();
            }

            if (isr != null) {
                ((InputStreamReader) isr).close();
            }

            if (inputStream != null) {
                ((InputStream) inputStream).close();
            }

            if (hsc != null) {
                hsc.disconnect();
            }

            if (hc != null) {
                hc.disconnect();
            }

        }

        return var24;
    }

    public static String headerByHttpRequest(String requestUrl, int timeOut, String requestMethod, String contentType, String postString, String encoding, HashMap<String, String> headers) throws Exception {
        if ("".equals(encoding) || encoding == null) {
            encoding = DefalutEncoding;
        }

        URLConnection httpUrlConn = null;
        HttpsURLConnection hsc = null;
        HttpURLConnection hc = null;
        InputStream inputStream = null;
        InputStreamReader isr = null;
        Object br = null;

        StringBuilder responseHeaderString;
        try {
            URL url = new URL(requestUrl);
            if (requestUrl.startsWith("https")) {
                SSLContext sslContext = SSLContext.getInstance("SSL");
                TrustManager[] tm = new TrustManager[]{new X509TrustUtiil()};
                sslContext.init((KeyManager[]) null, tm, new SecureRandom());
                SSLSocketFactory ssf = sslContext.getSocketFactory();

                Proxy proxy = (Proxy) currentProxy.get("proxy");
                if (proxy != null) {
                    hsc = (HttpsURLConnection) url.openConnection(proxy);
                } else {
                    hsc = (HttpsURLConnection) url.openConnection();
                }

//                hsc = (HttpsURLConnection)url.openConnection();
                hsc.setSSLSocketFactory(ssf);
                hsc.setHostnameVerifier(allHostsValid);
                httpUrlConn = hsc;
            } else {
                Proxy proxy = (Proxy) currentProxy.get("proxy");
                if (proxy != null) {
                    hc = (HttpURLConnection) url.openConnection(proxy);
                } else {
                    hc = (HttpURLConnection) url.openConnection();
                }

//                hc = (HttpURLConnection)url.openConnection();
                hc.setRequestMethod(requestMethod);
                httpUrlConn = hc;
            }

            ((URLConnection) httpUrlConn).setReadTimeout(timeOut);
            if (contentType != null && !"".equals(contentType)) {
                ((URLConnection) httpUrlConn).setRequestProperty("Content-Type", contentType);
            }

            //((URLConnection) httpUrlConn).setRequestProperty("User-Agent", "Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)");
            //((URLConnection) httpUrlConn).setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
            //((URLConnection) httpUrlConn).setRequestProperty("Accept-Encoding", "gzip, deflate");
            //((URLConnection) httpUrlConn).setRequestProperty("Accept-Language", "zh-CN,zh;q=0.9");
            //((URLConnection) httpUrlConn).setRequestProperty("Connection", "close");
            if (headers != null) {
                Iterator var27 = headers.keySet().iterator();

                while (var27.hasNext()) {
                    String key = (String) var27.next();
                    String val = (String) headers.get(key);
                    ((URLConnection) httpUrlConn).addRequestProperty(key, val);
                }
            }

            ((URLConnection) httpUrlConn).setDoOutput(true);
            ((URLConnection) httpUrlConn).setDoInput(true);
            ((URLConnection) httpUrlConn).connect();
            if (null != postString && !"".equals(postString)) {
                OutputStream outputStream = ((URLConnection) httpUrlConn).getOutputStream();
                outputStream.write(postString.getBytes(encoding));
                outputStream.close();
            }

            Iterator var17;
            String key;
            Map responseheaders;
            Set keys;
            String var34;
            if (hsc != null) {
                responseHeaderString = new StringBuilder();
                responseheaders = hsc.getHeaderFields();

                Set<String> respHeaderKeys = responseheaders.keySet();
                Iterator<String> iterator = respHeaderKeys.iterator();
                while (iterator.hasNext()) {
                    String respHeaderKey = iterator.next();
                    List<String> arrayList = (List<String>) responseheaders.get(respHeaderKey);
                    for (String value : arrayList) {
                        responseHeaderString.append(value);
                    }
                }

//                keys = responseheaders.keySet();
//
//                String val;
//                for (var17 = keys.iterator(); var17.hasNext(); responseHeaderString = responseHeaderString + val + "\r\n") {
//                    key = (String) var17.next();
////                    responseheaders.get()
//                    val = hsc.getHeaderField(key);
//                }

                var34 = responseHeaderString.toString();
                return var34;
            }

            if (hc != null) {
                responseHeaderString = new StringBuilder();
                responseheaders = hc.getHeaderFields();
//                keys = responseheaders.keySet();

                Set<String> respHeaderKeys = responseheaders.keySet();
                Iterator<String> iterator = respHeaderKeys.iterator();
                while (iterator.hasNext()) {
                    String respHeaderKey = iterator.next();
                    List<String> arrayList = (List<String>) responseheaders.get(respHeaderKey);
                    for (String value : arrayList) {
                        responseHeaderString.append(value);
                    }
                }
//                List val;
//                for (var17 = keys.iterator(); var17.hasNext(); responseHeaderString.append(key).append(": ").append(val.toString())) {
//                    key = (String) var17.next();
//                    val = (List) responseheaders.get(key);
//                    System.out.println(key + ": " + val.toString());
//                }

                var34 = responseHeaderString.toString();
                return var34;
            }

            responseHeaderString = new StringBuilder();
        } catch (IOException var24) {
            throw var24;
        } catch (Exception var25) {
            throw var25;
        } finally {
            if (br != null) {
                ((BufferedReader) br).close();
            }

            if (isr != null) {
                ((InputStreamReader) isr).close();
            }

            if (inputStream != null) {
                ((InputStream) inputStream).close();
            }

            if (hsc != null) {
                hsc.disconnect();
            }

            if (hc != null) {
                hc.disconnect();
            }

        }

        return responseHeaderString.toString();
    }

    public static String httpReuest(String requestUrl, String method, String contentType, String postString, String encoding) throws Exception {
        return httpRequest(requestUrl, Timeout, method, contentType, postString, encoding);
    }

    public static String postHttpReuest(String requestUrl, int timeOut, String contentType, String postString, String encoding) throws Exception {
        return httpRequest(requestUrl, timeOut, "POST", contentType, postString, encoding);
    }

    public static String postHttpReuest(String requestUrl, String postString, String encoding, HashMap<String, String> headers, String contentType, int timeout) throws Exception {
        return httpRequestAddHeader(requestUrl, timeout, "POST", contentType, postString, encoding, headers);
    }

    public static String postHttpReuest(String requestUrl, String contentType, String postString, String encoding) throws Exception {
        return httpRequest(requestUrl, Timeout, "POST", contentType, postString, encoding);
    }

    public static String postHttpReuest(String requestUrl, int timeOut, String postString, String encoding) throws Exception {
        return httpRequest(requestUrl, timeOut, "POST", "application/x-www-form-urlencoded", postString, encoding);
    }

    public static String postHttpReuest(String requestUrl, String postString, String encoding) throws Exception {
        return httpRequest(requestUrl, Timeout, "POST", "application/x-www-form-urlencoded", postString, encoding);
    }

//    public static String getHttpReuest(String requestUrl,int timeout, String contentType, String encoding) throws Exception {
//        return httpRequest(requestUrl, timeout, "GET", contentType, "", encoding);
//    }

    public static String getHttpReuest(String requestUrl, int timeout, String encoding, HashMap<String, String> headers) throws Exception {
        return httpRequestAddHeader(requestUrl, timeout, "GET", "", "", encoding, headers);
    }

    public static String postHttpReuestByXML(String requestUrl, int timeOut, String postString, String encoding) throws Exception {
        return httpRequest(requestUrl, timeOut, "POST", "text/xml", postString, encoding);
    }

    public static String postHttpReuestByXML(String requestUrl, String postString, String encoding) throws Exception {
        return httpRequest(requestUrl, Timeout, "POST", "text/xml", postString, encoding);
    }

    public static String postHttpReuestByXMLAddHeader(String requestUrl, String postString, String encoding, HashMap<String, String> headers) throws Exception {
        return httpRequestAddHeader(requestUrl, Timeout, "POST", "text/xml", postString, encoding, headers);
    }

    public static int codeByHttpRequest(String requestUrl, String method, String contentType, String postString, String encoding) throws Exception {
        return codeByHttpRequest(requestUrl, Timeout, method, contentType, postString, encoding);
    }

    public static int getCodeByHttpRequest(String requestUrl, String encoding) throws Exception {
        return codeByHttpRequest(requestUrl, "GET", (String) null, "", encoding);
    }

    public static int getCodeByHttpRequest(String requestUrl, int timeout, String encoding) throws Exception {
        return codeByHttpRequest(requestUrl, timeout, "GET", (String) null, "", encoding);
    }

    public static int postCodeByHttpRequest(String requestUrl, String contentType, String postString, String encoding) throws Exception {
        return codeByHttpRequest(requestUrl, Timeout, "POST", contentType, postString, encoding);
    }

    public static int postCodeByHttpRequestWithNoContenType(String requestUrl, String postString, String encoding) throws Exception {
        return codeByHttpRequest(requestUrl, Timeout, "POST", (String) null, postString, encoding);
    }

    public static int postCodeByHttpRequest(String requestUrl, String encoding) throws Exception {
        return codeByHttpRequest(requestUrl, Timeout, "POST", (String) null, (String) null, encoding);
    }

    public static int postCodeByHttpRequest(String requestUrl, String postString, String encoding) throws Exception {
        return codeByHttpRequest(requestUrl, Timeout, "POST", "application/x-www-form-urlencoded", postString, encoding);
    }

    public static int postCodeByHttpRequestXML(String requestUrl, String postString, String encoding) throws Exception {
        return codeByHttpRequest(requestUrl, Timeout, "POST", "text/xml", postString, encoding);
    }

    public static String getHeaderByHttpRequest(String requestUrl, String encoding, HashMap<String, String> headers, int timeout) throws Exception {
        return headerByHttpRequest(requestUrl, Timeout, "GET", "text/xml", "", encoding, headers);
    }

    public static String postHeaderByHttpRequest(String requestUrl, String encoding, String postString, HashMap<String, String> headers, int timeout) throws Exception {
        return headerByHttpRequest(requestUrl, Timeout, "POST", "text/xml", postString, encoding, headers);
    }

    public static boolean downloadFile(String downURL, File file) throws Exception {
        HttpURLConnection httpURLConnection = null;
        BufferedInputStream bin = null;
        FileOutputStream out = null;

        try {
            URL url = new URL(downURL);
            httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("GET");
            httpURLConnection.connect();
            bin = new BufferedInputStream(httpURLConnection.getInputStream());
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }

            out = new FileOutputStream(file);
            int len = 0;
            byte[] buf = new byte[2048];

            int size;
            while ((size = bin.read(buf)) != -1) {
                len += size;
                out.write(buf, 0, size);
            }
        } catch (Exception var12) {
            throw var12;
        } finally {
            if (bin != null) {
                bin.close();
            }

            if (out != null) {
                out.flush();
                out.close();
            }

            if (httpURLConnection != null) {
                httpURLConnection.disconnect();
            }

        }

        return true;
    }

    public static boolean downloadFile(String downURL, String path) throws Exception {
        return downloadFile(downURL, new File(path));
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
}