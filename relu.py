import numpy as np

def backward_relu(input_relu, prediction, target):
    if (len(prediction) == len(target)):
        dz = np.array(input_relu)
        dz[input_relu <= 0] = 0
        # dz[self.image ]
        dA_loss = 2 * (np.array(prediction) - np.array(target)) / len(prediction)
        dz *= dA_loss
        return dz
    else: return 

def forward_relu(x):
    if (x < 0): return 0
    else: return x