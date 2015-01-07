package edu.ucl.mrrr.callbacks;

import edu.ucl.mrrr.recipe.*;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.List;

/**
 * Created by jgeyti on 22/12/14.
 */
public class HadoopMapperCallback extends CommonRunnerCallback implements YamlJobMapperCallback  {

    private Mapper.Context context;

    public HadoopMapperCallback(Mapper.Context context) {
        this.context = context;
    }

    @Override
    public void preMappers(Recipe job) {
        // never called
    }

    @Override
    public void preMapper(MapperRecipe mapper) {

    }

    @Override
    public void postMapper(MapperRecipe mapper) {
        // never called
    }

    @Override
    public void postMappers(Recipe job) {

    }

    @Override
    public boolean mapHasNext() {
        // never used
        return false;
    }

    @Override
    public void emit(MrrrEmitKeyVal emit, int hashCode) throws Exception {
        Writable key = simpleTypeToWritable(resolveArgument(emit.getKey()));
        WritableComparable value = (WritableComparable)simpleTypeToWritable(resolveArgument(emit.getValue()));

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
