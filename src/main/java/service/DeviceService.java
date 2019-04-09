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
    private DeviceDao DeviceDao;

    public List<Device> getDeviceByType(String type) {

        List<Device> devices = new LinkedList<>();
        if (type.equals("lamp")) {
            devices = DeviceDao.getDeviceLamp();
        } else if (type.equals("window")) {
            devices = DeviceDao.getDeviceWindow();
        } else if (type.equals("curtain")) {
            devices = DeviceDao.getDeviceCurtain();
        } else if (type.equals("smokeDetector")) {
            devices = DeviceDao.getDeviceSmokeDetector();
        } else if (type.equals("airConditioning")) {
            devices = DeviceDao.getDeviceAirConditioning();
        } else if (type.equals("camera")) {
            devices = DeviceDao.getDeviceCamera();
        }


        return devices;
    }

    public List<Type> getAllDeviceType(){
        return DeviceDao.getAllDeviceType();
    }

}
