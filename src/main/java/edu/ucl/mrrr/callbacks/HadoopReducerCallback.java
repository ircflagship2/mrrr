package edu.ucl.mrrr.callbacks;

import edu.ucl.mrrr.recipe.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Reducer;

import java.util.List;

/**
 * Created by jgeyti on 22/12/14.
 */
public class HadoopReducerCallback extends CommonRunnerCallback implements YamlJobReducerCallback {

    private final Reducer.Context context;

    public HadoopReducerCallback(Reducer.Context context) {
        this.context = context;
    }

    @Override
    public void preReduce(Recipe recipe) {

    }

    @Override
    public void postReduce(Recipe recipe) {

    }

    @Override
    public boolean reducerHasNext() {
        // Not used
        return false;
    }

    @Override
    public void emit(MrrrEmitKeyVal emit, int hashCode) throws Exception {
        Writable key = simpleTypeToWritable(resolveArgument(emit.getKey()));
        WritableComparable value = (WritableComparable)simpleTypeToWritable(resolveArgument(emit.getValue()));

        context.getCounter("reduce_emit", emit.getKey().getValue().toString()).increment(1);

        context.write(key, value);
    }

    private Writable simpleTypeToWritable(Object in) {
        if (in instanceof String) {
            return new Text((String)in);
        } else if (in instanceof Integer) {
            return new IntWritable((Integer)in);
        } else if (in instanceof Float) {
            return new FloatWritable((Float)in);
        } else if (in instanceof Double) {
            return new DoubleWritable((Double)in);
        } else if (in instanceof Boolean) {
            return new BooleanWritable((Boolean)in);
        }
        return (Writable)in;
    }
}
