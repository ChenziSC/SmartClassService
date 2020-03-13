package service;

import com.alibaba.fastjson.JSONObject;
import compute.*;
//import dao.*;
import domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Service
public class VrpService {

    public JSONObject getVrpResult(Vrp vrp) {
//        System.out.println(System.getProperty("user.dir"));//user.dir指定了当前的路径
        String src = "../webapps/vrpData/" + vrp.getBaseSrc() + "/";

        long startTime = System.currentTimeMillis(); //获取开始时间

        vrpSolution vrpSolution = new vrpSolution();

        vrpSolution.setBaseSrc(src);
        vrpSolution.setIsSPFA(false);
        vrpSolution.setLimitOption(
                vrp.getLimitTime(),
                vrp.getLimitDistance(),
                vrp.getBalancePlan(),
                vrp.getTwiceLoadingTime()
        );
        vrpSolution.setCarType(vrp.getCarArr());

        vrpSolution.solveVrp();
        long endTime = System.currentTimeMillis(); //获取结束时间
        System.out.println("车辆储备是否足够：" + vrpSolution.confirmCarNum());
        System.out.println("整个算法总计花费：" + (endTime - startTime) / 1000 + "秒");
        return vrpSolution.getVrpResult();
    }


}
