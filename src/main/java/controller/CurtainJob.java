package controller;

import domain.*;
import org.quartz.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import service.DeviceService;
import service.SurroundingsService;

import java.util.List;

public class CurtainJob implements Job{
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
        List<Curtain> curtainList = deviceService.getCurtainList();
        //update
        for(Curtain curtain:curtainList) {
            int curCoverPercent =curtain.getCoverPercent();
            if(curSurroundings.getLight()>preSurroundings.getLight()) {
                //拉下窗帘 覆盖率增加
                System.out.print("设备："+curtain.getId()+" 光变强 拉下窗帘 增加覆盖率"+curCoverPercent+"--->");
                if((curCoverPercent+10)>100){
                    curtain.setCoverPercent(100);
                }
                if((curCoverPercent+10)<=100){
                    curtain.setCoverPercent(curCoverPercent+10);
                }
            }
            if(curSurroundings.getLight()<preSurroundings.getLight()) {
                //拉上窗帘 覆盖率减少
                System.out.print("设备："+curtain.getId()+" 光变弱 拉上窗帘 减少覆盖率"+curCoverPercent+"--->");
                if((curCoverPercent-10)<0){
                    curtain.setCoverPercent(0);
                }
                if((curCoverPercent-10)>=0){
                    curtain.setCoverPercent(curCoverPercent-10);
                }
            }
            try {
                System.out.println(curtain.getCoverPercent());
                deviceService.setCurtainCoverPercent(curtain);
            } catch (Exception e) {
                System.out.println("setCurtainCoverPercent-fail");
            }
        }
    }
}
