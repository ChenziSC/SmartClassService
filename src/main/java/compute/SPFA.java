package compute;

import java.util.*;

public class SPFA {
    public static final int maxLineNum = 10000000;
    public static final int maxPointNum = 100000;
    public static final int maxCustomerNum = 100000;


    public A[] a = new A[maxLineNum];

    //SPFS数组，vis记录最短路，ven记录是否在队列
    double[] visTime = new double[maxPointNum];//时间
    boolean[] ven = new boolean[maxPointNum];

    //链式前向星数组 pre同head，表示以i为起点的第一条边存储的位置（最后输入的位置）
    int[] pre = new int[maxPointNum];

    int cent = 0;

    //链式前向星，加入节点
    void add(int x, int y, double time) {
        a[cent] = new A();
        a[cent].y = y;
        a[cent].time = time;
        a[cent].next = pre[x];
        pre[x] = cent++;
    }

    double[] getSingleRes(int s) {
        Queue<Integer> q = new LinkedList<Integer>();
        Arrays.fill(visTime, 999999999);
        Arrays.fill(ven, false);
        visTime[s] = 0;//初始化距离
        ven[s] = true;//标记s节点在队列
        q.offer(s);
        while (!q.isEmpty()) {
            int x = q.poll();//出队
            ven[x] = false;//标记不在队列
            for (int i = pre[x]; i >= 0; i = a[i].next) {//遍历与x节点连通的点
                int y = a[i].y;

                if (visTime[y] > visTime[x] + a[i].time) {//更新
                    visTime[y] = visTime[x] + a[i].time;
                    if (!ven[y]) {
                        //由于更新了节点，所以后续以这个为基础的最短路，也要更新下
                        //所以如果在队列就不用加入，不在的话加入更新后续节点
                        q.offer(y);
                        ven[y] = true;//标记这个节点在队列中
                    }
                }
            }
        }
        return visTime;
    }

    public double[][] getResult(HashMap<String, Integer> customerIndexMap, String[] customerIds, List<vrpSolution.Line> line) {
        int customerNum = customerIds.length;
        double[][] res = new double[customerNum][customerNum];

        int index = customerNum;

        Arrays.fill(pre, -1);

        for (vrpSolution.Line currentLine : line) {
            if (!customerIndexMap.containsKey(currentLine.fromNo)) {
                customerIndexMap.put(currentLine.fromNo, index);
                index++;
            }
            if (!customerIndexMap.containsKey(currentLine.toNo)) {
                customerIndexMap.put(currentLine.toNo, index);
                index++;
            }

            add(customerIndexMap.get(currentLine.fromNo), customerIndexMap.get(currentLine.toNo), currentLine.time);
        }

        for (int i = 0; i < customerNum; i++) {
            getSingleRes(customerIndexMap.get(customerIds[i]));
            for (int j = 0; j < customerNum; j++) {
                res[customerIndexMap.get(customerIds[i])][customerIndexMap.get(customerIds[j])]
                        = visTime[customerIndexMap.get(customerIds[j])];
            }
        }

        return res;
    }


    public class A {
        //y:这条边的终点；time边权重；next与这条边同起点的下条边的存储位置
        public int y;
        public int next;
        public double time;//边的权重
    }

}
