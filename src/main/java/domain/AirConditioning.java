package domain;


public class AirConditioning extends Device {
    String state;
    int settingTemperature;

    public AirConditioning() {
        type = "airConditioning";
    }

    public void temperatureUp() {
        settingTemperature++;
    }

    public void temperatureDown() {
        settingTemperature--;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public int getSettingTemperature() {
        return settingTemperature;
    }

    public void setSettingTemperature(int settingTemperature) {
        this.settingTemperature = settingTemperature;
    }

}
