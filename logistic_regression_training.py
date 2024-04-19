import os
import numpy as np
import matplotlib.pyplot as plt
from PIL import Image

## Resize RGB image
'''
All images are passed through this function to ensure consistancy in
image size. The aspect ratio is maintained
'''
def resize_image(imageFile):
    with Image.open(imageFile) as img:
        width, height = img.size
        aspect_ratio = width / height
        
        new_width = 250
        new_height = int(new_width / aspect_ratio)
        if new_height > 100:
            new_height = 100
            new_width = int(new_height * aspect_ratio)
        
        resized_img = Image.new("RGB", (250, 100), (0, 0, 0))
        resized_img.paste(img.resize((new_width, new_height), Image.ANTIALIAS), ((250 - new_width) // 2, (100 - new_height) // 2))

        image = np.array(resized_img)
    
    return image


# Flatten image
'''
Images are flattened into a 1D array, esentially creating a feature
vector where each pixel is a "feature"
'''
def extract_features(image_path):
  with open(image_path, 'rb') as filename:
    resized_img = resize_image(filename)
    flattened_image = resized_img.flatten()
  return flattened_image

# Make list of feature vectors
'''
Each element in the list is the feature vector for an
image in the folder
'''
def create_feature_list(folder):
  images = os.listdir(folder)
  features_list = []
  for name in images:
    image_path = os.path.join(folder, name)
    curr_feature = extract_features(image_path)
    features_list.append(curr_feature)
  features_list_nparray = np.array(features_list)
  return features_list_nparray

# Sigmoid function
def sigmoid(z):
    z = np.clip(z, -709.78, 709.78)  
    return (1 / (1 + np.exp(-z)))

# Cost function
def cost_function(X, y, theta):
  m = len(y)
  h = sigmoid(np.dot(X, theta))
  h = np.clip(h, 1e-1, 1 - 1e-1)
  cost = (-1 / m) * np.sum(y * np.log(h) + (1 - y) * np.log(1 - h))
  return cost

# Gradient descent
def gradient_descent(X, y, theta, alpha, num_iterations, X_test, y_test):
  m = len(y)
  costs = []
  accuracies = []  # Store training accuracies
  accuracies2 = []  # Store testing accuracies
  theta_list = []
  for _ in range(num_iterations):
      h = sigmoid(np.dot(X, theta))
      gradient = np.dot(X.T, (h - y)) / m
      theta -= alpha * gradient
      cost = cost_function(X, y, theta)
      costs.append(cost)

      # Calculate training accuracy
      predictions = np.round(sigmoid(np.dot(X, theta)))
      accuracy = np.mean(predictions == y)
      accuracies.append(accuracy)
      
      # Calculate testing accuracy
      predictions = np.round(sigmoid(np.dot(X_test, theta)))
      accuracy2 = np.mean(predictions == y_test)
      accuracies2.append(accuracy2)
      theta_list.append(theta)

  return theta, costs, accuracies, accuracies2, theta_list


if __name__ == "__main__":
    # Load the resistor images
    resistor_folder = "cropped_original_resistor_images"
    resistor_features = create_feature_list(resistor_folder)
    resistor_labels = np.ones((resistor_features.shape[0], 1))
    
    #Load the random images
    non_resistor_folder = "grayscale_randoms"
    non_resistor_features = create_feature_list(non_resistor_folder)
    non_resistor_labels = np.zeros((non_resistor_features.shape[0], 1))
    
    # Vertically stack features and labels
    x = np.vstack((resistor_features, non_resistor_features))
    y = np.vstack((resistor_labels, non_resistor_labels))
    
    # Add intercept term to x (this is necessary to account for bias or offset)
    intercept = np.ones((x.shape[0], 1))
    x = np.hstack((intercept, x))
    
    # Split the data into training and testing sets
    '''
    This code sets a random seed for replication purposes and splits the data into
    train and test data. The last 20 images are test data, everything else is
    training data
    '''
    np.random.seed(18)
    indices = np.random.permutation(x.shape[0])
    X_train, X_test = x[indices[:-20]], x[indices[-20:]]
    y_train, y_test = y[indices[:-20]], y[indices[-20:]]
    
    # Initialize theta
    theta = np.zeros((X_train.shape[1], 1))
    
    alpha = 0.01
    num_iterations = 250
    
    # Train the model
    theta, costs, accuracies, test_accuracies, theta_list = gradient_descent(X_train, y_train, theta, alpha, num_iterations, X_test, y_test)
    np.save('best_theta.npy', theta)
    
    predictions = np.round(sigmoid(np.dot(X_test, theta)))
    accuracy = np.mean(predictions == y_test)
    print("Accuracy:", accuracy)
    
    plt.plot(range(1, num_iterations + 1), accuracies, label="training")
    plt.plot(range(1, num_iterations + 1), test_accuracies, label="testing")
    plt.legend()
    plt.xlabel('Iterations')
    plt.ylabel('Accuracy')
    plt.title('Accuracy vs. Iterations')
    plt.show()