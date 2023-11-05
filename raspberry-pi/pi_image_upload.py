import requests
import base64
import os

# take a photo
os.system('fswebcam -r 2560x1600 --no-banner image.jpg')

# Read the local image, passing after coding
with open(r'image.jpg', 'rb') as f:
        image_bytes = base64.b64encode(f.read())
        image_bytes = image_bytes.decode('utf-8')
img = image_bytes

data = {'city': '上海市', 'file': img}
resp = requests.post("http://192.168.43.79:5000/image_post", data=data)

print(resp.text)
