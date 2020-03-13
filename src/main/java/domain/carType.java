package domain;

public class carType {
    double limitWeight;
    double cost;
    int carNum;

    public carType() {
    }

    public carType(double limitWeight, double cost, int carNum) {
        this.limitWeight = limitWeight;
        this.cost = cost;
        this.carNum = carNum;
    }

    public double getLimitWeight() {
        return limitWeight;
    }

    public void setLimitWeight(double limitWeight) {
        this.limitWeight = limitWeight;
    }

    public double getCost() {
        return cost;
    }

    public void setCost(double cost) {
        this.cost = cost;
    }

    public int getCarNum() {
        return carNum;
    }

    public void setCarNum(int carNum) {
        this.carNum = carNum;
    }
}
