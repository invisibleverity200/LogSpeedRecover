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
    double oldd = 12381983;
    int s = 10;

    // timestamp OneG acc(x y z) hdg(z y x)
    VelDataSet(ArrayList<double[]> data) {
        int ü = 1;
        Filter filter = new DyncFilter(0.02, 1);
        bufferStrength = data.size() / 1000;
        double old = 12381983;
        do {
            processedData.clear();
            double[] Vel = {0, 0, 0};
            double oneG_calibrated = data.get(0)[1];

            double[][] res = createaccArray(data);
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


            int u = 4;
            double oldVel = 0;

            for (int i = u; i < data.size() - u; i += u) {
                double delta_T = (data.get(i)[0] - data.get(i - u)[0]);

                /*double acc_x = (data.get(i - u)[2]);
                double acc_y = (data.get(i - u)[3]);
                double acc_z = (data.get(i - u)[4]);

                double hdg_roll = data.get(i - u)[5];
                double hdg_pitch = data.get(i - u)[6];
                double hdg_yaw = data.get(i - u)[7];
                */
                double acc_x = 0;
                double acc_y = 0;
                double acc_z = 0;

                double hdg_roll = 0;
                double hdg_pitch = 0;
                double hdg_yaw = 0;
                double tru_verticle_velocity = (data.get(i)[8] - data.get(i - 4)[8]) / (delta_T / 1000000);
                double tru_accerleration = ((tru_verticle_velocity - oldVel) / (delta_T / 1000000)) / 9.81;
                oldVel = tru_verticle_velocity;

                /*for (int o = 0; o < u / 2; o++) {
                    acc_x += (accXArray[i - o]);
                    acc_x += (accXArray[i + o]);
                    acc_y += (accYArray[i - o]);
                    acc_y += (accYArray[i + o]);
                    acc_z += (accZArray[i - o]);
                    acc_z += (accZArray[i + o]);
                    hdg_roll += hdgXArray[i - o];
                    hdg_roll += hdgXArray[i + o];
                    hdg_pitch += hdgYArray[i - o];
                    hdg_pitch += hdgYArray[i + o];
                    hdg_yaw += hdgZArray[i - o];
                    hdg_yaw += hdgZArray[i + o];
                }*/

                acc_x = accXArray[i];
                acc_y = accYArray[i];
                acc_z = accZArray[i];

                hdg_roll = hdgXArray[i];
                hdg_pitch = hdgYArray[i];
                hdg_yaw = hdgZArray[i];

                double acc_x_range = 0;
                double acc_y_range = 0;
                double acc_z_range = 0;
                int c = 0; //TODO change
                for (int o = 0; o < c; o++) {
                    if (i + c > data.size() - 1) c = data.size() - 1 - i;
                    acc_x_range += accXArray[i + o];

                    acc_y_range += accYArray[i + o];

                    acc_z_range += accZArray[i + o];
                }

                acc_x_range = acc_x_range / c;
                acc_y_range = acc_y_range / c;
                acc_z_range = acc_z_range / c;

                double[] hdgVec = new double[]{Utils.RadToGrad(hdg_roll) % (Math.PI * 2), Utils.RadToGrad(hdg_pitch) % (Math.PI * 2), Utils.RadToGrad(hdg_yaw) % (Math.PI * 2)};

                double[] unconvertedAccVector = Utils.convertToRotatedVectorSpace(new double[]{acc_x, acc_y, acc_z}, hdgVec);
                double[] accVec = Utils.getAccVector(unconvertedAccVector, oneG_calibrated);
                accVec = filter.filter(accVec, accVec); //new double[]{(acc_x_range / 2058) * 9.81, (acc_y_range / 2058) * 9.81, (acc_z_range / 2058) * 9.81}

                Vel[0] += accVec[0] * delta_T / 1000000;
                Vel[1] += accVec[1] * delta_T / 1000000;
                Vel[2] += ((accVec[2] * delta_T) - (9.81 * delta_T)) / 1000000;

                //processedData.add(new double[]{data.get(i)[0], Utils.getVecLength(Vel), accVec[0], accVec[1], accVec[2], acc_x / 2048, acc_y / 2048, acc_z / 2048});

                processedData.add(new double[]{data.get(i)[0], Utils.getVecLength(Vel), accVec[0], accVec[1], accVec[2], acc_x / 2048, acc_y / 2048, acc_z / 2048, Vel[0], Vel[1], Vel[2], data.get(i - 1)[8], tru_verticle_velocity, tru_accerleration});

            }
            // 8x 9 y 10 z
            if (processedData.get(processedData.size() - 1)[1] > 2) {
                filter.update(processedData.get(processedData.size() - 1));
            }
            // createDataSet with buffer function
            /*System.out.println("Dynamic Filter X: x_down: "+x_down+"      x_up: "+x_up);
            System.out.println("Dynamic Filter Y: y_down: "+y_down+"      y_up: "+y_up);`*/
            System.out.println("Vel Mag: " + processedData.get(processedData.size() - 1)[1]);

            if (old == processedData.get(processedData.size() - 1)[1]) break;
            else if (oldd == processedData.get(processedData.size() - 1)[1]) break;
            oldd = old;
            old = processedData.get(processedData.size() - 1)[1];

            if (ü == 1) {
                bufferData();
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
            }
            ü++;
        } while (processedData.get(processedData.size() - 1)[1] > 1);


        bufferData();
    }

    public void bufferData() {
        for (int i = bufferStrength; i < processedData.size(); i += bufferStrength) {
            double averagedVel = 0;
            double avaragedAcc_x = 0;
            double avaragedAcc_y = 0;
            double avaragedAcc_z = 0;
            double height = 0;
            double un_avaragedAcc_y = 0;
            double un_avaragedAcc_z = 0;
            for (int x = 0; x < bufferStrength; x++) {
                averagedVel += processedData.get(i - x)[s];
                avaragedAcc_x += processedData.get(i - x)[2] - 1;
                avaragedAcc_y += processedData.get(i - x)[3];
                avaragedAcc_z += processedData.get(i - x)[4];
                height += processedData.get(i - x)[11];
                un_avaragedAcc_y += processedData.get(i - x)[12];
                un_avaragedAcc_z += processedData.get(i - x)[13];
            }

            this.addValue(averagedVel / bufferStrength, "Velocity (m/s)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(avaragedAcc_x / bufferStrength / 9.81, "Acceleration x (m/s^2)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(avaragedAcc_y / bufferStrength / 9.81, "Acceleration y (m/s^2)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(avaragedAcc_z / bufferStrength / 9.81, "Acceleration z (m/s^2)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(height / bufferStrength, "Height (m)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(un_avaragedAcc_y / bufferStrength, "Tru_Verticle_velocity (m)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            this.addValue(un_avaragedAcc_z / bufferStrength, "true_acceleration (m/s^2)", String.valueOf((float) ((processedData.get(i - bufferStrength)[0] - processedData.get(0)[0]) / 1000000)));
            //this.addValue(un_avaragedAcc_z / bufferStrength, "un_Acceleration z (m/s)", String.valueOf(processedData.get(i - bufferStrength)[0] / 1000000));
        }
    }

    // timestamp OneG acc(x y z) hdg(z y x)
    double[][] createaccArray(ArrayList<double[]> data) {
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
