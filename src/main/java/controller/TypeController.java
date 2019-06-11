package controller;


import domain.Type;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import service.TypeService;


@Controller
@RequestMapping(value = "/TypeController")
public class TypeController {
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";

    @Autowired
    private TypeService typeService;
    @Autowired
    public QuartzManagerDevices quartzManagerDevices;

    @RequestMapping(value = "/setTypeByTypeName", method = RequestMethod.POST)
    @ResponseBody
    public String setTypeByTypeName(@RequestBody Type type) {
        try {
            typeService.setTypeByTypeName(type);

            //定时器
            quartzManagerDevices.setDeviceTimer(type);

            return SUCCESS;
        } catch (Exception e) {
            return FAIL;
        }
    }

    @RequestMapping(value = "/getTypeByTypeName", method = RequestMethod.POST)
    @ResponseBody
    public Type getTypeByTypeName(@RequestBody Type type) {
        return typeService.getTypeByTypeName(type);
    }
}