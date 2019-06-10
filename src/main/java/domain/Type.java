package domain;

import java.util.ArrayList;
import java.util.List;

public class Type {
    int id;
    String typeName;
    String chineseName;
    int autoIsOpen;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getChineseName() {
        return chineseName;
    }

    public void setChineseName(String chineseName) {
        this.chineseName = chineseName;
    }

    public int getAutoIsOpen() {
        return autoIsOpen;
    }

    public void setAutoIsOpen(int autoIsOpen) {
        this.autoIsOpen = autoIsOpen;
    }
}
