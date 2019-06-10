package controller;

import domain.AirConditioning;
import domain.Surroundings;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import service.DeviceService;
import service.SurroundingsService;

import java.util.List;


public class AirConditioningJob implements Job{

    @Autowired
    private SurroundingsService surroundingsService;
    @Autowired
    private DeviceService deviceService;
    @Override
    public void execute(JobExecutionContext jobExecutionContext) throws JobExecutionException {
        JobDataMap dataMap = jobExecutionContext.getJobDetail().getJobDataMap();
        SpringBeanAutowiringSupport.processInjectionBasedOnCurrentContext(this);
        List<Surroundings> cpSurroundings = surroundingsService.getPreSurroundind();
        Surroundings curSurroundings = cpSurroundings.get(0);
        Surroundings preSurroundings = cpSurroundings.get(1);
        List<AirConditioning> airConditioningList = deviceService.getAirConditioningList();
        //update
        for(AirConditioning airConditioning:airConditioningList) {
            int temperature =airConditioning.getSettingTemperature();
            if(curSurroundings.getTemperature()>preSurroundings.getTemperature()) {
                System.out.print("设备："+airConditioning.getId()+" 温度升高 调低空调 "+temperature+"--->");
                // 增加
                airConditioning.setSettingTemperature(temperature-1);
            }
            if(curSurroundings.getTemperature()<preSurroundings.getTemperature()){
                System.out.print("设备："+airConditioning.getId()+" 温度降低 调高空调 "+temperature+"--->");
                airConditioning.setSettingTemperature(temperature+1);
            }
            try {
                System.out.println(airConditioning.getSettingTemperature());
                deviceService.setAirconditioningTem(airConditioning);
            } catch (Exception e) {
                System.out.println("setAirconditioningTem-fail");
            }
        }
    }
}
