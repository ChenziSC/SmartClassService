package domain;

import java.util.ArrayList;
import java.util.List;

public class Surroundings {

    double light;
    int temperature;
    double smoke;
    String createTime;

    public double getLight() {
        return light;
    }

    public void setLight(double light) {
        this.light = light;
    }

    public int getTemperature() {
        return temperature;
    }

    public void setTemperature(int temperature) {
        this.temperature = temperature;
    }

    public double getSmoke() {
        return smoke;
    }

    public void setSmoke(double smoke) {
        this.smoke = smoke;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
