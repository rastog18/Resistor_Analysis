## Logistic Regression Image Preprocessing
# Updated 03/28/2024 - Joseph Huang
# Created 03/28/2024 - Joseph Huang

import os
import shutil
import numpy as np
from PIL import Image
import matplotlib.pyplot as plt 

def copy_images(source_folder, destination_folder):
    # Create the destination folder if it doesn't exist
    if not os.path.exists(destination_folder):
        os.makedirs(destination_folder)

    # Get a list of all files in the source folder
    files = os.listdir(source_folder)

    # Iterate through the files and copy images to the destination folder
    for file_name in files:
        # Check if the file is an image (you can modify this condition as needed)
        if file_name.endswith(('.jpg', '.jpeg', '.png')):
            source_path = os.path.join(source_folder, file_name)
            destination_path = os.path.join(destination_folder, file_name)
            shutil.copyfile(source_path, destination_path)
            print(f"Copied {file_name} to {destination_folder}")

    print("Image copy process completed.")
    
def process_images_in_directory(directory_path):
    # Get a list of all files in the directory
    files = os.listdir(directory_path)

    # Iterate through the files
    for file_name in files:
        # Check if the file is an image (you can modify this condition as needed)
        if file_name.endswith(('.jpg', '.jpeg', '.png')):
            image_path = os.path.join(directory_path, file_name)
            # Process the image
            image = plt.imread(image_path)
            
            row = len(image)
            col = len(image[1])
            
            # Create zero filled arrays for red, green, blue, and gray
            red = np.zeros((row, col), np.dtype(np.uint8))
            green = np.zeros((row, col), np.dtype(np.uint8))
            blue = np.zeros((row, col), np.dtype(np.uint8))
            gray = np.zeros((row, col), np.dtype(np.uint8))
            
            # Load values into red, green, blue arrays
            for i in range(row):
                for j in range(col):
                    red[i][j] = image[i][j][0]
                    green[i][j] = image[i][j][1]
                    blue[i][j] = image[i][j][2]
            
            # BT 709 Grayscale Equation
            gray = (0.183 * red) + (0.614 * green) + (0.062 * blue)
            gray_image = Image.fromarray(gray.astype('uint8'), mode='L')
            gray_image.save(image_path)

if __name__ == "__main__":
    source_folder = "random_train_images"
    destination_folder = "grayscale_randoms"
    copy_images(source_folder, destination_folder)
    process_images_in_directory(destination_folder)