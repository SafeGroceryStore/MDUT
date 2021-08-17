package Controller;

import Dao.ManagerDao;
import Entity.DatabaseDateEntity;
import Util.MessageUtil;
import Util.Utils;
import Util.YamlConfigs;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.net.URL;

import java.util.*;

import Entity.ControllersFactory;

/**
 * @author ch1ng
 */
public class MainController implements Initializable {

    @FXML
    private MenuItem resetConfigFile;

    @FXML
    private TableColumn<?, ?> ipCol;

    @FXML
    private TableColumn<?, ?> idCol;
    @FXML
    private MenuItem about;

    @FXML
    private MenuItem update;

    @FXML
    private MenuItem refreshMenuItem;

    @FXML
    private Label infoLab;

    @FXML
    private MenuItem setting;

    @FXML
    private TableColumn<?, ?> datebaseTypeCol;

    @FXML
    private TableColumn<?, ?> memoCol;

    @FXML
    private MenuItem openMenuItem;

    @FXML
    private TableView databaseTableView;

    @FXML
    private MenuItem addMenuItem;

    @FXML
    private TableColumn<?, ?> addTimeCol;

    @FXML
    private TableColumn<?, ?> connectTypeCol;

    @FXML
    private MenuItem editMenuItem;

    @FXML
    private MenuItem close;

    @FXML
    private MenuItem deleteMenuItem;

    public ManagerDao managerDao;

    @Override // 初始化函数
    public void initialize(URL location, ResourceBundle resources) {
        initDatabase();
        initConfigFile();
        // 免责声明
        initAlert();
        initTableView();
        // 初始化时保存当前 Controller 实例
        ControllersFactory.controllers.put(this.getClass().getSimpleName(), this);

    }

    /**
     * 初始化用户的数据库连接表
     */
    public void initTableView() {
        try {
            databaseTableView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
            databaseTableView.getItems().clear();
            // 从数据库获取用户连接的数据
            JSONArray jsonArray = this.managerDao.listDatabases();
            ObservableList data = FXCollections.observableArrayList();
            // 将数据进行分别获取，初始化绑定实类
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject rowObj = jsonArray.getJSONObject(i);
                int id = rowObj.getInt("id");
                String ip = rowObj.getString("ipaddress");
                String databasetype = rowObj.getString("databasetype");
                String connecttype = rowObj.getString("connecttype");
                String memo = rowObj.getString("memo");
                String addTime = rowObj.getString("addtime");
                data.add(new DatabaseDateEntity(id + "", ip, databasetype, connecttype, memo, addTime));
            }
            // 将 TableColumn 与数据关联起来，PropertyValueFactory 的值必须与 DatabaseDateEntity 的参数值一致
            idCol.setCellValueFactory(new PropertyValueFactory<>("id"));
            ipCol.setCellValueFactory(new PropertyValueFactory<>("ipaddress"));
            datebaseTypeCol.setCellValueFactory(new PropertyValueFactory<>("databasetype"));
            connectTypeCol.setCellValueFactory(new PropertyValueFactory<>("connecttype"));
            memoCol.setCellValueFactory(new PropertyValueFactory<>("memo"));
            addTimeCol.setCellValueFactory(new PropertyValueFactory<>("addtime"));
            // 将数据输出到界面
            databaseTableView.setItems(data);

            // 初始化 tableviews双击
            databaseTableView.setRowFactory(tv -> {
                TableRow row = new TableRow();
                row.setOnMouseClicked(event -> {
                    if (event.getClickCount() == 2 && (!row.isEmpty())) {
                        String type = ((DatabaseDateEntity) row.getItem()).getDatabasetype();
                        openWindows(type);
                    }
                });
                return row;
            });
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }

    }


    /**
     * 免责声明
     */
    public void initAlert() {
        YamlConfigs configs = new YamlConfigs();
        Map<String, Object> yamlToMap = configs.getYamlToMap("config.yaml");
        String Status = (String) configs.getValue("Global.StartWarn",yamlToMap);
        if ("true".equals(Status)) {
            MessageUtil.showErrorMessage("用户须知", "特别提醒，使用该工具必须遵守国家有关的政策和法律，如刑法、国家安全法、保密法、计算机信息系统安全保护条例等，保护国家利益，保护国家安全，对于违法使用该工具而引起的一切责任，由用户负全部责任。一旦您使用了本程序，将视为您已清楚了解上列全部声明并且完全同意。本程序仅供合法的渗透测试以及爱好者参考学习。");
        }
    }

    /**
     * 初始化连接数据库
     */
    public void initDatabase() {
        try {
            //初始化数据库，不存在则提示错误并退出
            this.managerDao = new ManagerDao();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
            System.exit(0);
        }
    }

    /**
     * 检查全局配置文件，若不存在则初始化配置文件
     */
    public void initConfigFile() {
        String data = "Global:\n" +
                "    StartWarn: 'true'\n" +
                "Mysql:\n" +
                "    Driver: %s\n" +
                "    ClassName: %s\n" +
                "    JDBCUrl: %s\n" +
                "Mssql:\n" +
                "    Driver: %s\n" +
                "    ClassName: %s\n" +
                "    JDBCUrl: %s\n" +
                "Oracle:\n" +
                "    Driver: %s\n" +
                "    ClassName: %s\n" +
                "    JDBCUrl: %s\n" +
                "PostgreSql:\n" +
                "    Driver: %s\n" +
                "    ClassName: %s\n" +
                "    JDBCUrl: %s";
        OutputStreamWriter osw = null;
        try {
            String path = Utils.getSelfPath();
            File checkFile = new File(path + File.separator + "config.yaml");
            if (checkFile.exists()) {
                return;
            } else {
                checkFile.createNewFile();// 创建目标文件
            }
            // {0} - IP , {1} - 端口 ，{2} - 数据库名，{3} - 超时时间
            data = String.format(data,
                    path + File.separator + "Driver" + File.separator + "mysql.jar",
                    "com.mysql.cj.jdbc.Driver",
                    "jdbc:mysql://{0}:{1}/{2}?connectTimeout={3}&socketTimeout={3}&characterEncoding=utf-8&useSSL=false&serverTimezone=UTC&rewriteBatchedStatements=true",
                    path + File.separator + "Driver" + File.separator + "mssql.jar",
                    "net.sourceforge.jtds.jdbc.Driver",
                    "jdbc:jtds:sqlserver://{0}:{1}/{2};loginTimeout={3};socketTimeout={3}",
                    path + File.separator + "Driver" + File.separator + "oracle.jar",
                    "oracle.jdbc.driver.OracleDriver",
                    "jdbc:oracle:thin:@{0}:{1}:{2}",
                    path + File.separator + "Driver" + File.separator + "postgresql.jar",
                    "org.postgresql.Driver",
                    "jdbc:postgresql://{0}:{1}/{2}?loginTimeout={3}&socketTimeout={3}"
            );
            // FileWriter(File file, boolean append)，append为true时为追加模式，false或缺省则为覆盖模式
            osw = new OutputStreamWriter(new FileOutputStream(checkFile), "UTF-8");
            osw.append(data);
            osw.flush();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        } finally {
            if (null != osw) {
                try {
                    osw.close();
                } catch (Exception e) {
                    MessageUtil.showExceptionMessage(e, e.getMessage());
                }
            }
        }
    }

    @FXML
    void settingAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/setting.fxml"));
            Stage primaryStage = new Stage();
            primaryStage.setTitle("设置");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
            //e.printStackTrace();
        }
    }

    @FXML
    void closeAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("提示");
        alert.setHeaderText("");
        alert.setContentText("确定关闭程序?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.exit(0);
        }
    }

    /**
     * 软件更新
     * @param event
     */
    @FXML
    void updateAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/update.fxml"));
            Stage primaryStage = new Stage();
            primaryStage.setTitle("更新");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
            //e.printStackTrace();
        }
    }

    @FXML
    void aboutAction(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("关于");
        alert.setHeaderText(null);
        alert.setContentText("@白帽100安全攻防实验室提醒您：\n道路千万条，安全第一条\n" +
                "渗透不规范，亲人两行泪\n\n" + "Github:https://github.com/SafeGroceryStore/MDUT");
        alert.showAndWait();
    }


    @FXML
    void OpenDatabaseAction(ActionEvent event) {
        // 获取 tableview 中的数据库类型
        String databaseType = ((DatabaseDateEntity) this.databaseTableView.getSelectionModel().getSelectedItem()).getDatabasetype();
        openWindows(databaseType);
    }

    @FXML
    void AddDatabaseAction(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/addAndEdit.fxml"));
            Stage primaryStage = new Stage();
            primaryStage.setTitle("添加");
            primaryStage.setScene(new Scene(root));
            primaryStage.setResizable(false);
            primaryStage.show();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
            //e.printStackTrace();
        }
    }

    @FXML
    void EditDatabaseAction(ActionEvent event) {
        try {
            // 由于 initTableView 里面绑定了 DatabaseDateEntity，所以只能强转成 DatabaseDateEntity 实类
            // 然后借用 DatabaseDateEntity 类进行获取值操作
            String id = ((DatabaseDateEntity) this.databaseTableView.getSelectionModel().getSelectedItem()).getId();
            Stage st = new Stage();
            // 获取数据库的数据
            JSONArray jsonArray = this.managerDao.findDataByid(id);
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/addAndEdit.fxml"));
            Parent sceneMain = loader.load();
            //loader.setController(this);
            // 初始化 controller 然后进行传值
            AddAndEditController controller = loader.<AddAndEditController>getController();
            // 将数据传到 initVariable 里面以便 AddAndEditController 进行获取和使用
            controller.initVariableShow(jsonArray);
            // 将 MainController 传到 AddAndEditController，以便 AddAndEditController 能够正常使用 initTableView 方法

            Scene scene = new Scene(sceneMain);
            st.setScene(scene);
            st.setResizable(false);
            st.setTitle("编辑");
            st.show();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
            //e.printStackTrace();
        }
    }

    @FXML
    void DeleteDatabaseAction(ActionEvent event) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("提示");
            alert.setHeaderText("");
            alert.setContentText("确定删除?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                List items = new ArrayList(this.databaseTableView.getSelectionModel().getSelectedItems());
                for (int i = 0; i < items.size(); i++){
                    String id = ((DatabaseDateEntity)items.get(i)).getId();
                    this.managerDao.delDatebaseById(id);
                }
                infoLab.setText("删除成功!");
                // 重新刷新 TableViews
                this.initTableView();
            }
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    @FXML
    void RefreshAction(ActionEvent event) {
        try {
            this.initTableView();
            infoLab.setText("刷新成功!");
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }

    /**
     * 打开数据库的总入口
     * @param databaseType
     */
    public void openWindows(String databaseType){
        switch (databaseType) {
            case "Mysql":
                this.openMysqlDatebase();
                break;
            case "PostgreSql":
                this.openPostgreSqlDatebase();
                break;
            case "Mssql":
                this.openMssqlDatebase();
                break;
            case "Redis":
                this.openRedisDatebase();
                break;
            case "Oracle":
                this.openOracleDatebase();
                break;
            default:
        }
    }

    /**
     * 打开 mysql 的窗口
     */
    private void openMysqlDatebase() {
        try {
            // 由于 initTableView 里面绑定了 DatabaseDateEntity，所以只能强转成 DatabaseDateEntity 实类
            String id = ((DatabaseDateEntity) this.databaseTableView.getSelectionModel().getSelectedItem()).getId();
            // 获取数据库
            JSONArray jsondata = this.managerDao.findDataByid(id);
            JSONObject dbObj = jsondata.getJSONObject(0);
            // 然后借用 DatabaseDateEntity 类进行获取值操作
            Stage st = new Stage();
            st.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            // 获取数据库的数据
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mysqlViewTab.fxml"));
            Parent sceneMain = loader.load();
            //loader.setController(this);
            // 初始化 controller 然后进行传值
            MysqlController mysqlcontroller = loader.<MysqlController>getController();
            // 将数据传到 initMysqlDao 里面以便 MysqlController 进行获取和使用
            mysqlcontroller.initMysqlDao(dbObj);
            // 将 MainController 传到 AddAndEditController，以便 AddAndEditController 能够正常使用 initTableView 方法
            Scene scene = new Scene(sceneMain);
            st.setScene(scene);
            //st.setResizable(false);
            st.setTitle(String.format("%s - %s - %s", dbObj.getString("databasetype"),dbObj.getString("ipaddress"), dbObj.getString("connecttype")));
            st.setOnCloseRequest((e) -> {
                Runnable runner = () -> {
                    List workerList = mysqlcontroller.getWorkList();
                    Iterator var2 = workerList.iterator();
                    while (var2.hasNext()) {
                        Thread worker = (Thread) var2.next();
                        while (worker.isAlive()) {
                            try {
                                worker.stop();
                            } catch (Exception var5) {
                            } catch (Error var6) {
                            }
                        }
                    }
                    workerList.clear();
                };
                Thread worker = new Thread(runner);
                worker.start();
            });
            st.show();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
            //e.printStackTrace();
        }
    }


    /**
     * 打开 PostgreSql 的窗口
     */
    private void openPostgreSqlDatebase() {
        try {
            // 由于 initTableView 里面绑定了 DatabaseDateEntity，所以只能强转成 DatabaseDateEntity 实类
            String id = ((DatabaseDateEntity) this.databaseTableView.getSelectionModel().getSelectedItem()).getId();
            // 获取数据库
            JSONArray jsondata = this.managerDao.findDataByid(id);
            JSONObject dbObj = jsondata.getJSONObject(0);
            // 然后借用 DatabaseDateEntity 类进行获取值操作
            Stage st = new Stage();
            st.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

            // 获取数据库的数据
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/postgreViewTab.fxml"));
            Parent sceneMain = loader.load();
            //loader.setController(this);
            // 初始化 controller 然后进行传值
            PostgreSqlController postgrecontroller = loader.<PostgreSqlController>getController();
            // 将数据传到 initMysqlDao 里面以便 MysqlController 进行获取和使用
            postgrecontroller.initPostgreDao(dbObj);
            // 将 MainController 传到 AddAndEditController，以便 AddAndEditController 能够正常使用 initTableView 方法
            Scene scene = new Scene(sceneMain);
            st.setScene(scene);
            //st.setResizable(false);
            st.setTitle(String.format("%s - %s - %s", dbObj.getString("databasetype"),dbObj.getString("ipaddress"), dbObj.getString("connecttype")));
            st.setOnCloseRequest((e) -> {
                Runnable runner = () -> {
                    List workerList = postgrecontroller.getWorkList();
                    Iterator var2 = workerList.iterator();
                    while (var2.hasNext()) {
                        Thread worker = (Thread) var2.next();
                        while (worker.isAlive()) {
                            try {
                                worker.stop();
                            } catch (Exception var5) {
                            } catch (Error var6) {
                            }
                        }
                    }
                    workerList.clear();
                };
                Thread worker = new Thread(runner);
                worker.start();
            });
            st.show();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * 打开 mssql 的窗口
     */
    private void openMssqlDatebase() {
        try {
            // 由于 initTableView 里面绑定了 DatabaseDateEntity，所以只能强转成 DatabaseDateEntity 实类
            String id = ((DatabaseDateEntity) this.databaseTableView.getSelectionModel().getSelectedItem()).getId();
            // 获取数据库
            JSONArray jsondata = this.managerDao.findDataByid(id);
            JSONObject dbObj = jsondata.getJSONObject(0);
            // 然后借用 DatabaseDateEntity 类进行获取值操作
            Stage st = new Stage();
            st.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

            // 获取数据库的数据
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/mssqlViewTab.fxml"));
            Parent sceneMain = loader.load();
            //loader.setController(this);
            // 初始化 controller 然后进行传值
            MssqlController mssqlController = loader.<MssqlController>getController();
            // 将数据传到 initMysqlDao 里面以便 MysqlController 进行获取和使用
            mssqlController.initMssqlDao(dbObj);
            // 将 MainController 传到 AddAndEditController，以便 AddAndEditController 能够正常使用 initTableView 方法
            Scene scene = new Scene(sceneMain);
            st.setScene(scene);
            //st.setResizable(false);
            st.setTitle(String.format("%s - %s - %s",dbObj.getString("databasetype"), dbObj.getString("ipaddress"), dbObj.getString("connecttype")));
            st.setOnCloseRequest((e) -> {
                Runnable runner = () -> {
                    List workerList = mssqlController.getWorkList();
                    Iterator var2 = workerList.iterator();
                    while (var2.hasNext()) {
                        Thread worker = (Thread) var2.next();
                        while (worker.isAlive()) {
                            try {
                                worker.stop();
                            } catch (Exception var5) {
                            } catch (Error var6) {
                            }
                        }
                    }
                    workerList.clear();
                };
                Thread worker = new Thread(runner);
                worker.start();
            });
            st.show();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * 打开 redis 的窗口
     */
    private void openRedisDatebase() {
        try {
            // 由于 initTableView 里面绑定了 DatabaseDateEntity，所以只能强转成 DatabaseDateEntity 实类
            String id = ((DatabaseDateEntity) this.databaseTableView.getSelectionModel().getSelectedItem()).getId();
            // 获取数据库
            JSONArray jsondata = this.managerDao.findDataByid(id);
            JSONObject dbObj = jsondata.getJSONObject(0);
            // 然后借用 DatabaseDateEntity 类进行获取值操作
            Stage st = new Stage();
            st.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));

            // 获取数据库的数据
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/redisViewTab.fxml"));
            Parent sceneMain = loader.load();
            //loader.setController(this);
            // 初始化 controller 然后进行传值
            RedisController redisController = loader.<RedisController>getController();
            // 将数据传到 initMysqlDao 里面以便 MysqlController 进行获取和使用
            redisController.initRedisDao(dbObj);
            // 将 MainController 传到 AddAndEditController，以便 AddAndEditController 能够正常使用 initTableView 方法
            Scene scene = new Scene(sceneMain);
            st.setScene(scene);
            //st.setResizable(false);
            st.setTitle(String.format("%s - %s - %s",dbObj.getString("databasetype"), dbObj.getString("ipaddress"), dbObj.getString("connecttype")));
            st.setOnCloseRequest((e) -> {
                Runnable runner = () -> {
                    List workerList = redisController.getWorkList();
                    Iterator var2 = workerList.iterator();
                    while (var2.hasNext()) {
                        Thread worker = (Thread) var2.next();
                        while (worker.isAlive()) {
                            try {
                                worker.stop();
                            } catch (Exception var5) {
                            } catch (Error var6) {
                            }
                        }
                    }
                    workerList.clear();
                };
                Thread worker = new Thread(runner);
                worker.start();
            });
            st.show();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * 打开 Oracle 的窗口
     */
    private void openOracleDatebase() {
        try {
            // 由于 initTableView 里面绑定了 DatabaseDateEntity，所以只能强转成 DatabaseDateEntity 实类
            String id = ((DatabaseDateEntity) this.databaseTableView.getSelectionModel().getSelectedItem()).getId();
            // 获取数据库
            JSONArray jsondata = this.managerDao.findDataByid(id);
            JSONObject dbObj = jsondata.getJSONObject(0);
            // 然后借用 DatabaseDateEntity 类进行获取值操作
            Stage st = new Stage();
            st.getIcons().add(new Image(getClass().getResourceAsStream("/images/logo.png")));
            // 获取数据库的数据
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/oracleViewTab.fxml"));
            Parent sceneMain = loader.load();
            //loader.setController(this);
            // 初始化 controller 然后进行传值
            OracleController oracleController = loader.<OracleController>getController();
            // 将数据传到 initMysqlDao 里面以便 MysqlController 进行获取和使用
            oracleController.initOraclelDao(dbObj);
            // 将 MainController 传到 AddAndEditController，以便 AddAndEditController 能够正常使用 initTableView 方法
            Scene scene = new Scene(sceneMain);
            st.setScene(scene);
            //st.setResizable(false);
            st.setTitle(String.format("%s - %s - %s",dbObj.getString("databasetype"), dbObj.getString("ipaddress"), dbObj.getString("connecttype")));
            st.setOnCloseRequest((e) -> {
                Runnable runner = () -> {
                    List workerList = oracleController.getWorkList();
                    Iterator var2 = workerList.iterator();
                    while (var2.hasNext()) {
                        Thread worker = (Thread) var2.next();
                        while (worker.isAlive()) {
                            try {
                                worker.stop();
                            } catch (Exception var5) {
                            } catch (Error var6) {
                            }
                        }
                    }
                    workerList.clear();
                };
                Thread worker = new Thread(runner);
                worker.start();
            });
            st.show();
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
            //e.printStackTrace();
        }
    }

    @FXML
    void resetConfigFileAction(ActionEvent event) {
        try {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("提示");
            alert.setHeaderText("");
            alert.setContentText("确定重设配置文件?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK) {
                String path = Utils.getSelfPath();
                File checkFile = new File(path + File.separator + "config.yaml");
                if (checkFile.exists()) {
                    checkFile.delete();
                }
                initConfigFile();
                infoLab.setText("重新生成配置文件成功!");
            }
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }
}
