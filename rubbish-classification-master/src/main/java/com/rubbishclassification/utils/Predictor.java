package com.rubbishclassification.utils;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;
import org.opencv.core.*;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @program: RubbishClassification
 * @description:
 * @author: ZXY
 * @create: 2022-03-22 22:16
 **/

public class Predictor {

    public int CLASSIFICATION_PIC_SIZE;
    public int YOLO_PIC_SIZE;
    public int YOLO_PAD_VALUE;
    public int CLASSIFICATION_PAD_VALUE;
    private String classificationPath;
    private String yoloPath;

    private OrtEnvironment environment;
    private OrtSession classificationSession;
    private OrtSession yoloSession;

    @Autowired
    ArrayUtils arrayUtils;

    public long classification(Mat img) throws OrtException {
        Imgproc.resize(img, img, new Size(CLASSIFICATION_PIC_SIZE, CLASSIFICATION_PIC_SIZE));
        float[][][][] input = new float[1][3][CLASSIFICATION_PIC_SIZE][CLASSIFICATION_PIC_SIZE];
        long s = System.currentTimeMillis();
        ArrayUtils.normalize(img, input);
        System.out.println(new Date(System.currentTimeMillis()));
        System.out.println("normalize:" + (System.currentTimeMillis() - s));
        s = System.currentTimeMillis();
        OrtSession.Result results = getResult(img, input, "modelInput", classificationSession);
        System.out.println("onnx:" + (System.currentTimeMillis() - s));
        float[][] result = (float[][]) results.get(0).getValue();
        return arrayUtils.getType(result);
    }

    public OrtSession.Result getResult(Mat img, float[][][][] input, String name, OrtSession ortSession) throws OrtException {

        OnnxTensor array = OnnxTensor.createTensor(environment, input);
        HashMap<String, OnnxTensor> inputs = new HashMap<>();
        inputs.put(name, array);
        OrtSession.Result results = ortSession.run(inputs);

        return results;
    }

    public ArrayList<float[]> yoloDetect(Mat img) throws Exception {
        resize(img, YOLO_PIC_SIZE, YOLO_PAD_VALUE);
        float[][][][] input = new float[1][3][YOLO_PIC_SIZE][YOLO_PIC_SIZE];
        long s = System.currentTimeMillis();
        System.out.println(new Date(s));
        ArrayUtils.ToOne(img, input);

        System.out.println("toOne:" + (System.currentTimeMillis() - s));
        s = System.currentTimeMillis();
        OrtSession.Result results = getResult(img, input, "images", yoloSession);
        System.out.println("onnx:" + (System.currentTimeMillis() - s));
        float[][][] result = (float[][][]) results.get(0).getValue();
        s = System.currentTimeMillis();
        ArrayList<float[]> arrayList = arrayUtils.non_max_suppression(result);
        System.out.println("nms:" + (System.currentTimeMillis() - s));

        return arrayList;
    }

    public void resize(Mat img, int PIC_SIZE, int pad_value) {  // YOLO_PIC_SIZE: 640

        int width = img.cols();
        int height = img.rows();

        double scale_ratio = Math.min((double) PIC_SIZE / (double) width, (double) PIC_SIZE / (double) height);
        double[] NewUnpad = {(double) Math.round((double) width * scale_ratio), (double) Math.round((double) height * scale_ratio)};
        double dw = ((double) PIC_SIZE - NewUnpad[0]) / 2.f, dh = ((double) PIC_SIZE - NewUnpad[1]) / 2.f;

        Imgproc.resize(img, img, new Size(NewUnpad));

        int top = (int) Math.round(dh - 0.1), bottom = (int) Math.round(dh + 0.1);
        int left = (int) Math.round(dw - 0.1), right = (int) Math.round(dw + 0.1);

        Core.copyMakeBorder(img, img, top, bottom, left, right, Core.BORDER_CONSTANT, new Scalar(pad_value, pad_value, pad_value));
    }

    public int getCLASSIFICATION_PIC_SIZE() {
        return CLASSIFICATION_PIC_SIZE;
    }

    public void setCLASSIFICATION_PIC_SIZE(int CLASSIFICATION_PIC_SIZE) {
        this.CLASSIFICATION_PIC_SIZE = CLASSIFICATION_PIC_SIZE;
    }

    public int getYOLO_PIC_SIZE() {
        return YOLO_PIC_SIZE;
    }

    public void setYOLO_PIC_SIZE(int YOLO_PIC_SIZE) {
        this.YOLO_PIC_SIZE = YOLO_PIC_SIZE;
    }

    public int getYOLO_PAD_VALUE() {
        return YOLO_PAD_VALUE;
    }

    public void setYOLO_PAD_VALUE(int YOLO_PAD_VALUE) {
        this.YOLO_PAD_VALUE = YOLO_PAD_VALUE;
    }

    public int getCLASSIFICATION_PAD_VALUE() {
        return CLASSIFICATION_PAD_VALUE;
    }

    public void setCLASSIFICATION_PAD_VALUE(int CLASSIFICATION_PAD_VALUE) {
        this.CLASSIFICATION_PAD_VALUE = CLASSIFICATION_PAD_VALUE;
    }

    public String getClassificationPath() {
        return classificationPath;
    }

    public void setClassificationPath(String classificationPath) {
        this.classificationPath = classificationPath;
    }

    public String getYoloPath() {
        return yoloPath;
    }

    public void setYoloPath(String yoloPath) {
        this.yoloPath = yoloPath;
    }

    public OrtEnvironment getEnvironment() {
        return environment;
    }

    public void setEnvironment(OrtEnvironment environment) {
        this.environment = environment;
    }

    public OrtSession getClassificationSession() {
        return classificationSession;
    }

    public void setClassificationSession(OrtSession classificationSession) {
        this.classificationSession = classificationSession;
    }

    public OrtSession getYoloSession() {
        return yoloSession;
    }

    public void setYoloSession(OrtSession yoloSession) {
        this.yoloSession = yoloSession;
    }
}
