import torch
import torchvision
import torchvision.transforms as transforms
import torch.nn as nn
import torch.optim as optim
from torchvision import datasets


# 数据准备
data_transforms = transforms.Compose([
    transforms.Resize(224),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406], std=[0.229, 0.224, 0.225])
])
data_root = "train_dataset"  # 数据集的根文件夹
train_dataset = datasets.ImageFolder(data_root, transform=data_transforms)

train_loader = torch.utils.data.DataLoader(train_dataset, batch_size=128, shuffle=True)


# 定义模型
resnet = torchvision.models.resnet50(pretrained=True)
for param in resnet.parameters():
    param.requires_grad = False

num_classes = len(train_dataset.classes)


print(num_classes)
#efficientnet.fc = nn.Linear(efficientnet.fc.in_features, num_classes)
resnet.fc = nn.Sequential(nn.Linear(resnet.fc.in_features, num_classes))
# 损失函数和优化器
criterion = nn.CrossEntropyLoss()
optimizer = optim.SGD(resnet.parameters(), lr=0.001,momentum=0.92)


# 5. 使用CUDA加速
device = torch.device("cuda:0" if torch.cuda.is_available() else "cpu")
resnet.to(device)
# 训练循环
print(device)
num_epochs=5
for epoch in range(15):
    running_loss = 0.0
    val_loss = 0
    correct = 0
    total = 0
    for i, data in enumerate(train_loader, 0):
        inputs, labels = data
        inputs, labels = inputs.to(device), labels.to(device)
        optimizer.zero_grad()
        outputs = resnet(inputs)
        loss = criterion(outputs, labels)
        loss.backward()
        optimizer.step()
        running_loss += loss.item()
        _, predicted = torch.max(outputs, 1)
        correct = (predicted == labels).sum().item()/128
        print(correct)

    print(epoch)

torch.save(resnet, 'resnet50.pth')
print('Finished Training')

