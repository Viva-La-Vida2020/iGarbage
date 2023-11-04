import cv2
import base64
import time
import requests
from collections import Counter
import urllib3
# from PIL import Image
# from io import BytesIO
# import subprocess
# import json

# 禁用警告
urllib3.disable_warnings()

# # 获得输入
# def get_input(request):
#     return video_path


# 调用Java接口
def call_java_pic(file, city='上海市'):

    # 接口URL
    url = 'https://localhost:8443/api/v1/multi'
    # 请求参数
    data = {
        'file': file,
        'city': city
    }
    try:
        response = requests.post(url, data=data, verify=False)
        response.raise_for_status()
        data = response.json()
        if 'data' in data and data['data'] is not None and len(data['data']) > 0:
            result = {'name': data['data'][0]['name'], 'type': data['data'][0]['type']}
        else:
            result = data['message']

        return result
    #     # 处理响应数据
    #     if data['code'] == 1:
    #         results = data['data']
    #         return {"检测成功": results}
    #     else:
    #         return "检测失败"
    #
    except requests.exceptions.RequestException as e:
        return {"请求发生错误": e}

    except ValueError as e:
        return {"无法解析JSON响应": e}


# 投票
def vote_logic(input_list):
    if not input_list:
        return None

    # 将字典的字符串表示作为键，然后进行计数
    element_count = Counter(str(element) for element in input_list)
    most_common_elements = element_count.most_common()

    # 找到出现次数最多的元素
    max_count = most_common_elements[0][1]
    majority_elements = [eval(element) for element, count in most_common_elements if count == max_count]

    return majority_elements


# 分类
def classify(video_path):
    start_time = time.time()

    # 读取视频文件
    cap = cv2.VideoCapture(video_path)
    frames = []

    while True:
        ret, frame = cap.read()
        if not ret:
            break
        frames.append(frame)

    cap.release()

    base64_frames = []

    for frame in frames:
        head = 'data:image/jpeg;base64,'
        (pic, buffer) = cv2.imencode('.jpg', frame)
        base64_data = base64.b64encode(buffer)
        base64_data = base64_data.decode('utf-8')
        base64_data_final = head + base64_data
        base64_frames.append(base64_data_final)

    # 进行检测和投票
    detection_results = []

    for base64_frame in base64_frames:
        category = call_java_pic(file=base64_frame)
        detection_results.append(category)

    # 投票
    final_result = vote_logic(detection_results)

    end_time = time.time()
    run_time = end_time - start_time
    print(str(run_time)+'s')

    # 输出最终结果
    print("Output:", final_result[0])
    return final_result[0]

