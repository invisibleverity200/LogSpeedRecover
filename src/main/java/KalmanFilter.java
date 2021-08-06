import org.apache.commons.math3.exception.DimensionMismatchException;
import org.apache.commons.math3.exception.NullArgumentException;
import org.apache.commons.math3.filter.DefaultMeasurementModel;
import org.apache.commons.math3.filter.DefaultProcessModel;
import org.apache.commons.math3.filter.MeasurementModel;
import org.apache.commons.math3.filter.ProcessModel;
import org.apache.commons.math3.geometry.euclidean.twod.Euclidean2D;
import org.apache.commons.math3.linear.*;
import org.apache.commons.math3.random.JDKRandomGenerator;
import org.apache.commons.math3.random.RandomGenerator;

import java.util.ArrayList;

public class KalmanFilter  {
    Matrix X;          //State Space
    Matrix P;          //error coveriance
    double T;          //Delta T time
    Matrix F;          //Transition Matrix
    double Q;          //Proces Noise Matrix
    double R;          //Measurment Noise or variance in sensor
    double Y;          //Residual
    Matrix K;          //Kalman Gain
    double Bu;         //Model Control input
    Matrix H;          //Measurement funtion

    //Cunstructer initializing the state space
    public KalmanFilter(double accelration, double t, double r) { //recieving sensor initial value, time and noise in sensor
        //State Space 3 x 1
        double[][] x = new double[][]{{0.}, {0.}, {accelration}};
        this.X = new Matrix(x);
        //error coveriance 3 x 3
        double[][] p = new double[][]{{10., 0., 0}, {0., 10, 0.}, {0., 0., 10.}};
        this.P = new Matrix(p);
        //Delta T time
        this.T = t;
        //State Transition Matrix 3 x 3
        double[][] f = new double[][]{{1., T, 0.5 * (T * T)}, {0., 1., T}, {0., 0., 1.}};
        this.F = new Matrix(f);
        //Proces Noise Matrix
        this.Q = 0;
        //Measurment Noise or variance in sensor
        this.R = r;
        //Residual
        this.Y = 0;
        //Kalman Gain
        double[][] k = new double[][]{{1.}, {1.}, {1.}};
        this.K = new Matrix(k);
        //Model Control input
        this.Bu = 0;
        //Measurement Funtion
        double[][] h = new double[][]{{0., 0., 1.}};
        this.H = new Matrix(h);
    }

    //getter for accelration in state space
    double estimatedAccelration() {
        return X.elementAt(2, 0);
    }

    //Predict
    // X' = X*F + B*u
    // P' = F*P*Ft + Q

    public void predict() {
        X = F.times(X);
        P = F.times(P).times(F.transpose());
    }

    //Update
    // Y = Z - H*X'
    // K = P*H't / (H*P*H't + R)
    // X = X' + K*Y
    // p = (1 - K*H)*P'

    public void update(double Z) { //here is Z measurement value from sensor which is to be filter
        Y = Z - H.times(X).elementAt(0, 0);
        K = P.times(H.transpose()).dividedByNumber((H.times(P.times(H.transpose())).elementAt(0, 0) + R));
        X = X.plus(K.multiplyByNumber(Y));
        P = (K.numberSubtractedByMatrix(1).times(H)).times(P);
    }
}



