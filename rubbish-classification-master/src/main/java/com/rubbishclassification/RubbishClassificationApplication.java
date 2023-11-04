package com.rubbishclassification;

import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import com.aliyun.imagerecog20190930.*;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.models.RuntimeOptions;
import com.rubbishclassification.utils.Predictor;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.tomcat.util.descriptor.web.SecurityCollection;
import org.apache.tomcat.util.descriptor.web.SecurityConstraint;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.context.annotation.Bean;

import java.io.IOException;

@SpringBootApplication
public class RubbishClassificationApplication {

    @Value("${predictor.classification-pic-size}")
    public int CLASSIFICATION_PIC_SIZE;
    @Value("${predictor.yolo-pic-size}")
    public int YOLO_PIC_SIZE;
    @Value("${predictor.yolo-pad-value}")
    public int YOLO_PAD_VALUE;
    @Value("${predictor.classification-pad-value}")
    public int CLASSIFICATION_PAD_VALUE;
    @Value("${predictor.model.classification.path}")
    private String classificationPath;
    @Value("${predictor.model.yolo.path}")
    private String yoloPath;

    public static void main(String[] args) {
        System.load("D:\\NUS\\5002\\rubbish-classification-master\\src\\main\\resources\\opencv_java480.dll");
//        System.load("/usr/local/share/java/opencv4/libopencv_java455.so");
        SpringApplication.run(RubbishClassificationApplication.class, args);
    }

    @Bean
    public TomcatServletWebServerFactory servletContainer() { //springboot2

        TomcatServletWebServerFactory tomcat = new TomcatServletWebServerFactory() {

            @Override
            protected void postProcessContext(Context context) {

                SecurityConstraint securityConstraint = new SecurityConstraint();
                securityConstraint.setUserConstraint("CONFIDENTIAL");
                SecurityCollection collection = new SecurityCollection();
                collection.addPattern("/*");
                securityConstraint.addCollection(collection);
                context.addConstraint(securityConstraint);
            }
        };
        tomcat.addAdditionalTomcatConnectors(initiateHttpConnector());
        return tomcat;
    }

    private Connector initiateHttpConnector() {
        Connector connector = new Connector("org.apache.coyote.http11.Http11NioProtocol");
        connector.setScheme("http");
        connector.setPort(8080);
        connector.setSecure(false);
        connector.setRedirectPort(8443);
        return connector;
    }

    @Bean
    public Predictor getPredictor() throws OrtException {
        Predictor predictor = new Predictor();

        OrtEnvironment environment = OrtEnvironment.getEnvironment();
//        OrtSession.SessionOptions sessionOptions = new OrtSession.SessionOptions();

        int gpuDeviceId = 0;
        var sessionOptions = new OrtSession.SessionOptions();
//        System.out.println("test:" + sessionOptions);
//        sessionOptions.addCUDA(gpuDeviceId);

        var session_class = environment.createSession(classificationPath, sessionOptions);
        var session_yolo = environment.createSession(yoloPath, sessionOptions);

        sessionOptions.setInterOpNumThreads(8);
        predictor.setClassificationSession(session_class);
        predictor.setYoloSession(session_yolo);
        predictor.setEnvironment(environment);

        predictor.setCLASSIFICATION_PAD_VALUE(CLASSIFICATION_PAD_VALUE);
        predictor.setCLASSIFICATION_PIC_SIZE(CLASSIFICATION_PIC_SIZE);
        predictor.setYOLO_PIC_SIZE(YOLO_PIC_SIZE);
        predictor.setYOLO_PAD_VALUE(YOLO_PAD_VALUE);

        return predictor;
    }

    @Bean
    public Client getAliClient() throws Exception {
        Config config = new Config();
        config.accessKeyId = "LTAI5tRuMUzWETVahfYg4Vyx";
        config.accessKeySecret = "o9d5qqRH9NQQbH2MIRGL5VQHHUFsay";
        config.type = "access_key";
        config.regionId = "cn-shanghai";
//        config.endpointType = "internal";  //默认通过公网访问OSS，如需通过内网请打开这一行
        Client client = new Client(config);
        return client;
    }

    @Bean
    public RuntimeOptions getRuntimeOptions() {
        return new RuntimeOptions();
    }
}
