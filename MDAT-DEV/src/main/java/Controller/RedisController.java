package Controller;

import Dao.RedisDao;
import Entity.ControllersFactory;
import Util.MessageUtil;
import Util.Utils;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RedisController implements Initializable {

    @FXML
    private TextField redisVPSAddressTextField;

    @FXML
    private TextField redisCommandTextField;

    @FXML
    private ComboBox<String> redisEncodeCombox;

    @FXML
    private Button redisEvalCommandBtn;

    @FXML
    private Button redisScheduledTasksBtn;

    @FXML
    private Button redisReplaceSSHKeyBtn;

    @FXML
    private Button redisClearBtn;


    @FXML
    private Button redisSlavebtn;

    @FXML
    public TextArea redisOutputTextFArea;

    @FXML
    public TextArea redisLogTextFArea;

    @FXML
    public TextArea redisPublicKeyInput;

    @FXML
    public TextArea redisCronTaskInput;

    @FXML
    private TextField redisVPSPortTextField;

    @FXML
    private TextField redisVPSTimeOutTextField;

    /**
     * 存储从 PostgreSqlDao 传递过来的 postgreSqlDao 使用
     */
    private RedisDao redisDao;

    private JSONObject dataObj;

    private List workList = new ArrayList();

    public List getWorkList() {
        return this.workList;
    }

    /**
     * 初始化连接，赋予全局变量 redisDao
     *
     * @param dbObj
     */
    public void initRedisDao(JSONObject dbObj) {
        this.dataObj = dbObj;
        redisLogTextFArea.appendText(Utils.log("正在连接..."));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        initComboBox();
        ControllersFactory.controllers.put(this.getClass().getSimpleName(), this);
        // 尝试连接
        Runnable runner = () -> {
            try {
                this.redisDao = new RedisDao(this.dataObj.getString("ipaddress"), this.dataObj.getString("port"), this.dataObj.getString("password"), this.dataObj.getString("timeout"));
                this.redisDao.getConnection();
                this.redisDao.getInfo();
                Platform.runLater(() -> {
                    redisLogTextFArea.appendText(Utils.log("连接成功！"));
                });
                // 获取信息输出
            } catch (Exception e) {
                Platform.runLater(() -> {
                    redisLogTextFArea.appendText(Utils.log("连接失败！"));
                    MessageUtil.showExceptionMessage(e, e.getMessage());
                    try {
                        this.redisDao.closeConnection();
                    } catch (Exception ex) {
                    }
                });
            }
        };
        Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }

    @FXML
    void redisScheduledTasks(ActionEvent event) {
        Runnable runner = () -> {
            this.redisDao.crontab(redisCronTaskInput.getText());
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();

    }

    @FXML
    void redisReplaceSSHKey(ActionEvent event) {
        Runnable runner = () -> {
            this.redisDao.sshkey(redisPublicKeyInput.getText());
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    @FXML
    void redisSlave(ActionEvent event) {
        String vpsAddress = redisVPSAddressTextField.getText();
        String vpsPort = redisVPSPortTextField.getText();
        Runnable runner = () -> {
            if (!(vpsAddress.equals("") && vpsPort.equals(""))) {
                int timeout = Integer.parseInt(redisVPSTimeOutTextField.getText()) * 1000;
                try {
                    this.redisDao.rogue(vpsAddress, vpsPort, timeout);
                } catch (Exception e) {
                    Platform.runLater(() -> {
                        MessageUtil.showExceptionMessage(e, e.getMessage());
                    });

                }
            } else {
                Platform.runLater(() -> {
                    MessageUtil.showErrorMessage( "请输入vps地址和端口");
                });

            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    @FXML
    void redisClear(ActionEvent event) {
        Runnable runner = () -> {
            this.redisDao.clean();
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    @FXML
    void redisEvalCommand(ActionEvent event) {
        Runnable runner = () -> {
            String command = this.redisCommandTextField.getText();
            String code = redisEncodeCombox.getValue();
            if (code == null) {
                Platform.runLater(() -> {
                    MessageUtil.showErrorMessage( "请选择编码类型");
                });
                return;
            }
            String result = this.redisDao.eval(command, code);
            Platform.runLater(() -> {
                redisOutputTextFArea.setText(result);
            });
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();

    }

    /**
     * 下拉框初始化
     */
    public void initComboBox() {
        ObservableList<String> postgreSqlTypeCodeoptions = FXCollections.observableArrayList(
                "UTF-8",
                "GB2312",
                "GBK"
        );
        // 初始化下拉框
        redisEncodeCombox.setPromptText("UTF-8");
        redisEncodeCombox.setValue("UTF-8");
        redisEncodeCombox.setItems(postgreSqlTypeCodeoptions);
    }
}
