from flask import Flask, request, jsonify
import base64
import numpy as np
import cv2
import time
import requests
from collections import Counter
import urllib3

from io import BytesIO
from PIL import Image
import matplotlib.pyplot as plt

urllib3.disable_warnings()

app = Flask(__name__)


@app.route('/image_post', methods=['get', 'POST'])
def img_post():
    if request.method == 'POST':

        # data
        # img = request.form.get('img')

        # byte
        # img = base64.b64decode(img.encode('ascii'))
        # image_data = np.frombuffer(img, np.uint8)

        # decode
        # image_data = cv2.imdecode(image_data, -1)
        # ori_img_array = image_data
        # print(type(ori_img_array))
        # print('img_shape: ', ori_img_array.shape)

        # save
        head = 'data:image/jpeg;base64,'
        form = request.form
        city = form.get('city')
        file_ = form.get('file')

        decoded_image = base64.b64decode(file_)
        image = Image.open(BytesIO(decoded_image))

        plt.imshow(image)
        plt.axis('off')
        plt.show()

        file = head + file_
        # data = {'city': city, 'file': head + file}
        # print(data)

        # call_java_pic(file=file, city=city)
        url = 'https://localhost:8443/api/v1/multi'
        # 请求参数
        data = {
            'file': file,
            'city': city
        }

        # response = requests.post(url, data=data, verify=False)
        # output = response.json()
        # print(output)

        try:
            response = requests.post(url, data=data, verify=False)
            response.raise_for_status()
            data = response.json()
            if 'data' in data and data['data'] is not None and len(data['data']) > 0:
                result = {'name': data['data'][0]['name'], 'type': data['data'][0]['type']}
            else:
                result = data['message']

            print(result)
            # return jsonify(result)
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

    return 'done'


if __name__ == "__main__":
    app.run(host="0.0.0.0", port=5000)
