package com.rubbishclassification.utils;

import org.opencv.core.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
public class ArrayUtils {

	@Value("${utils.classification.threshold}")
	public  Float CLASSIFICATION_THRESHOLD;
	@Value("${utils.yolo.threshold}")
	public  Float YOLO_THRESHOLD;
	
	public long getType(float[][] result) {
		
		float sum = 0;
		
		float max_conf = 0;
		
		for (int i = 0; i < result[0].length; i++) {
			if (max_conf < result[0][i]) {
				max_conf = result[0][i];
			}
		}
		
		for (float x : result[0]) {
			sum += Math.exp(x - max_conf);
		}
		
		float max = 0;
		int type = -1;
		
		for (int i = 0; i < result[0].length; i++) {
			float ratio = (float) (Math.exp((result[0][i] - max_conf)) / sum);
			if (max < ratio && ratio > CLASSIFICATION_THRESHOLD) {
				max = ratio;
				type = i;
			}
		}
		System.out.println("置信度:" + max);
		return type;
	}
	
	// 标准化
	public static float[][][][] normalize(Mat image, float[][][][] result) {
		/**
		 *  v1.0：目前仅支持一张图片的标准化
		 *  return: 标准化后的图片
		 *  time: 2022.3.22
		 */
		int size = image.cols();
		float[] std = new float[]{(float) 0.5, (float) 0.5, (float) 0.5};
		float[] mean = new float[]{(float) 0.5, (float) 0.5, (float) 0.5};
		image.convertTo(image, CvType.CV_32F, 1.f / 255.f); // 转为无符号的8位，可以试试不转
		float[] floats = new float[image.cols() * image.rows() * image.channels()];
		image.get(0, 0, floats);
		
		for (int j = 0; j < image.rows(); ++j) {
			for (int i = 0; i < image.cols(); ++i) {
				result[0][0][j][i] = (floats[j * size * 3 + i * 3] - mean[0]) / std[0];
				result[0][1][j][i] = (floats[j * size * 3 + i * 3 + 1] - mean[1]) / std[1];
				result[0][2][j][i] = (floats[j * size * 3 + i * 3 + 2] - mean[2]) / std[2];
			}
		}
		
		return result;
	}
	
	// 从大到小   优化->归并  TODO
	public static void InsertSort(float[] array, ArrayList<Integer> order) {
		for (int i = 0; i < array.length; i++) {
			for (int j = i; j > 0; --j) {
				if (array[j] > array[j - 1]) {
					float temp = array[j];
					array[j] = array[j - 1];
					array[j - 1] = temp;
					
					order.set(j, order.get(j) ^ order.get(j - 1));
					order.set(j - 1, order.get(j) ^ order.get(j - 1));
					order.set(j, order.get(j) ^ order.get(j - 1));
				}
			}
		}
	}
	
	public static void nms(ArrayList<float[]> box, ArrayList<Integer> keep, float[] scores, float iou_thres) {
		float[] area = new float[box.size()];
		
		for (int i = 0; i < box.size(); ++i) {
			area[i] = (box.get(i)[2] - box.get(i)[0] + 1) * (box.get(i)[3] - box.get(i)[1] + 1);
		}
		
		ArrayList<Integer> order = new ArrayList<>();
		
		for (int i = 0; i < box.size(); i++) {
			order.add(i);
		}
		
		InsertSort(scores, order);
		
		while (order.size() > 0) {
			
			int MaxScoreIndexInArea;
			
			if (order.size() == 1) {
				keep.add(order.get(0));
				break;
			} else {
				MaxScoreIndexInArea = order.get(0);
				order.remove(0);
				keep.add(MaxScoreIndexInArea);
			}
			
			ArrayList<Integer> IouthresUp = new ArrayList<>();
			for (int i = 0; i < order.size(); ++i) {
				float xx1 = Math.max(box.get(order.get(i))[0], box.get(MaxScoreIndexInArea)[0]);
				float yy1 = Math.max(box.get(order.get(i))[1], box.get(MaxScoreIndexInArea)[1]);
				float xx2 = Math.min(box.get(order.get(i))[2], box.get(MaxScoreIndexInArea)[2]);
				float yy2 = Math.min(box.get(order.get(i))[3], box.get(MaxScoreIndexInArea)[3]);
				
				float inter_area = Math.max(0.f, xx2 - xx1) * Math.max(0.f, yy2 - yy1);
				float iou = inter_area / (area[order.get(i)] + area[MaxScoreIndexInArea] - inter_area);
				if (iou > iou_thres) {
					IouthresUp.add(order.get(i));
				}
			}
			for (Integer integer : IouthresUp) {
				order.remove(integer);
			}
		}
	}
	
	public ArrayList<float[]> non_max_suppression(float[][][] pred) throws Exception {
		/**
		 * v1.0：仅支持一张图片!
		 * input: (batch_size, 25500, cls+5) -> batch_size=1
		 * time: 2022.3.22
		 */
		float conf_thres = YOLO_THRESHOLD;
		float iou_thres = 0.45f;
		float min_wh = 2.f, max_wh = 7680.f;
		
		ArrayList<float[]> Anchors = new ArrayList<>();
		
		for (int i = 0; i < pred[0].length; i++) {
			if (pred[0][i][4] > conf_thres && pred[0][i][2] > min_wh && pred[0][i][3] > min_wh &&
					pred[0][i][2] < max_wh && pred[0][i][3] < max_wh) {
				Anchors.add(pred[0][i]);
			}
		}
		
		// 需要反馈信息，没有方框 --> 不存在物体
		// 或者说是报exception... TODO
		if (Anchors.size() == 0) {
			throw new Exception("无法检测到物体");
		}
		
		int AnchorSize = Anchors.get(0).length;
		for (int i = 0; i < Anchors.size(); i++) {
			float[] anchor = new float[AnchorSize];
			for (int j = 0; j < AnchorSize; j++) {
				if (j < 5) anchor[j] = Anchors.get(i)[j];
				else anchor[j] = Anchors.get(i)[j] * Anchors.get(i)[4];
			}
			Anchors.set(i, anchor);
		}
		// 将坐标还原
		ArrayList<float[]> Box = new ArrayList<>();
		for (int i = 0; i < Anchors.size(); ++i) {
			float top_x = Anchors.get(i)[0] - Anchors.get(i)[2] / 2.f;
			float top_y = Anchors.get(i)[1] - Anchors.get(i)[3] / 2.f;
			float bottom_x = Anchors.get(i)[0] + Anchors.get(i)[2] / 2.f;
			float bottom_y = Anchors.get(i)[1] + Anchors.get(i)[3] / 2.f;
			Box.add(i, new float[]{top_x, top_y, bottom_x, bottom_y});
		}
		// 初始化0，小于所有conf的值！
		// 每个方框的最大置信度
		float[] MaxConf = new float[Anchors.size()];
		float[] cls_number = new float[Anchors.size()];
		for (int i = 0; i < Anchors.size(); i++) {
			for (int j = 5; j < AnchorSize; ++j) {
				if (Anchors.get(i)[j] > MaxConf[i]) {
					MaxConf[i] = Anchors.get(i)[j];
					cls_number[i] = j - 5; // 实际类别 --> 0-27
				}
			}
		}
		// concatenate
		ArrayList<float[]> output = new ArrayList<>();
		for (int i = 0; i < Anchors.size(); i++) {
			float[] BoxConfCls = new float[6]; // 6 = bbox size + conf + index
			System.arraycopy(Box.get(i), 0, BoxConfCls, 0, 4);
			BoxConfCls[4] = MaxConf[i];
			BoxConfCls[5] = cls_number[i];
			output.add(BoxConfCls);
		}
		
		ArrayList<Integer> keep = new ArrayList<>();
		nms(Box, keep, MaxConf.clone(), iou_thres);
		ArrayList<float[]> output_residual = new ArrayList<>();
		for (Integer anchor_index : keep) {
			output_residual.add(output.get(anchor_index));
		}
		return output_residual;
	}
	
	public static void ToOne(Mat image, float[][][][] result) {
		int size = image.cols();
		int channel = image.channels();
		image.convertTo(image, CvType.CV_32F, 1.f / 255.f); // 转为无符号的8位，可以试试不转
		float[] floats = new float[image.cols() * image.rows() * image.channels()];
		image.get(0, 0, floats);
		// 假定从0开始
		for (int j = 0; j < image.rows(); ++j) {
			for (int i = 0; i < image.cols(); ++i) {
				result[0][0][j][i] = floats[j * size * channel + i * channel];
				result[0][1][j][i] = floats[j * size * channel + i * channel + 1];
				result[0][2][j][i] = floats[j * size * channel + i * channel + 2];
			}
		}
	}
	
	public static void ReturnToOriginalPic(int[] ImageOriginalShape, int[] PadShape, ArrayList<float[]> output) { // (height, weight)
		float gain = Math.min((float) PadShape[0] / (float) ImageOriginalShape[0], (float) PadShape[0] / (float) ImageOriginalShape[0]);
		float[] pad = new float[]{((float) PadShape[1] - (float) ImageOriginalShape[1] * gain) / 2.f,
				((float) PadShape[0] - (float) ImageOriginalShape[0] * gain) / 2.f};
		for (int i = 0; i < output.size(); ++i) {
			float[] originalBox = new float[6];
			originalBox[0] = Math.min((float) ImageOriginalShape[1], Math.max(0, (output.get(i)[0] - pad[0]) / gain));
			originalBox[1] = Math.min((float) ImageOriginalShape[0], Math.max(0, (output.get(i)[1] - pad[1]) / gain));
			originalBox[2] = Math.min((float) ImageOriginalShape[1], Math.max(0, (output.get(i)[2] - pad[0]) / gain));
			originalBox[3] = Math.min((float) ImageOriginalShape[0], Math.max(0, (output.get(i)[3] - pad[1]) / gain));
			originalBox[4] = output.get(i)[4];
			originalBox[5] = output.get(i)[5];
			output.set(i, originalBox);
		}
	}
	
}
