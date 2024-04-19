import numpy as np
import cv2
import matplotlib.pyplot as plt
from matplotlib.colors import hsv_to_rgb

# Convert image to HSV
def rgb_to_hsv(rgb_image):
    hsv_image = cv2.cvtColor(rgb_image, cv2.COLOR_RGB2HSV)
    
    return hsv_image


# READ IMAGE
image_path =  r'RESISTOR PICTURES/47ohm.JPEG'
rgb_image = plt.imread(image_path)

# Convert RGB to HSV
hsv_image = rgb_to_hsv(rgb_image)

# Display the original RGB image
plt.subplot(1, 2, 1)
plt.imshow(rgb_image)
plt.title('RGB Image')
plt.axis('off')

# Display the converted HSV image
plt.subplot(1, 2, 2)
rgb_from_hsv = hsv_to_rgb(hsv_image)
plt.imshow(rgb_from_hsv)
plt.title('HSV Image')
plt.axis('off')

plt.show()
