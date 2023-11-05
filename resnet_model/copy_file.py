import os
import random
import shutil

# 指定原始文件夹和目标文件夹
source_folder = 'rubbish_data'  # 包含40个子文件夹的原始文件夹
target_folder = 'path_to_target_folder'  # 用于存放复制后的图片的目标文件夹
target_folder_1 = 'train_dataset'  # 用于存放70%图片的目标文件夹
target_folder_2 = 'test_dataset'

# 确保目标文件夹存在
os.makedirs(target_folder, exist_ok=True)

# 获取原始文件夹中的所有子文件夹
subfolders = [f for f in os.listdir(source_folder) if os.path.isdir(os.path.join(source_folder, f))]

for subfolder in subfolders:
    source_subfolder = os.path.join(source_folder, subfolder)

# 获取原始子文件夹中的所有图片文件
    image_files = [f for f in os.listdir(source_subfolder) if f.endswith(".jpg") or f.endswith(".png")]

    # 随机打乱图片文件的顺序
    random.shuffle(image_files)

    # 计算分割点，按照70%:30%的比例分类
    split_point = int(0.7 * len(image_files))

    # 将图片复制到目标文件夹1
    for i, image_file in enumerate(image_files[:split_point]):
        source_path = os.path.join(source_subfolder, image_file)
        target_path = os.path.join(target_folder_1, subfolder, image_file)
        os.makedirs(os.path.dirname(target_path), exist_ok=True)
        shutil.copy(source_path, target_path)

    # 将图片复制到目标文件夹2
    for i, image_file in enumerate(image_files[split_point:]):
        source_path = os.path.join(source_subfolder, image_file)
        target_path = os.path.join(target_folder_2, subfolder, image_file)
        os.makedirs(os.path.dirname(target_path), exist_ok=True)
        shutil.copy(source_path, target_path)

print("Images have been categorized into two folders.")

