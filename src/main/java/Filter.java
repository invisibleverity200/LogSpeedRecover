public interface Filter {
    void update(double[] signal);

    double[] filter(double[] vec, double[] vec_two);
}
