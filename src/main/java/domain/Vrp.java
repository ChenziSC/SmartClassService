package domain;

import java.util.ArrayList;
import java.util.List;

public class Vrp {
    String baseSrc;

    double limitTime;
    double limitDistance;
    double balancePlan;// 能接受的最大平衡差距
    double twiceLoadingTime;

    boolean isSPFA;

    List<carType> carArr;

    public Vrp() {
    }

    public Vrp(String baseSrc, double limitTime, double limitDistance, double balancePlan,
               double twiceLoadingTime, boolean isSPFA, List<carType> carArr) {
        this.baseSrc = baseSrc;
        this.limitTime = limitTime;
        this.limitDistance = limitDistance;
        this.balancePlan = balancePlan;
        this.twiceLoadingTime = twiceLoadingTime;
        this.isSPFA = isSPFA;
        this.carArr = new ArrayList<>(carArr);
    }

    public String getBaseSrc() {
        return baseSrc;
    }

    public void setBaseSrc(String baseSrc) {
        this.baseSrc = baseSrc;
    }

    public double getLimitTime() {
        return limitTime;
    }

    public void setLimitTime(double limitTime) {
        this.limitTime = limitTime;
    }

    public double getLimitDistance() {
        return limitDistance;
    }

    public void setLimitDistance(double limitDistance) {
        this.limitDistance = limitDistance;
    }

    public double getBalancePlan() {
        return balancePlan;
    }

    public void setBalancePlan(double balancePlan) {
        this.balancePlan = balancePlan;
    }

    public double getTwiceLoadingTime() {
        return twiceLoadingTime;
    }

    public void setTwiceLoadingTime(double twiceLoadingTime) {
        this.twiceLoadingTime = twiceLoadingTime;
    }

    public boolean isSPFA() {
        return isSPFA;
    }

    public void setSPFA(boolean SPFA) {
        isSPFA = SPFA;
    }

    public List<carType> getCarArr() {
        return carArr;
    }

    public void setCarArr(List<carType> carArr) {
        this.carArr = carArr;
    }
}
