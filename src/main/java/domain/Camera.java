package domain;


public class Camera extends Device{
    String state;
    int lazyNum;

    public Camera() {
        type = "camera";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getLazyNum() {
        return lazyNum;
    }

    public void setLazyNum(int lazyNum) {
        this.lazyNum = lazyNum;
    }
}
