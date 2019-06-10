package controller;

import domain.*;
import org.quartz.JobKey;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.impl.matchers.GroupMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import service.DeviceService;
import service.SurroundingsService;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


abstract class DeviceTimer{
    public String name;
    public abstract void update(int autoId, String corn);
}

class WindowTimer extends DeviceTimer{
    private static WindowTimer windowTimer;
    private WindowTimer(){
        name = "windowTimer";
    }
    public static synchronized WindowTimer getInstance(){
        if(windowTimer==null){
            windowTimer = new WindowTimer();
        }
        return windowTimer;
    }
    public void update(int autoId, String corn){
        if(autoId == 1) {
            QuartzManagerDevices.quartzManager.addJob(
                    "windowAuto",
                    "device",
                    "windowAuto",
                    "device",
                    WindowJob.class,
                    corn);
        }
        if(autoId == 0) {
            QuartzManagerDevices.removeJob("windowAuto");
        }
    }
}

class CurtainTimer extends DeviceTimer{
    private CurtainTimer(){
        name = "curtainTimer";
    }
    private static CurtainTimer curtainTimer;

    public static synchronized CurtainTimer getInstance(){
        if(curtainTimer==null){
            curtainTimer = new CurtainTimer();
        }
        return curtainTimer;
    }

    public void update(int autoId, String corn){

        if(autoId == 1) {
            if(QuartzManagerDevices.quartzManager==null){
                System.out.println("quartzManager==null");
            }
            QuartzManagerDevices.quartzManager.addJob(
                    "curtainAuto",
                    "device",
                    "curtainAuto",
                    "device",
                    CurtainJob.class,
                    corn);
        }
        if(autoId == 0) {
            //remove job
            QuartzManagerDevices.removeJob("curtainAuto");
        }
    }
}


class AirConditioningTimer extends DeviceTimer{
    private AirConditioningTimer(){
        name = "airConditioningTimer";
    }

    private static AirConditioningTimer airConditioningTimer;

    public static synchronized AirConditioningTimer getInstance(){
        if(airConditioningTimer==null){
            airConditioningTimer = new AirConditioningTimer();
        }
        return airConditioningTimer;
    }

    public void update(int autoId, String corn){
        if (autoId == 1) {
            QuartzManagerDevices.quartzManager.addJob(
                    "airConditioningAuto",
                    "device",
                    "airConditioningAuto",
                    "device",
                    AirConditioningJob.class,
                    corn);
        }
        if (autoId == 0) {
            //remove job
            QuartzManagerDevices.removeJob("airConditioningAuto");
        }
    }
}

class LampTimer extends DeviceTimer{
    private static LampTimer lampTimer;
    private LampTimer(){
        name = "windowTimer";
    }
    public static synchronized LampTimer getInstance(){
        if(lampTimer==null){
            lampTimer = new LampTimer();
        }
        return lampTimer;
    }
    public void update(int autoId, String corn){
        if(autoId == 1) {
            QuartzManagerDevices.quartzManager.addJob(
                    "lampAuto",
                    "device",
                    "lampAuto",
                    "device",
                    LampJob.class,
                    corn);
        }
        if(autoId == 0) {
            QuartzManagerDevices.removeJob("lampAuto");
        }
    }
}


@Controller
@Component
@RequestMapping(value = "/QuartzManagerDevices")
public class QuartzManagerDevices {

    static QuartzManager quartzManager;

    @Autowired
    QuartzManager quartzManager1;

    boolean windowExist = false;
    boolean curtainExist = false;
    boolean airConditioningExist = false;
    boolean lampExist = false;

    @Autowired
    private DeviceService deviceService;
    @RequestMapping(value = "/setDeviceTimer", method = RequestMethod.POST)
    @ResponseBody
    public void setDeviceTimer(@RequestBody Type type) {
        try {
            List<String> jobGroupNames = quartzManager.scheduler.getJobGroupNames();
            for (String groupNames : jobGroupNames) {
                if (groupNames.equals("device")) {
                    for (JobKey jobKey : quartzManager.scheduler.getJobKeys(GroupMatcher.jobGroupEquals(groupNames))) {
                        String jobName = jobKey.getName();
                        String jobGroup = jobKey.getGroup();
                        //get job's trigger
                        if (jobName.equals("airConditioningAuto")) {
                            airConditioningExist = true;
                            break;
                        }
                        if (jobName.equals("curtainAuto")) {
                            curtainExist = true;
                            break;
                        }
                        if (jobName.equals("windowAuto")) {
                            windowExist = true;
                            break;
                        }
                        if (jobName.equals("windowAuto")) {
                            lampExist = true;
                            break;
                        }
                    }
                }
            }
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        notifyAllObservers(type);
    }

    public void notifyAllObservers(Type type){
        if (type.getTypeName().equals("window")){
            if(!windowExist) {
                String corn = "*/10 * * * * ?";
                WindowTimer windowTimer = WindowTimer.getInstance();
                windowTimer.update(type.getAutoIsOpen(), corn);
            }else {
                System.out.println("windowTimer已经存在");
            }
        }
        if (type.getTypeName().equals("curtain")){
            if(!curtainExist) {
                String corn = "*/10 * * * * ?";
                CurtainTimer curtainTimer = CurtainTimer.getInstance();
                curtainTimer.update(type.getAutoIsOpen(), corn);
            }else {
                System.out.println("curtainTimer已经存在");
            }
        }
        if (type.getTypeName().equals("airConditioning")){
            if(!airConditioningExist) {
                String corn = "*/10 * * * * ?";
                AirConditioningTimer airConditioningTimer = AirConditioningTimer.getInstance();
                airConditioningTimer.update(type.getAutoIsOpen(), corn);
            }else {
                System.out.println("airConditioningTimer已经存在");
            }
        }
        if (type.getTypeName().equals("lamp")){
            if(!lampExist) {
                String corn = "*/10 * * * * ?";
                LampTimer lampTimer = LampTimer.getInstance();
                lampTimer.update(type.getAutoIsOpen(), corn);
            }else {
                System.out.println("lampTimer已经存在");
            }
        }
    }

    @RequestMapping(value = "/removeAllJob", method = RequestMethod.POST)
    @ResponseBody
    public static void removeAllJob() {
        quartzManager.removeAllJob("device", "device");
    }

    @RequestMapping(value = "/removeJob", method = RequestMethod.POST)
    @ResponseBody
    public static void removeJob(@RequestBody String jobName) {
        quartzManager.removeJob(jobName, "device", jobName, "device");
    }
    @Autowired
    public void setQuartzManager(QuartzManager quartzManager){
        QuartzManagerDevices.quartzManager = quartzManager;
    }
}
