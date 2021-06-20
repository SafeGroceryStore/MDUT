package Controller;

import Util.MessageUtil;
import Util.YamlConfigs;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.Map;
import java.util.ResourceBundle;

/**
 * @author ch1ng
 */
public class SettingController implements Initializable {


    @FXML
    private CheckBox warnBox;

    @FXML
    private Button mssqlBtn;

    @FXML
    private CheckBox autoUpdateBox;

    @FXML
    private TextField mysqlJDBCUrlText;

    @FXML
    private Button oracleBtn;

    @FXML
    private Button mysqlBtn;

    @FXML
    private TextField mssqlJDBCUrlText;

    @FXML
    private TextField postgreSqlJDBCUrlText;

    @FXML
    private TextField mysqlText;

    @FXML
    private TextField mssqlClassNameText;

    @FXML
    private Button cancelBtn;

    @FXML
    private TextField oracleText;

    @FXML
    private TextField oracleJDBCUrlText;

    @FXML
    private Button postgreSqlBtn;

    @FXML
    private TextField mysqlClassNameText;

    @FXML
    private TextField oracleClassNameText;

    @FXML
    private TextField mssqlText;

    @FXML
    private TextField postgreSqlText;

    @FXML
    private TextField postgreSqlClassNameText;

    @FXML
    private Button saveBtn;


    // 当前文件全局初始化入口
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        InitProperties();
    }


    /**
     * 初始化界面参数
     */
    public void InitProperties(){
        try {
            YamlConfigs configs = new YamlConfigs();
            Map<String, Object> yamlToMap = configs.getYamlToMap("config.yaml");
            //
            // 获取 conf.properties 内容并且加载到对应变量
            String autoUpdate = (String) configs.getValue("Global.AutoUpdate",yamlToMap);
            String StartWarn = (String) configs.getValue("Global.StartWarn",yamlToMap);

            String mysqlDriver = (String) configs.getValue("Mysql.Driver",yamlToMap);
            String mysqlClassName = (String) configs.getValue("Mysql.ClassName",yamlToMap);
            String mysqlJDBCUrl = (String) configs.getValue("Mysql.JDBCUrl",yamlToMap);

            String mssqlDriver = (String) configs.getValue("Mssql.Driver",yamlToMap);
            String mssqlClassName = (String) configs.getValue("Mssql.ClassName",yamlToMap);
            String mssqlJDBCUrl = (String) configs.getValue("Mssql.JDBCUrl",yamlToMap);

            String oracleDriver = (String) configs.getValue("Oracle.Driver",yamlToMap);
            String oracleClassName = (String) configs.getValue("Oracle.ClassName",yamlToMap);
            String oracleJDBCUrl = (String) configs.getValue("Oracle.JDBCUrl",yamlToMap);


            String posetgreSqlDriver = (String) configs.getValue("PostgreSql.Driver",yamlToMap);
            String posetgreSqlClassName = (String) configs.getValue("PostgreSql.ClassName",yamlToMap);
            String posetgreSqlJDBCUrl = (String) configs.getValue("PostgreSql.JDBCUrl",yamlToMap);
            // 判断是否需要自启更新
            if("false".equals(autoUpdate)){
                autoUpdateBox.setSelected(false);
            }else {
                autoUpdateBox.setSelected(true);
            }
            // 判断是否需要弹出用户须知
            if("false".equals(StartWarn)){
                warnBox.setSelected(false);
            }else {
                warnBox.setSelected(true);
            }
            // 输出到对应的 Text 框
            mysqlText.setText(mysqlDriver);
            mysqlClassNameText.setText(mysqlClassName);
            mysqlJDBCUrlText.setText(mysqlJDBCUrl);

            mssqlText.setText(mssqlDriver);
            mssqlClassNameText.setText(mssqlClassName);
            mssqlJDBCUrlText.setText(mssqlJDBCUrl);

            oracleText.setText(oracleDriver);
            oracleClassNameText.setText(oracleClassName);
            oracleJDBCUrlText.setText(oracleJDBCUrl);

            postgreSqlText.setText(posetgreSqlDriver);
            postgreSqlClassNameText.setText(posetgreSqlClassName);
            postgreSqlJDBCUrlText.setText(posetgreSqlJDBCUrl);
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e,e.getMessage());
        }
    }

    @FXML
    void MysqlAction(ActionEvent event) {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            mysqlText.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void MssqlAction(ActionEvent event) {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            mssqlText.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void OracleAction(ActionEvent event) {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            oracleText.setText(file.getAbsolutePath());
        }
    }

    @FXML
    void PostgreSqlAction(ActionEvent event) {
        Stage stage = new Stage();
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            postgreSqlText.setText(file.getAbsolutePath());
        }
    }


    @FXML
    void SaveAction(ActionEvent event) {
        // 获取对应的控件状态以及值
        String mysqltext = mysqlText.getText();
        String mysqlClassName = mysqlClassNameText.getText();
        String mysqlJDBCUrl = mysqlJDBCUrlText.getText();

        String mssqltext = mssqlText.getText();
        String mssqlClassName = mssqlClassNameText.getText();
        String mssqlJDBCUrl = mssqlJDBCUrlText.getText();

        String oracletext = oracleText.getText();
        String oracleClassName = oracleClassNameText.getText();
        String oracleJDBCUrl = oracleJDBCUrlText.getText();

        String postgreSqltext = postgreSqlText.getText();
        String postgreSqlClassName = postgreSqlClassNameText.getText();
        String postgreSqlJDBCUrl = postgreSqlJDBCUrlText.getText();

        String autoupdatebox = autoUpdateBox.isSelected() ? "true" : "false";
        String warnebox = warnBox.isSelected() ? "true" : "false";
        YamlConfigs configs = new YamlConfigs();
        Map<String, Object> yamlToMap = configs.getYamlToMap("config.yaml");
        try {
            // 修改配置文件对应的值
            configs.updateYaml("Global.AutoUpdate",autoupdatebox, "config.yaml");
            configs.updateYaml("Global.StartWarn",warnebox, "config.yaml");


            configs.updateYaml("Mysql.Driver",mysqltext, "config.yaml");
            configs.updateYaml("Mysql.ClassName",mysqlClassName, "config.yaml");
            configs.updateYaml("Mysql.JDBCUrl",mysqlJDBCUrl, "config.yaml");

            configs.updateYaml("Mssql.Driver",mssqltext, "config.yaml");
            configs.updateYaml("Mssql.ClassName",mssqlClassName, "config.yaml");
            configs.updateYaml("Mssql.JDBCUrl",mssqlJDBCUrl, "config.yaml");

            configs.updateYaml("Oracle.Driver",oracletext, "config.yaml");
            configs.updateYaml("Oracle.ClassName",oracleClassName, "config.yaml");
            configs.updateYaml("Oracle.JDBCUrl",oracleJDBCUrl, "config.yaml");

            configs.updateYaml("PostgreSql.Driver",postgreSqltext, "config.yaml");
            configs.updateYaml("PostgreSql.ClassName",postgreSqlClassName, "config.yaml");
            configs.updateYaml("PostgreSql.JDBCUrl",postgreSqlJDBCUrl, "config.yaml");

            MessageUtil.showInfoMessage("提示","保存成功！");
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e,e.getMessage());
        }
    }

    @FXML
    void CancelAction(ActionEvent event) {
        //获取窗口 windows 然后关闭
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

}