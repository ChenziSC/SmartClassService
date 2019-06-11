package controller;

import domain.Curtain;
import domain.Surroundings;
import domain.Window;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;
import service.DeviceService;
import service.SurroundingsService;
import sun.awt.WindowIDProvider;

import java.util.ArrayList;
import java.util.List;

public class WindowJob implements Job {
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
        List<Window> windowList = new ArrayList<Window>();
        //update
        for (Window window : windowList) {
            int curCoverPercent = window.getCoverPercent();
            if (curSurroundings.getLight() > preSurroundings.getLight()) {
                System.out.print("设备："+window.getId()+" 光变强 拉下窗户 增加覆盖率"+curCoverPercent+"--->");
                //拉下窗户 覆盖率增加
                if ((curCoverPercent + 10) > 100) {
                    window.setCoverPercent(100);
                }
                if ((curCoverPercent + 10) <= 100) {
                    window.setCoverPercent(curCoverPercent + 10);
                }
            }
            if (curSurroundings.getLight() < preSurroundings.getLight()) {
                System.out.print("设备："+window.getId()+" 光变弱 拉上窗户 减少覆盖率 "+curCoverPercent+"--->");
                //拉上窗户 覆盖率减少
                if ((curCoverPercent - 10) < 0) {
                    window.setCoverPercent(0);
                }
                if ((curCoverPercent - 10) >= 0) {
                    window.setCoverPercent(curCoverPercent - 10);
                }
            }
            try {
                System.out.println(window.getCoverPercent());
                deviceService.setWindowCoverPercent(window);
            } catch (Exception e) {
                System.out.println("setWindowCoverPercent-fail");
            }
        }
    }
}
