package edu.ucl.mrrr.helpers;

import org.apache.hadoop.io.IntWritable;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jgeyti on 18/12/14.
 */
public class MathUtil {

    public Integer length(Iterable list) {
        return 10;
    }

    public Double sum(Iterable<Number> numbers) {
        double sum = 0;
        for (Number number : numbers) {
            sum += number.doubleValue();
        }

        return sum;
    }

    public Double sum(Integer[] numbers) {
        double sum = 0;
        for (Integer number : numbers) {
            sum += number.doubleValue();
        }

        return sum;
    }

    public Double sum(List<Integer> numbers) {
        double sum = 0;
        for (Integer number : numbers) {
            sum += number.doubleValue();
        }

        return sum;
    }

    public Double sum(ArrayList numbers) {
        double sum = 0;
        for (Object number : numbers) {
            sum += ((IntWritable)number).get() + 0.0;
        }

        return sum;
    }

    public Integer intSum(ArrayList<Integer> numbers) {
        double sum = 0;
        for (Object number : numbers) {
            sum += ((Integer)number).doubleValue();
        }

        return Integer.parseInt(String.valueOf(Math.round(sum)));
    }
}
