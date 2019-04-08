package domain;


public class Window extends Device{
    String state;
    int coverPercent;

    public Window() {
        type = "window";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getCoverPercent() {
        return coverPercent;
    }

    public void setCoverPercent(int coverPercent) {
        this.coverPercent = coverPercent;
    }
}
