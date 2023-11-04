package com.rubbishclassification;

import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.rubbishclassification.controller.RubbishController;
import com.rubbishclassification.service.TypeService;
import org.junit.jupiter.api.Test;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.util.Base64Utils;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.Locale;

@SpringBootTest
class RubbishClassificationApplicationTests {

    @Test
    void contextLoads() throws IOException, OrtException {
    }

}
