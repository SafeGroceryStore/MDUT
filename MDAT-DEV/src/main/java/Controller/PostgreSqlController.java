package Controller;

import Dao.MysqlHttpDao;
import Dao.PostgreSqlDao;
import Dao.PostgreSqlHttpDao;
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

    private PostgreSqlHttpDao postgreSqlHttpDao;

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

                if("false".equals(this.dataObj.getString("ishttp"))){
                    this.postgreDao = new PostgreSqlDao(this.dataObj.getString("ipaddress"), this.dataObj.getString("port"), this.dataObj.getString("database"), this.dataObj.getString("username"), this.dataObj.getString("password"), this.dataObj.getString("timeout"));
                    this.postgreDao.getConnection();
                    Platform.runLater(() -> {
                        postgreSqlLogTextArea.appendText(Utils.log("连接成功！"));
                        // 获取信息输出
                        this.postgreDao.getInfo();
                    });
                }else {
                    Platform.runLater(() -> {
                        this.postgreSqlHttpDao = new PostgreSqlHttpDao(this.dataObj);
                        this.postgreSqlHttpDao.getConnection();
                        this.postgreSqlHttpDao.getInfo();
                    });
                }

            } catch (Exception e) {
                if("false".equals(this.dataObj.getString("ishttp"))){
                    Platform.runLater(() -> {
                        postgreSqlLogTextArea.appendText(Utils.log("连接失败！"));
                        MessageUtil.showExceptionMessage(e, e.getMessage());
                        try {
                            this.postgreDao.closeConnection();
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
    public void initComboBox(){
        ObservableList<String> postgreSqlTypeCodeoptions = FXCollections.observableArrayList(
                "UTF-8",
                "GB2312",
                "GBK"
        );
        // 初始化下拉框
        postgreSqlEncodeCombox.setValue("UTF-8");
        postgreSqlEncodeCombox.setItems(postgreSqlTypeCodeoptions);
    }


    @FXML
    void postgreSqlSystem(ActionEvent event) {
        Runnable runner = () -> {
            if("false".equals(this.dataObj.getString("ishttp"))){
                this.postgreDao.createEval();
            }else {
                this.postgreSqlHttpDao.createEval();
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    @FXML
    void postgreSqlcUdf(ActionEvent event) {
        Runnable runner = () -> {
            if("false".equals(this.dataObj.getString("ishttp"))){
                this.postgreDao.udf();
            }else {
                this.postgreSqlHttpDao.udf();
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();

    }

    @FXML
    void postgreSqlclean(ActionEvent event) {
        Runnable runner = () -> {
            if("false".equals(this.dataObj.getString("ishttp"))){
                this.postgreDao.clear();
            }else {
                this.postgreSqlHttpDao.clear();
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();

    }

    @FXML
    void postgreSqlEval(ActionEvent event) {

        Runnable runner = () -> {
            try {
                String command = postgreSqlCommandText.getText();
                String code = postgreSqlEncodeCombox.getValue();
                if(code == null){
                    MessageUtil.showErrorMessage("请选择编码类型");
                    return;
                }
                if("false".equals(this.dataObj.getString("ishttp"))){
                    String result = this.postgreDao.eval(command,code);
                    postgreSqlOutputTextArea.setText(result);
                }else {
                    String result = this.postgreSqlHttpDao.eval(command,code);
                    postgreSqlOutputTextArea.setText(result);
                }
            } catch (Exception e) {
                MessageUtil.showExceptionMessage(e, e.getMessage());
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();


    }
}
