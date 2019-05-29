package service;

import dao.*;
import domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public List<Type> getAllDeviceType(){
        return deviceDao.getAllDeviceType();
    }

    public void updateDeviceIsOpenById(Device device){
        deviceDao.updateDeviceIsOpenById(device);
    }

}
