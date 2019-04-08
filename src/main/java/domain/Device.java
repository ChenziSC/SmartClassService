package domain;

public class Device {
    int id;
    String name;
    String type;
    int isOpen;
    String position;
    int invaild;

    public Device() {
    }


    void turnUp() {
        isOpen = 1;
    }

    void turnOff() {
        isOpen = 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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
