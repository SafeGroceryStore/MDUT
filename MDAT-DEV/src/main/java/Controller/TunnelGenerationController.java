package Controller;

/**
 * @author ch1ng
 * @date 2022/6/14
 */

import Util.MessageUtil;
import Util.Utils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class TunnelGenerationController implements Initializable {

    @FXML
    private Button createBtn;

    @FXML
    private ComboBox<String> databaseTypeComboBox;

    @FXML
    private TextField keyTextField;

    @FXML
    private Button randomGenerateBtn;

    @FXML
    private Button saveBtn;

    @FXML
    private ComboBox<String> scriptTypeComboBox;

    @FXML
    private TextArea tunnelScriptTextArea;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化 scriptType
        ObservableList<String> scriptTypeOptions = FXCollections.observableArrayList(
                "JSP",
                "ASPX",
                "PHP"
        );
        // 初始化下拉框
        scriptTypeComboBox.setPromptText("JSP");
        scriptTypeComboBox.setValue("JSP");
        scriptTypeComboBox.setItems(scriptTypeOptions);

        // 初始化 databaseType
        ObservableList<String> databaseTypeOptions = FXCollections.observableArrayList(
                "Mssql",
                "Mysql",
                "Oracle",
                "PostgreSql"
        );
        // 初始化下拉框
        databaseTypeComboBox.setPromptText("Oracle");
        databaseTypeComboBox.setValue("Oracle");
        databaseTypeComboBox.setItems(databaseTypeOptions);


    }

    @FXML
    void create(ActionEvent event) {
        try {

            String databaseType = databaseTypeComboBox.getValue();
            String scriptType = scriptTypeComboBox.getValue();
            String key = keyTextField.getText();
            String path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Template" + File.separator + databaseType.toLowerCase() + "_tunnel." + scriptType.toLowerCase();
            String content = Utils.readFile(path);
            if(key.equals("")){
                MessageUtil.showErrorMessage("请填写密钥！");
                return;
            }
            tunnelScriptTextArea.setText(content.replace("{KeyString}",key));
        } catch (Exception e) {
            String except = e.getMessage();
            if(except.contains("No such file or directory")){
                tunnelScriptTextArea.setText("暂时不支持此脚本");
            }
        }


    }

    @FXML
    void randomGenerate(ActionEvent event) {
        keyTextField.setText(Utils.getRandomString());
    }

    @FXML
    void save(ActionEvent event) {
        FileChooser dc = new FileChooser();
        dc.setTitle("选择一个文件夹");
        File file = dc.showSaveDialog(new Stage());
        if(file != null){
            try {
                Utils.writeFile(file.toString() ,tunnelScriptTextArea.getText());
                MessageUtil.showInfoMessage("保存成功！");
            } catch (Exception ignored) {
            }
        }
    }


}

