package controller;

import domain.*;
import net.sf.json.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import service.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping(value = "/DeviceController")
public class DeviceController {
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";

    @Autowired
    private DeviceService deviceService;

    @RequestMapping(value = "/getDeviceByType", method = RequestMethod.POST)
    @ResponseBody
    public List<Device> getDeviceByType(@RequestBody Device device) {
        return deviceService.getDeviceByType(device.getType());

    }

    @RequestMapping(value = "/getAllDeviceType", method = RequestMethod.POST)
    @ResponseBody
    public List<Type> getAllDeviceType(@RequestBody Device device) {
        return deviceService.getAllDeviceType();

    }

    @RequestMapping(value = "/updateDeviceIsOpenById", method = RequestMethod.POST)
    @ResponseBody
    public String updateDeviceIsOpenById(@RequestBody Device device) {
        try {
            deviceService.updateDeviceIsOpenById(device);
            return SUCCESS;
        } catch (Exception e) {
            return FAIL;
        }
    }
}

