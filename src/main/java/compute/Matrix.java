package compute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

public class Matrix implements Cloneable {
    public HashSet<Integer> existLineNO = new HashSet<>();

    public HashMap<Integer, List<Integer>> matrix;
    public HashMap<Integer, Double> timeMap;
    public HashMap<Integer, Double> distanceMap;
    public HashMap<Integer, Double> weightMap;
    public HashMap<Integer, Double> centerDisMap;
    public HashMap<Integer, Double[]> centerGPSMap;

    public HashMap<Integer, List<Integer>> matrixTwice;
    public HashMap<Integer, Double> timeMapTwice;
    public HashMap<Integer, Double> distanceTwice;
    public HashMap<Integer, Double> weightMapTwice;
    public HashMap<Integer, Double> centerDisMapTwice;
    public HashMap<Integer, Double[]> centerGPSMapTwice;

    public HashMap<Integer, Integer> chooseCar;

    public double costAll;
    public double timeAll;
    public double distanceAll;
    public double balance;
    public int carNum;

    public Matrix() {
        matrix = new HashMap<Integer, List<Integer>>();
        matrixTwice = new HashMap<Integer, List<Integer>>();
        existLineNO = new HashSet<Integer>();
        chooseCar = new HashMap<Integer, Integer>();
    }

    @Override
    public Matrix clone() {
        Matrix m = null;
        try {
            m = (Matrix) super.clone();
        } catch (CloneNotSupportedException e) {
            System.out.println(e.toString());
        }

        m.existLineNO = (HashSet<Integer>) existLineNO.clone();

        m.matrix = (HashMap<Integer, List<Integer>>) matrix.clone();
        m.timeMap = (HashMap<Integer, Double>) timeMap.clone();
        m.distanceMap = (HashMap<Integer, Double>) distanceMap.clone();
        m.weightMap = (HashMap<Integer, Double>) weightMap.clone();
        m.centerDisMap = (HashMap<Integer, Double>) centerDisMap.clone();
        m.centerGPSMap = (HashMap<Integer, Double[]>) centerGPSMap.clone();

        m.matrixTwice = (HashMap<Integer, List<Integer>>) matrixTwice.clone();
        m.timeMapTwice = (HashMap<Integer, Double>) timeMapTwice.clone();
        m.distanceTwice = (HashMap<Integer, Double>) distanceTwice.clone();
        m.weightMapTwice = (HashMap<Integer, Double>) weightMapTwice.clone();
        m.centerDisMapTwice = (HashMap<Integer, Double>) centerDisMapTwice.clone();
        m.centerGPSMapTwice = (HashMap<Integer, Double[]>) centerGPSMapTwice.clone();

        m.chooseCar = (HashMap<Integer, Integer>) chooseCar.clone();
        return m;
    }
}