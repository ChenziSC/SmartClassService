package service;

import dao.SurroundingsDao;
import domain.Surroundings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

@Service
public class SurroundingsService {

    @Autowired
    private SurroundingsDao surroundingsDao;

    public void addSurroudings() {
        List<Surroundings> surroundings = surroundingsDao.getAllSurroundings();
        Surroundings last = surroundings.get(0);
        double light = last.getLight();
        int temperature = last.getTemperature();
        double smoke = last.getSmoke();

        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        last.setCreateTime(df.format(new Date()));
        Random ra = new Random();
        int addOrCut = ra.nextInt(2);
        if (addOrCut == 0) {
            light = light + (0 + Math.random() * (3 - 0 + 1));
            temperature = temperature + (int) (0 + Math.random() * (3 - 0 + 1));
            smoke = smoke + (0 + Math.random() * (3 - 0 + 1));
        } else {
            light = light - (0 + Math.random() * (3 - 0 + 1));
            temperature = temperature - (int) (0 + Math.random() * (3 - 0 + 1));
            smoke = smoke - (0 + Math.random() * (3 - 0 + 1));
        }
        last.setLight(light);
        last.setTemperature(temperature);
        last.setSmoke(smoke);

        surroundingsDao.addNewSurrounding(last);
    }
}
