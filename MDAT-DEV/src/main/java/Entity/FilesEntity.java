package Entity;

import javafx.beans.property.SimpleStringProperty;
import javafx.scene.image.ImageView;

/**
 * @author ch1ng
 */
public class FilesEntity {


    private ImageView fileIcon = new ImageView();
    private SimpleStringProperty fileName = new SimpleStringProperty();
    private SimpleStringProperty fileStartTime = new SimpleStringProperty();
    private SimpleStringProperty fileSize = new SimpleStringProperty();
    private SimpleStringProperty filePermission = new SimpleStringProperty();
    private SimpleStringProperty fileType = new SimpleStringProperty();

    public FilesEntity(ImageView fileIcon,String fileName, String fileStartTime, String fileSize, String filePermission,String fileTypeCol) {
        this.setFileIcon(fileIcon);
        this.setFileName(fileName);
        this.setFileStartTime(fileStartTime);
        this.setFileSize(fileSize);
        this.setFilePermission(filePermission);
        this.setFileType(fileTypeCol);
    }
    public String getFileType() {
        return fileType.get();
    }

    public SimpleStringProperty fileTypeColProperty() {
        return fileType;
    }

    public void setFileType(String fileTypeCol) {
        this.fileType.set(fileTypeCol);
    }

    public ImageView getFileIcon() {
        return fileIcon;
    }

    public void setFileIcon(ImageView fileIcon) {
        this.fileIcon = fileIcon;
    }

    public String getFileName() {
        return fileName.get();
    }

    public SimpleStringProperty fileNameProperty() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName.set(fileName);
    }

    public String getFileStartTime() {
        return fileStartTime.get();
    }

    public SimpleStringProperty fileStartTimeProperty() {
        return fileStartTime;
    }

    public void setFileStartTime(String fileStartTime) {
        this.fileStartTime.set(fileStartTime);
    }

    public String getFileSize() {
        return fileSize.get();
    }

    public SimpleStringProperty fileSizeProperty() {
        return fileSize;
    }

    public void setFileSize(String fileSize) {
        this.fileSize.set(fileSize);
    }

    public String getFilePermission() {
        return filePermission.get();
    }

    public SimpleStringProperty filePermissionProperty() {
        return filePermission;
    }

    public void setFilePermission(String filePermission) {
        this.filePermission.set(filePermission);
    }



}
