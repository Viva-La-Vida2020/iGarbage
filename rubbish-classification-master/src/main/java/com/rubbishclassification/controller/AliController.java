package com.rubbishclassification.controller;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.imagerecog20190930.Client;
import com.aliyun.imagerecog20190930.models.ClassifyingRubbishAdvanceRequest;
import com.aliyun.imagerecog20190930.models.ClassifyingRubbishResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teautil.models.RuntimeOptions;
import com.rubbishclassification.entity.Rubbish;
import com.rubbishclassification.entity.Type;
import com.rubbishclassification.service.RubbishService;
import com.rubbishclassification.service.TypeService;
import com.rubbishclassification.utils.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.util.Base64;
import java.util.HashMap;

@Controller
@RequestMapping("/api/v1/ali")
public class AliController {
    @Autowired
    Client client;
    @Autowired
    RuntimeOptions runtimeOptions;
    @Autowired
    TypeService typeService;
    @Autowired
    RubbishService rubbishService;

    @PostMapping("/classification")
    @ResponseBody
    public Response AliClassification(@RequestParam("file") String file, @RequestParam("city") String city) throws Exception {

        file = file.substring(file.indexOf(",") + 1);
        byte[] bytes = Base64.getDecoder().decode(file);
        InputStream inputStream = new ByteArrayInputStream(bytes);
        ClassifyingRubbishAdvanceRequest req = new ClassifyingRubbishAdvanceRequest();
        req.imageURLObject = inputStream;
        ClassifyingRubbishResponse rep = client.classifyingRubbishAdvance(req, runtimeOptions);
        TypeService.TypeAndRubbish typeAndRubbish = getResult(rep, city);

        if (typeAndRubbish != null) {
            return Response.success("检测成功", typeAndRubbish);
        }
        return Response.fail("检测失败");

    }

    public TypeService.TypeAndRubbish getResult(ClassifyingRubbishResponse classifyingRubbishResponse, String city) throws Exception {
        String s = JSON.toJSONString(classifyingRubbishResponse);
        String name = s.substring(s.indexOf("\"rubbish\":\"") + 11, s.indexOf("\"", s.indexOf("\"rubbish\":\"") + 11));
        if (!name.equals("")) {
            TypeService.TypeAndRubbish typeAndRubbish = typeService.findTypeAndRubbishByCityAndRubbishName(city, name);
            return typeAndRubbish;
        } else {
            return null;
        }
    }
}
