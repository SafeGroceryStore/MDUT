package Controller;


import Util.HttpUtil;
import Util.MessageUtil;
import com.alibaba.fastjson.JSONObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

import java.io.File;
import java.net.URL;
import java.util.ResourceBundle;
import Util.Utils;

/**
 * @author ch1ng
 * @date 2021/8/16
 */
public class UpdateController implements Initializable {

    @FXML
    private TextArea updateLogTextArea;

    @FXML
    private Label currentVersionLabel;

    @FXML
    private Button cancelBtn;

    @FXML
    private Button checkVersionBtn;

    @FXML
    private Label updateMsgLabel;

    @FXML
    private Label newVersionLabel;

    @FXML
    private Button downloadBtn;

    /**
     * 存放 checkVersion 的返回值
     */
    private JSONObject versionData;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        currentVersionLabel.setText("当前版本: " + Utils.getCurrentVersion());
        downloadBtn.setDisable(true);
    }

    @FXML
    void downloadAction(ActionEvent event) {
        Runnable runner = () -> {
            try {
                Platform.runLater(() -> {
                    updateMsgLabel.setText("正在下载请稍等...");
                });
                String downloadUrl = versionData.getString("downloadurl");
                String name = versionData.getString("name");
                String time = Utils.currentTime();
                String currentPath = Utils.getSelfPath() + File.separator;
                if(HttpUtil.downloadFile(downloadUrl, currentPath + time + "-" + name)){
                    Platform.runLater(() -> {
                        updateMsgLabel.setText("下载完成！请手动解压替换！");
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    MessageUtil.showExceptionMessage(e, e.getMessage());
                    updateMsgLabel.setText("下载失败！请检查网络是否通畅！");
                });
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    @FXML
    void cancelAction(ActionEvent event) {
        //获取窗口 windows 然后关闭
        Stage stage = (Stage) cancelBtn.getScene().getWindow();
        stage.close();
    }

    @FXML
    void checkVersion(ActionEvent event) {
        Runnable runner = () -> {
            try {
                Platform.runLater(() -> {
                    updateMsgLabel.setText("正在检查请稍等...");
                });
                versionData = Utils.checkVersion();
                if("true".equals(versionData.getString("isupdate"))){
                    Platform.runLater(() -> {
                        newVersionLabel.setText("最新版本: " + versionData.getString("version"));
                        updateMsgLabel.setText("新版本已发布！请点击下载按钮下载更新");
                        updateLogTextArea.setText(versionData.getString("body"));
                        downloadBtn.setDisable(false);
                    });
                }else {
                    Platform.runLater(() -> {
                        updateMsgLabel.setText("当前版本已经最新！");
                        newVersionLabel.setText("最新版本: " + versionData.getString("version"));
                    });
                }
            } catch (Exception e) {
                Platform.runLater(() -> {
                    MessageUtil.showExceptionMessage(e, e.getMessage());
                    updateMsgLabel.setText("检查失败！请检查网络是否通畅！");
                });
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }
}
