package Controller;

import Dao.*;
import Util.MessageUtil;
import Util.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;
import java.net.URL;
import Entity.ControllersFactory;
import java.util.ResourceBundle;

/**
 * @author ch1ng
 */
public class AddAndEditController implements Initializable {

    @FXML
    private Button generateBtn;

    @FXML
    private TextField proxyAddressText;

    @FXML
    private TextField portText;

    @FXML
    private TextField passwordText;

    @FXML
    private Button cancelBtn;

    @FXML
    private TextField urlText;

    @FXML
    private ChoiceBox<String> databaseTypeChBox;

    @FXML
    private TextField keyText;

    @FXML
    private TextField userNameText;

    @FXML
    private TextField databaseText;

    @FXML
    private Button testConnectBtn;

    @FXML
    private CheckBox usedHttpTunnelCheckBox;

    @FXML
    private TextField proxyPasswordText;

    @FXML
    private TextField timeOutText;

    @FXML
    private ChoiceBox<String> proxyTypeChBox;

    @FXML
    private TextField proxyUserNameText;

    @FXML
    private CheckBox usedProxyCheckBox;

    @FXML
    private TextArea HttpHeadersTextA;

    @FXML
    private TextField ipAddressText;

    @FXML
    private TextArea memoText;

    @FXML
    private TextField proxyPortText;

    @FXML
    private Label infoLaberl;

    @FXML
    private Button saveBtn;


    /**
     * 从数据库获取的用户存储的数据
     */
    public JSONArray databaseList;

    /**
     * 临时变量
     */
    private int tempID;

    /**
     * 点击编辑后需要初始化数据
     *
     * @param datalist
     */
    public void initVariableShow(JSONArray datalist) {
        this.databaseList = datalist;
        // 将 array 转成 obj，数据只有一条，所以取0即可
        JSONObject dbObj = databaseList.getJSONObject(0);
        // 开始赋值给控件 - 常规 Tab 页
        databaseTypeChBox.setValue(dbObj.getString("databasetype"));
        ipAddressText.setText(dbObj.getString("ipaddress"));
        portText.setText(dbObj.getString("port"));
        userNameText.setText(dbObj.getString("username"));
        passwordText.setText(dbObj.getString("password"));
        databaseText.setText(dbObj.getString("database"));
        timeOutText.setText(dbObj.getString("timeout"));
        //赋值给临时 ID
        tempID = dbObj.getInt("id");
        // memo 为空可能会出错。
        memoText.setText(dbObj.getString("memo"));
        //开始赋值给控件 - HTTP Tab 页
        if (!"false".equals(dbObj.getString("ishttp"))) {
            usedHttpTunnelCheckBox.setSelected(true);
            urlText.setText(dbObj.getString("url"));
            keyText.setText(dbObj.getString("encryptionkey"));
            HttpHeadersTextA.setText(dbObj.getString("httpheaders"));
        }
        if (!"false".equals(dbObj.getString("isproxy"))) {
            usedProxyCheckBox.setSelected(true);
            proxyTypeChBox.setValue(dbObj.getString("proxytype"));
            proxyAddressText.setText(dbObj.getString("proxyaddress"));
            proxyPortText.setText(dbObj.getString("proxyport"));
            proxyUserNameText.setText(dbObj.getString("proxyusername"));
            proxyPasswordText.setText(dbObj.getString("proxypassword"));
        }
    }

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化CheckBox监听事件
        initCheckBox();
        // 初始化ChoicekBox监听事件
        initChoiceBox();

    }

    /**
     * 初始化ChoicekBox监听事件
     */
    public void initChoiceBox() {
        proxyTypeChBox.getItems().addAll("HTTP", "SOCKS5");
        databaseTypeChBox.getItems().addAll("Mysql", "Mssql", "Oracle", "PostgreSql", "Redis");
        // 监听选择的数据库并且将默认的值设置在编辑框中
        databaseTypeChBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                userNameText.setDisable(false);
                databaseText.setDisable(false);
                switch (newValue) {
                    case "Mysql":
                        userNameText.setText("root");
                        portText.setText("3306");
                        break;
                    case "Mssql":
                        userNameText.setText("sa");
                        portText.setText("1433");
                        break;
                    case "Oracle":
                        databaseText.setText("orcl");
                        portText.setText("1521");
                        break;
                    case "PostgreSql":
                        userNameText.setText("postgres");
                        portText.setText("5432");
                        break;
                    case "Redis":
                        portText.setText("6379");
                        userNameText.setDisable(true);
                        databaseText.setText("");
                        databaseText.setDisable(true);
                        break;
                    default:
                        break;
                }
            }
        });
    }

    /**
     * 初始化CheckBox监听事件
     */
    public void initCheckBox() {
        // 监听 HTTP 通道勾选状态，如果勾选则代表使用 HTTP 通道
        usedHttpTunnelCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // System.out.println(newValue);
                // 勾选时 newValue 为 True，所以 setDisable 要取反
                urlText.setDisable(!newValue);
                keyText.setDisable(!newValue);
                HttpHeadersTextA.setDisable(!newValue);
                generateBtn.setDisable(!newValue);
            }
        });
        // 监听 Proxy 勾选状态，如果勾选则代表使用 Proxy 代理
        usedProxyCheckBox.selectedProperty().addListener(new ChangeListener<Boolean>() {
            @Override
            public void changed(ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) {
                // 勾选时 newValue 为 True，所以 setDisable 要取反
                proxyAddressText.setDisable(!newValue);
                proxyPasswordText.setDisable(!newValue);
                proxyPortText.setDisable(!newValue);
                proxyTypeChBox.setDisable(!newValue);
                proxyUserNameText.setDisable(!newValue);
            }
        });
    }

    @FXML
    void TestConnectAction(ActionEvent event) {

        String ip = ipAddressText.getText();
        String port = portText.getText();
        String username = userNameText.getText();
        String password = passwordText.getText();
        String database = databaseText.getText();
        String timeout = timeOutText.getText();
        String tempDatabaseType = databaseTypeChBox.getValue();
        switch (tempDatabaseType) {
            case "Mysql":
                infoLaberl.setText("正在连接...请稍等");
                Runnable runnerMysql = () -> {
                    try {
                        MysqlDao mysqlDao = new MysqlDao(ip, port, database, username, password, timeout);
                        // 测试连接
                        mysqlDao.testConnection();
                        Platform.runLater(() -> {
                            infoLaberl.setText("连接成功");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            infoLaberl.setText("连接失败");
                            MessageUtil.showExceptionMessage(e, e.getMessage());
                        });
                    }
                };
                Thread workThradMysql = new Thread(runnerMysql);
                //this.workList.add(workThrad);
                workThradMysql.start();
                break;
            case "PostgreSql":
                infoLaberl.setText("正在连接...请稍等");
                Runnable runnerPostgre = () -> {
                    try {
                        PostgreSqlDao postgreSqlDao = new PostgreSqlDao(ip, port, database, username, password, timeout);
                        // 测试连接
                        postgreSqlDao.testConnection();
                        Platform.runLater(() -> {
                            infoLaberl.setText("连接成功");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            infoLaberl.setText("连接失败");
                            MessageUtil.showExceptionMessage(e, e.getMessage());
                        });
                    }
                };
                Thread workThradPostgre = new Thread(runnerPostgre);
                //this.workList.add(workThrad);
                workThradPostgre.start();
                break;
            case "Mssql":
                infoLaberl.setText("正在连接...请稍等");
                Runnable runnerMssql = () -> {
                    try {
                        MssqlDao mssqlDao = new MssqlDao(ip, port, database, username, password, timeout);
                        // 测试连接
                        mssqlDao.testConnection();
                        Platform.runLater(() -> {
                            infoLaberl.setText("连接成功");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            infoLaberl.setText("连接失败");
                            MessageUtil.showExceptionMessage(e, e.getMessage());
                        });
                    }
                };
                Thread workThradMssql = new Thread(runnerMssql);
                //this.workList.add(workThrad);
                workThradMssql.start();
                break;
            case "Redis":
                infoLaberl.setText("正在连接...请稍等");
                Runnable runnerRedis = () -> {
                    try {
                        RedisDao redisDao = new RedisDao(ip, port, password, timeout);
                        // 测试连接
                        redisDao.testConnection();
                        Platform.runLater(() -> {
                            infoLaberl.setText("连接成功");
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            infoLaberl.setText("连接失败");
                            MessageUtil.showExceptionMessage(e, e.getMessage());
                        });
                    }
                };
                Thread workThradRedis = new Thread(runnerRedis);
                //this.workList.add(workThrad);
                workThradRedis.start();
                break;
            case "Oracle":
                infoLaberl.setText("正在连接...请稍等");
                    Runnable runnerOracle = () -> {
                        try {
                            OracleDao oracleDao = new OracleDao(ip, port, database, username, password, timeout);
                            // 测试连接
                            oracleDao.testConnection();
                            Platform.runLater(() -> {
                                infoLaberl.setText("连接成功");
                            });
                        } catch (Exception e) {
                            Platform.runLater(() -> {
                                infoLaberl.setText("连接失败");
                                MessageUtil.showExceptionMessage(e, e.getMessage());
                            });
                        }
                    };
                    Thread workThradOracle = new Thread(runnerOracle);
                    //this.workList.add(workThrad);
                    workThradOracle.start();
                break;
            default:
        }
    }

    @FXML
    void SaveAction(ActionEvent event) {
        try {
            // 如果不等于 null，就是从数据库加载的，调用saveData方法
            if (tempID != 0) {
                saveData();
            } else {
                addData();
            }

        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }


    /**
     * 保存数据
     */
    public void saveData() throws Exception {
        MainController mainController = (MainController) ControllersFactory.controllers.get(MainController.class.getSimpleName());
        String databasetype = databaseTypeChBox.getValue();
        String ipaddress = ipAddressText.getText();
        String port = portText.getText();
        String username = userNameText.getText();
        String password = passwordText.getText();
        String database = databaseText.getText();
        String timeout = timeOutText.getText();
        String memo = memoText.getText();
        String ishttp = usedHttpTunnelCheckBox.isSelected() ? "true" : "false";
        String url = urlText.getText();
        String encryptionkey = keyText.getText();
        String isproxy = usedProxyCheckBox.isSelected() ? "true" : "false";
        String proxytype = proxyTypeChBox.getValue() == null ? "" : proxyTypeChBox.getValue();
        String proxyaddress = proxyAddressText.getText();
        String proxyport = proxyPortText.getText();
        String proxyusername = proxyUserNameText.getText();
        String proxypassword = proxyPasswordText.getText();
        String httpheaders = HttpHeadersTextA.getText();
        String connecttype = usedHttpTunnelCheckBox.isSelected() ? (usedProxyCheckBox.isSelected() ? "HTTP连接(代理)" : "HTTP连接") : "常规连接";
        mainController.managerDao.updateDatebase(
                databasetype,
                ipaddress,
                port,
                username,
                password,
                database,
                timeout,
                memo,
                ishttp,
                url,
                encryptionkey,
                isproxy,
                proxytype,
                proxyaddress,
                proxyport,
                proxyusername,
                proxypassword,
                httpheaders,
                connecttype,
                tempID + ""
        );
        MessageUtil.showInfoMessage("提示", "保存成功");
        // 关闭窗口
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
        // 刷新数据
        mainController.initTableView();
    }

    /**
     * 添加数据
     */
    public void addData() throws Exception {
        MainController mainController = (MainController) ControllersFactory.controllers.get(MainController.class.getSimpleName());
        String databasetype = databaseTypeChBox.getValue();
        String ipaddress = ipAddressText.getText();
        String port = portText.getText();
        String username = userNameText.getText();
        String password = passwordText.getText();
        String database = databaseText.getText();
        String timeout = timeOutText.getText();
        String memo = memoText.getText();
        String ishttp = usedHttpTunnelCheckBox.isSelected() ? "true" : "false";
        String url = urlText.getText();
        String encryptionkey = keyText.getText();
        String isproxy = usedProxyCheckBox.isSelected() ? "true" : "false";
        String proxytype = proxyTypeChBox.getValue() == null ? "" : proxyTypeChBox.getValue();
        String proxyaddress = proxyAddressText.getText();
        String proxyport = proxyPortText.getText();
        String proxyusername = proxyUserNameText.getText();
        String proxypassword = proxyPasswordText.getText();
        String httpheaders = HttpHeadersTextA.getText();
        String connecttype = usedHttpTunnelCheckBox.isSelected() ? (usedProxyCheckBox.isSelected() ? "HTTP链接(代理)" : "HTTP链接") : "常规连接";
        String addtime = Utils.getCurrentTimeToString();
        mainController.managerDao.addDatebase(
                databasetype,
                ipaddress,
                port,
                username,
                password,
                database,
                timeout,
                memo,
                ishttp,
                url,
                encryptionkey,
                isproxy,
                proxytype,
                proxyaddress,
                proxyport,
                proxyusername,
                proxypassword,
                httpheaders,
                connecttype,
                addtime
        );
        MessageUtil.showInfoMessage("提示", "添加成功");
        // 关闭窗口
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
        // 刷新数据
        mainController.initTableView();
    }

    @FXML
    void CancelAction(ActionEvent event) {
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    void GenerateAction(ActionEvent event) {
        keyText.setText(Utils.getRandomString());
    }

}
