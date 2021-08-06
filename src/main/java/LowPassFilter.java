import org.apache.commons.math3.complex.Complex;
import org.apache.commons.math3.transform.DftNormalization;
import org.apache.commons.math3.transform.FastFourierTransformer;
import org.apache.commons.math3.transform.TransformType;

public class LowPassFilter {



    public static double[] filter(double[] data, double lowPass, double frequency) {

//FOURIER SAVE ME PLLSSS
        int minPowerOf2 = 1;
        while (minPowerOf2 < data.length)
            minPowerOf2 = 2 * minPowerOf2;


        double[] padded = new double[minPowerOf2];
        for (int i = 0; i < data.length; i++)
            padded[i] = data[i];


        FastFourierTransformer transformer = new FastFourierTransformer(DftNormalization.STANDARD);
        Complex[] fourierTransform = transformer.transform(padded, TransformType.FORWARD);


        double[] frequencyDomain = new double[fourierTransform.length];
        for (int i = 0; i < frequencyDomain.length; i++)
            frequencyDomain[i] = frequency * i / (double) fourierTransform.length;


        double[] keepPoints = new double[frequencyDomain.length];
        keepPoints[0] = 1;
        for (int i = 1; i < frequencyDomain.length; i++) {
            if (frequencyDomain[i] < lowPass)
                keepPoints[i] = 2;
            else
                keepPoints[i] = 0;
        }


        for (int i = 0; i < fourierTransform.length; i++)
            fourierTransform[i] = fourierTransform[i].multiply((double) keepPoints[i]);


        Complex[] reverseFourier = transformer.transform(fourierTransform, TransformType.INVERSE);


        double[] result = new double[data.length];
        for (int i = 0; i < result.length; i++) {
            result[i] = reverseFourier[i].getReal();
        }

        return result;
    }
}

