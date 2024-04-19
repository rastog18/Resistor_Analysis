'''
final_algorithm.py

This is the complete testing algorithm used to determine reistance of a resistor.
Slightly modified version is used on the bluepill sever 
(can be found in Server_Integration_Files/public_html/calculate.py).

Contributors: Joseph Huang, Chaeeun Kim, Nikhita Anantha Madhavan
Last Updated: Joseph Huang - 4/13/2024
'''
import sys
import numpy as np
import matplotlib.pyplot as plt
from PIL import Image
import array
from logistic_regression_training import extract_features, sigmoid

def find_closest(pixel_value, color_set):
    closest_key = min(color_set, key=lambda x: np.linalg.norm(np.array(x) - np.array(pixel_value)))
    return color_set[closest_key]

def most_common(list_of_colors):
    max_count = 0
    most_common = None
    for i in list_of_colors:  # check if i is most common
        count = 0
        for j in list_of_colors:  #check all other letters in list to count
            if j == i:
                count += 1
        if count > max_count:
            max_count = count
            most_common = i
    return most_common

def find_xPixels(band_count, bandNum):
    if band_count == 5:
        if bandNum == 1:
            return 67
        elif bandNum == 2:
            return 100
        elif bandNum == 3:
            return 130
        elif bandNum == 4:
            return 165
        elif bandNum == 5:
            return 195
    elif band_count == 4:
        if bandNum == 1:
            return 67
        elif bandNum == 2:
            return 105
        elif bandNum == 3:
            return 145
        elif bandNum == 4:
            return 200
    elif band_count == 6:
        if bandNum == 1:
            return 50
        elif bandNum == 2:
            return 70
        elif bandNum == 3:
            return 90
        elif bandNum == 4:
            return 120
        elif bandNum == 5:
            return 158
        elif bandNum == 6:
            return 200
        

def brightness(im_file):
    with open(im_file, 'rb') as f:
        data = array.array('B', f.read())
    mean_brightness = sum(data) / len(data)
    return mean_brightness

def adjust_brightness(im_file, factor):
    image = plt.imread(im_file)
    row = len(image)
    col = len(image[1])
    
    # Create zero filled arrays for red, green, blue, and gray
    red = np.zeros((row, col), np.dtype(np.uint8))
    green = np.zeros((row, col), np.dtype(np.uint8))
    blue = np.zeros((row, col), np.dtype(np.uint8))
    
    # Load values into red, green, blue arrays
    for i in range(row):
        for j in range(col):
            red[i][j] = min(255, int(image[i][j][0] * factor))
            green[i][j] = min(255, int(image[i][j][1] * factor))
            blue[i][j] = min(255, int(image[i][j][2] * factor))
            
    rgb_image = np.stack([red, green, blue], axis=-1)
    image = Image.fromarray(rgb_image.astype('uint8'))
    image.save(im_file)
        
def make_grayscale(original_path, gray_path):
    image = plt.imread(original_path)
    
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
    gray_image.save(gray_path)


if __name__ == "__main__":
    num_bands = int(sys.argv[1])
    IMG_PATH = "resistor.jpg"
    logistic_regression_img_path = "lr_resistor.jpg"
    brightness_value = brightness(IMG_PATH)
    #print(brightness_value)
    if brightness_value < 100:
        adjust_brightness(IMG_PATH, 1.5)
    plt.imshow(plt.imread(IMG_PATH))
    
    make_grayscale(IMG_PATH, logistic_regression_img_path)
    features = extract_features(logistic_regression_img_path)
    test_features = np.hstack(([1], features))

    theta = np.load('best_theta.npy')
    z = np.dot(test_features, theta)
    #TODO: Look into this. Certain images end up with values over 709.78 which cause overflow errors in np.exp. Find out why they can be so big 
    z = np.clip(z, -709.78, 709.78)  
    probability = sigmoid(z)
    
    
    #if probability >= 0.5:
    if True:
        # Load image using Mathplotlib
        image = plt.imread(IMG_PATH)
        
        # Resize image to have width 250px to speed up algorithm
        with Image.open(IMG_PATH) as img:
          width, height = img.size
          new_width = 250
          new_height = 125
          image = np.array(img.resize((new_width, new_height)))
        plt.imshow(image)
            
        color_set = {
            (30, 30, 30):      'Black',
            (69, 51, 47):      'Brown',
            (41, 14, 7):       'Brown',
            (85, 41, 35):      'Brown',
            (95, 33, 36):      'Red',
            (121, 49, 55):     'Red',
            (41, 78, 125):     'Blue',
            (31, 59, 107):     'Blue',
            (202, 144, 72):    'Orange',
            (104, 117, 47):    'Yellow',
            (157, 191, 121):   'Yellow',
            (216, 164, 55):    'Yellow',
            (58, 96, 82):      'Green',
            (33, 69, 5):       'Green',
            (80, 121, 81):     'Green',
            (85, 123, 110):    'Violet',
            (67, 50, 77):      'Violet',
            (255, 255, 255):   'White',
            (79, 77, 78):      'Gray',
            (100, 80, 52):     'Gold',
        }
        
        color_set_tolerance = {
            (69, 51, 47):      'Brown',
            (41, 14, 7):      'Brown',
            (85, 41, 35):      'Brown',
            (95, 33, 36):      'Red',
            (121, 49, 55):    'Red',
            (41, 78, 125):    'Blue',
            (31, 59, 107):    'Blue',
            (58, 96, 82):      'Green',
            (33, 69, 5):      'Green',
            (80, 121, 81):    'Green',
            (85, 123, 110):    'Violet',
            (255, 255, 255):  'White',
            (100, 80, 52):    'Gold',
            (135, 83, 45):    'Gold',
            (106, 57, 19):    'Gold',
        }

        color_set_ppm = {
            (69, 51, 47):      'Brown',
            (41, 14, 7):      'Brown',
            (95, 33, 36):      'Red',
            (121, 49, 55):    'Red',
            (41, 78, 125):    'Blue',
            (31, 59, 107):    'Blue',
            (85, 123, 110):    'Violet',
            (202, 144, 72):    'Orange',
        }

        color_list = []
        row = len(image)

        band1_list = []
        for i in range(row):
          if i >= 40 and i < 100:
            band1_list.append(find_closest(image[i][find_xPixels(num_bands, 1)], color_set))
        #print(band1_list)
        color_list.append(most_common(band1_list))
        
        band2_list = []
        for i in range(row):
          if i >= 40 and i < 100:
            band2_list.append(find_closest(image[i][find_xPixels(num_bands, 2)], color_set))
        #print(band2_list)
        color_list.append(most_common(band2_list))
        
        band3_list = []
        for i in range(row):
          if i >= 40 and i < 100:
            band3_list.append(find_closest(image[i][find_xPixels(num_bands, 3)], color_set))
        #print(band3_list)
        color_list.append(most_common(band3_list))
        
        band4_list = []
        for i in range(row):
          if i >= 40 and i < 100:
            color_check_set = color_set if num_bands >= 5 else color_set_tolerance
            band4_list.append(find_closest(image[i][find_xPixels(num_bands, 4)], color_check_set))
        #print(band4_list)
        color_list.append(most_common(band4_list))
        
        if num_bands > 4:
          band5_list = []
          for i in range(row):
            if i >= 40 and i < 100:
              band5_list.append(find_closest(image[i][find_xPixels(num_bands, 5)], color_set_tolerance))
          color_list.append(most_common(band5_list))


        if num_bands > 5:
          band6_list = []
          for i in range(row):
            if i >= 40 and i < 100:
              band6_list.append(find_closest(image[i][find_xPixels(num_bands, 6)], color_set_ppm))
          color_list.append(most_common(band6_list))
        
        # Value of the resistor bands. First 3 bands use this (5-band and 6-band resistor, 2 for 4-band)
        resistor_value = {
            'Black': 0,
            'Brown': 1,
            'Red': 2,
            'Orange': 3,
            'Yellow': 4,
            'Green': 5,
            'Blue': 6,
            'Violet': 7,
            'Gray': 8,
            'White': 9
        }
        
        # Value of multiplies based on color. 4th band on 5-band resistor
        resistor_multiplier = {
            'Black': 1,
            'Brown': 10,
            'Red': 100,
            'Orange': 1000,
            'Yellow': 10000,
            'Green': 100000,
            'Blue': 1000000,
            'Violet': 10000000,
            'Gray': 100000000,
            'White': 1000000000,
            'Gold': 0.1,
            'Silver': 0.01,
        }
        
        # Hex color codes of resistors
        resistor_code = {
            'Black': "#000000",
            'Brown': "#7B3F00",
            'Red': "#FF0000",
            'Orange': "#FF7400",
            'Yellow': "#FFF109",
            'Green': "#008F00",
            'Blue': "#00009e",
            'Violet': "#79009e",
            'Gray': "#808080",
            'White': "#FFFFFF",
            'Gold': "#AE8625",
            'Silver': "#C9C9C9",
        }
        
        # Used to format resistor value at end
        multipliers = {0: '', 3: 'k', 6: 'M', 9: 'G', 12: 'T'}
        
        # Calculate Resistor Value based on detected colors
        starting_value = ""
        cutoff_num = 2 if num_bands == 4 else 3
        for i in range(len(color_list)):
          if i < cutoff_num:
            number = str(resistor_value.get(color_list[i])) if str(resistor_value.get(color_list[i])) != "None" else "0" 
            starting_value += number
          if i == cutoff_num:
            raw_value = int(starting_value)
            raw_value *= resistor_multiplier.get(color_list[i])
        
        # Format Resistor Value with k/M/G/T
        for exponent, letter in sorted(multipliers.items(), reverse=True):
          if raw_value >= 10**exponent:
            value = raw_value / 10**exponent
            formatted_value = f'{value:.2f}{letter}'
            break
        
        if formatted_value is None:
            formatted_value = f'{raw_value:.2f}'
        
        # final_str is what is passed back to application
        # This is the structure that the application is looking for
        final_str = formatted_value + "    5%"
        for color in color_list:
            final_str += ","
            final_str += resistor_code.get(color)
        #print(color_list)
        print(final_str)
    else:
        print("-1")