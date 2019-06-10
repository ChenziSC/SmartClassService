package service;

import dao.*;
import domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Service
public class DeviceService {

    @Autowired
    private DeviceDao deviceDao;

    public List<Device> getDeviceByType(String type) {
        List<Device> devices = new LinkedList<>();
        if (type.equals("lamp")) {
            devices = deviceDao.getDeviceLamp();
        } else if (type.equals("window")) {
            devices = deviceDao.getDeviceWindow();
        } else if (type.equals("curtain")) {
            devices = deviceDao.getDeviceCurtain();
        } else if (type.equals("smokeDetector")) {
            devices = deviceDao.getDeviceSmokeDetector();
        } else if (type.equals("airConditioning")) {
            devices = deviceDao.getDeviceAirConditioning();
        } else if (type.equals("camera")) {
            devices = deviceDao.getDeviceCamera();
        }
        return devices;
    }

    public List<Window> getWindowList(){
        List<Window> windows = new ArrayList<Window>();
        windows = deviceDao.getWindowList();
        return windows;
    }

    public List<Curtain> getCurtainList(){
        List<Curtain> curtains = new ArrayList<Curtain>();
        curtains = deviceDao.getCurtainList();
        return curtains;
    }

    public List<AirConditioning> getAirConditioningList(){
        List<AirConditioning> airConditionings = new ArrayList<AirConditioning>();
        airConditionings = deviceDao.getAirConditioningList();
        return airConditionings;
    }

    public List<Lamp> getLampList(){
        List<Lamp> lamps = new ArrayList<Lamp>();
        lamps = deviceDao.getLampList();
        return lamps;
    }

    public List<Type> getAllDeviceType(){
        return deviceDao.getAllDeviceType();
    }

    public void updateDeviceIsOpenById(Device device){
        deviceDao.updateDeviceIsOpenById(device);
    }

    public void updataBySurroundings(Device device){
        deviceDao.updateBySurroundings(device);
    }
    public void setWindowCoverPercent(Window window){
        deviceDao.setWindowCoverPercent(window);
    }
    public void setAirconditioningTem(AirConditioning airconditioning){
        deviceDao.setAirconditioningTem(airconditioning);
    }
    public void setCurtainCoverPercent(Curtain curtain){
        deviceDao.setCurtainCoverPercent(curtain);
    }

    public void updateLamp(Lamp lamp){
        deviceDao.updateLampById(lamp);
    }
}
