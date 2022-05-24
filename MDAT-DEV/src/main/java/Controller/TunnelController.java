package Controller;

import Util.MessageUtil;
import Util.Utils;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;

public class TunnelController implements Initializable {

    @FXML
    private TextArea tunnelScript;

    @FXML
    private Button createTunnel;

    @FXML
    private ComboBox<String> databaseType;

    @FXML
    private TextField encryptKey;

    @FXML
    private Button randomKey;

    @FXML
    private ComboBox<String> tunnelType;


    @Override
    public void initialize(URL location, ResourceBundle resources) {

    }

    @FXML
    void createTunnelcAction(ActionEvent event) {
        String key = encryptKey.getText();
        try {
            if("".equals(key)){
                MessageUtil.showErrorMessage("请填写加密密钥！");
                return;
            }
            String path = Utils.getSelfPath() + File.separator + "Plugins" + File.separator + "Template" + File.separator;
            String savePath = Utils.getSelfPath() + File.separator + "Server" + File.separator ;
            File file = new File(path);
            File[] fs = file.listFiles();
            for(File f:fs){
                if(!f.isDirectory()) {
                    String content = Utils.readFile(f.getAbsolutePath());
                    Utils.writeFile(savePath + f.getName() ,content.replace("{KeyString}",key));
                }
            }
            MessageUtil.showInfoMessage("生成成功！脚本当前目录的 Server 目录");
        } catch (Exception e) {
            MessageUtil.showExceptionMessage(e,e.getMessage());
        }
    }

    @FXML
    void randomKeyAction(ActionEvent event) {
        encryptKey.setText(Utils.getRandomString());
    }




}
