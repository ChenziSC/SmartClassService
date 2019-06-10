package dao;

import domain.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface TypeDao {
    public void setTypeByTypeName(Type type);

    public Type getTypeByTypeName(Type type);
}
