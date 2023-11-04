package com.rubbishclassification.controller;

import com.rubbishclassification.entity.Label;
import com.rubbishclassification.service.LabService;
import com.rubbishclassification.service.RubbishService;
import com.rubbishclassification.service.TypeService;
import com.rubbishclassification.utils.ArrayUtils;
import com.rubbishclassification.utils.PicProcessor;
import com.rubbishclassification.utils.Predictor;
import com.rubbishclassification.utils.Response;
import org.opencv.core.*;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.*;
import java.util.List;

/**
 * @program: RubbishClassification
 * @description:
 * @author: ZXY
 * @create: 2022-03-03 21:04
 **/
@RestController
@RequestMapping("/api/v1")
public class RubbishController {
	
	@Autowired
	private RubbishService rubbishService;
	@Autowired
	private TypeService typeService;
	@Autowired
	private Predictor predictor;
	@Autowired
	private LabService labService;
	@Value("${predictor.yolo-pic-size}")
	private int YOLO_PIC_SIZE;
	
	@GetMapping("/search")
	public Response getTypeByRubbishNameAndCity(String city, String kw) throws Exception {
		
		if (city == null || kw == null || kw.equals("")) {
			throw new Exception("参数不合法");
		}
		
		List<TypeService.Result> results = typeService.getTypesByCityAndRubbishName(city, kw);
		
		if (results.isEmpty()) {
			throw new Exception("很抱歉，没有找到相应的垃圾");
		}
		
		return Response.success("查询成功", results);
	}
	
	@PostMapping("/cf")
	public Response classifyRubbishPic(@RequestParam("file") String file, @RequestParam("city") String city) throws Exception {
		System.out.println("cf————————————————————————");
		String type = file_check(file);
		byte[] bytes = getFileBytes(file);
		//正式上线删除
//		savePic(bytes, type);
		Mat mat = getInput(bytes);
		Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);
		long rubbish_id = predictor.classification(mat);
		if (rubbish_id == -1) {
			throw new Exception("检测失败");
		}
		long s = System.currentTimeMillis();
		TypeService.TypeAndRubbish typeAndRubbish = typeService.getTypeByCityAndRubbishID(city, rubbish_id);
		System.out.println("sql:" + (System.currentTimeMillis() - s));
		System.out.println("———————————————————————————");
		return Response.success("检测成功", typeAndRubbish);
	}
	
	@RequestMapping("/multi")
	public Response yolo_detect(@RequestParam("file") String file, @RequestParam("city") String city) throws Exception {
		System.out.println("yd————————————————————————");
		String type = file_check(file);
		byte[] bytes = getFileBytes(file);
		
		//正式上线删除
//		savePic(bytes, type);
		
		Mat mat = getInput(bytes);
		Mat img = mat.clone();
		Imgproc.cvtColor(img, img, Imgproc.COLOR_BGR2RGB);
		List<float[]> floats = getLocation(img);
		if (floats.size() > 6) {
			floats =  floats.subList(0, 6);
		}
		List<PicProcessor.Pic> pics = new PicProcessor().getPicsFromMat(mat, floats, type);
		if (pics.isEmpty()) {
			throw new Exception("检测失败");
		}
		for (int i = 0; i < pics.size(); i++) {
			PicProcessor.Pic p = pics.get(i);
			Optional<Label> optionalLabel = labService.getLabel((long) floats.get(i)[5]);
			if (optionalLabel.isPresent()) {
				Label label = optionalLabel.get();
				p.setName(label.getName());
				p.setType(label.getType());
			} else {
				throw new Exception("种类不存在");
			}
		}
		System.out.println("————————————————————————————");
		return Response.success("检测成功", pics);
	}

	public void savePic(byte[] bytes, String fileType) throws IOException {
		
		File f = new File("/root/rc/pic/" + UUID.randomUUID() + "-" + "." + fileType);
		f.createNewFile();
		System.out.println(f.getName());
		FileOutputStream fileOutputStream = new FileOutputStream(f);
		fileOutputStream.write(bytes);
		fileOutputStream.close();
	}
	
	public ArrayList<float[]> getLocation(Mat mat) throws Exception {
		
		int[] shape = new int[]{mat.rows(), mat.cols()};
		int[] padShape = new int[]{YOLO_PIC_SIZE, YOLO_PIC_SIZE};
		ArrayList<float[]> floats = predictor.yoloDetect(mat);
		ArrayUtils.ReturnToOriginalPic(shape, padShape, floats);
		return floats;
	}
	
	public String file_check(String file) throws Exception {
		if (file == null) {
			throw new Exception("文件传输丢失");
		}
		
		String fileType = file.substring(file.indexOf("/") + 1, file.indexOf(";")).toLowerCase(Locale.ROOT);
		
		if (!fileType.equals("jpg")
				&& !fileType.equals("jpeg")
				&& !fileType.equals("png")
				&& !fileType.equals("bmp")) {
			throw new Exception("图片格式错误");
		}
		return fileType;
	}
	
	public byte[] getFileBytes(String file) {
		file = file.substring(file.indexOf(",") + 1);
		byte[] bytes = Base64.getDecoder().decode(file);
		return bytes;
	}
	
	public Mat getInput(byte[] bytes) {
		MatOfByte matb = new MatOfByte(bytes);
		Mat img = Imgcodecs.imdecode(matb, Imgcodecs.IMREAD_COLOR);
		return img;
	}
}
