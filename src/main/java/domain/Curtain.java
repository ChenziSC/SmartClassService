package domain;


public class Curtain extends Device{
    String state;
    int coverPercent;

    public Curtain() {
        type = "curtain";
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
