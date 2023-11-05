import os
import torch
import torchvision.transforms as transforms
from torchvision.datasets import ImageFolder
import torchvision.datasets as datasets
from torch.utils.data import DataLoader
from sklearn.metrics import confusion_matrix, accuracy_score
import matplotlib.pyplot as plt
import seaborn as sns
import time
import torch.nn as nn

# 加载模型
  # 创建一个新的模型实例

# 指定图像文件夹路径
data_dir= 'test_dataset'

  # 替换为包含数据的文件夹路径
batch_size = 128
num_classes = 40  # 假设有40个类别

# 数据预处理
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])

# 创建数据集
dataset = datasets.ImageFolder(data_dir, transform=transform)

# 创建数据加载器
dataloader = DataLoader(dataset, batch_size=batch_size, shuffle=False)
model = torch.load('resnet50.pth').to('cuda')
model.eval()


true_labels = []
predicted_labels = []

start_time = time.time()
with torch.no_grad():
    for images, labels in dataloader:
        outputs = model(images.to('cuda'))
        _, predicted = torch.max(outputs, 1)

        true_labels.extend(labels.numpy())
        predicted_labels.extend(predicted.cpu().numpy())
end_time = time.time()
prediction_time = end_time - start_time
print(len(true_labels))
print(f"Prediction time: {prediction_time:.4f} seconds")

accuracy = accuracy_score(true_labels, predicted_labels)
print(f'Accuracy: {accuracy * 100:.2f}%')

'''
confusion = confusion_matrix(true_labels, predicted_labels)
plt.figure(figsize=(12, 10),dpi=300)
sns.heatmap(confusion, annot=True, fmt='d', cbar=True, xticklabels=True, yticklabels=True)
plt.xlabel('Predicted', fontsize=12)
plt.ylabel('True', fontsize=12)
plt.title('Confusion Matrix', fontsize=16)
plt.savefig('confusion.jpg')
plt.show()
'''


