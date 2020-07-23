package flappybirdai;

import flappybirdai.NNlib.*;
import flappybirdai.NNlib.Layer.*;

public class Network {

    private static NN fc = new NN("nn", 0, .0001f, LossFunction.QUADRATIC(.5), Optimizer.ADAM,
            new Dense(7, 21, Activation.TANH, Initializer.XAVIER),
            new Dense(21, 42, Activation.TANH, Initializer.XAVIER),
            new Dense(42, 7, Activation.TANH, Initializer.XAVIER),
            new Dense(7, 2, Activation.SIGMOID, Initializer.XAVIER)
    );
    public static NN nn = fc;
}
