import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;

public class LogParser {
    public static ArrayList parseLog(String fileName) throws IOException {
        ArrayList<double[]> points = new ArrayList();
        Reader in = new FileReader(fileName);
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.parse(in);
        int oneGValue = 0;
        boolean header = true;

        for (CSVRecord record : records) {
            if (record.get(0).equals("acc_1G") && header) oneGValue = Integer.valueOf(record.get(1));
            if (!header) {
                double timeStamp = Double.valueOf(record.get(1));
                double acc_z = Double.valueOf(record.get(30));
                double acc_y = Double.valueOf(record.get(29));
                double acc_x = Double.valueOf(record.get(28));
                double hdg_roll = Double.valueOf(record.get(44));
                double hdg_pitch = Double.valueOf(record.get(45));
                double hdg_yaw = Double.valueOf(record.get(46));
                double height = Double.valueOf(record.get(23)) * ((0.6/57));
                points.add(new double[]{timeStamp, oneGValue, acc_x, acc_y, acc_z, hdg_roll, hdg_pitch, hdg_yaw, height});
            }
            if (record.get(1).equals("time") && header) header = false;
        }
        return points;
    }
}
