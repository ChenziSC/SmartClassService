package domain;


public class SmokeDetector extends Device{
    String state;

    public SmokeDetector() {
        type = "smokeDetector";
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

}
