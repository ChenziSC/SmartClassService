package service;

import dao.*;
import domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.lang.model.element.TypeElement;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;


@Service
public class TypeService {

    @Autowired
    private TypeDao typeDao;

    public void setTypeByTypeName(Type type){
        typeDao.setTypeByTypeName(type);
    }

    public Type getTypeByTypeName(Type type){
        Type type1 = typeDao.getTypeByTypeName(type);
        return type1;
    }


}
