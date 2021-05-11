package Controller;

import Dao.PostgreSqlDao;
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

public class PostgreSqlController implements Initializable {

    @FXML
    private ComboBox<String> postgreSqlEncodeCombox;

    @FXML
    private Button postgreSqlcCearnBtn;

    @FXML
    private Button postgreSqlcUdfBtn;

    @FXML
    public TextArea postgreSqlLogTextArea;

    @FXML
    private Button postgreSqlEvalBtn;

    @FXML
    private TextArea postgreSqlOutputTextArea;

    @FXML
    private TextField postgreSqlCommandText;

    @FXML
    private Button postgreSqlSystemBtn;

    /**
     * 存储从 PostgreSqlDao 传递过来的 postgreSqlDao 使用
     */
    private PostgreSqlDao postgreDao;

    private JSONObject dataObj;

    private List workList = new ArrayList();

    public List getWorkList() {
        return this.workList;
    }

    /**
     * 初始化连接，赋予全局变量 PostgreDao
     *
     * @param dbObj
     */
    public void initPostgreDao(JSONObject dbObj) {
        this.dataObj = dbObj;
        postgreSqlLogTextArea.appendText(Utils.log("正在连接..."));
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        //初始化下拉框
        initComboBox();
        ControllersFactory.controllers.put(this.getClass().getSimpleName(), this);
        // 尝试连接
        Runnable runner = () -> {
            try {
                this.postgreDao = new PostgreSqlDao(this.dataObj.getString("ipaddress"), this.dataObj.getString("port"), this.dataObj.getString("database"), this.dataObj.getString("username"), this.dataObj.getString("password"), this.dataObj.getString("timeout"));
                this.postgreDao.getConnection();
                Platform.runLater(() -> {
                    postgreSqlLogTextArea.appendText(Utils.log("连接成功！"));
                    // 获取信息输出
                    this.postgreDao.getInfo();
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    postgreSqlLogTextArea.appendText(Utils.log("连接失败！"));
                    MessageUtil.showExceptionMessage(e, e.getMessage());
                    try {
                        this.postgreDao.closeConnection();
                    } catch (Exception ex) {
                    }
                });
            }
        };
        Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }

    /**
     * 下拉框初始化
     */
    public void initComboBox(){
        ObservableList<String> postgreSqlTypeCodeoptions = FXCollections.observableArrayList(
                "UTF-8",
                "GB2312",
                "GBK"
        );
        // 初始化下拉框
        postgreSqlEncodeCombox.setPromptText("编码");
        postgreSqlEncodeCombox.setItems(postgreSqlTypeCodeoptions);
    }


    @FXML
    void postgreSqlSystem(ActionEvent event) {
        this.postgreDao.createEval();
    }

    @FXML
    void postgreSqlcUdf(ActionEvent event) {
        this.postgreDao.udf();
    }

    @FXML
    void postgreSqlClearn(ActionEvent event) {
        this.postgreDao.clear();
    }

    @FXML
    void postgreSqlEval(ActionEvent event) {
        try {
            String command = postgreSqlCommandText.getText();
            String code = postgreSqlEncodeCombox.getValue();
            if(code == null){
                MessageUtil.showErrorMessage("错误","请选择编码类型");
                return;
            }
            String result = this.postgreDao.eval(command,code);
            postgreSqlOutputTextArea.setText(result);
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }
}
