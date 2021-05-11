package Util;

import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

import java.io.PrintWriter;
import java.io.StringWriter;

/**
 * @author ch1ng
 */
public class MessageUtil {

    /**
     * 获取 Exception 进行美化输出
     * @param ex Exception eg: ex
     * @param contentText 简要的报错 eg: ex.getMessage()
     */
    public static void showExceptionMessage(Exception ex,String contentText) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("错误");
        alert.setHeaderText("");
        alert.setContentText(contentText);
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);

        alert.getDialogPane().setExpandableContent(expContent);
        alert.showAndWait();
    }

    /**
     * 报错美化输出
     * @param title 提示标题
     * @param msg 提示内容
     */
    public static void showErrorMessage(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest((event) -> {
            window.hide();
        });
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(msg);
        alert.showAndWait();
    }

    /**
     * 信息美化输出
     * @param title 提示标题
     * @param msg 提示内容
     */
    public static void showInfoMessage(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        Window window = alert.getDialogPane().getScene().getWindow();
        window.setOnCloseRequest((event) -> {
            window.hide();
        });
        alert.setTitle(title);
        alert.setHeaderText("");
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
