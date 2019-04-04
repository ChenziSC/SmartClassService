package domain;


public class Lamp {
    int id;
    String type;
    int isOpen;
    String state;
    String position;
    int invaild;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getIsOpen() {
        return isOpen;
    }

    public void setIsOpen(int isOpen) {
        this.isOpen = isOpen;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public int getInvaild() {
        return invaild;
    }

    public void setInvaild(int invaild) {
        this.invaild = invaild;
    }
}
