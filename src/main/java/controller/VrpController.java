package controller;

import com.alibaba.fastjson.JSONObject;
import domain.*;
import service.*;
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


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping(value = "/VrpController")
public class VrpController {
    public static final String SUCCESS = "success";
    public static final String FAIL = "fail";

    @Autowired
    private VrpService vrpService;

    @RequestMapping(value = "/getVrpResult", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject getVrpResult(@RequestBody Vrp vrp) {
        try {
            return vrpService.getVrpResult(vrp);
        } catch (Exception e) {
            JSONObject fail = new JSONObject();
            fail.put("fail", e.toString());
            return fail;
        }
    }


}
