package compute;

public class Main {

    public static void main(String[] args) {
        long startTime = System.currentTimeMillis(); //获取开始时间
        vrpSolution vrp = new vrpSolution();

        vrp.setBaseSrc("C:\\Users\\zhichen\\Desktop\\物流文档\\bisai\\aWeek_fangshan\\");
//        vrp.setBaseSrc("C:\\Users\\zhichen\\Desktop\\物流文档\\bisai\\20190408\\");
//        vrp.setBaseSrc("C:\\Users\\zhichen\\Desktop\\物流文档\\bisai\\demo01\\");

//        vrp.setIsSPFA(true);
        vrp.setIsSPFA(false);

        vrp.setLimitOption(7,300,15,10);
//        vrp.setLimitOption(8,35,15,10);

        vrp.solveVrp();
        long endTime = System.currentTimeMillis(); //获取结束时间

        System.out.println("车辆储备是否足够：" + vrp.confirmCarNum());
        System.out.println("整个算法总计花费：" + (endTime - startTime) / 1000 + "秒");
        System.out.println(vrp.getVrpResult());
    }
}
