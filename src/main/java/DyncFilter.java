public class DyncFilter implements Filter{

    private double x_up = -0.55;
    private double x_down = 0.55;
    private double y_down = 0.55;
    private double y_up = -0.55;
    private double z_down = 1.33;
    private double z_up = 0.5;

    private double delta_rate ;
    private double target;

    DyncFilter(double delta_rate, double target) {
        this.delta_rate = delta_rate;
        this.target = target;
    }

    public void update(double[] signal) {
        if (signal[1] > target) {
            if (signal[8] > target) {
                x_down += delta_rate;
                x_up += delta_rate;
            }
            if (signal[9] > target) {
                y_down += delta_rate;
                y_up += delta_rate;
            }
            if (signal[10] > target) {
                z_down += delta_rate;
                z_up += delta_rate;
            }
            if (signal[8] < -target) {
                x_down -= delta_rate;
                x_up -= delta_rate;
            }
            if (signal[9] < -target) {
                y_down -= delta_rate;
                y_up -= delta_rate;
            }
            if (signal[10] < -target) {
                z_down -= delta_rate;
                z_up -= delta_rate;
            }
        }
    }

    public double[] filter(double[] accVec, double[] accVec_range) {
        if (accVec_range[0] < x_down * 9.81 && accVec_range[0] > 0) {
            accVec[0] = 0;
        }
        if (accVec_range[1] < y_down * 9.81 && accVec_range[1] > 0) {
            accVec[1] = 0;
        }
        if (accVec_range[2] < 1 * 9.81 && accVec_range[2] > z_up * 9.81) {
            accVec[2] = 9.81;
        }
        if (accVec_range[2] < z_down * 9.81 && accVec_range[2] > 1 * 9.81) {
            accVec[2] = 9.81;
        }
        if (accVec_range[0] > x_up * 9.81 && accVec_range[0] < 0) {
            accVec[0] = 0;
        }
        if (accVec_range[1] > y_up * 9.81 && accVec_range[1] < 0) {
            accVec[1] = 0;
        }
        if (accVec_range[2] > -1 * 9.81 && accVec_range[2] < -0.5 * 9.81) {
            accVec[2] = -9.81;
        }
        return accVec;
    }
}

