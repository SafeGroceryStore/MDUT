package Controller;

import Dao.MssqlDao;
import Dao.MssqlHttpDao;
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

public class MssqlController implements Initializable {

    @FXML
    private TextField TextField_MssqlCommand;

    @FXML
    private Button Button_ReadPath;

    @FXML
    private Button Button_MssqlCommandRun;

    @FXML
    private RadioButton RadioButton_AgentJob;

    @FXML
    private RadioButton RadioButton_oashellcom;

    @FXML
    private TextField TextField_FilePath;

    @FXML
    private RadioButton RadioButton_CLR;

    @FXML
    private TableColumn<?, ?> fileSizeCol;

    @FXML
    private TableColumn<?, ?> filePermissionCol;

    @FXML
    private Button Button_CloseExp;

    @FXML
    private TableColumn<?, ?> fileNameCol;

    @FXML
    private TableColumn<?, ?> fileStartTimeCol;

    @FXML
    private MenuItem MenuItem_normalupload;

    @FXML
    private Label Label_TimeOut;

    @FXML
    private TreeView<?> TreeView_PathTree;

    @FXML
    private TableColumn<?, ?> fileTypeCol;

    @FXML
    private MenuItem MenuItem_clrmkdir;

    @FXML
    private ToggleGroup MssqlCommandTypeGroup;

    @FXML
    private Button Button_Return;

    @FXML
    public TextArea mssqlLogTextArea;

    @FXML
    private RadioButton RadioButton_xpcmdshell;

    @FXML
    private MenuItem MenuItem_clrupload;

    @FXML
    private RadioButton RadioButton_oashellbulk;

    @FXML
    private TextArea Textarea_MssqlCommandResult;

    @FXML
    private TableView<FilesEntity> TableView_Filetable;

    @FXML
    private MenuItem MenuItem_normalmkdir;

    @FXML
    private MenuItem MenuItem_normaldeletefile;

    @FXML
    private MenuItem MenuItem_normaldownload;

    @FXML
    private Button Button_getAdminPassword;

    @FXML
    private MenuItem MenuItem_clrdeletefile;

    @FXML
    private ComboBox<String> ComboBox_CLRCommnadType;

    @FXML
    private TextField TextField_OAShellTimeOut;


    @FXML
    private ComboBox<String> ComboBox_EncodeType;

    @FXML
    private Button Button_recoveryAll;


    @FXML
    private TableColumn<FilesEntity, ImageView> fileIconCol;

    /**
     * 存储从 MssqlDao 传递过来的 mssqlDao 使用
     */
    private MssqlDao mssqlDao;

    private MssqlHttpDao mssqlHttpDao;

    private JSONObject dataObj;

    private List workList = new ArrayList();

    Image folderImage = new Image(getClass().getResourceAsStream("/images/folder.png"));
    Image fileImage = new Image(getClass().getResourceAsStream("/images/file.png"));
    ObservableList fileData = FXCollections.observableArrayList();


    public List getWorkList() {
        return this.workList;
    }



    public void initMssqlDao(JSONObject dbObj){
        this.dataObj = dbObj;
        mssqlLogTextArea.appendText(Utils.log("正在连接..."));
    }


    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // 初始化当前 controllers 方便其他 controllers 调用
        ControllersFactory.controllers.put(this.getClass().getSimpleName(), this);
        // 组件初始化
        initComBoBox();
        initGroup();
        initFilesTableViews();
        // 尝试连接
        Runnable runner = () -> {
            if("false".equals(this.dataObj.getString("ishttp"))){
                try {
                    this.mssqlDao = new MssqlDao(this.dataObj.getString("ipaddress"),this.dataObj.getString("port"),this.dataObj.getString("database"),this.dataObj.getString("username"),this.dataObj.getString("password"),this.dataObj.getString("timeout"));
                    this.mssqlDao.getConnection();
                    Platform.runLater(() -> {
                        mssqlLogTextArea.appendText(Utils.log("连接成功！"));
                        // 获取版本
                        this.mssqlDao.getVersion();
                        // 检查数据库账户权限
                        this.mssqlDao.getisdba();
                        // 检查CLR函数是否存在
                        boolean isexit = this.mssqlDao.checkCLR();
                        // 文件管理参数模块初始化
                        initFileManager();
                        mssqlLogTextArea.appendText(Utils.log("文件管理初始化成功！"));
                        if(isexit){
                            RadioButton_CLR.setDisable(false);
                        }
                    });

                }catch (Exception e){
                    if("false".equals(this.dataObj.getString("ishttp"))){
                        Platform.runLater(() -> {
                            mssqlLogTextArea.appendText(Utils.log("连接失败！"));
                            MessageUtil.showExceptionMessage(e,e.getMessage());
                            try {
                                this.mssqlDao.closeConnection();
                            }catch (Exception ex){
                            }
                        });
                    }
                }
            }else {
                Platform.runLater(() -> {
                    this.mssqlHttpDao = new MssqlHttpDao(this.dataObj);
                    if(this.mssqlHttpDao.getConnection()){
                        this.mssqlHttpDao.getVersion();
                        this.mssqlHttpDao.getisdba();
                        boolean isexit = this.mssqlHttpDao.checkCLR();
                        initFileManager();
                        mssqlLogTextArea.appendText(Utils.log("文件管理初始化成功！"));
                        if(isexit){
                            RadioButton_CLR.setDisable(false);
                        }
                    }
                });

            }

        };
        Thread workThrad = new Thread(runner);
        this.workList.add(workThrad);
        workThrad.start();
    }

    /**
     * ComBoBox 组件初始化
     */
    public void initComBoBox(){
        // CLR ComBoBox 组件初始化
        RadioButton_CLR.setDisable(true);
        ComboBox_CLRCommnadType.setDisable(true);
        ObservableList<String> CLRCodeoptions = FXCollections.observableArrayList(
                "普通执行",
                "提权执行"
        );
        ComboBox_CLRCommnadType.setItems(CLRCodeoptions);

        // 编码组件初始化
        ObservableList<String> mssqlTypeCodeoptions = FXCollections.observableArrayList(
                "UTF-8",
                "GB2312",
                "GBK"
        );
        // 初始化下拉框
        ComboBox_EncodeType.setValue("UTF-8");
        ComboBox_EncodeType.setItems(mssqlTypeCodeoptions);
    }

    /**
     * 初始化 MssqlCommandTypeGroup 优化交互逻辑
     */
    public void initGroup(){
        RadioButton_oashellbulk.setUserData("oashellbulk");
        RadioButton_CLR.setUserData("clr");

        //将延时标签和编辑框设置为不可用
        Label_TimeOut.setDisable(true);
        TextField_OAShellTimeOut.setDisable(true);
        MssqlCommandTypeGroup.selectedToggleProperty().addListener(
                (ObservableValue<? extends Toggle> ov, Toggle old_toggle,
                 Toggle new_toggle) -> {
                    if (MssqlCommandTypeGroup.getSelectedToggle() != null) {
                        String userdata = "";
                        try {
                            userdata = MssqlCommandTypeGroup.getSelectedToggle().getUserData().toString();
                        }catch (Exception e){}
                        if(userdata.equals("oashellbulk")){
                            Label_TimeOut.setDisable(false);
                            TextField_OAShellTimeOut.setDisable(false);
                        }else {
                            Label_TimeOut.setDisable(true);
                            TextField_OAShellTimeOut.setDisable(true);
                        }

                        if (userdata.equals("clr")){
                            ComboBox_CLRCommnadType.setDisable(false);

                        }else {
                            ComboBox_CLRCommnadType.setDisable(true);

                        }
                    }
                });
    }

    /**
     * 文件管理基本参数控件初始化
     */
    public void initFileManager(){
        try {
            ArrayList<String> disk = new ArrayList<>();
            //设置root节点
            TreeItem rootitem = new TreeItem<>();
            Image diskImage = new Image(getClass().getResourceAsStream("/images/disk.png"));
            //获取到磁盘个数
            if("false".equals(this.dataObj.getString("ishttp"))){
               disk = this.mssqlDao.getDisk();
            }else {
               disk = this.mssqlHttpDao.getDisk();
            }
            //默认设置第一个盘为当前路径
            TextField_FilePath.setText(disk.get(0) + ":/");
            for (int i = 0; i < disk.size(); i++) {
                TreeItem<String> item = new TreeItem<> (disk.get(i) + ":/",new ImageView(diskImage));
                rootitem.getChildren().add(item);
            }
            TreeView_PathTree.setRoot(rootitem);
            TreeView_PathTree.setShowRoot(false);

            // 监听 treeview 事件
            this.TreeView_PathTree.getSelectionModel().selectedItemProperty().addListener(new ChangeListener() {
                @Override
                public void changed(ObservableValue observable, Object oldValue, Object newValue) {
                    TreeItem currentTreeItem = (TreeItem)newValue;
                    if (currentTreeItem != null) {
                        // 从下往上寻找到root的路径
                        String path = getTreeViewsPath();
                        selectFolder(currentTreeItem,path);
                    }

                }
            });
            // 监听 tableview 双击事件
        }catch (Exception e){
            MessageUtil.showExceptionMessage(e, e.getMessage());
        }
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
                        currentPath = currentPath + fileName + "/";
                        TextField_FilePath.setText(currentPath);
                        showFilesOnTable(currentPath);
                    }
                }
            });
            return row;
        });
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

    /**
     * 用于左侧 treeview 的目录检索功能
     * @param treeItem
     * @param path
     */
    public void selectFolder(TreeItem treeItem,String path){
        ArrayList<String> arrayFiles = new ArrayList<String>();
        // 设置路径
        TextField_FilePath.setText(path);
        // 清除旧数据
        treeItem.getChildren().clear();
        if("false".equals(this.dataObj.getString("ishttp"))){
            arrayFiles = this.mssqlDao.getFiles(path);
        }else {
            arrayFiles = this.mssqlHttpDao.getFiles(path);
        }

        for (int i = 0; i < arrayFiles.size(); i++) {
            String file = arrayFiles.get(i);
            String[] arrfile = file.split("\\|");
            // 0 是代表文件夹
            if(arrfile[0].equals("0")){
                treeItem.getChildren().add(new TreeItem<>(arrfile[1],new ImageView(folderImage)));
            }
        }
        treeItem.setExpanded(true);
        showFilesOnTable(path);
        //treeItem.getChildren().add();

    }

    /**
     * 检索当前目录下的文件和文件夹并且输出到 tableview
     * @param path
     */
    public void showFilesOnTable(String path){
        Runnable runnable = () -> {
            Platform.runLater(() -> {
                mssqlLogTextArea.appendText(Utils.log("正在加载目录..."));
            });
            try {
                // 清除旧数据，防止数据叠加
                fileData.clear();
                ArrayList<String> arrayFiles = new ArrayList<String>();
                // 获取当前路径所有文件夹和文件
                if("false".equals(this.dataObj.getString("ishttp"))){
                    arrayFiles = this.mssqlDao.getFiles(path);
                }else {
                    arrayFiles = this.mssqlHttpDao.getFiles(path);
                }

                for (int i = 0; i < arrayFiles.size(); i++) {
                    String file = arrayFiles.get(i);
                    String[] arrfile = file.split("\\|");
                    // 0 是目录
                    if(arrfile[0].equals("0")){
                        fileData.add(new FilesEntity(new ImageView(folderImage),arrfile[1],"","","","folder"));
                    }else {
                        fileData.add(new FilesEntity(new ImageView(fileImage),arrfile[1],"","","","file"));
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
                TableView_Filetable.refresh();
                mssqlLogTextArea.appendText(Utils.log("目录加载完成！"));
            });
        };
        Thread workThrad = new Thread(runnable);
        workThrad.start();
    }


    @FXML
    void activatexpcmdshell(ActionEvent event) {
        Runnable runner = () -> {
            Platform.runLater(() -> {
                mssqlLogTextArea.appendText(Utils.log("正在激活，请稍等..."));
                if("false".equals(this.dataObj.getString("ishttp"))){
                    this.mssqlDao.activateXPCS();
                }else {
                    this.mssqlHttpDao.activateXPCS();
                }
            });
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    @FXML
    void activateoap(ActionEvent event) {
        Runnable runner = () -> {
            Platform.runLater(() -> {
                mssqlLogTextArea.appendText(Utils.log("正在激活，请稍等..."));
                if("false".equals(this.dataObj.getString("ishttp"))){
                    this.mssqlDao.activateOAP();
                }else {
                    this.mssqlHttpDao.activateOAP();
                }
            });
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    @FXML
    void activateclr(ActionEvent event) {
        Runnable runner = () -> {
            Platform.runLater(() -> {
                mssqlLogTextArea.appendText(Utils.log("正在激活，请稍等..."));
            });
            if("false".equals(this.dataObj.getString("ishttp"))){
                if(this.mssqlDao.setTrustworthy(this.dataObj.getString("database"),"on") && this.mssqlDao.activateCLR() && this.mssqlDao.initCLR() && this.mssqlDao.createCLRFunc()){
                    RadioButton_CLR.setDisable(false);
                }
            }else {
                if(this.mssqlHttpDao.setTrustworthy(this.dataObj.getString("database"),"on") && this.mssqlHttpDao.activateCLR() && this.mssqlHttpDao.initCLR() && this.mssqlHttpDao.createCLRFunc()){
                    RadioButton_CLR.setDisable(false);
                }
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    //@FXML
    //void getAdminPassword(ActionEvent event) {
    //    Runnable runner = () -> {
    //        Platform.runLater(() -> {
    //            mssqlLogTextArea.appendText(Utils.log("正在获取，请稍等..."));
    //        });
    //        if("false".equals(this.dataObj.getString("ishttp"))){
    //            try {
    //                String res = this.mssqlDao.clrgetadminpassword();
    //                mssqlLogTextArea.appendText(Utils.log("获取成功！"));
    //                Textarea_MssqlCommandResult.setText(res);
    //            }catch (Exception e){
    //                Platform.runLater(() -> {
    //                    mssqlLogTextArea.appendText(Utils.log("获取失败！"));
    //                    MessageUtil.showExceptionMessage(e,e.getMessage());
    //                });
    //            }
    //        }else {
    //            String res = this.mssqlHttpDao.clrgetadminpassword();
    //            if(res.contains("ERROR://")){
    //                mssqlLogTextArea.appendText(Utils.log("获取失败！"));
    //            }else {
    //                mssqlLogTextArea.appendText(Utils.log("获取成功！"));
    //            }
    //            Textarea_MssqlCommandResult.setText(res);
    //
    //
    //        }
    //    };
    //    Thread workThrad = new Thread(runner);
    //    workThrad.start();
    //}

    @FXML
    void CloseExp(ActionEvent event) {
        Runnable runner = () -> {
            Platform.runLater(() -> {
                mssqlLogTextArea.appendText(Utils.log("正在清理，请稍等..."));
                if("false".equals(this.dataObj.getString("ishttp"))){
                    if(this.mssqlDao.clearHistory()){
                        Platform.runLater(() -> {
                            RadioButton_CLR.setDisable(true);
                        });
                    }

                }else {
                    if(this.mssqlHttpDao.clearHistory()){
                        Platform.runLater(() -> {
                            RadioButton_CLR.setDisable(true);
                        });
                    }
                }

            });

        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }

    @FXML
    void MssqlCommandRun(ActionEvent event) {
        String filename = Utils.getRandomString();
        String CLRtype = ComboBox_CLRCommnadType.getValue();
        String code = ComboBox_EncodeType.getValue();
        try {
            if("".equals(TextField_MssqlCommand.getText())){
                MessageUtil.showErrorMessage("请输入命令");
                return;
            }
            if(code == null){
                MessageUtil.showErrorMessage("请选择编码类型");
                return;
            }
            if(RadioButton_oashellbulk.isSelected()){
                // 由于有延时，所以利用多线程进行结果获取
                Runnable oashellBulkRunner = () -> {
                    if("false".equals(this.dataObj.getString("ishttp"))){
                        try {
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                            });
                            String temp = null;
                            temp = this.mssqlDao.runcmdOAPBULK(TextField_MssqlCommand.getText(),filename,TextField_OAShellTimeOut.getText(),code);
                            String finalTemp = temp;
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                Textarea_MssqlCommandResult.setText(finalTemp);
                            });
                        }catch (Exception e){
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                MessageUtil.showExceptionMessage(e,e.getMessage());
                            });
                        }
                    }else {
                        try {
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                            });
                            String temp = null;
                            temp = this.mssqlHttpDao.runcmdOAPBULK(TextField_MssqlCommand.getText(),filename,TextField_OAShellTimeOut.getText(),code);
                            String finalTemp = temp;
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                Textarea_MssqlCommandResult.setText(finalTemp);
                            });
                        }catch (Exception e){
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                MessageUtil.showExceptionMessage(e,e.getMessage());
                            });
                        }
                    }

                };
                Thread oashellBulkWorkThrad = new Thread(oashellBulkRunner);
                oashellBulkWorkThrad.start();
            }else if(RadioButton_oashellcom.isSelected()){
                Runnable oashellcomRunner = () -> {
                    if("false".equals(this.dataObj.getString("ishttp"))){
                        try {
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                            });
                            String temp = null;
                            temp = this.mssqlDao.runcmdOAPCOM(TextField_MssqlCommand.getText(),code);
                            String finalTemp = temp;
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                Textarea_MssqlCommandResult.setText(finalTemp);
                            });
                        }catch (Exception e){
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                MessageUtil.showExceptionMessage(e,e.getMessage());
                            });
                        }
                    }else {
                        try {
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                            });
                            String temp = null;
                            temp = this.mssqlHttpDao.runcmdOAPCOM(TextField_MssqlCommand.getText(),code);
                            String finalTemp = temp;
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                Textarea_MssqlCommandResult.setText(finalTemp);
                            });
                        }catch (Exception e){
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                MessageUtil.showExceptionMessage(e,e.getMessage());
                            });
                        }
                    }

                };
                Thread oashellcomWorkThrad = new Thread(oashellcomRunner);
                oashellcomWorkThrad.start();
            }else if(RadioButton_xpcmdshell.isSelected()){
                Runnable xpcmdshellRunner = () -> {
                    if("false".equals(this.dataObj.getString("ishttp"))){
                        try {
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                            });
                            String temp = null;
                            temp = this.mssqlDao.runcmdXPCS(TextField_MssqlCommand.getText(),code);
                            String finalTemp = temp;
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                Textarea_MssqlCommandResult.setText(finalTemp);
                            });
                        }catch (Exception e){
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                MessageUtil.showExceptionMessage(e,e.getMessage());
                            });
                        }
                    }else {
                        try {
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                            });
                            String temp = null;
                            temp = this.mssqlHttpDao.runcmdXPCS(TextField_MssqlCommand.getText(),code);
                            String finalTemp = temp;
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                Textarea_MssqlCommandResult.setText(finalTemp);
                            });
                        }catch (Exception e){
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                MessageUtil.showExceptionMessage(e,e.getMessage());
                            });
                        }
                    }

                };
                Thread xpcmdshellWorkThrad = new Thread(xpcmdshellRunner);
                xpcmdshellWorkThrad.start();
            }else if(RadioButton_AgentJob.isSelected()){
                Runnable agentJobRunner = () -> {
                    if("false".equals(this.dataObj.getString("ishttp"))){
                        try {
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                            });
                            String temp = null;
                            temp = this.mssqlDao.runcmdagent(TextField_MssqlCommand.getText(),code);
                            String finalTemp = temp;
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                Textarea_MssqlCommandResult.setText(finalTemp);
                            });
                        }catch (Exception e){
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                MessageUtil.showExceptionMessage(e,e.getMessage());
                            });
                        }
                    }else {
                        try {
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                            });
                            String temp = null;
                            temp = this.mssqlHttpDao.runcmdagent(TextField_MssqlCommand.getText(),code);
                            String finalTemp = temp;
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                Textarea_MssqlCommandResult.setText(finalTemp);
                            });
                        }catch (Exception e){
                            Platform.runLater(() -> {
                                mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                MessageUtil.showExceptionMessage(e,e.getMessage());
                            });
                        }
                    }

                };
                Thread agentJobWorkThrad = new Thread(agentJobRunner);
                agentJobWorkThrad.start();
            }else if(RadioButton_CLR.isSelected()){
                if(CLRtype == null){
                    MessageUtil.showErrorMessage("请选择执行类型");
                    return;
                }
                switch (CLRtype){
                    case "普通执行":
                        Runnable Runner = () -> {
                            if("false".equals(this.dataObj.getString("ishttp"))){
                                try {
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                                    });
                                    String temp = null;
                                    temp = this.mssqlDao.clrruncmd(TextField_MssqlCommand.getText(),"0",code);
                                    String finalTemp = temp;
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                        Textarea_MssqlCommandResult.setText(finalTemp);
                                    });
                                }catch (Exception e){
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                        MessageUtil.showExceptionMessage(e,e.getMessage());
                                    });
                                }
                            }else {
                                try {
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                                    });
                                    String temp = null;
                                    temp = this.mssqlHttpDao.clrruncmd(TextField_MssqlCommand.getText(),"0",code);
                                    temp = temp.replace("\t|\t","");
                                    String finalTemp = temp;
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                        Textarea_MssqlCommandResult.setText(finalTemp);
                                    });
                                }catch (Exception e){
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                        MessageUtil.showExceptionMessage(e,e.getMessage());
                                    });
                                }
                            }

                        };
                        Thread WorkThrad = new Thread(Runner);
                        WorkThrad.start();
                        break;
                    case "提权执行":
                        Runnable Runner2 = () -> {
                            if("false".equals(this.dataObj.getString("ishttp"))){
                                try {
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                                    });
                                    String temp = null;
                                    temp = this.mssqlDao.clrruncmd(TextField_MssqlCommand.getText(),"1",code);
                                    String finalTemp = temp;
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                        Textarea_MssqlCommandResult.setText(finalTemp);
                                    });
                                }catch (Exception e){
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                        MessageUtil.showExceptionMessage(e,e.getMessage());
                                    });
                                }
                            }else {
                                try {
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("正在执行并获取结果，请稍等..."));
                                    });
                                    String temp = null;
                                    temp = this.mssqlHttpDao.clrruncmd(TextField_MssqlCommand.getText(),"1",code);
                                    temp = temp.replace("\t|\t","");
                                    String finalTemp = temp;
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("获取结果成功！"));
                                        Textarea_MssqlCommandResult.setText(finalTemp);
                                    });
                                }catch (Exception e){
                                    Platform.runLater(() -> {
                                        mssqlLogTextArea.appendText(Utils.log("获取结果失败！"));
                                        MessageUtil.showExceptionMessage(e,e.getMessage());
                                    });
                                }
                            }

                        };
                        Thread WorkThrad2 = new Thread(Runner2);
                        WorkThrad2.start();
                        break;
                    default:
                        break;
                }
            }
            //Textarea_MssqlCommandResult.setText(res);
        }catch (Exception e) {
            MessageUtil.showExceptionMessage(e,e.getMessage());
        }
    }

    @FXML
    void ReadPath(ActionEvent event) {
        String path = TextField_FilePath.getText();
        showFilesOnTable(path);
    }

    @FXML
    void Return(ActionEvent event) {
        String path = TextField_FilePath.getText();
        path = Utils.getBeforePath(path);
        TextField_FilePath.setText(path);
        showFilesOnTable(path);
    }

    @FXML
    void normalmkdir(ActionEvent event) {
        // 获取当前文件路径
        String path = TextField_FilePath.getText();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("新建文件夹");
        dialog.setContentText("文件夹名字:");
        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if("".equals(result.get())){
                MessageUtil.showErrorMessage("请输入文件夹名称");
                return;
            }
            Runnable runnable = () -> {
                Platform.runLater(() ->{
                    mssqlLogTextArea.appendText(Utils.log("正在新建目录请稍等..."));
                });
                try {
                    if("false".equals(this.dataObj.getString("ishttp"))){
                        this.mssqlDao.normalmkdir(path+result.get());
                    }else {
                        String temp = this.mssqlHttpDao.normalmkdir(path+result.get());
                        if(temp.contains("ERROR://")){
                            Platform.runLater(() ->{
                                mssqlLogTextArea.appendText(Utils.log("新建目录失败!错误：" + temp.replace("ERROR://","")));
                            });
                            return;
                        }
                    }
                    Platform.runLater(() ->{
                        mssqlLogTextArea.appendText(Utils.log("新建目录成功!"));
                    });
                    // 刷新目录
                    showFilesOnTable(path);
                } catch (SQLException e) {
                    Platform.runLater(() ->{
                        mssqlLogTextArea.appendText(Utils.log("新建目录失败!"));
                        MessageUtil.showExceptionMessage(e,e.getMessage());
                    });
                }
            };
            Thread workThrad = new Thread(runnable);
            workThrad.start();
        }
    }

    @FXML
    void clrmkdir(ActionEvent event) {
        // 获取当前文件路径
        String path = TextField_FilePath.getText();
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("新建文件夹");
        dialog.setContentText("文件夹名字:");
        // Traditional way to get the response value.
        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()){
            if("".equals(result.get())){
                MessageUtil.showErrorMessage("请输入文件夹名称");
                return;
            }
            Runnable runnable = () -> {
                Platform.runLater(() ->{
                    mssqlLogTextArea.appendText(Utils.log("正在新建目录请稍等..."));
                });
                try {
                    if("false".equals(this.dataObj.getString("ishttp"))) {
                        this.mssqlDao.clrmkdir(path+result.get());
                        Platform.runLater(() ->{
                            mssqlLogTextArea.appendText(Utils.log("新建目录成功!"));
                        });
                    }else {
                        this.mssqlHttpDao.clrmkdir(path+result.get());
                    }
                    // 刷新目录
                    showFilesOnTable(path);
                } catch (SQLException e) {
                    Platform.runLater(() ->{
                        mssqlLogTextArea.appendText(Utils.log("新建失败!"));
                        MessageUtil.showExceptionMessage(e,e.getMessage());
                    });
                }
            };
            Thread workThrad = new Thread(runnable);
            workThrad.start();
        }
    }

    @FXML
    void normalUpload(ActionEvent event) {
        Stage stage = new Stage();
        if("".equals(TextField_FilePath.getText())){
            mssqlLogTextArea.appendText(Utils.log("当前目录不存在！"));
            return;
        }
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            Runnable runnable = () -> {
                Platform.runLater(() ->{
                    mssqlLogTextArea.appendText(Utils.log("正在上传请稍等..."));
                });
                String res = null;
                //res = Utils.binToHexString(file.getAbsolutePath());
                res = Utils.bytes2HexString(Utils.toByteArray(file.getAbsolutePath()));
                if("false".equals(this.dataObj.getString("ishttp"))) {
                    this.mssqlDao.normalUpload(TextField_FilePath.getText()+file.getName(),res);
                }else {
                    this.mssqlHttpDao.normalUpload(TextField_FilePath.getText()+file.getName(),res);
                }
                showFilesOnTable(TextField_FilePath.getText());
            };
            Thread workThrad = new Thread(runnable);
            workThrad.start();
        }

    }

    @FXML
    void normalDownload(ActionEvent event) {
        try {
            String fileName = this.TableView_Filetable.getSelectionModel().getSelectedItem().getFileName();
            String fileType = this.TableView_Filetable.getSelectionModel().getSelectedItem().getFileType();
            String path = TextField_FilePath.getText();
            if("folder".equals(fileType)){
                MessageUtil.showErrorMessage("不能下载文件夹！");
                return;
            }
            if("".equals(TextField_FilePath.getText()) || "".equals(fileName)){
                MessageUtil.showErrorMessage("当前文件不存在！");
                return;
            }
            FileChooser dc = new FileChooser();
            dc.setTitle("选择一个文件夹");
            dc.setInitialFileName(fileName);
            File file = dc.showSaveDialog(new Stage());
            if(file != null){
                //System.out.println(file.getAbsolutePath());
                Runnable runnable = () -> {
                    Platform.runLater(() ->{
                        mssqlLogTextArea.appendText(Utils.log("正在下载请稍等..."));
                    });
                    try {
                        String res = "";
                        if("false".equals(this.dataObj.getString("ishttp"))) {
                            // 下载文本的数据
                            res = this.mssqlDao.normalDownload(path + fileName);
                            // 写入到本地文件
                            Utils.writeFile(file.toString(),res);
                            // 将 hex 数据转为 byte 数组
                            //byte[] resByte = Utils.hexToByte(res);
                            // 写入到本地文件
                            //Utils.writeFileByBytes(file.toString(), resByte, false);
                        }else {
                            res = this.mssqlHttpDao.normalDownload(path+fileName);
                            Utils.writeFile(file.toString(),res);
                        }
                        Platform.runLater(() ->{
                            mssqlLogTextArea.appendText(Utils.log(fileName + " 下载成功!"));
                        });
                        // 刷新目录
                        showFilesOnTable(path);
                    } catch (Exception e) {
                        Platform.runLater(() ->{
                            mssqlLogTextArea.appendText(Utils.log("下载失败!"));
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
    void normalDelete(ActionEvent event) {
        try {
            String fileName = this.TableView_Filetable.getSelectionModel().getSelectedItem().getFileName();
            String fileType = this.TableView_Filetable.getSelectionModel().getSelectedItem().getFileType();
            String path = TextField_FilePath.getText();
            if("folder".equals(fileType)){
                MessageUtil.showErrorMessage("不能删除文件夹！");
                return;
            }
            if("".equals(TextField_FilePath.getText()) || "".equals(fileName)){
                MessageUtil.showErrorMessage("当前文件不存在！");
                return;
            }
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("删除");
            alert.setHeaderText("");
            alert.setContentText("是否删除 " + fileName + "?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                Runnable runnable = () -> {
                    Platform.runLater(() ->{
                        mssqlLogTextArea.appendText(Utils.log("正在删除请稍等..."));
                    });
                    try {
                        if("false".equals(this.dataObj.getString("ishttp"))) {
                            this.mssqlDao.normaldelete(path+fileName);
                        }else {
                            String temp = this.mssqlHttpDao.normaldelete(path+fileName);
                            if(temp.contains("ERROR://")){
                                Platform.runLater(() ->{
                                    mssqlLogTextArea.appendText(Utils.log("删除失败!错误：" + temp.replace("ERROR://","")));
                                });
                                return;
                            }
                        }
                        Platform.runLater(() ->{
                            mssqlLogTextArea.appendText(Utils.log("删除成功!"));
                        });
                        // 刷新目录
                        showFilesOnTable(path);
                    } catch (SQLException e) {
                        Platform.runLater(() ->{
                            mssqlLogTextArea.appendText(Utils.log("删除失败!"));
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
    void clrUpload(ActionEvent event) {
        Stage stage = new Stage();
        if("".equals(TextField_FilePath.getText())){
            mssqlLogTextArea.appendText(Utils.log("当前目录不存在！"));
            return;
        }
        FileChooser fileChooser = new FileChooser();
        File file = fileChooser.showOpenDialog(stage);
        if(file != null){
            Runnable runnable = () -> {
                Platform.runLater(() ->{
                    mssqlLogTextArea.appendText(Utils.log("正在上传请稍等..."));
                });
                String res = null;
                res = Utils.bytes2HexString(Utils.toByteArray(file.getAbsolutePath()));
                try {
                    if("false".equals(this.dataObj.getString("ishttp"))) {
                        this.mssqlDao.clrupload(TextField_FilePath.getText()+file.getName(),res);
                        Platform.runLater(() ->{
                            mssqlLogTextArea.appendText(Utils.log("上传成功!"));
                        });
                    }else {
                        this.mssqlHttpDao.clrupload(TextField_FilePath.getText()+file.getName(),res);
                    }
                    showFilesOnTable(TextField_FilePath.getText());
                } catch (SQLException e) {
                    Platform.runLater(() ->{
                        mssqlLogTextArea.appendText(Utils.log("删除失败!"));
                        MessageUtil.showExceptionMessage(e,e.getMessage());
                    });
                }

            };
            Thread workThrad = new Thread(runnable);
            workThrad.start();
        }
    }

    @FXML
    void clrDelete(ActionEvent event) {
        try {
            String fileName = this.TableView_Filetable.getSelectionModel().getSelectedItem().getFileName();
            String path = TextField_FilePath.getText();
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmation Dialog");
            alert.setHeaderText("");
            alert.setContentText("是否删除 " + fileName + "?");
            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == ButtonType.OK){
                Runnable runnable = () -> {
                    Platform.runLater(() ->{
                        mssqlLogTextArea.appendText(Utils.log("正在删除请稍等..."));
                    });
                    try {
                        if("false".equals(this.dataObj.getString("ishttp"))) {
                            this.mssqlDao.clrdelete(path+fileName);
                            Platform.runLater(() ->{
                                mssqlLogTextArea.appendText(Utils.log("删除成功!"));
                            });
                        }else {
                            this.mssqlHttpDao.clrdelete(path+fileName);
                        }
                        // 刷新目录
                        showFilesOnTable(path);
                    } catch (SQLException e) {
                        Platform.runLater(() ->{
                            mssqlLogTextArea.appendText(Utils.log("删除失败!"));
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
    void recoveryAllAction(ActionEvent event) {
        Runnable runner = () -> {
            Platform.runLater(() -> {
                mssqlLogTextArea.appendText(Utils.log("正在恢复所有组件，请稍等..."));
            });
            if("false".equals(this.dataObj.getString("ishttp"))){
                this.mssqlDao.recoveryAll();
            }else {
                this.mssqlHttpDao.recoveryAll();
            }
        };
        Thread workThrad = new Thread(runner);
        workThrad.start();
    }


}
