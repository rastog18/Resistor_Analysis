import cv2
import numpy as np

def extract_resistor_bands(image_path):
    # Read the image
    img = cv2.imread(image_path)

    # Convert the image to the HSV color space
    hsv = cv2.cvtColor(img, cv2.COLOR_BGR2HSV)

    # Define the lower and upper bounds for each resistor band color
    # HERE WE ARE ONLY TESTING WITH SOME COLORS
    lower_bounds = np.array([
        [0, 50, 50],   # Red
        [15, 50, 50],  # Orange
        [30, 50, 50],  # Yellow
        [60, 50, 50],  # Green
        [120, 50, 50]  # Blue
    ])

    upper_bounds = np.array([
        [15, 255, 255],  # Red
        [30, 255, 255],  # Orange
        [60, 255, 255],  # Yellow
        [120, 255, 255], # Green
        [179, 255, 255]  # Blue
    ])

    # Initialize an empty list to store the extracted bands
    bands = []

    # Extract each resistor band
    for i in range(5):
        # Create a mask using color thresholding
        mask = cv2.inRange(hsv, lower_bounds[i], upper_bounds[i])

        # Find contours in the mask
        contours, _ = cv2.findContours(mask, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)

        # Sort contours based on area and select the largest one (assumes the band is the largest)
        largest_contour = max(contours, key=cv2.contourArea)

        # Draw the contour on the original image (optional)
        cv2.drawContours(img, [largest_contour], -1, (0, 255, 0), 2)

        # Get the bounding box of the contour
        x, y, w, h = cv2.boundingRect(largest_contour)

        # Crop the region of interest (resistor band) from the original image
        band = img[y:y+h, x:x+w]

        # Append the extracted band to the list
        bands.append(band)

    cv2.imshow('Resistor Bands', img)
    cv2.waitKey(0)
    cv2.destroyAllWindows()

    return bands

image_path = r'RESISTOR PICTURES/22ohm.JPEG'
extract_resistor_bands(image_path)
