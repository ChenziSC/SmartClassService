package controller;

import domain.*;
import org.apache.ibatis.annotations.Lang;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import service.DeviceService;
import service.SurroundingsService;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LampJob implements Job{
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
        List<Lamp> lampList = deviceService.getLampList();
        int lampNum = lampList.size();
        List<Lamp> lampListOpen = new ArrayList<Lamp>();
        List<Lamp> lampListClose = new ArrayList<Lamp>();
        int id = 0;
        for (Lamp lamp:lampList){
            if(lamp.getIsOpen()==1){
                lampListOpen.add(lamp);
            }else {
                lampListClose.add(lamp);
            }
        }
        //根据当前光度开启或者关闭灯

        if(curSurroundings.getLight()>preSurroundings.getLight()) {
            //如果光度变强，随机关一个灯
            if(lampListOpen.size()>0){
                id =(int)((lampListOpen.size()-0+1)*Math.random()+0);
                lampListOpen.get(id).setIsOpen(0);
                System.out.print("设备："+lampListOpen.get(id).getId()+" 光变强 关灯"+lampListOpen.get(id).getState()+"--->");
            }
        }
        if(curSurroundings.getLight()<preSurroundings.getLight()) {
            //如果光度变弱，随机开一个灯
            if(lampListClose.size()>0){
                id =(int)((lampListClose.size()-0+1)*Math.random()+0);
                lampListClose.get(id).setIsOpen(1);
                System.out.print("设备："+lampListOpen.get(id).getId()+" 光变弱 开灯"+lampListOpen.get(id).getState()+"--->");
            }
        }
        try {
            System.out.println(lampList.get(id).getState());
            deviceService.updateDeviceIsOpenById(lampList.get(id));
        } catch (Exception e) {
            System.out.println("updateLamp-fail");
        }
    }

//    public Set<Integer> getRandomNum(int num, int size){
//        Set<Integer> set = new HashSet<Integer>();
//        while(set.size()<num){
//            set.add((int)((size-0+1)*Math.random()+0));
//        }
//        return set;
//    }
}
