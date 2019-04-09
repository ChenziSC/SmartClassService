package dao;

import domain.*;

import java.util.List;
import java.util.Map;

public interface DeviceDao {
    /**
     * 获取lamp类别的device
     *
     * @return
     */
    List<Device> getDeviceLamp();

    /**
     * 获取window类别的device
     *
     * @return
     */
    List<Device> getDeviceWindow();

    /**
     * 获取curtain类别的device
     *
     * @return
     */
    List<Device> getDeviceCurtain();

    /**
     * 获取smokeDetector类别的device
     *
     * @return
     */
    List<Device> getDeviceSmokeDetector();

    /**
     * 获取airConditioning类别的device
     *
     * @return
     */
    List<Device> getDeviceAirConditioning();

    /**
     * 获取camera类别的device
     *
     * @return
     */
    List<Device> getDeviceCamera();

    /**
     * 获取所有device的type
     *
     * @return
     */
    List<Type> getAllDeviceType();
}
