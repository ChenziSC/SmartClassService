package domain;


public class Lamp extends Device {
    String state;

    public Lamp() {
        this.type = "lamp";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
