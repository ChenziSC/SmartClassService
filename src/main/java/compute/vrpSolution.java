package compute;

import com.alibaba.fastjson.JSONObject;
import domain.carType;
import kn.uni.voronoitreemap.datastructure.OpenList;
import kn.uni.voronoitreemap.diagram.PowerDiagram;
import kn.uni.voronoitreemap.j2d.PolygonSimple;
import kn.uni.voronoitreemap.j2d.Site;

import javax.persistence.criteria.CriteriaBuilder;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;

public class vrpSolution {
    Random ra;

    //车辆信息
    int carTypeNum;// 车型的数量
    double[] carLimitWeightArr;
    double[] carCostArr;// 单次出行成本
    int[] carNumArr;
    int[] carUsedNumArr;
    double avgLimitWeight;

    //订单信息
    int customerNum;// customerNum不包含配送中心在内
    String[] customerIds;
    double[] customerGpsX;
    double[] customerGpsY;
    int[] samePoint;// 表示坐标相同的地点，samePoint[66]=1表示第66个点与第一个点坐标相同
    double[] weightArr;
    double[] waitTimeArr;

    //距离信息
    HashMap<String, Integer> customerIndexMap = new HashMap<>();//存储客户id的下标
    double[][] time;//单位秒
    double[][] distance;//单位公里

    //限制信息
    double limitTime;// 限制工作时间
    double limitDistance;
    double balancePlan;// 能接受的最大平衡差距
    double twiceLoadingTime;

    //结果
    Matrix matrixAll = new Matrix();

    //维诺图n阶存储
    HashMap<Integer, List<Integer>> k1 = new HashMap<>();
    HashMap<Integer, List<Integer>> k2 = new HashMap<>();

    double localSearchFail = 0;

//    String baseSrc = "C:\\Users\\zhichen\\Desktop\\物流文档\\bisai\\aWeek_fangshan\\";
//    String baseSrc = "C:\\Users\\zhichen\\Desktop\\物流文档\\bisai\\20190408\\";
//    String baseSrc = "C:\\Users\\zhichen\\Desktop\\物流文档\\bisai\\demo01\\";

    String baseSrc;

    boolean isSPFA;

    long timeInitData;
    long timeInitGroup;
    long timeGetInitLines;
    long timeTwiceLine;
    long timeBalance;

    public void setBaseSrc(String baseSrc) {
        this.baseSrc = baseSrc;
    }

    public void setIsSPFA(boolean isSPFA) {
        this.isSPFA = isSPFA;
    }

    public void setLimitOption(double limitTime, double limitDistance, double balancePlan, double twiceLoadingTime) {
        this.limitTime = limitTime * 3600.0;
        this.limitDistance = limitDistance;
        this.balancePlan = balancePlan * 60.0;
        this.twiceLoadingTime = twiceLoadingTime * 60.0;
    }


    //初始化函数
    void initData() {
        ra = new Random();

//        initCarTypeNum();
        initCustomerNum();

//        searchCarType();
        searchOrder();

        searchCenter();

        if (isSPFA) {
            searchTimeSPFA();
            searchDistanceSPFA();
        } else {
            searchTime();
            searchDistance();
        }

//        searchLimitOption();

//        limitTime = 7.0 * 3600.0;//7小时
////        limitDistance = 300;//3000数量级，大地图
//        limitDistance = 150;//1000数量级，小地图
//        balancePlan = 60 * 15;//15分钟
//        twiceLoadingTime = 60 * 10;//10分钟，二次配送的装载时间
    }

    //载入车型数量
    void initCarTypeNum() {
        String pathname = baseSrc + "carType.txt";
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            br.readLine();
            int num = 0;
            while (br.readLine() != null) {
                // 一次读入一行数据
                num++;
            }
            carTypeNum = num;
            System.out.println("车型数为：" + carTypeNum);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //载入客户数量
    void initCustomerNum() {
        String pathname = baseSrc + "order.txt";
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            br.readLine();
            int num = 0;
            while (br.readLine() != null) {
                // 一次读入一行数据
                num++;
            }
            customerNum = num;
            System.out.println("不包含配送中心在内的客户数为：" + customerNum);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //载入车型数据
    void searchCarType() {
        String pathname = baseSrc + "carType.txt";
        int index = 0;
        carLimitWeightArr = new double[carTypeNum];
        carCostArr = new double[carTypeNum];// 单次出行成本
        carNumArr = new int[carTypeNum];
        carUsedNumArr = new int[carTypeNum];
        avgLimitWeight = 0;
        int allCarNum = 0;

        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                String[] data = line.split(",");
                carLimitWeightArr[index] = Double.parseDouble(data[1]);
                carCostArr[index] = Double.parseDouble(data[2]);
                carNumArr[index] = Integer.parseInt(data[3]);

                avgLimitWeight += Double.parseDouble(data[1]) * Integer.parseInt(data[3]);
                allCarNum += Integer.parseInt(data[3]);
                carUsedNumArr[index] = 0;
                index++;
            }
            avgLimitWeight = avgLimitWeight / allCarNum;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //载入车型数据
    public void setCarType(List<carType> carArr) {
        carTypeNum = carArr.size();

        carLimitWeightArr = new double[carTypeNum];
        carCostArr = new double[carTypeNum];// 单次出行成本
        carNumArr = new int[carTypeNum];
        carUsedNumArr = new int[carTypeNum];
        avgLimitWeight = 0;
        int allCarNum = 0;

        for (int i = 0; i < carTypeNum; i++) {
            carLimitWeightArr[i] = carArr.get(i).getLimitWeight();
            carCostArr[i] = carArr.get(i).getCost();
            carNumArr[i] = carArr.get(i).getCarNum();

            avgLimitWeight += carArr.get(i).getCarNum() * carArr.get(i).getLimitWeight();
            allCarNum += carArr.get(i).getCarNum();
            carUsedNumArr[i] = 0;
        }
        avgLimitWeight = avgLimitWeight / allCarNum;

//        System.out.println(Arrays.toString(carLimitWeightArr));
//        System.out.println(Arrays.toString(carCostArr));
//        System.out.println(Arrays.toString(carNumArr));
    }

    //载入订单数据
    void searchOrder() {
        String pathname = baseSrc + "order.txt";
        int index = 1;
        customerIds = new String[customerNum + 1];
        customerGpsX = new double[customerNum + 1];
        customerGpsY = new double[customerNum + 1];
        samePoint = new int[customerNum + 1];
        weightArr = new double[customerNum + 1];
        waitTimeArr = new double[customerNum + 1];

        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            br.readLine();

            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                String[] data = line.split(",");
                customerIds[index] = data[1];
                customerGpsX[index] = Double.parseDouble(data[4]);
                customerGpsY[index] = Double.parseDouble(data[5]);
                weightArr[index] = Double.parseDouble(data[2]);
                waitTimeArr[index] = Double.parseDouble(data[3]) * 60;
                customerIndexMap.put(data[1], index);
                index++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //载入配送中心数据
    void searchCenter() {
        String pathname = baseSrc + "center.txt";
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            br.readLine();
            String line = br.readLine();
            String[] data = line.split(",");

            customerIds[0] = data[0];
            customerGpsX[0] = Double.parseDouble(data[1]);
            customerGpsY[0] = Double.parseDouble(data[2]);
            weightArr[0] = 0;
            customerIndexMap.put(data[0], 0);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //载入时间距离数据
    void searchTime() {
        String pathname = baseSrc + "timeCu2Cu.txt";
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            br.readLine();

            time = new double[customerNum + 1][customerNum + 1];
            for (int i = 0; i < customerNum + 1; i++) {
                for (int j = 0; j < customerNum + 1; j++) {
                    if (i == j) {
                        time[i][j] = 0;
                    } else {
                        time[i][j] = 100 * 3600;//设为较大的数
                    }
                }
            }

            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                String[] data = line.split(",");
                int indexPre = customerIndexMap.get(data[0]);
                int indexAfter = customerIndexMap.get(data[1]);
                time[indexPre][indexAfter] = Double.parseDouble(data[2]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //载入时间距离数据
    void searchTimeSPFA() {
        String pathname = baseSrc + "timeCu2Cu.txt";
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String str;
            br.readLine();

            List<Line> line = new ArrayList<>();

            while ((str = br.readLine()) != null) {
                // 一次读入一行数据
                String[] data = str.split(",");
                line.add(new Line(data[0], data[1], Double.parseDouble(data[2])));
            }

            SPFA spfa = new SPFA();

            time = spfa.getResult(customerIndexMap, customerIds, line);

//            Arrays.stream(time).forEach(x -> System.out.println(Arrays.toString(x)));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //载入物理距离数据
    void searchDistance() {
        String pathname = baseSrc + "distanceCu2Cu.txt";
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String line;
            br.readLine();

            distance = new double[customerNum + 1][customerNum + 1];
            for (int i = 0; i < customerNum + 1; i++) {
                for (int j = 0; j < customerNum + 1; j++) {
                    if (i == j) {
                        distance[i][j] = 0;
                    } else {
                        distance[i][j] = 100 * 3600;//设为较大的数
                    }
                }
            }

            while ((line = br.readLine()) != null) {
                // 一次读入一行数据
                String[] data = line.split(",");
                int indexPre = customerIndexMap.get(data[0]);
                int indexAfter = customerIndexMap.get(data[1]);
                distance[indexPre][indexAfter] = Double.parseDouble(data[2]);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //载入物理距离数据
    void searchDistanceSPFA() {
        String pathname = baseSrc + "distanceCu2Cu.txt";
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            String str;
            br.readLine();

            List<Line> line = new ArrayList<>();

            while ((str = br.readLine()) != null) {
                // 一次读入一行数据
                String[] data = str.split(",");
                line.add(new Line(data[0], data[1], Double.parseDouble(data[2])));
            }

            SPFA spfa = new SPFA();

            distance = spfa.getResult(customerIndexMap, customerIds, line);

//            Arrays.stream(distance).forEach(x -> System.out.println(Arrays.toString(x)));

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //载入限制条件
    void searchLimitOption() {
        String pathname = baseSrc + "limitOption.txt";
        try (FileReader reader = new FileReader(pathname);
             BufferedReader br = new BufferedReader(reader) // 建立一个对象，它把文件内容转成计算机能读懂的语言
        ) {
            br.readLine();
            String line = br.readLine();
            String[] data = line.split(",");

            limitTime = Double.parseDouble(data[0]) * 3600.0;
            limitDistance = Double.parseDouble(data[1]);
            balancePlan = 60.0 * Double.parseDouble(data[2]);
            twiceLoadingTime = 60.0 * Double.parseDouble(data[3]);//二次配送的装载时间
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void initGroup() {
        List<PointToPoint> lineK1 = new ArrayList<>();
        getVoronoi();

        //把k1的路径都推入lineK1
        for (int i = 1; i <= customerNum; i++) {
            List<Integer> currentK1 = new ArrayList<>(k1.get(i));
            for (int j = 0; j < currentK1.size(); j++) {
                lineK1.add(new PointToPoint(i, currentK1.get(j), time[i][currentK1.get(j)], distance[i][currentK1.get(j)]));
            }
        }

        //lineK1根据距离排序
        Collections.sort(lineK1, new Comparator<PointToPoint>() {
            @Override
            public int compare(PointToPoint p1, PointToPoint p2) {
                if (p1.time > p2.time) {
                    return 1;
                } else if (p1.time < p2.time) {
                    return -1;
                } else {
                    return 0;
                }
            }
        });

        //初始化
        HashMap<Integer, HashSet<Integer>> initMap = new HashMap<>(getInitMatrix(lineK1));
        int classNum = initMap.size();
        System.out.println("根据重量能分成 " + classNum + " 条路线");
        for (int i = 0; i < classNum; i++) {
            HashSet<Integer> h = initMap.get(i);
            System.out.print("i:" + i + " ");
            System.out.println(h);
        }

        HashMap<Integer, List<Integer>> res = new HashMap<>();
        HashMap<Integer, List<Integer>> resTwice = new HashMap<>();
        for (int j = 0; j < classNum; j++) {
            List<Integer> currentClass = new ArrayList<>(initMap.get(j));

            res.put(j, currentClass);
            resTwice.put(j, new ArrayList<Integer>());
        }

        matrixAll.matrix = new HashMap<>(res);
        matrixAll.matrixTwice = new HashMap<>(resTwice);
    }

    void getVoronoi() {
        PowerDiagram diagram = new PowerDiagram();

        OpenList sites = new OpenList();

        double x_min = customerGpsX[0];
        double x_max = customerGpsX[0];

        double y_min = customerGpsY[0];
        double y_max = customerGpsY[0];

        for (int i = 1; i < customerNum + 1; i++) {
            if (customerGpsX[i] < x_min) {
                x_min = customerGpsX[i];
            }
            if (customerGpsX[i] > x_max) {
                x_max = customerGpsX[i];
            }
            if (customerGpsY[i] < y_min) {
                y_min = customerGpsY[i];
            }
            if (customerGpsY[i] > y_max) {
                y_max = customerGpsY[i];
            }
        }

        double mult = 10000;

        x_min *= mult;
        x_max *= mult;
        y_min *= mult;
        y_max *= mult;

        PolygonSimple rootPolygon = new PolygonSimple();
        rootPolygon.add(x_min, y_min);
        rootPolygon.add(x_min, y_max);
        rootPolygon.add(x_max, y_max);
        rootPolygon.add(x_max, y_min);

        //查找相同坐标的点

        for (int i = 0; i <= customerNum; i++) {
            samePoint[i] = i;
        }

        for (int i = 1; i < customerNum; i++) {
            for (int j = i + 1; j < customerNum + 1; j++) {
                if (customerGpsX[i] == customerGpsX[j] && customerGpsY[i] == customerGpsY[j] && samePoint[j] == j) {
                    samePoint[j] = i;
                }
            }
        }

        for (int i = 0; i <= customerNum; i++) {
            if (samePoint[i] == i) {
                Site site = new Site(customerGpsX[i] * mult, customerGpsY[i] * mult);
                sites.add(site);
            }
        }

        diagram.setSites(sites);
        diagram.setClipPoly(rootPolygon);

        diagram.computeDiagram();

        //计算k1
        //次序回正
        ArrayList<Site> orderSites = new ArrayList<>();
        for (int i = 0; i <= customerNum; i++) {
            for (int j = 0; j < sites.size; j++) {
                if (customerGpsX[i] * mult == sites.get(j).getX() && customerGpsY[i] * mult == sites.get(j).getY()) {
                    Site s = sites.get(j);
                    orderSites.add(s);
                    break;
                }
            }
        }

        for (int i = 1; i <= customerNum; i++) {
            List<Integer> currentK1 = new ArrayList<>();

            List<Site> neighbours = new ArrayList<>(orderSites.get(i).getNeighbours());

            for (Site neighbour : neighbours) {
                double neiX = neighbour.getX();
                double neiY = neighbour.getY();

                for (int q = 1; q <= customerNum; q++) {
                    if (orderSites.get(q).getX() == neiX && orderSites.get(q).getY() == neiY) {
                        currentK1.add(q);
                    }
                }
            }

            //重复点添加k1
            for (int j = 1; j <= customerNum; j++) {
                if (i != j && samePoint[i] == samePoint[j]) {
                    currentK1.add(j);
                }
            }
            k1.put(i, currentK1);
//            System.out.println("k1:" + i + "   |   " + currentK1.size());
        }

        //计算k2
        for (int i = 1; i <= customerNum; i++) {
            List<Integer> currentK1 = new ArrayList<>(k1.get(i));
            List<Integer> currentK2 = new ArrayList<>(currentK1);

            for (int j = 0; j < currentK1.size(); j++) {
                int no = currentK1.get(j);
                List<Integer> currentNO = new ArrayList<>(k1.get(no));
                currentNO.remove(new Integer(i));
                currentK2.addAll(currentNO);
            }
            currentK2 = removeDuplicate(currentK2);
            k2.put(i, currentK2);
//            System.out.println("k2:" + i + "   |   " + currentK2.size());
        }
    }

    List<Integer> removeDuplicate(List<Integer> list) {
        HashSet<Integer> hash = new HashSet<>(list);
        List<Integer> res = new ArrayList<>(hash);
        return res;
    }

    HashMap<Integer, HashSet<Integer>> getInitMatrix(List<PointToPoint> lineK1) {
        int[] classIndex = new int[customerNum + 1];
        for (int i = 1; i <= customerNum; i++) {
            classIndex[i] = i;
        }

        for (int i = 0; i < lineK1.size(); i++) {
            PointToPoint p = lineK1.get(i);
            int index1 = classIndex[p.fromNo];
            int index2 = classIndex[p.toNo];

            List<Integer> temp = new ArrayList<>();
            for (int q = 1; q <= customerNum; q++) {
                if (classIndex[q] == index1 || classIndex[q] == index2) {
                    temp.add(q);
                }
            }

            if (index1 != index2 && !isOverWeight(classIndex, index1, index2)) {
                mergeCustomers(classIndex, index1, index2);
            }
        }

        HashMap<Integer, HashSet<Integer>> result = new HashMap<>();
        HashSet<Integer> allClass = new HashSet<>();
        for (int i = 1; i <= customerNum; i++) {
            allClass.add(classIndex[i]);
        }

        int classNo = 0;
        for (Integer index : allClass) {
            HashSet<Integer> currentClass = new HashSet<>();
            for (int i = 1; i <= customerNum; i++) {
                if (classIndex[i] == index) {
                    currentClass.add(i);
                }
            }

            result.put(classNo, currentClass);
            classNo++;
        }
        return result;
    }

    boolean isOverWeight(int[] classIndex, int index1, int index2) {
        double weight = 0;
        for (int i = 1; i <= customerNum; i++) {
            if (classIndex[i] == index1 || classIndex[i] == index2) {
                weight += weightArr[i];
            }
        }
        return weight > avgLimitWeight;
    }

    void mergeCustomers(int[] classIndex, int index1, int index2) {
        for (int i = 1; i <= customerNum; i++) {
            if (classIndex[i] == index2) {
                classIndex[i] = index1;
            }
        }
    }

    List<Integer> savingAlgorithm(HashSet<Integer> currentClass) {
        List<Integer> customerList = new ArrayList<>(currentClass);
        int currentNum = currentClass.size();

        //heng为尾部寻找，zong为头部寻找
        int[][] savingRankNO_heng = new int[currentNum][currentNum];
        int[][] savingRankNO_zong = new int[currentNum][currentNum];
        double[][] savingRankVal_heng = new double[currentNum][currentNum];
        double[][] savingRankVal_zong = new double[currentNum][currentNum];

        for (int i = 0; i < currentNum; i++) {
            for (int j = 0; j < currentNum; j++) {
                int ii = customerList.get(i);
                int jj = customerList.get(j);

                savingRankNO_heng[i][j] = j;
                savingRankVal_heng[i][j] = time[ii][0] + time[0][jj] - time[ii][jj];

                savingRankNO_zong[i][j] = j;
                savingRankVal_zong[i][j] = time[jj][0] + time[0][ii] - time[jj][ii];
            }
        }

        for (int i = 0; i < currentNum; i++) {

            for (int ii = 0; ii < currentNum - 1; ii++) {
                for (int jj = ii + 1; jj < currentNum; jj++) {
                    if (savingRankVal_heng[i][ii] < savingRankVal_heng[i][jj]) {
                        int tempNO = savingRankNO_heng[i][ii];
                        savingRankNO_heng[i][ii] = savingRankNO_heng[i][jj];
                        savingRankNO_heng[i][jj] = tempNO;

                        double tempValue = savingRankVal_heng[i][ii];
                        savingRankVal_heng[i][ii] = savingRankVal_heng[i][jj];
                        savingRankVal_heng[i][jj] = tempValue;
                    }
                }
            }

            for (int ii = 0; ii < currentNum - 1; ii++) {
                for (int jj = ii + 1; jj < currentNum; jj++) {
                    if (savingRankVal_zong[i][ii] < savingRankVal_zong[i][jj]) {
                        int tempNO = savingRankNO_zong[i][ii];
                        savingRankNO_zong[i][ii] = savingRankNO_zong[i][jj];
                        savingRankNO_zong[i][jj] = tempNO;

                        double tempValue = savingRankVal_zong[i][ii];
                        savingRankVal_zong[i][ii] = savingRankVal_zong[i][jj];
                        savingRankVal_zong[i][jj] = tempValue;
                    }
                }
            }
        }

        double minTime = 10000 * 3600;
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < currentNum; i++) {
            Set<Integer> available = new HashSet<>(currentClass);

            int currentHead;
            int currentFoot;

//            int random = ra.nextInt(currentNum);
            currentHead = i;
            currentFoot = i;
            available.remove(customerList.get(i));
            List<Integer> currentCar = new ArrayList<>();
            currentCar.add(customerList.get(i));

            while (available.size() > 0) {
                int headBest = 0, footBest = 0;
                double headValue = 0, footValue = 0;
                for (int q = 0; q < currentNum; q++) {
                    if (available.contains(customerList.get(savingRankNO_heng[currentHead][q]))) {
                        footBest = savingRankNO_heng[currentHead][q];
                        footValue = savingRankVal_heng[currentHead][q];
                        break;
                    }
                }
                for (int q = 0; q < currentNum; q++) {
                    if (available.contains(customerList.get(savingRankNO_zong[currentFoot][q]))) {
                        headBest = savingRankNO_zong[currentFoot][q];
                        headValue = savingRankVal_zong[currentFoot][q];
                        break;
                    }
                }

                if (headValue >= footValue) {
                    currentCar.add(0, customerList.get(headBest));
                    available.remove(customerList.get(headBest));
                    currentHead = headBest;
                } else {
                    currentCar.add(customerList.get(footBest));
                    available.remove(customerList.get(footBest));
                    currentFoot = footBest;
                }
            }
            double time = getTimeSingleLine(currentCar);
            if (time < minTime) {
                result = new ArrayList<>(currentCar);
                minTime = time;
//                System.out.println(currentNum + "   " + time / 3600);
            }
        }
        return result;
    }

    List<Integer> savingAlgorithm(List<Integer> currentClass) {
        List<Integer> customerList = new ArrayList<>(currentClass);
        int currentNum = currentClass.size();

        //heng为尾部寻找，zong为头部寻找
        int[][] savingRankNO_heng = new int[currentNum][currentNum];
        int[][] savingRankNO_zong = new int[currentNum][currentNum];
        double[][] savingRankVal_heng = new double[currentNum][currentNum];
        double[][] savingRankVal_zong = new double[currentNum][currentNum];

        for (int i = 0; i < currentNum; i++) {
            for (int j = 0; j < currentNum; j++) {
                int ii = customerList.get(i);
                int jj = customerList.get(j);

                savingRankNO_heng[i][j] = j;
                savingRankVal_heng[i][j] = time[ii][0] + time[0][jj] - time[ii][jj];

                savingRankNO_zong[i][j] = j;
                savingRankVal_zong[i][j] = time[jj][0] + time[0][ii] - time[jj][ii];
            }
        }

        for (int i = 0; i < currentNum; i++) {

            for (int ii = 0; ii < currentNum - 1; ii++) {
                for (int jj = ii + 1; jj < currentNum; jj++) {
                    if (savingRankVal_heng[i][ii] < savingRankVal_heng[i][jj]) {
                        int tempNO = savingRankNO_heng[i][ii];
                        savingRankNO_heng[i][ii] = savingRankNO_heng[i][jj];
                        savingRankNO_heng[i][jj] = tempNO;

                        double tempValue = savingRankVal_heng[i][ii];
                        savingRankVal_heng[i][ii] = savingRankVal_heng[i][jj];
                        savingRankVal_heng[i][jj] = tempValue;
                    }
                }
            }

            for (int ii = 0; ii < currentNum - 1; ii++) {
                for (int jj = ii + 1; jj < currentNum; jj++) {
                    if (savingRankVal_zong[i][ii] < savingRankVal_zong[i][jj]) {
                        int tempNO = savingRankNO_zong[i][ii];
                        savingRankNO_zong[i][ii] = savingRankNO_zong[i][jj];
                        savingRankNO_zong[i][jj] = tempNO;

                        double tempValue = savingRankVal_zong[i][ii];
                        savingRankVal_zong[i][ii] = savingRankVal_zong[i][jj];
                        savingRankVal_zong[i][jj] = tempValue;
                    }
                }
            }
        }

        double minTime = 10000 * 3600;
        List<Integer> result = new ArrayList<>();

        for (int i = 0; i < currentNum; i++) {
            Set<Integer> available = new HashSet<>(currentClass);

            int currentHead;
            int currentFoot;

//            int random = ra.nextInt(currentNum);
            currentHead = i;
            currentFoot = i;
            available.remove(customerList.get(i));
            List<Integer> currentCar = new ArrayList<>();
            currentCar.add(customerList.get(i));

            while (available.size() > 0) {
                int headBest = 0, footBest = 0;
                double headValue = 0, footValue = 0;
                for (int q = 0; q < currentNum; q++) {
                    if (available.contains(customerList.get(savingRankNO_heng[currentHead][q]))) {
                        footBest = savingRankNO_heng[currentHead][q];
                        footValue = savingRankVal_heng[currentHead][q];
                        break;
                    }
                }
                for (int q = 0; q < currentNum; q++) {
                    if (available.contains(customerList.get(savingRankNO_zong[currentFoot][q]))) {
                        headBest = savingRankNO_zong[currentFoot][q];
                        headValue = savingRankVal_zong[currentFoot][q];
                        break;
                    }
                }

                if (headValue >= footValue) {
                    currentCar.add(0, customerList.get(headBest));
                    available.remove(customerList.get(headBest));
                    currentHead = headBest;
                } else {
                    currentCar.add(customerList.get(footBest));
                    available.remove(customerList.get(footBest));
                    currentFoot = footBest;
                }
            }
            double time = getTimeSingleLine(currentCar);
            if (time < minTime) {
                result = new ArrayList<>(currentCar);
                minTime = time;
            }
        }
//        System.out.println(result.toString());
        return result;
    }

    List<Integer> tsp(HashSet<Integer> currentClass) {
        List<Integer> customerList = new ArrayList<>(currentClass);
        int currentNum = currentClass.size();

        int[] referenceDisNO = new int[currentNum];
        double[] referenceDisVal = new double[currentNum];

        int reference = ra.nextInt(currentNum + 1);

        for (int i = 0; i < currentNum; i++) {
            int ii = customerList.get(i);

            referenceDisNO[i] = ii;
            referenceDisVal[i] = time[reference][ii] + time[ii][reference];
        }

        for (int i = 0; i < currentNum - 1; i++) {
            for (int j = i + 1; j < currentNum; j++) {
                if (referenceDisVal[i] > referenceDisVal[j]) {
                    int tempNO = referenceDisNO[i];
                    referenceDisNO[i] = referenceDisNO[j];
                    referenceDisNO[j] = tempNO;

                    double tempValue = referenceDisVal[i];
                    referenceDisVal[i] = referenceDisVal[j];
                    referenceDisVal[j] = tempValue;
                }
            }
        }

        List<Integer> result = new ArrayList<>();
        result.add(referenceDisNO[0]);
        int k = 1;
        while (k < currentNum) {
            int thisCustomer = referenceDisNO[k];

            int minIndex = 0;
            double minTime = 10000 * 3600;

            for (int i = 0; i <= result.size(); i++) {
                List<Integer> temp = new ArrayList<>(result);
                temp.add(i, thisCustomer);
                double time = getTimeSingleLine(temp);
                if (time < minTime) {
                    minIndex = i;
                    minTime = time;
                }
            }

            result.add(minIndex, thisCustomer);
            k++;
        }


        return result;
    }

    List<Integer> tsp(List<Integer> currentClass) {
        List<Integer> customerList = new ArrayList<>(currentClass);
        int currentNum = currentClass.size();

        int[] referenceDisNO = new int[currentNum];
        double[] referenceDisVal = new double[currentNum];

        int reference = ra.nextInt(currentNum + 1);
//        int reference = 0;

        for (int i = 0; i < currentNum; i++) {
            int ii = customerList.get(i);

            referenceDisNO[i] = ii;
            referenceDisVal[i] = time[reference][ii] + time[ii][reference];
        }

        for (int i = 0; i < currentNum - 1; i++) {
            for (int j = i + 1; j < currentNum; j++) {
                if (referenceDisVal[i] > referenceDisVal[j]) {
                    int tempNO = referenceDisNO[i];
                    referenceDisNO[i] = referenceDisNO[j];
                    referenceDisNO[j] = tempNO;

                    double tempValue = referenceDisVal[i];
                    referenceDisVal[i] = referenceDisVal[j];
                    referenceDisVal[j] = tempValue;
                }
            }
        }

        List<Integer> result = new ArrayList<>();
        result.add(referenceDisNO[0]);
        int k = 1;
        while (k < currentNum) {
            int thisCustomer = referenceDisNO[k];

            int minIndex = 0;
            double minTime = 10000 * 3600;

            for (int i = 0; i <= result.size(); i++) {
                List<Integer> temp = new ArrayList<>(result);
                temp.add(i, thisCustomer);
                double time = getTimeSingleLine(temp);
                if (time < minTime) {
                    minIndex = i;
                    minTime = time;
                }
            }

            result.add(minIndex, thisCustomer);
            k++;
        }

        return result;
    }

    List<Integer> insertSearch(List<Integer> currentLine) {
//        insertSearchNum++;

        int num = currentLine.size();
        int insertIndex = ra.nextInt(num);
        int insertNO = currentLine.get(insertIndex);

//        int insertedIndex = ra.nextInt(num);
//        while (insertedIndex == insertIndex) {
//            insertedIndex = ra.nextInt(num);
//        }
//        int insertedNO = currentLine.get(insertedIndex);

        List<Integer> existK2 = new ArrayList<>(k2.get(insertNO));
        existK2.retainAll(currentLine);
        int ran = ra.nextInt(existK2.size());
        int insertedNO = existK2.get(ran);

        List<Integer> newLine = new ArrayList<>(currentLine);
        newLine.remove(insertIndex);
        newLine.add(newLine.indexOf(insertedNO), insertNO);

        if (getTimeSingleLine(newLine) < getTimeSingleLine(currentLine)) {
            currentLine = new ArrayList<>(newLine);
//            insertSearchSuccess++;
            localSearchFail = 0;
        }
        return currentLine;
    }

    List<Integer> swapSearch(List<Integer> currentLine) {
//        swapSearchNum++;

        int num = currentLine.size();
        int swapIndex = ra.nextInt(num);
        int swapNo = currentLine.get(swapIndex);

        int swappedIndex = ra.nextInt(num);
        while (swappedIndex == swapIndex) {
            swappedIndex = ra.nextInt(num);
        }
        int swappedNO = currentLine.get(swappedIndex);

        List<Integer> newLine = new ArrayList<>(currentLine);
        newLine.set(swapIndex, swappedNO);
        newLine.set(swappedIndex, swapNo);

        if (getTimeSingleLine(newLine) < getTimeSingleLine(currentLine)) {
            currentLine = new ArrayList<>(newLine);
//            swapSearchSuccess++;
            localSearchFail = 0;
        }
        return currentLine;
    }

    List<Integer> opt_2Search(List<Integer> currentLine) {
//        opt_2SearchNum++;

        int num = currentLine.size();
        int ran1 = ra.nextInt(num);
        int ran2 = ra.nextInt(num);
        while (ran1 == ran2) {
            ran2 = ra.nextInt(num);
        }
        if (ran1 > ran2) {// 确保ran1<ran2
            int temp = ran1;
            ran1 = ran2;
            ran2 = temp;
        }

        List<Integer> newLine = new ArrayList<>(currentLine);

        for (int i = ran1; i <= (ran1 + ran2) / 2; i++) {
            int temp = newLine.get(i);
            newLine.set(i, newLine.get(ran1 + ran2 - i));
            newLine.set(ran1 + ran2 - i, temp);
        }

        if (getTimeSingleLine(newLine) < getTimeSingleLine(currentLine)) {
            currentLine = new ArrayList<>(newLine);
//            opt_2SearchSuccess++;
            localSearchFail = 0;
        }
        return currentLine;
    }

    List<Integer> opt_3Search(List<Integer> currentLine) {
//        opt_3SearchNum++;

        int num = currentLine.size();

        if (num < 3) return currentLine;

        int separate1 = ra.nextInt(num + 1 - 2);//num+1个分割线，后两个不能用
        int separate2 = ra.nextInt((num + 1 - 1) - (separate1 + 1)) + (separate1 + 1);
        int separate3 = ra.nextInt((num + 1) - (separate2 + 1)) + (separate2 + 1);

        double minTime = 10000 * 3600;

        List<Integer>[] listOrder1 = new ArrayList[2];
        List<Integer>[] listOrder2 = new ArrayList[2];

        List<Integer> listHead = new ArrayList<>(currentLine.subList(0, separate1));

        listOrder1[0] = new ArrayList<>(currentLine.subList(separate1, separate2));
        listOrder2[0] = new ArrayList<>(currentLine.subList(separate2, separate3));

        listOrder1[1] = new ArrayList<>(currentLine.subList(separate2, separate3));
        listOrder2[1] = new ArrayList<>(currentLine.subList(separate1, separate2));

        List<Integer> listFoot = new ArrayList<>(currentLine.subList(separate3, num));


        List<Integer> newLine = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            List<Integer> firstRound = new ArrayList<>(listHead);
            if (i == 0) {
                firstRound.addAll(listOrder1[0]);
            } else {
                List<Integer> temp = new ArrayList<>(listOrder1[0]);
                Collections.reverse(temp);
                firstRound.addAll(temp);
            }
            for (int j = 0; j < 2; j++) {
                List<Integer> secondRound = new ArrayList<>(firstRound);
                if (j == 0) {
                    secondRound.addAll(listOrder1[1]);
                } else {
                    List<Integer> temp = new ArrayList<>(listOrder1[1]);
                    Collections.reverse(temp);
                    secondRound.addAll(temp);
                }
                secondRound.addAll(listFoot);
                double time = getTimeSingleLine(secondRound);
                if (time < minTime) {
                    minTime = time;
                    newLine = new ArrayList<>(secondRound);
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            List<Integer> firstRound = new ArrayList<>(listHead);
            if (i == 0) {
                firstRound.addAll(listOrder2[0]);
            } else {
                List<Integer> temp = new ArrayList<>(listOrder2[0]);
                Collections.reverse(temp);
                firstRound.addAll(temp);
            }
            for (int j = 0; j < 2; j++) {
                List<Integer> secondRound = new ArrayList<>(firstRound);
                if (j == 0) {
                    secondRound.addAll(listOrder2[1]);
                } else {
                    List<Integer> temp = new ArrayList<>(listOrder2[1]);
                    Collections.reverse(temp);
                    secondRound.addAll(temp);
                }
                secondRound.addAll(listFoot);
                double time = getTimeSingleLine(secondRound);
                if (time < minTime) {
                    minTime = time;
                    newLine = new ArrayList<>(secondRound);
                }
            }
        }

        if (minTime < getTimeSingleLine(currentLine) && !newLine.isEmpty()) {
            currentLine = new ArrayList<>(newLine);
//            opt_3SearchSuccess++;
            localSearchFail = 0;
        }

        return currentLine;
    }

    List<Integer> opt_orSearch(List<Integer> currentLine) {
//        opt_orSearchNum++;

        int num = currentLine.size();

        if (num < 2) return currentLine;

        int orNum = ra.nextInt(num / 5 + 1) + 1;
        int ran1 = ra.nextInt(num - orNum + 1);
        List<Integer> separate = new ArrayList<>(currentLine.subList(ran1, orNum + ran1));

        List<Integer> newLine1 = new ArrayList<>(currentLine);
        newLine1.removeAll(separate);
        List<Integer> newLine2 = new ArrayList<>(newLine1);

        List<Integer> existK2 = new ArrayList<>(k2.get(separate.get(0)));
        existK2.retainAll(newLine1);
        if (existK2.isEmpty()) {
            return currentLine;
        }
        int number = existK2.get(ra.nextInt(existK2.size()));
        int ran2 = newLine1.indexOf(number);

//        int ran2 = ra.nextInt(newLine.size() + 1);
        newLine1.addAll(ran2, separate);
        Collections.reverse(separate);
        newLine2.addAll(ran2, separate);

        List<Integer> newLine = new ArrayList<>();

        if (getTimeSingleLine(newLine1) < getTimeSingleLine(newLine2)) {
            newLine = new ArrayList<>(newLine1);
        } else {
            newLine = new ArrayList<>(newLine2);
        }

        if (getTimeSingleLine(newLine) < getTimeSingleLine(currentLine)) {
            currentLine = new ArrayList<>(newLine);
//            opt_orSearchSuccess++;
            localSearchFail = 0;
        }

        return currentLine;
    }

    List<Integer> localSearch(List<Integer> line) {
        List<Integer> currentLine = new ArrayList<>(line);
        int size = currentLine.size();
        localSearchFail = 0;

        double limit = 0;
        if (size < 40) {
            limit = Math.pow(10, 1 + size / 10) * (size % 10 + 1) * 1.2;
        } else {
            limit = 250000;
        }

        while (localSearchFail < limit) {
            localSearchFail++;
            switch (ra.nextInt(2)) {
//                case 0: {
//                    currentLine = insertSearch(currentLine);
//                    break;
//                }
//                case 0: {
//                    currentLine = swapSearch(currentLine);
//                    break;
//                }
//                case 1: {
//                    currentLine = opt_2Search(currentLine);
//                    break;
//                }
                case 0: {
                    currentLine = opt_3Search(currentLine);
                    break;
                }
                case 1: {
                    currentLine = opt_orSearch(currentLine);
                    break;
                }
            }
        }

        return currentLine;
    }

    double getTimeSingleLine(List<Integer> line) {
        if (line.isEmpty()) return 0;

        double res = 0;
        res += (time[0][line.get(0)] + waitTimeArr[line.get(0)]);
        for (int i = 1; i < line.size(); i++) {
            res += (time[line.get(i - 1)][line.get(i)] + waitTimeArr[line.get(i)]);
        }
        res += time[line.get(line.size() - 1)][0];
        return res;
    }

    double getDistanceSingleLine(List<Integer> line) {
        if (line.isEmpty()) return 0;

        double res = 0;
        res += distance[0][line.get(0)];
        for (int i = 1; i < line.size(); i++) {
            res += distance[line.get(i - 1)][line.get(i)];
        }
        res += distance[line.get(line.size() - 1)][0];
        return res;
    }

    double getWeightSingleLine(List<Integer> line) {
        double res = 0;
        for (int i = 0; i < line.size(); i++) {
            res += weightArr[line.get(i)];
        }
        return res;
    }

    int getLongestClassIndex(HashSet<Integer> avaliableLines) {
        double maxDisCenter = 0;
        int longestClassIndex = -1;
        for (Integer i : avaliableLines) {
            if (matrixAll.centerDisMap.get(i) > maxDisCenter) {
                longestClassIndex = i;
                maxDisCenter = matrixAll.centerDisMap.get(i);
            }
        }
        return longestClassIndex;
    }

    void getInitLinesSlowly() {
        //还没定的客户，size为0算法停止
        HashSet avaliableCustomers = new HashSet();
        for (int i = 1; i <= customerNum; i++) {
            avaliableCustomers.add(i);
        }

        //没定的线路，当有新增线路时应添加进去
        HashSet<Integer> avaliableLines = new HashSet<>(matrixAll.existLineNO);
//        for (int i = 0; i < matrixAll.matrix.size(); i++) {
//            avaliableLines.add(i);
//        }

        boolean nextClass = true;
        boolean isLocalSearch = false;//是否已开始n-opt
        int longestClassIndex = -1;
        double timeMoreBeforeLocalSearch = 0.05 * limitTime;
        double distanceMoreBeforeLocalSearch = 0.1 * limitDistance;

        List<Integer> lineTSP = new ArrayList<>();//
        List<Integer> lineSaving = new ArrayList<>();//

        while (avaliableCustomers.size() > 0) {
//            System.out.println(getDistance(matrixAll.centerGPSMap.get(0)[0], matrixAll.centerGPSMap.get(0)[1],
//                    matrixAll.centerGPSMap.get(4)[0], matrixAll.centerGPSMap.get(4)[1]));
//            System.out.println(getDistance(matrixAll.centerGPSMap.get(3)[0], matrixAll.centerGPSMap.get(3)[1],
//                    matrixAll.centerGPSMap.get(4)[0], matrixAll.centerGPSMap.get(4)[1]));
            if (nextClass) {
                longestClassIndex = getLongestClassIndex(avaliableLines);
                lineTSP = new ArrayList<>();//
                lineSaving = new ArrayList<>();//
            }

            List<Integer> line;
            if (isLocalSearch) {
                line = new ArrayList<>(matrixAll.matrix.get(longestClassIndex));
            } else {
                line = new ArrayList<>(tsp(matrixAll.matrix.get(longestClassIndex)));
            }


            if (getTimeSingleLine(line) <= limitTime + timeMoreBeforeLocalSearch
                    && getDistanceSingleLine(line) <= limitDistance + distanceMoreBeforeLocalSearch) {
//                System.out.println("longestClassIndex: " + longestClassIndex + "     line.size: " + line.size());
                if (isLocalSearch) {//
                    lineTSP = localSearch(lineTSP);//
                    lineSaving = localSearch(lineSaving);//
                } else {//
                    lineTSP = localSearch(tsp(matrixAll.matrix.get(longestClassIndex)));//
                    lineSaving = localSearch(savingAlgorithm(matrixAll.matrix.get(longestClassIndex)));//
                }//

                isLocalSearch = true;

                double timeTSP = getTimeSingleLine(lineTSP);//
                double timeSaving = getTimeSingleLine(lineSaving);//

                if (timeTSP < timeSaving) {//
                    line = new ArrayList<>(lineTSP);//
//                    System.out.println(longestClassIndex + " timeTSP   win   " + line.size());
                } else {//
                    line = new ArrayList<>(lineSaving);//
//                    System.out.println(longestClassIndex + " timeSaving   win   " + line.size());
                }//

                matrixAll.matrix.put(longestClassIndex, line);

                if (getTimeSingleLine(line) <= limitTime && getDistanceSingleLine(line) <= limitDistance) {
                    int lineLength = line.size();
                    avaliableLines = new HashSet(borrowIn(longestClassIndex, avaliableLines, avaliableCustomers));
                    if (lineLength < matrixAll.matrix.get(longestClassIndex).size()) {//成功借进
                        evaluate(matrixAll);
                        nextClass = false;

                        List<Integer> temp = new ArrayList<>(matrixAll.matrix.get(longestClassIndex));//
                        temp.removeAll(lineTSP);//
                        lineTSP = addCustomerToLine(lineTSP, temp.get(0));//
                        lineSaving = addCustomerToLine(lineSaving, temp.get(0));//

                    } else {//初步路线完成，开始计算重量下平均成本
//                        System.out.println("初步路线完成，开始计算重量下平均成本 " + longestClassIndex + " " + avaliableLines.toString());
                        avaliableLines = new HashSet(decideCarType(longestClassIndex, avaliableLines));

                        line = new ArrayList<>(matrixAll.matrix.get(longestClassIndex));
                        if (lineLength > line.size()) {
                            line = localSearch(line);
                            matrixAll.matrix.put(longestClassIndex, line);
                            evaluate(matrixAll);
                        }

                        avaliableLines.remove(longestClassIndex);
                        avaliableCustomers.removeAll(line);
                        nextClass = true;
                        isLocalSearch = false;
//                        System.out.println("最终路线完成 " + matrixAll.matrix.toString());
                    }
                } else {
                    avaliableLines = new HashSet(borrowOut(longestClassIndex, avaliableLines));
                    evaluate(matrixAll);
                    nextClass = false;

                    List<Integer> temp = new ArrayList<>(lineTSP);//
                    temp.removeAll(matrixAll.matrix.get(longestClassIndex));//
                    lineTSP.removeAll(temp);//
                    lineSaving.removeAll(temp);//
                }
            } else {
                avaliableLines = new HashSet(borrowOut(longestClassIndex, avaliableLines));
                evaluate(matrixAll);
                nextClass = false;

                List<Integer> temp = new ArrayList<>(lineTSP);//
                temp.removeAll(matrixAll.matrix.get(longestClassIndex));//
                lineTSP.removeAll(temp);//
                lineSaving.removeAll(temp);//
            }
        }
    }

    HashSet<Integer> decideCarType(int dealIndex, HashSet<Integer> avaliableLines) {
//        List<Integer> dealLine = new ArrayList<>(matrixAll.matrix.get(dealIndex));
        double weight = matrixAll.weightMap.get(dealIndex);
        int initCarIndex = -1;
        for (int i = 0; i < carTypeNum; i++) {
            if (carLimitWeightArr[i] >= weight) {
                initCarIndex = i;
                break;
            }
        }
        if (initCarIndex == -1) {
            initCarIndex = carTypeNum - 1;
        } else if (initCarIndex == 0) {
            initCarIndex = 0;
        } else {
            double costLittleCar = carCostArr[initCarIndex - 1] / carLimitWeightArr[initCarIndex - 1];
            double costLargeCar = carCostArr[initCarIndex] / weight;//每单位重量的运送成本，越小越好
            if (costLittleCar >= costLargeCar) {
                initCarIndex = initCarIndex;
            } else {
                initCarIndex = initCarIndex - 1;
            }
        }

        int finalCarIndex = -1;
        for (int i = initCarIndex; i >= 0; i--) {
            if (carUsedNumArr[i] < carNumArr[i]) {
                finalCarIndex = i;
                break;
            }
        }
        if (finalCarIndex == -1) {
            for (int i = initCarIndex + 1; i < carTypeNum; i++) {
                if (carUsedNumArr[i] < carNumArr[i]) {
                    finalCarIndex = i;
                    break;
                }
            }
        }

        if (finalCarIndex == -1) {
            finalCarIndex = initCarIndex;
        }

        matrixAll.chooseCar.put(dealIndex, finalCarIndex);
        carUsedNumArr[finalCarIndex]++;

//        System.out.println("before:" + dealIndex + " " + matrixAll.matrix.get(dealIndex).toString());
        System.out.println(dealIndex + ": 初始重量：" + weight + " 选择车型：" + carLimitWeightArr[finalCarIndex]);

        while (weight > carLimitWeightArr[finalCarIndex]) {
            avaliableLines = new HashSet(borrowOut(dealIndex, avaliableLines));
            evaluate(matrixAll);
            weight = matrixAll.weightMap.get(dealIndex);
        }

//        System.out.println("after:" + dealIndex + " " + matrixAll.matrix.get(dealIndex).toString());

        return avaliableLines;
    }

    HashSet<Integer> borrowOut(int dealIndex, HashSet<Integer> avaliableLines) {
        if (avaliableLines.size() == 1) {//如果没有路线可以借出了，就新开一条路借出，借出离中心最近的点
            int size = matrixAll.matrix.size();
            List<Integer> newLine = new ArrayList<>();
            double minTime = 99999999;
            int minTimeCustomerNO = 0;
            for (Integer customerNO : matrixAll.matrix.get(dealIndex)) {
                double curTime = time[0][customerNO] + time[customerNO][0] + waitTimeArr[customerNO];
                if (curTime < minTime) {
                    minTime = curTime;
                    minTimeCustomerNO = customerNO;
                }
            }
            newLine.add(minTimeCustomerNO);
            matrixAll.matrix.put(size, newLine);
            matrixAll.matrixTwice.put(size, new ArrayList<Integer>());

            List<Integer> dealLine = matrixAll.matrix.get(dealIndex);
            dealLine.remove(new Integer(minTimeCustomerNO));

            avaliableLines.add(size);

//            System.out.print(dealIndex + " 给 " + size + " borrowOut " + minTimeCustomerNO + " " + customerIds[minTimeCustomerNO]);
//            System.out.println(" " + matrixAll.matrix.get(size));
        } else {
            double minClassCenterDis = 999999999;
            int minClassCenterIndex = 0;
            double dealCenterX = matrixAll.centerGPSMap.get(dealIndex)[0];
            double dealCenterY = matrixAll.centerGPSMap.get(dealIndex)[1];

            for (Integer lineNO : avaliableLines) {
                if (lineNO != dealIndex) {
                    double curClassCenterDis = getDistance(dealCenterX, dealCenterY,
                            matrixAll.centerGPSMap.get(lineNO)[0], matrixAll.centerGPSMap.get(lineNO)[1]);

                    if (curClassCenterDis < minClassCenterDis) {
                        minClassCenterIndex = lineNO;
                        minClassCenterDis = curClassCenterDis;
                    }
                }
            }

            double outToClassCenterX = matrixAll.centerGPSMap.get(minClassCenterIndex)[0];
            double outToClassCenterY = matrixAll.centerGPSMap.get(minClassCenterIndex)[1];

            List<Integer> closerThanDeal = new ArrayList<>();
            for (Integer customerNO : matrixAll.matrix.get(dealIndex)) {
                double customerX = customerGpsX[customerNO];
                double customerY = customerGpsY[customerNO];

                if (getDistance(customerX, customerY, dealCenterX, dealCenterY)
                        > getDistance(customerX, customerY, outToClassCenterX, outToClassCenterY)) {
                    closerThanDeal.add(customerNO);
                }
            }

            if (closerThanDeal.isEmpty()) {
                double minCustomerDis = 999999999;
                int minCustomerNO = 0;
                for (Integer customerNO : matrixAll.matrix.get(dealIndex)) {
                    double curCustomerDis = getDistance(outToClassCenterX, outToClassCenterY,
                            customerGpsX[customerNO], customerGpsY[customerNO]);

                    if (curCustomerDis < minCustomerDis) {
                        minCustomerDis = curCustomerDis;
                        minCustomerNO = customerNO;
                    }
                }

                List<Integer> dealLine = matrixAll.matrix.get(dealIndex);
                dealLine.remove(new Integer(minCustomerNO));

                List<Integer> outToLine = matrixAll.matrix.get(minClassCenterIndex);
                outToLine.add(minCustomerNO);

//                System.out.print(dealIndex + " 给 " + minClassCenterIndex + " borrowOut " + minCustomerNO + " " + customerIds[minCustomerNO]);
//                System.out.println(" " + matrixAll.matrix.get(minClassCenterIndex));
            } else {
                double maxCustomerDis = 0;//离dealIndex中心最远的
                int maxCustomerNO = 0;
                for (Integer customerNO : closerThanDeal) {
                    double curCustomerDis = getDistance(dealCenterX, dealCenterY,
                            customerGpsX[customerNO], customerGpsY[customerNO]);

                    if (curCustomerDis > maxCustomerDis) {
                        maxCustomerDis = curCustomerDis;
                        maxCustomerNO = customerNO;
                    }
                }

                List<Integer> dealLine = matrixAll.matrix.get(dealIndex);
                dealLine.remove(new Integer(maxCustomerNO));

                List<Integer> outToLine = matrixAll.matrix.get(minClassCenterIndex);
                outToLine.add(maxCustomerNO);

//                System.out.print(dealIndex + " 给 " + minClassCenterIndex + " borrowOut " + maxCustomerNO + " " + customerIds[maxCustomerNO]);
//                System.out.println(" " + matrixAll.matrix.get(minClassCenterIndex));
            }
        }

        return avaliableLines;
    }

    HashSet<Integer> borrowIn(int dealIndex, HashSet<Integer> avaliableLines, HashSet<Integer> avaliableCustomers) {
        if (avaliableLines.size() == 1) return avaliableLines;

        List<Integer> dealLine = new ArrayList<>(matrixAll.matrix.get(dealIndex));
        List<Integer> avaliable = new ArrayList<>(avaliableCustomers);
        avaliable.removeAll(dealLine);
        List<Integer> readyCustomers = new ArrayList<>();

        final double dealCenterX = matrixAll.centerGPSMap.get(dealIndex)[0];
        final double dealCenterY = matrixAll.centerGPSMap.get(dealIndex)[1];

        double maxDis = 0;
        for (int i = 0; i < dealLine.size(); i++) {
            double dis = getDistance(dealCenterX, dealCenterY, customerGpsX[dealLine.get(i)], customerGpsY[dealLine.get(i)]);

            if (dis > maxDis) {
                maxDis = dis;
            }
        }

        double centerX = customerGpsX[0];
        double centerY = customerGpsY[0];

        for (Integer customerNO : avaliable) {
            if (getDistance(dealCenterX, dealCenterY, customerGpsX[customerNO], customerGpsY[customerNO]) * 1.5
                    + getDistance(centerX, centerY, customerGpsX[customerNO], customerGpsY[customerNO]) * 0.5
                    <= getDistance(dealCenterX, dealCenterY, centerX, centerY) + maxDis * 2
                    || time[0][customerNO] + time[customerNO][0] <= 1500) {
                List<Integer> temp = addCustomerToLine(dealLine, customerNO);
                if (getTimeSingleLine(temp) <= limitTime) {
                    readyCustomers.add(customerNO);
                }
            }
        }

        if (readyCustomers.size() > 0) {
            Collections.sort(readyCustomers, new Comparator<Integer>() {
                @Override
                public int compare(Integer p1, Integer p2) {
                    double dis1 = getDistance(dealCenterX, dealCenterY, customerGpsX[p1], customerGpsY[p1]);
                    double dis2 = getDistance(dealCenterX, dealCenterY, customerGpsX[p2], customerGpsY[p2]);
                    if (dis1 > dis2) {
                        return 1;
                    } else if (dis1 < dis2) {
                        return -1;
                    } else {
                        return 0;
                    }
                }
            });

            for (Integer customerNO : readyCustomers) {
                List<Integer> temp = addCustomerToLine(dealLine, customerNO);
                if (getDistanceSingleLine(temp) <= limitDistance
                        && getTimeSingleLine(temp) <= limitTime) {
                    for (Integer lineNO : avaliableLines) {
                        List<Integer> line = matrixAll.matrix.get(lineNO);
                        if (line.indexOf(customerNO) != -1) {
//                            System.out.print(dealIndex + " 从 " + lineNO + " borrowIn " + customerNO + " " + customerIds[customerNO]);
                            line.remove(new Integer(customerNO));
//                            System.out.println(" " + matrixAll.matrix.get(lineNO));
                            if (line.isEmpty()) {
//                                System.out.println("line.isEmpty()  " + lineNO);
                                avaliableLines.remove(lineNO);
                            }
                            break;
                        }
                    }
                    matrixAll.matrix.put(dealIndex, temp);
                    break;
                }
            }
        }
        return avaliableLines;
    }

    List<Integer> addCustomerToLine(List<Integer> line, int customerNO) {
        List<Integer> thisLine = new ArrayList<>(line);
        thisLine.add(0, 0);
        thisLine.add(0);
        double minTime = 999999999;//添加后增加的时间
        int minIndex = 0;
        for (int i = 1; i < thisLine.size(); i++) {
            double currentTime = time[i - 1][customerNO] + time[customerNO][i] - time[i - 1][i];
            if (currentTime < minTime) {
                minTime = currentTime;
                minIndex = i;
            }
        }

        thisLine.add(minIndex, customerNO);
        thisLine.remove(new Integer(0));
        thisLine.remove(new Integer(0));
        return thisLine;
    }

    double getDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }

    void evaluate(Matrix m) {
        HashSet<Integer> existLineNO = new HashSet<>();

        HashMap<Integer, List<Integer>> matrix = new HashMap<>(m.matrix);
        HashMap<Integer, Double> timeMap = new HashMap<>();
        HashMap<Integer, Double> distanceMap = new HashMap<>();
        HashMap<Integer, Double> weightMap = new HashMap<>();
        HashMap<Integer, Double> centerDisMap = new HashMap<>();
        HashMap<Integer, Double[]> centerGPSMap = new HashMap<>();

        HashMap<Integer, List<Integer>> matrixTwice = new HashMap<>(m.matrixTwice);
        HashMap<Integer, Double> timeMapTwice = new HashMap<>();
        HashMap<Integer, Double> distanceTwice = new HashMap<>();
        HashMap<Integer, Double> weightMapTwice = new HashMap<>();
        HashMap<Integer, Double> centerDisMapTwice = new HashMap<>();
        HashMap<Integer, Double[]> centerGPSMapTwice = new HashMap<>();

        double timeAll = 0;//总时间
        double distanceAll = 0;//总路程
        double costAll = 0;
        int carNum = 0;

        double maxTime = 0;
        double minTime = 99999999;

        for (int i = 0; i < matrix.size(); i++) {
            double singleTimeAll = 0;

            if (matrix.get(i).size() > 0) {
                carNum++;

                List<Integer> currentList = new ArrayList<>(matrix.get(i));
                double currentTime = 0;
                double currentDistance = 0;
                double currentWeight = 0;
                double avgX = 0;
                double avgY = 0;

                currentTime += (time[0][currentList.get(0)] + waitTimeArr[currentList.get(0)]);
                currentDistance += distance[0][currentList.get(0)];
                currentWeight += weightArr[currentList.get(0)];
                avgX += customerGpsX[currentList.get(0)];
                avgY += customerGpsY[currentList.get(0)];

                for (int j = 1; j < currentList.size(); j++) {
                    currentTime += time[currentList.get(j - 1)][currentList.get(j)] + waitTimeArr[currentList.get(j)];
                    currentDistance += distance[currentList.get(j - 1)][currentList.get(j)];
                    currentWeight += weightArr[currentList.get(j)];
                    avgX += customerGpsX[currentList.get(j)];
                    avgY += customerGpsY[currentList.get(j)];
                }
                currentTime += time[currentList.get(currentList.size() - 1)][0];
                currentDistance += distance[currentList.get(currentList.size() - 1)][0];

                timeMap.put(i, currentTime);
                distanceMap.put(i, currentDistance);
                singleTimeAll += currentTime;
                weightMap.put(i, currentWeight);
                timeAll += currentTime;
                distanceAll += currentDistance;

                avgX = avgX / currentList.size();
                avgY = avgY / currentList.size();

                centerDisMap.put(i, getDistance(avgX, avgY, customerGpsX[0], customerGpsY[0]));
                Double[] gps = {avgX, avgY};
                centerGPSMap.put(i, gps);

                if (m.chooseCar.containsKey(i)) {
                    costAll += carCostArr[m.chooseCar.get(i)];
                }

            } else {
                timeMap.put(i, new Double(0));
                distanceMap.put(i, new Double(0));
            }

            if (matrixTwice.get(i).size() > 0) {
                List<Integer> currentList = new ArrayList<>(matrixTwice.get(i));
                double currentTime = twiceLoadingTime;
                double currentDistance = 0;
                double currentWeight = 0;
                double avgX = 0;
                double avgY = 0;

                currentTime += (time[0][currentList.get(0)] + waitTimeArr[currentList.get(0)]);
                currentDistance += distance[0][currentList.get(0)];
                currentWeight += weightArr[currentList.get(0)];
                avgX += customerGpsX[currentList.get(0)];
                avgY += customerGpsY[currentList.get(0)];

                for (int j = 1; j < currentList.size(); j++) {
                    currentTime += time[currentList.get(j - 1)][currentList.get(j)] + waitTimeArr[currentList.get(j)];
                    currentDistance += distance[currentList.get(j - 1)][currentList.get(j)];
                    currentWeight += weightArr[currentList.get(j)];
                    avgX += customerGpsX[currentList.get(j)];
                    avgY += customerGpsY[currentList.get(j)];
                }
                currentTime += time[currentList.get(currentList.size() - 1)][0];
                currentDistance += distance[currentList.get(currentList.size() - 1)][0];

                timeMapTwice.put(i, currentTime);
                distanceTwice.put(i, currentDistance);
                singleTimeAll += currentTime;
                weightMapTwice.put(i, currentWeight);
                timeAll += currentTime;
                distanceAll += currentDistance;

                avgX = avgX / currentList.size();
                avgY = avgY / currentList.size();

                centerDisMapTwice.put(i, getDistance(avgX, avgY, customerGpsX[0], customerGpsY[0]));
                Double[] gps = {avgX, avgY};
                centerGPSMapTwice.put(i, gps);
            } else {
                timeMapTwice.put(i, new Double(0));
                distanceTwice.put(i, new Double(0));
            }

            if (singleTimeAll > 0) {
                existLineNO.add(i);
                if (singleTimeAll < minTime) {
                    minTime = singleTimeAll;
                }
                if (singleTimeAll > maxTime) {
                    maxTime = singleTimeAll;
                }
            }
        }

        m.existLineNO = new HashSet<>(existLineNO);

        m.timeMap = new HashMap<>(timeMap);
        m.distanceMap = new HashMap<>(distanceMap);
        m.weightMap = new HashMap<>(weightMap);
        m.centerDisMap = new HashMap<>(centerDisMap);
        m.centerGPSMap = new HashMap<>(centerGPSMap);

        m.timeMapTwice = new HashMap<>(timeMapTwice);
        m.distanceTwice = new HashMap<>(distanceTwice);
        m.weightMapTwice = new HashMap<>(weightMapTwice);
        m.centerDisMapTwice = new HashMap<>(centerDisMap);
        m.centerGPSMapTwice = new HashMap<>(centerGPSMap);

        m.costAll = costAll;
        m.timeAll = timeAll;
        m.distanceAll = distanceAll;
        m.balance = maxTime - minTime;
        m.carNum = carNum;
    }

    void outputFinally(Matrix m) {
        for (Integer i : m.existLineNO) {
            System.out.println("i：" + i);
            System.out.println("工作时间：" + (m.timeMap.get(i) + m.timeMapTwice.get(i)) / 3600 + " 小时");
            System.out.println("运输里程：" + (m.distanceMap.get(i) + m.distanceTwice.get(i)) + " 公里");
            System.out.println("一次运输客户数量：" + m.matrix.get(i).size());
            System.out.println("一次运输载重：" + m.weightMap.get(i) + " / 车型：" + carLimitWeightArr[m.chooseCar.get(i)]);
            System.out.println("一次运输时间：" + m.timeMap.get(i) / 3600 + " 小时");
            System.out.println("一次运输里程：" + m.distanceMap.get(i) + " 公里");
            System.out.println("染色体：" + m.matrix.get(i));
            if (m.matrixTwice.get(i).size() > 0) {
                System.out.println("二次运输客户数量：" + m.matrixTwice.get(i).size());
                System.out.println("二次运输载重：" + m.weightMapTwice.get(i));
                System.out.println("二次运输时间：" + m.timeMapTwice.get(i) / 3600 + " 小时");
                System.out.println("二次运输里程：" + m.distanceTwice.get(i) + " 公里");
                System.out.println("染色体：" + m.matrixTwice.get(i));
            }
            System.out.println();
        }

        String carString = "载重量/车辆数";
        for (int i = 0; i < carTypeNum; i++) {
            carString += " " + carLimitWeightArr[i] + "/" + carUsedNumArr[i];
        }

        System.out.println("车辆数：" + m.carNum);
        System.out.println("总成本：" + m.costAll);
        System.out.println("总工作时间：" + m.timeAll / 3600 + " 小时");
        System.out.println("总运输里程：" + m.distanceAll + " 公里");
        System.out.println("每辆车平均工作时间：" + m.timeAll / 3600 / m.carNum + " 小时");
        System.out.println("每辆车平均运输里程：" + m.distanceAll / m.carNum + " 公里");
        System.out.println("平衡指数：" + m.balance / 60 + " 分钟");
        System.out.println("车辆使用情况：" + carString);
        System.out.println();

//        System.out.println("insertSearch: " + (int) insertSearchSuccess + "/" + (int) insertSearchNum + " = "
//                + (insertSearchSuccess / insertSearchNum * 100) + " %");
//        System.out.println("swapSearch: " + (int) swapSearchSuccess + "/" + (int) swapSearchNum + " = "
//                + (swapSearchSuccess / swapSearchNum * 100) + " %");
//        System.out.println("opt_2Search: " + (int) opt_2SearchSuccess + "/" + (int) opt_2SearchNum + " = "
//                + (opt_2SearchSuccess / opt_2SearchNum * 100) + " %");
//        System.out.println("opt_3Search: " + (int) opt_3SearchSuccess + "/" + (int) opt_3SearchNum + " = "
//                + (opt_3SearchSuccess / opt_3SearchNum * 100) + " %");
//        System.out.println("opt_orSearch: " + (int) opt_orSearchSuccess + "/" + (int) opt_orSearchNum + " = "
//                + (opt_orSearchSuccess / opt_orSearchNum * 100) + " %");
//        System.out.println();
    }

    void outputIdeaData(Matrix m) {
        List<Double[][]> res = new ArrayList<>();
        for (Integer i : m.existLineNO) {
            List<Double[]> currentCar = new ArrayList<>();
            currentCar.add(new Double[]{customerGpsX[0], customerGpsY[0]});
            for (int j = 0; j < m.matrix.get(i).size(); j++) {
                int customerNO = m.matrix.get(i).get(j);
                currentCar.add(new Double[]{customerGpsX[customerNO], customerGpsY[customerNO]});
            }
            currentCar.add(new Double[]{customerGpsX[0], customerGpsY[0]});

            if (m.matrixTwice.get(i).size() > 0) {
                for (int j = 0; j < m.matrixTwice.get(i).size(); j++) {
                    int customerNO = m.matrixTwice.get(i).get(j);
                    currentCar.add(new Double[]{customerGpsX[customerNO], customerGpsY[customerNO]});
                }
                currentCar.add(new Double[]{customerGpsX[0], customerGpsY[0]});
            }

            Double[][] array = currentCar.toArray(new Double[0][]);
            res.add(array);
        }

        System.out.println("var path = [");
        for (int i = 0; i < res.size(); i++) {
            System.out.print("[");
            for (int j = 0; j < res.get(i).length; j++) {
                System.out.print(Arrays.toString(res.get(i)[j]) + ",");
            }
            System.out.println("],");
        }
        System.out.println("];");
        System.out.println();
    }

    //二次配送
    void twiceLine() {
        while (true) {
//            System.out.println(matrixAll.matrix.toString());
//            System.out.println(matrixAll.matrixTwice.toString());
            int size = matrixAll.matrix.size();
            int limitLineNO = -1;
            List<Integer> limitLine = new ArrayList<>();
            for (int i = size - 1; i >= 0; i--) {
                if (matrixAll.timeMap.get(i) + matrixAll.timeMapTwice.get(i) > 0) {
                    limitLineNO = i;
                    limitLine.addAll(matrixAll.matrix.get(i));
                    limitLine.addAll(matrixAll.matrixTwice.get(i));
                    break;
                }
            }
            HashSet<Integer> existLineNO = new HashSet<>(matrixAll.existLineNO);
            existLineNO.remove(limitLineNO);//除去被分的线路

            List<Integer> lineNO = new ArrayList<>();
            List<Double> lineTime = new ArrayList<>();
            for (Integer i : existLineNO) {
                double time = matrixAll.timeMap.get(i) + matrixAll.timeMapTwice.get(i);
                lineNO.add(i);
                lineTime.add(time);
            }

            for (int i = 0; i < existLineNO.size() - 1; i++) {
                for (int j = i + 1; j < existLineNO.size(); j++) {
                    if (lineTime.get(i) < lineTime.get(j)) {
                        int NOTemp = lineNO.get(i);
                        lineNO.set(i, lineNO.get(j));
                        lineNO.set(j, NOTemp);

                        double timeTemp = lineTime.get(i);
                        lineTime.set(i, lineTime.get(j));
                        lineTime.set(j, timeTemp);
                    }
                }
            }

            boolean success = false;
            for (Integer currentCarNO : lineNO) {
                double onceTime = matrixAll.timeMap.get(currentCarNO);
                double carWeight = carLimitWeightArr[matrixAll.chooseCar.get(currentCarNO)];

                List<Integer> currentTwiceLine = new ArrayList<>(matrixAll.matrixTwice.get(currentCarNO));

                while (true) {
                    if (limitLine.isEmpty()) {
                        if (matrixAll.chooseCar.containsKey(limitLineNO)) {
                            int carIndex = matrixAll.chooseCar.get(limitLineNO);
                            carUsedNumArr[carIndex]--;
                            matrixAll.chooseCar.remove(limitLineNO);
                        }
                        break;
                    }

                    //如果二次运输为空，则把最近的一个点给他
                    if (currentTwiceLine.isEmpty()) {
                        double minTime = 99999999;
                        int minTimeCustomerNO = -1;
                        for (Integer customerNO : limitLine) {
                            double curTime = time[0][customerNO] + time[customerNO][0] + waitTimeArr[customerNO];
                            if (curTime < minTime) {
                                minTime = curTime;
                                minTimeCustomerNO = customerNO;
                            }
                        }
                        currentTwiceLine.add(minTimeCustomerNO);
                        if (getTimeSingleLine(currentTwiceLine) + onceTime + twiceLoadingTime <= limitTime
                                && getDistanceSingleLine(currentTwiceLine) <= limitDistance
                                && getWeightSingleLine(currentTwiceLine) <= carWeight) {
                            limitLine.remove(new Integer(minTimeCustomerNO));
                            matrixAll.matrix.get(limitLineNO).remove(new Integer(minTimeCustomerNO));
                            matrixAll.matrixTwice.get(limitLineNO).remove(new Integer(minTimeCustomerNO));

                            success = true;
                            matrixAll.matrixTwice.put(currentCarNO, currentTwiceLine);
                            evaluate(matrixAll);
                            continue;
                        }
                    } else {
                        int limitLineSize = limitLine.size();
                        double currentTwiceLineCenterX = matrixAll.centerGPSMapTwice.get(currentCarNO)[0];
                        double currentTwiceLineCenterY = matrixAll.centerGPSMapTwice.get(currentCarNO)[1];

                        int[] customerNO = new int[limitLineSize];
                        double[] centerDis = new double[limitLineSize];
                        for (int j = 0; j < limitLineSize; j++) {
                            customerNO[j] = limitLine.get(j);
                            centerDis[j] = getDistance(currentTwiceLineCenterX, currentTwiceLineCenterY,
                                    customerGpsX[customerNO[j]], customerGpsY[customerNO[j]]);
                        }

                        if (limitLineSize > 1) {//根据距离排序,先给接近限时的路线添加点
                            for (int j = 0; j < limitLineSize - 1; j++) {
                                for (int k = j + 1; k < limitLineSize; k++) {
                                    if (centerDis[j] < centerDis[k]) {
                                        int NoTemp = customerNO[j];
                                        customerNO[j] = customerNO[k];
                                        customerNO[k] = NoTemp;

                                        double disTemp = centerDis[j];
                                        centerDis[j] = centerDis[k];
                                        centerDis[k] = disTemp;
                                    }
                                }
                            }
                        }

                        boolean isLoop = false;
                        for (int j = 0; j < limitLineSize; j++) {
                            List<Integer> spare = new ArrayList<>(currentTwiceLine);

                            currentTwiceLine = addCustomerToLine(currentTwiceLine, customerNO[j]);

//                            ArrayList<Integer> temp = new ArrayList<>(currentTwiceLine);

                            currentTwiceLine = new ArrayList<>(localSearch(currentTwiceLine));
                            if (getTimeSingleLine(currentTwiceLine) + onceTime + twiceLoadingTime <= limitTime
                                    && getDistanceSingleLine(currentTwiceLine) <= limitDistance
                                    && getWeightSingleLine(currentTwiceLine) <= carWeight) {

                                limitLine.remove(new Integer(customerNO[j]));
                                matrixAll.matrix.get(limitLineNO).remove(new Integer(customerNO[j]));
                                matrixAll.matrixTwice.get(limitLineNO).remove(new Integer(customerNO[j]));

                                success = true;
                                matrixAll.matrixTwice.put(currentCarNO, currentTwiceLine);
                                evaluate(matrixAll);
                                isLoop = true;

                                break;
                            } else {
                                currentTwiceLine.remove(new Integer(customerNO[j]));
                                if (getTimeSingleLine(spare) < getTimeSingleLine(currentTwiceLine)) {
                                    currentTwiceLine = new ArrayList<>(spare);
                                }
                            }
                        }
                        if (isLoop) continue;
                    }
                    break;
                }
            }
            if (!success) break;
        }
    }

    void fixLastLine() {
        int size = matrixAll.matrix.size();
        int limitLineNO = size;
        for (int i = size - 1; i >= 0; i--) {
            if (matrixAll.matrix.get(i).size() > 0 || matrixAll.matrixTwice.get(i).size() > 0) {
                limitLineNO = i;
                break;
            }
        }

        fixLine(limitLineNO, matrixAll);
    }

    void fixLine(int lineIndex, Matrix m) {
        List<Integer> lastLineOnce = new ArrayList<>(m.matrix.get(lineIndex));
        List<Integer> lastLineTwice = new ArrayList<>(m.matrixTwice.get(lineIndex));
        int carIndex = m.chooseCar.get(lineIndex);

        if (!lastLineOnce.isEmpty() && !lastLineTwice.isEmpty()) {

            while (true) {
                if (lastLineTwice.isEmpty()) {
                    break;
                }

                boolean success = false;
                double onceAvgX = m.centerGPSMap.get(lineIndex)[0];
                double onceAvgY = m.centerGPSMap.get(lineIndex)[1];

                int twiceSize = lastLineTwice.size();

                int[] customerNO = new int[twiceSize];
                double[] centerDis = new double[twiceSize];
                for (int i = 0; i < twiceSize; i++) {
                    customerNO[i] = lastLineTwice.get(i);
                    centerDis[i] = getDistance(onceAvgX, onceAvgY,
                            customerGpsX[customerNO[i]], customerGpsY[customerNO[i]]);
                }

                if (twiceSize > 1) {//根据距离排序,先给接近限时的路线添加点
                    for (int i = 0; i < twiceSize - 1; i++) {
                        for (int j = i + 1; j < twiceSize; j++) {
                            if (centerDis[i] > centerDis[j]) {
                                int NoTemp = customerNO[i];
                                customerNO[i] = customerNO[j];
                                customerNO[j] = NoTemp;

                                double disTemp = centerDis[i];
                                centerDis[i] = centerDis[j];
                                centerDis[j] = disTemp;
                            }
                        }
                    }
                }

                for (int i = 0; i < twiceSize; i++) {
                    List<Integer> spareOnce = new ArrayList<>(lastLineOnce);
                    List<Integer> spareTwice = new ArrayList<>(lastLineTwice);

                    lastLineOnce = addCustomerToLine(lastLineOnce, customerNO[i]);
                    lastLineOnce = new ArrayList<>(localSearch(lastLineOnce));
                    lastLineTwice.remove(new Integer(customerNO[i]));

                    double currTime;
                    if (lastLineTwice.isEmpty()) {
                        currTime = getTimeSingleLine(lastLineOnce);
                    } else {
                        currTime = getTimeSingleLine(lastLineOnce) + getTimeSingleLine(lastLineTwice) + twiceLoadingTime;
                    }

                    if (currTime <= limitTime
                            && getDistanceSingleLine(lastLineOnce) + getDistanceSingleLine(lastLineTwice) <= limitDistance
                            && getWeightSingleLine(lastLineOnce) <= carLimitWeightArr[carIndex]) {
                        success = true;
                    } else {
                        lastLineOnce.remove(new Integer(customerNO[i]));
                        lastLineTwice = addCustomerToLine(lastLineTwice, customerNO[i]);

                        if (getTimeSingleLine(spareOnce) < getTimeSingleLine(lastLineOnce)) {
                            lastLineOnce = new ArrayList<>(spareOnce);
                        }

                        if (getTimeSingleLine(spareTwice) < getTimeSingleLine(lastLineTwice)) {
                            lastLineTwice = new ArrayList<>(spareTwice);
                        }
                    }
                }

                if (!success) break;
            }

            m.matrix.put(lineIndex, lastLineOnce);
            m.matrixTwice.put(lineIndex, lastLineTwice);

        } else if (lastLineOnce.isEmpty() && !lastLineTwice.isEmpty()) {
            lastLineTwice = new ArrayList<>(localSearch(lastLineTwice));
            m.matrix.put(lineIndex, lastLineTwice);
            m.matrixTwice.put(lineIndex, lastLineOnce);
        }

        double onceWeight = getWeightSingleLine(m.matrix.get(lineIndex));
        for (int i = carIndex - 1; i >= 0; i--) {
            if (carLimitWeightArr[i] < onceWeight) {
                break;
            } else if (carUsedNumArr[i] < carNumArr[i]) {
                m.chooseCar.put(lineIndex, i);
                carUsedNumArr[i + 1]--;
                carUsedNumArr[i]++;
            }
        }
    }

    void balance(Matrix matrix, int lastBalanceNum) {
        Matrix m = matrix;

        int balanceNum = 0;

        HashSet<Integer> banTable = new HashSet<>();

        while (m.balance > balancePlan) {
            //先找到时间最小的那条路线，编号为limitLineNO
            boolean success = false;
            List<Integer> existLine = new ArrayList<>(m.existLineNO);

            double minTime = 99999999;
            int minLineNO = -1;
            List<Integer> minLine = new ArrayList<>();//默认twice线路，后续fix
            for (Integer i : existLine) {
                double lineTime = m.timeMap.get(i) + m.timeMapTwice.get(i);
                if (lineTime < minTime) {
                    minTime = lineTime;
                    minLineNO = i;
                    minLine = new ArrayList<>(m.matrixTwice.get(i));
                }
            }
            //最小的路线不算在内
            existLine.remove(new Integer(minLineNO));

            if (minLine.isEmpty()) {
                //如果minLine为空，则找到时间最长的路线，将离中心最近的点给minLine
                List<Integer> lineNO = new ArrayList<>();
                List<Double> lineTime = new ArrayList<>();
                for (Integer NO : existLine) {
                    lineNO.add(NO);
                    lineTime.add(m.timeMap.get(NO) + m.timeMapTwice.get(NO));
                }
                //排序,从大到小
                for (int i = 0; i < lineNO.size() - 1; i++) {
                    for (int j = i + 1; j < lineNO.size(); j++) {
                        if (lineTime.get(i) > lineTime.get(j)) {
                            int NOTemp = lineNO.get(i);
                            lineNO.set(i, lineNO.get(j));
                            lineNO.set(j, NOTemp);

                            double TimeTemp = lineTime.get(i);
                            lineTime.set(i, lineTime.get(j));
                            lineTime.set(j, TimeTemp);
                        }
                    }
                }

                for (Integer outLineNO : lineNO) {
                    if (banTable.contains(outLineNO)) continue;

                    List<Integer> maxLine = new ArrayList<>();
                    if (m.matrixTwice.get(outLineNO).isEmpty()) {
                        maxLine = new ArrayList<>(m.matrix.get(outLineNO));
                    } else {
                        maxLine = new ArrayList<>(m.matrixTwice.get(outLineNO));
                    }

                    int closestCustNO = -1;
                    double closestTime = 999999999;
                    for (Integer customerNO : maxLine) {
                        double currTime = time[0][customerNO] + time[customerNO][0] + waitTimeArr[customerNO];
                        if (currTime < closestTime) {
                            closestTime = currTime;
                            closestCustNO = customerNO;
                        }
                    }

                    minLine.add(closestCustNO);
                    if (m.timeMap.get(minLineNO) + twiceLoadingTime + getTimeSingleLine(minLine) <= limitTime
                            && getDistanceSingleLine(minLine) <= limitDistance
                            && getWeightSingleLine(minLine) <= carLimitWeightArr[m.chooseCar.get(minLineNO)]) {
                        success = true;
                        m.matrixTwice.put(minLineNO, minLine);

                        m.matrix.get(outLineNO).remove(new Integer(closestCustNO));
                        m.matrixTwice.get(outLineNO).remove(new Integer(closestCustNO));

                        banTable.add(minLineNO);

                        fixLine(minLineNO, m);
                        evaluate(m);
                        balanceNum++;

                        break;
                    } else {
                        minLine.remove(new Integer(closestCustNO));
                    }
                }
            } else {
                //如果minLine不为空，则对所有路线的中心的距离排序，从小到大，如果不在ban表里，对中心距离从小到大取点，取一个点即重新while
                double minLineCenterX = m.centerGPSMapTwice.get(minLineNO)[0];
                double minLineCenterY = m.centerGPSMapTwice.get(minLineNO)[1];

                List<Integer> lineNO = new ArrayList<>();
                List<Double> lineCenterDis = new ArrayList<>();
                for (Integer NO : existLine) {
                    lineNO.add(NO);
                    if (m.matrixTwice.get(NO).isEmpty()) {
                        lineCenterDis.add(getDistance(m.centerGPSMap.get(NO)[0], m.centerGPSMap.get(NO)[1],
                                minLineCenterX, minLineCenterY));
                    } else {
                        lineCenterDis.add(getDistance(m.centerGPSMapTwice.get(NO)[0], m.centerGPSMapTwice.get(NO)[1],
                                minLineCenterX, minLineCenterY));
                    }
                }

                //排序,从小到大
                for (int i = 0; i < lineNO.size() - 1; i++) {
                    for (int j = i + 1; j < lineNO.size(); j++) {
                        if (lineCenterDis.get(i) > lineCenterDis.get(j)) {
                            int NOTemp = lineNO.get(i);
                            lineNO.set(i, lineNO.get(j));
                            lineNO.set(j, NOTemp);

                            double centerDisTemp = lineCenterDis.get(i);
                            lineCenterDis.set(i, lineCenterDis.get(j));
                            lineCenterDis.set(j, centerDisTemp);
                        }
                    }
                }

                for (Integer outLineNO : lineNO) {
                    if (banTable.contains(outLineNO)) continue;

                    List<Integer> outLine = new ArrayList<>();
                    if (m.matrixTwice.get(outLineNO).isEmpty()) {
                        outLine = new ArrayList<>(m.matrix.get(outLineNO));
                    } else {
                        outLine = new ArrayList<>(m.matrixTwice.get(outLineNO));
                    }

                    List<Double> disToMinCenter = new ArrayList<>();
                    for (Integer customerNO : outLine) {
                        disToMinCenter.add(getDistance(minLineCenterX, minLineCenterY,
                                customerGpsX[customerNO], customerGpsY[customerNO]));
                    }

                    for (int i = 0; i < outLine.size() - 1; i++) {
                        for (int j = i + 1; j < outLine.size(); j++) {
                            if (disToMinCenter.get(i) > disToMinCenter.get(j)) {
                                int NOTemp = outLine.get(i);
                                outLine.set(i, outLine.get(j));
                                outLine.set(j, NOTemp);

                                double disTemp = disToMinCenter.get(i);
                                disToMinCenter.set(i, disToMinCenter.get(j));
                                disToMinCenter.set(j, disTemp);
                            }
                        }
                    }

                    for (Integer customerNO : outLine) {
                        minLine = addCustomerToLine(minLine, customerNO);

                        if (m.timeMap.get(minLineNO) + twiceLoadingTime + getTimeSingleLine(minLine) <= limitTime
                                && getDistanceSingleLine(minLine) <= limitDistance
                                && getWeightSingleLine(minLine) <= carLimitWeightArr[m.chooseCar.get(minLineNO)]) {
                            success = true;
                            m.matrixTwice.put(minLineNO, minLine);

                            m.matrix.get(outLineNO).remove(new Integer(customerNO));
                            m.matrixTwice.get(outLineNO).remove(new Integer(customerNO));

                            banTable.add(minLineNO);

                            fixLine(minLineNO, m);
                            evaluate(m);

                            balanceNum++;

                            break;
                        } else {
                            minLine.remove(new Integer(customerNO));
                        }
                    }
                    if (success) break;
                }
            }

            if (!success) break;
        }

        System.out.println("balanceNum " + balanceNum);
        if (balanceNum != 0 && balanceNum < lastBalanceNum) {
            balance(m, balanceNum);
        }
    }

    public boolean confirmCarNum() {
        for (int i = 0; i < carTypeNum; i++) {
            if (carUsedNumArr[i] > carNumArr[i]) {
                return false;
            }
        }
        return true;
    }

//    public static void main(String[] args) {
//        long startTime = System.currentTimeMillis(); //获取开始时间
//        ServiceOutsource serviceOutsource = new ServiceOutsource();
//        serviceOutsource.solveVrp();
//        long endTime = System.currentTimeMillis(); //获取结束时间
//
//        System.out.println("车辆储备是否足够：" + serviceOutsource.confirmCarNum());
//        System.out.println("整个算法总计花费：" + (endTime - startTime) / 1000 + "秒");
//    }

    public void solveVrp() {
        // 初始化数据，不同问题初始化数据不一样
        System.out.println("------------------------------------------------");
        long initDataStart = System.currentTimeMillis(); //获取开始时间
        initData();
        long initDataEnd = System.currentTimeMillis(); //获取结束时间
        timeInitData = initDataEnd - initDataStart;
        System.out.println("初始化(读取)数据用时： " + timeInitData / 1000 + "秒");
        System.out.println("------------------------------------------------");

        //---------------------------------------------------------------

        //初始化种群
        System.out.println("------------------------------------------------");
        long initGroupStart = System.currentTimeMillis(); //获取开始时间
        initGroup();
        long initGroupEnd = System.currentTimeMillis(); //获取结束时间
        timeInitGroup = initGroupEnd - initGroupStart;
        evaluate(matrixAll);
        System.out.println("考虑载重限制(平均载重量)的初始化集群计算用时： " + timeInitGroup + "ms");
        System.out.println("------------------------------------------------");

        //---------------------------------------------------------------

        System.out.println("------------------------------------------------");
        long getInitLinesStart = System.currentTimeMillis(); //获取开始时间
        getInitLinesSlowly();
//        getInitLinesQuickly();
        long getInitLinesEnd = System.currentTimeMillis(); //获取结束时间
        timeGetInitLines = getInitLinesEnd - getInitLinesStart;
        evaluate(matrixAll);
        outputFinally(matrixAll);
        System.out.println("选择车型并考虑时间、路程限制的计算用时：" + timeGetInitLines / 1000 + " s");
        System.out.println("------------------------------------------------");

        //---------------------------------------------------------------

        System.out.println("------------------------------------------------");
        long twiceLineStart = System.currentTimeMillis(); //获取开始时间
        twiceLine();
        fixLastLine();
        long twiceLineEnd = System.currentTimeMillis(); //获取结束时间
        timeTwiceLine = twiceLineEnd - twiceLineStart;
        evaluate(matrixAll);
        outputFinally(matrixAll);
        System.out.println("考虑二次运输的计算用时：" + timeTwiceLine / 1000 + " s");
        System.out.println("------------------------------------------------");

        //---------------------------------------------------------------

        System.out.println("------------------------------------------------");
        long balanceStart = System.currentTimeMillis(); //获取开始时间
        balance(matrixAll, 9999);
        long balanceEnd = System.currentTimeMillis(); //获取开始时间
        timeBalance = balanceEnd - balanceStart;
        evaluate(matrixAll);
        outputFinally(matrixAll);
        System.out.println("考虑用时平衡的计算用时：" + timeBalance / 1000 + " s");
        System.out.println("------------------------------------------------");


        //---------------------------------------------------------------

        System.out.println("------------------------------------------------");
        outputIdeaData(matrixAll);
        System.out.println("------------------------------------------------");

        //---------------------------------------------------------------

        System.out.println();
        System.out.println("各步骤计算时间:");
        System.out.println("读取数据时间：" + timeInitData / 1000 + " s");
        System.out.println("考虑载重限制的初始化集群计算用时：" + timeInitGroup + " ms");
        System.out.println("考虑时间限制的计算用时：" + timeGetInitLines / 1000 + " s");
        System.out.println("考虑二次运输的计算用时：" + timeTwiceLine / 1000 + " s");
        System.out.println("考虑用时平衡(不减少车辆数)的计算用时：" + timeBalance / 1000 + " s");
    }

    public JSONObject getVrpResult() {
        JSONObject json = new JSONObject();

        NumberFormat nf = NumberFormat.getNumberInstance();
        // 保留两位小数
        nf.setMaximumFractionDigits(2);
        // 四舍五入
        nf.setRoundingMode(RoundingMode.UP);

        //各项步骤计算时间
        json.put("timeInitData", timeInitData);
        json.put("timeInitGroup", timeInitGroup);
        json.put("timeGetInitLines", timeGetInitLines);
        json.put("timeTwiceLine", timeTwiceLine);
        json.put("timeBalance", timeBalance);
        json.put("timeAllSteps", timeInitData + timeInitGroup + timeGetInitLines + timeTwiceLine + timeBalance);

        //运送路线
        JSONObject route = new JSONObject();
        for (Integer i : matrixAll.existLineNO) {
            JSONObject singleRoute = new JSONObject();
            singleRoute.put("timeAll", nf.format((matrixAll.timeMap.get(i) + matrixAll.timeMapTwice.get(i)) / 3600));
            singleRoute.put("distanceAll", nf.format(matrixAll.distanceMap.get(i) + matrixAll.distanceTwice.get(i)));
            singleRoute.put("weightOnce", nf.format(matrixAll.weightMap.get(i)));
            singleRoute.put("carLimitWeight", nf.format(carLimitWeightArr[matrixAll.chooseCar.get(i)]));
            singleRoute.put("timeOnce", nf.format(matrixAll.timeMap.get(i) / 3600));
            singleRoute.put("distanceOnce", nf.format(matrixAll.distanceMap.get(i)));

            List<String> arrayOnce = new ArrayList<>();
            arrayOnce.add(customerIds[0]);
            for (Integer j : matrixAll.matrix.get(i)) {
                arrayOnce.add(customerIds[j]);
            }
            arrayOnce.add(customerIds[0]);

            singleRoute.put("arrayOnce", arrayOnce);


            if (matrixAll.matrixTwice.get(i).size() > 0) {
                singleRoute.put("weightTwice", nf.format(matrixAll.weightMapTwice.get(i)));
                singleRoute.put("timeTwice", nf.format(matrixAll.timeMapTwice.get(i) / 3600));
                singleRoute.put("distanceTwice", nf.format(matrixAll.distanceTwice.get(i)));

                List<String> arrayTwice = new ArrayList<>();
                arrayTwice.add(customerIds[0]);
                for (Integer j : matrixAll.matrixTwice.get(i)) {
                    arrayTwice.add(customerIds[j]);
                }
                arrayTwice.add(customerIds[0]);

                singleRoute.put("arrayTwice", arrayTwice);
            }

            route.put(i + "", singleRoute);
        }
        json.put("route", route);

        //车种类信息
        JSONObject carType = new JSONObject();
        for (int i = 0; i < carTypeNum; i++) {
            carType.put(carLimitWeightArr[i] + "", carUsedNumArr[i]);
        }
        json.put("carType", carType);

        //点信息
        JSONObject pointX = new JSONObject();
        JSONObject pointY = new JSONObject();
        JSONObject pointWeight = new JSONObject();
        JSONObject pointWaitTime = new JSONObject();
        for (int i = 0; i < customerNum + 1; i++) {
            pointX.put(customerIds[i], customerGpsX[i]);
            pointY.put(customerIds[i], customerGpsY[i]);
            pointWeight.put(customerIds[i], nf.format(weightArr[i]));
            pointWaitTime.put(customerIds[i], nf.format(waitTimeArr[i]));
        }
        json.put("pointX", pointX);
        json.put("pointY", pointY);
        json.put("pointWeight", pointWeight);
        json.put("pointWaitTime", pointWaitTime);

        //其他信息
        json.put("carNum", matrixAll.carNum);
        json.put("costAll", matrixAll.costAll);
        json.put("timeAll", nf.format(matrixAll.timeAll / 3600));
        json.put("distanceAll", nf.format(matrixAll.distanceAll));
        json.put("balance", nf.format(matrixAll.balance / 60));
        json.put("isCarEnough", confirmCarNum());

        return json;
    }


    public class PointToPoint implements Cloneable {
        public int fromNo;
        public int toNo;

        public double time;
        public double distance;

        public PointToPoint(int fromNo, int toNo, double time, double distance) {
            this.fromNo = fromNo;
            this.toNo = toNo;
            this.time = time;
            this.distance = distance;
        }

        @Override
        public PointToPoint clone() {
            PointToPoint p = null;
            try {
                p = (PointToPoint) super.clone();
            } catch (CloneNotSupportedException e) {
                System.out.println(e.toString());
            }
            return p;
        }
    }

    public class Line {
        public String fromNo;
        public String toNo;

        public double time;

        public Line(String fromNo, String toNo, double time) {
            this.fromNo = fromNo;
            this.toNo = toNo;
            this.time = time;
        }
    }
}
