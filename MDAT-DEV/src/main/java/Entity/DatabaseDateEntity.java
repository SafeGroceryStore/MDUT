package Entity;

import javafx.beans.property.SimpleStringProperty;


/**
 * TableView 视图绑定类
 * @author ch1ng
 */
public class DatabaseDateEntity {
    private SimpleStringProperty id = new SimpleStringProperty();
    private SimpleStringProperty ipaddress = new SimpleStringProperty();
    private SimpleStringProperty databasetype = new SimpleStringProperty();
    private SimpleStringProperty connecttype = new SimpleStringProperty();
    private SimpleStringProperty memo = new SimpleStringProperty();
    private SimpleStringProperty addtime = new SimpleStringProperty();


    public DatabaseDateEntity(String id, String ipaddress, String databasetype, String connecttype, String memo, String addtime) {
        this.id.set(id);
        this.ipaddress.set(ipaddress);
        this.databasetype.set(databasetype);
        this.connecttype.set(connecttype);
        this.memo.set(memo);
        this.addtime.set(addtime);
    }

    public String getId() {
        return id.get();
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getIpaddress() {
        return ipaddress.get();
    }

    public SimpleStringProperty ipaddressProperty() {
        return ipaddress;
    }

    public void setIpaddress(String ipaddress) {
        this.ipaddress.set(ipaddress);
    }

    public String getDatabasetype() {
        return databasetype.get();
    }

    public SimpleStringProperty databasetypeProperty() {
        return databasetype;
    }

    public void setDatabasetype(String databasetype) {
        this.databasetype.set(databasetype);
    }

    public String getConnecttype() {
        return connecttype.get();
    }

    public SimpleStringProperty connecttypeProperty() {
        return connecttype;
    }

    public void setConnecttype(String connecttype) {
        this.connecttype.set(connecttype);
    }

    public String getMemo() {
        return memo.get();
    }

    public SimpleStringProperty memoProperty() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo.set(memo);
    }

    public String getAddtime() {
        return addtime.get();
    }

    public SimpleStringProperty addtimeProperty() {
        return addtime;
    }

    public void setAddtime(String addtime) {
        this.addtime.set(addtime);
    }
}
