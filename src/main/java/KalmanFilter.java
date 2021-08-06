

public class KalmanFilter  {
    Matrix X;
    Matrix P;
    double T;
    Matrix F;
    double Q;
    double R;
    double Y;
    Matrix K;
    double Bu;
    Matrix H;


    public KalmanFilter(double accelration, double t, double r) {

        double[][] x = new double[][]{{0.}, {0.}, {accelration}};
        this.X = new Matrix(x);

        double[][] p = new double[][]{{10., 0., 0}, {0., 10, 0.}, {0., 0., 10.}};
        this.P = new Matrix(p);

        this.T = t;

        double[][] f = new double[][]{{1., T, 0.5 * (T * T)}, {0., 1., T}, {0., 0., 1.}};
        this.F = new Matrix(f);

        this.Q = 0;

        this.R = r;

        this.Y = 0;

        double[][] k = new double[][]{{1.}, {1.}, {1.}};
        this.K = new Matrix(k);

        this.Bu = 0;

        double[][] h = new double[][]{{0., 0., 1.}};
        this.H = new Matrix(h);
    }


    double estimatedAccelration() {
        return X.elementAt(2, 0);
    }


    public void predict() {
        X = F.times(X);
        P = F.times(P).times(F.transpose());
    }

    public void update(double Z) {
        Y = Z - H.times(X).elementAt(0, 0);
        K = P.times(H.transpose()).dividedByNumber((H.times(P.times(H.transpose())).elementAt(0, 0) + R));
        X = X.plus(K.multiplyByNumber(Y));
        P = (K.numberSubtractedByMatrix(1).times(H)).times(P);
    }
}



