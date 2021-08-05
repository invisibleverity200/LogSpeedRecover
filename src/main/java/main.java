import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;

import javax.swing.*;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class main {
    public static void main(String[] args) throws IOException {
        ArrayList<double[]> data = new ArrayList<>();
        JFileChooser chooser = new JFileChooser();
        chooser.addChoosableFileFilter(new FileNameExtensionFilter("csv","csv"));
        int retVal = chooser.showOpenDialog(null);
        JFreeChart lineChartObject = null;
        if (retVal == JFileChooser.APPROVE_OPTION) {
            data = LogParser.parseLog(chooser.getSelectedFile().getPath());
            VelDataSet dataSet = new VelDataSet(data);
            /*lineChartObject = ChartFactory.createLineChart(
                    "Drone Velocity Magnitude", "Time (s)",
                    "Velocity (m/s)",
                    dataSet, PlotOrientation.VERTICAL,
                    true, true, false);*/
        }
       /* ChartPanel panel = new ChartPanel(lineChartObject);
        JFrame frame = new JFrame("Vel Plotter");
        frame.setContentPane(panel);
        frame.setSize(800, 700);
        frame.setVisible(true);*/
    }
}
