

public class Utils {
    public static double RadToGrad(double rad) {

        //return rad * 180 / Math.PI;
        return rad;
    }

    public static double[] convertToRotatedVectorSpace(double[] vec, double[] hdg) {
        //return multiplyRotationMatrixVector(invertRotationMatrix(rotationMatrix), vec);
        return rotateVectorCC(vec, new double[][]{{1, 0, 0}, {0, 1, 0}, {0, 0, 1}}, hdg, 0);
    }

    public static double[] getAccVector(double[] acc, double oneG) {
        double f = 9.81 / oneG;
        return new double[]{acc[0] * f, acc[1] * f, acc[2] * f};
    }

    public static double getVecLength(double[] vec) {
        return Math.pow((Math.pow(vec[0], 2) + Math.pow(vec[1], 2) + Math.pow(vec[2], 2)), 0.5);
    }

    // pitch, roll, yaw
    public static double[][] createRotationMatrix(double[] hdg) {
        double rotationMatrix[][] = {{Math.cos(hdg[2]) * Math.cos(hdg[1]), Math.cos(hdg[2]) * Math.sin(hdg[1]) * Math.sin(hdg[0]) - Math.sin(hdg[2]) * Math.cos(hdg[0]), Math.cos(hdg[2]) * Math.sin(hdg[1]) * Math.cos((hdg[0])) + Math.sin(hdg[2]) * Math.sin(hdg[0])},
                {Math.cos(hdg[2]) * Math.cos(hdg[1]), Math.cos(hdg[2]) * Math.sin(hdg[1]) * Math.sin(hdg[0]) + Math.sin(hdg[2]) * Math.cos(hdg[0]), Math.cos(hdg[2]) * Math.sin(hdg[1]) * Math.cos((hdg[0])) - Math.sin(hdg[2]) * Math.sin(hdg[0])},
                {-Math.sin(hdg[1]), Math.cos(hdg[1]) * Math.sin(hdg[0]), Math.cos(hdg[1]) * Math.cos(hdg[0])}};

        return rotationMatrix;
    }

    // vector x , y , z
    public static double[] multiplyRotationMatrixVector(double[][] matrix, double[] vector) {
        double[] rotatedVector = new double[3];
        for (int i = 0; i < 3; i++) {
            for (int x = 0; x < 3; x++) {
                rotatedVector[i] += matrix[i][x] * vector[x];
            }
        }
        return rotatedVector;
    }

    public static double[] rotateVectorCC(double[] vec, double[][] axis, double[] theta, int i) {
        if (i == 3) return vec;
        double x, y, z;
        double u, v, w;
        x = vec[0];
        y = vec[1];
        z = vec[2];
        u = axis[i][0];
        v = axis[i][1];
        w = axis[i][2];
        double xPrime = u * (u * x + v * y + w * z) * (1d - Math.cos(theta[i]))
                + x * Math.cos(theta[i])
                + (-w * y + v * z) * Math.sin(theta[i]);
        double yPrime = v * (u * x + v * y + w * z) * (1d - Math.cos(theta[i]))
                + y * Math.cos(theta[i])
                + (w * x - u * z) * Math.sin(theta[i]);
        double zPrime = w * (u * x + v * y + w * z) * (1d - Math.cos(theta[i]))
                + z * Math.cos(theta[i])
                + (-v * x + u * y) * Math.sin(theta[i]);
        i++;
        return rotateVectorCC(new double[]{xPrime, yPrime, zPrime}, axis, theta, i);
    }

    public static double[][] invertRotationMatrix(double a[][]) {

        int n = a.length;

        double x[][] = new double[n][n];

        double b[][] = new double[n][n];

        int index[] = new int[n];

        for (int i = 0; i < n; ++i)

            b[i][i] = 1;


        gaussian(a, index);


        for (int i = 0; i < n - 1; ++i)

            for (int j = i + 1; j < n; ++j)

                for (int k = 0; k < n; ++k)

                    b[index[j]][k]

                            -= a[index[j]][i] * b[index[i]][k];

        for (int i = 0; i < n; ++i) {

            x[n - 1][i] = b[index[n - 1]][i] / a[index[n - 1]][n - 1];

            for (int j = n - 2; j >= 0; --j) {

                x[j][i] = b[index[j]][i];

                for (int k = j + 1; k < n; ++k) {

                    x[j][i] -= a[index[j]][k] * x[k][i];

                }

                x[j][i] /= a[index[j]][j];

            }

        }

        return x;

    }


    public static void gaussian(double a[][], int index[]) {

        int n = index.length;

        double c[] = new double[n];

        for (int i = 0; i < n; ++i)

            index[i] = i;

        for (int i = 0; i < n; ++i) {

            double c1 = 0;

            for (int j = 0; j < n; ++j) {

                double c0 = Math.abs(a[i][j]);

                if (c0 > c1) c1 = c0;

            }

            c[i] = c1;

        }

        int k = 0;

        for (int j = 0; j < n - 1; ++j) {

            double pi1 = 0;

            for (int i = j; i < n; ++i) {

                double pi0 = Math.abs(a[index[i]][j]);

                pi0 /= c[index[i]];

                if (pi0 > pi1) {

                    pi1 = pi0;

                    k = i;

                }

            }


            int itmp = index[j];

            index[j] = index[k];

            index[k] = itmp;

            for (int i = j + 1; i < n; ++i) {

                double pj = a[index[i]][j] / a[index[j]][j];

                a[index[i]][j] = pj;


                for (int l = j + 1; l < n; ++l)

                    a[index[i]][l] -= pj * a[index[j]][l];

            }

        }

    }

}
