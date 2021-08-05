import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;

import javax.swing.*;
import java.util.ArrayList;

public class VelDataSet extends DefaultCategoryDataset {
    ArrayList<double[]> processedData = new ArrayList<>();
    int bufferStrength;
    double old = 12381983;
    double old_old = 12381983;
    int s = 1;

    // timestamp OneG acc(x y z) hdg(z y x)
    VelDataSet(ArrayList<double[]> data) {
        int chartCreated = 1;
        Filter filter = new DyncFilter(0.02, 1);
        bufferStrength = data.size() / 1000;
        double old = 12381983;
        do {
            processedData.clear();
            double[] Vel = {0, 0, 0};
            double oneG_calibrated = data.get(0)[1];

            double[][] res = createHDGAccArray(data);
            double[] accXArray = res[0];
            double[] accYArray = res[1];
            double[] accZArray = res[2];
            double[] hdgXArray = res[3];
            double[] hdgYArray = res[4];
            double[] hdgZArray = res[5];

            double lowPassFreq = 20;

            accXArray = LowPassFilter.filter(accXArray, lowPassFreq, 1 / ((data.get(1)[0] - data.get(0)[0]) / 1000000));
            accYArray = LowPassFilter.filter(accYArray, lowPassFreq, 1 / ((data.get(1)[0] - data.get(0)[0]) / 1000000));
            accZArray = LowPassFilter.filter(accZArray, lowPassFreq, 1 / ((data.get(1)[0] - data.get(0)[0]) / 1000000));
            hdgXArray = LowPassFilter.filter(hdgXArray, lowPassFreq, 1 / ((data.get(1)[0] - data.get(0)[0]) / 1000000));
            hdgYArray = LowPassFilter.filter(hdgYArray, lowPassFreq, 1 / ((data.get(1)[0] - data.get(0)[0]) / 1000000));
            hdgZArray = LowPassFilter.filter(hdgZArray, lowPassFreq, 1 / ((data.get(1)[0] - data.get(0)[0]) / 1000000));


            double oldVel = 0;

            for (int i = 1; i < data.size(); i++) {
                double delta_T = (data.get(i)[0] - data.get(i - 1)[0]);

                double acc_x;
                double acc_y;
                double acc_z;

                double hdg_roll;
                double hdg_pitch;
                double hdg_yaw;

                double true_vertical_velocity = (data.get(i)[8] - data.get(i - 4)[8]) / (delta_T / 1000000);
                double true_acceleration = ((true_vertical_velocity - oldVel) / (delta_T / 1000000)) / 9.81;
                oldVel = true_vertical_velocity;

                acc_x = accXArray[i];
                acc_y = accYArray[i];
                acc_z = accZArray[i];

                hdg_roll = hdgXArray[i];
                hdg_pitch = hdgYArray[i];
                hdg_yaw = hdgZArray[i];

                double[] hdgVec = new double[]{Utils.RadToGrad(hdg_roll) % (Math.PI * 2), Utils.RadToGrad(hdg_pitch) % (Math.PI * 2), Utils.RadToGrad(hdg_yaw) % (Math.PI * 2)};

                double[] unconvertedAccVector = Utils.convertToRotatedVectorSpace(new double[]{acc_x, acc_y, acc_z}, hdgVec);
                double[] accVec = Utils.getAccVector(unconvertedAccVector, oneG_calibrated);


                accVec = filter.filter(accVec);

                Vel[0] += accVec[0] * delta_T / 1000000;
                Vel[1] += accVec[1] * delta_T / 1000000;
                Vel[2] += ((accVec[2] * delta_T) - (9.81 * delta_T)) / 1000000;

                processedData.add(new double[]{data.get(i)[0], Utils.getVecLength(Vel), accVec[0], accVec[1], accVec[2], acc_x / 2048, acc_y / 2048, acc_z / 2048, Vel[0], Vel[1], Vel[2], data.get(i - 1)[8], true_vertical_velocity, true_acceleration});

            }
            //update dynamic Filter
            if (processedData.get(processedData.size() - 1)[1] > 2) {
                filter.update(processedData.get(processedData.size() - 1));
            }

            //exit
            if (old == processedData.get(processedData.size() - 1)[1]) break;
            else if (old_old == processedData.get(processedData.size() - 1)[1]) break;

            old_old = old;
            old = processedData.get(processedData.size() - 1)[1];

            if (chartCreated == 1) {
                addToDataset();
                JFreeChart lineChartObject = ChartFactory.createLineChart(
                        "Drone Velocity Magnitude", "Time (s)",
                        "Velocity (m/s)",
                        this, PlotOrientation.VERTICAL,
                        true, true, false);

                ChartPanel panel = new ChartPanel(lineChartObject);
                JFrame frame = new JFrame("Velocity Plotter");
                frame.setContentPane(panel);
                frame.setSize(800, 700);
                frame.setVisible(true);
                chartCreated = 0;
            }
        } while (processedData.get(processedData.size() - 1)[1] > 1);
        addToDataset();
    }

    public void addToDataset() {
        for (int i = bufferStrength; i < processedData.size(); i += bufferStrength) {
            double averagedVel = 0;
            double avaragedAcc_x = 0;
            double avaragedAcc_y = 0;
            double avaragedAcc_z = 0;
            double height = 0;
            double true_vertical_velocity = 0;
            double true_acceleration = 0;

            //buffer
            for (int x = 0; x < bufferStrength; x++) {
                averagedVel += processedData.get(i - x)[s];
                avaragedAcc_x += processedData.get(i - x)[2] - 1;
                avaragedAcc_y += processedData.get(i - x)[3];
                avaragedAcc_z += processedData.get(i - x)[4];
                height += processedData.get(i - x)[11];
                true_vertical_velocity += processedData.get(i - x)[12];
                true_acceleration += processedData.get(i - x)[13];
            }

            this.addValue(averagedVel / bufferStrength, "Velocity (m/s)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(avaragedAcc_x / bufferStrength / 9.81, "Acceleration x (m/s^2)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(avaragedAcc_y / bufferStrength / 9.81, "Acceleration y (m/s^2)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(avaragedAcc_z / bufferStrength / 9.81, "Acceleration z (m/s^2)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(height / bufferStrength, "Height (m)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(true_vertical_velocity / bufferStrength, "Tru_Verticle_velocity (m)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(true_acceleration / bufferStrength, "true_acceleration (m/s^2)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
        }
    }

    double[][] createHDGAccArray(ArrayList<double[]> data) {
        double[][] res = new double[6][data.size()];
        for (int i = 1; i < data.size(); i++) {
            res[0][i] = data.get(i / 1)[2];
            res[1][i] = data.get(i / 1)[3];
            res[2][i] = data.get(i / 1)[4];
            res[3][i] = data.get(i)[5];
            res[4][i] = data.get(i)[6];
            res[5][i] = data.get(i)[7];
        }
        return res;
    }
}
