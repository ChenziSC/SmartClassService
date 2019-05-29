package dao;

import domain.Surroundings;

import java.util.List;

public interface SurroundingsDao {
    /**
     * 获取环境数据数组
     *
     * @return
     */
    List<Surroundings> getAllSurroundings();


    /**
    * 添加最新环境数据
    *
    * @return
    */
    void addNewSurrounding(Surroundings surroundings);
}
