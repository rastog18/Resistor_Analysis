import numpy as np
import cv2
import matplotlib.pyplot as plt
from matplotlib.colors import hsv_to_rgb



image_path =  r'RESISTOR PICTURES/47ohm.JPEG'
image = plt.imread(image_path)

# hsvcv2 = cv2.cvtColor(image, cv2.COLOR_BGR2HSV)
# cv2.imshow("CV2 IMAGE",hsvcv2)


row = len(image)
col = len(image[1])

# # Create zero filled arrays for red, green, blue, and gray
red = np.zeros((row, col))
green = np.zeros((row, col))
blue = np.zeros((row, col))


# # Load values into red, green, blue arrays
# for i in range(row):
#     for j in range(col):
#         red[i][j] = image[i][j][0]
#         green[i][j] = image[i][j][1]
#         blue[i][j] = image[i][j][2]
# # BT 709 Grayscale Equation



red = image[:,:,0]
green = image[:,:,1]
blue = image[:,:,2]

r = red / 255.0
g = green / 255.0
b = blue / 255.0

h= np.zeros((row, col))
s = np.zeros((row, col))
v = np.zeros((row, col))

for i in range(row):
  for j in range(col):
    cmax = max(r[i][j],g[i][j],b[i][j])
    cmin = min(r[i][j],g[i][j],b[i][j])
    
    diff = cmax - cmin

    if diff == 0:
      h[i][j] = 0
    elif cmax == r[i][j]:
      h[i][j] = (60 * ((g[i][j] - b[i][j]) / diff)) % 360
    elif cmax == g[i][j]: 
      h[i][j] = (60 * ((b[i][j] - r[i][j]) / diff) + 120) % 360
    elif cmax == b[i][j]:
      h[i][j] = (60 * ((r[i][j] - g[i][j]) / diff) + 240) % 360

    h[i][j] /= 360

    if cmax == 0:
      s[i][j] = 0
    else:
      s[i][j] = (diff/ cmax) 

    v[i][j] = cmax 
print(h[1][1], s[1][1], v[1][1])

hsv_image_normal = np.stack((h, s, v), axis=-1)
hsv_image_normal = np.clip(hsv_image_normal, 0 , 255).astype(np.float32) / 255.0

plt.hsv()
cv2.imshow(hsv_image_normal)

# import cv2

# # Load the RGB image
# rgb_image = cv2.imread(image_path)

# # Convert RGB image to HSV
# hsv_image_cv2 = cv2.cvtColor(rgb_image, cv2.COLOR_BGR2HSV)

# # Display the original RGB image and the converted HSV image
# print(hsv_image_cv2)
