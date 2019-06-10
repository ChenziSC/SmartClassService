package dao;

import domain.*;
import org.apache.http.impl.client.AIMDBackoffManager;
import org.apache.ibatis.annotations.Param;

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

    List<Window> getWindowList();

    List<AirConditioning> getAirConditioningList();

    List<Curtain> getCurtainList();
    List<Lamp> getLampList();

    /**
     * 修改设备开关状态
     *
     * @return
     */
    void updateDeviceIsOpenById(Device device);

    /**
     * 修改窗户状态
     *
     */
    void setWindowCoverPercent(Window window);

    /**
     * 修改空调状态
     *
     */
    void setAirconditioningTem(AirConditioning airconditioning);

    /**
     * 修改摄像头状态
     * @param device
     */
    void updateCameraById(Device device);

    /**
     * 修改窗帘状态
     *
     */
    void setCurtainCoverPercent(Curtain curtain);

    /**
     * 修改灯光状态
     * @param device
     */
    void updateLampById(Device device);

    /**
     * 修改烟雾警报器状态
     * @param device
     */
    void updateSmokeDetecorById(Device device);

    void updateBySurroundings(Device device);
}
