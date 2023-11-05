package com.rubbishclassification.utils;

import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

/**
 * @program: RubbishClassification
 * @description:
 * @author: ZXY
 * @create: 2022-03-26 15:50
 **/
public class PicProcessor {
	
	public List<Mat> splitMat(Mat mat, List<float[]> floats) {
		ArrayList<Mat> mats = new ArrayList<>();
		for (float[] f : floats) {
			Mat m = mat.submat(Math.round(f[1]), Math.round(f[3]), Math.round(f[0]), Math.round(f[2]));
			mats.add(m);
		}
		
		return mats;
	}
	
	public byte[] getBytesFromMat(Mat mat, String type) {
		MatOfByte matOfByte = new MatOfByte();
		Imgcodecs.imencode("." + type, mat, matOfByte);
		return matOfByte.toArray();
	}
	
	public String getStringFromBytes(byte[] bytes, String type) {
		String s = "data:image/";
		s += type + ";base64,";
		s += Base64.getEncoder().encodeToString(bytes);
		return s;
	}
	
	public List<Pic> getPicsFromMat(Mat mat, List<float[]> floats, String type) {
		List<Mat> mats = splitMat(mat, floats);
		ArrayList<Pic> pics = new ArrayList<>();
		for (Mat m : mats) {
			byte[] bytes = getBytesFromMat(m, type);
			String s = getStringFromBytes(bytes, type);
			Pic p = new Pic(s);
			pics.add(p);
		}
		return pics;
	}
	
	public class Pic {
		String name;
		String type;
		String pic;
		
		public Pic(String pic) {
			this.pic = pic;
		}
		
		public String getName() {
			return name;
		}
		
		public void setName(String name) {
			this.name = name;
		}
		
		public String getType() {
			return type;
		}
		
		public void setType(String type) {
			this.type = type;
		}
		
		public String getPic() {
			return pic;
		}
		
		public void setPic(String pic) {
			this.pic = pic;
		}
		
	}
}
