package edu.ucl.mrrr.callbacks;

import edu.ucl.mrrr.recipe.*;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by jgeyti on 18/12/14.
 */
public class LocalRunnerCallback extends CommonRunnerCallback implements YamlJobMapperCallback, YamlJobReducerCallback {

    private RecordReader recordReader;

    private enum Stage { MAP, REDUCE }
    private Stage stage;


    private Object currentValue;
    private Object currentKey;
    public Map<String, Object> globalVariables = new HashMap<String, Object>();


    private Map<Writable, List<WritableComparable>> mapEmits = new HashMap<Writable, List<WritableComparable>>();
    private Iterator<Map.Entry<Writable, List<WritableComparable>>> reduceIterator;
    private BufferedWriter reduceWriter;

    public LocalRunnerCallback(HashMap<Integer, MethodInstance> methods) {
        this.methods = methods;
    }

    @Override
    public void preMappers(Recipe job) {

        stage = Stage.MAP;

        for (Object objO : job.getConf().entrySet()) {
            Map.Entry<String, Object> obj = (Map.Entry<String, Object>)objO;
            globalVariables.put(obj.getKey(), obj.getValue());
        }
    }

    @Override
    public void postMappers(Recipe job) {

    }

    @Override
    public void preMapper(MapperRecipe mapStep) {
        // reset variables
        resetVariables();
        // set input path type, so it can be references correctly
        variables.put("path", mapStep.getInput());

        // create recordreader
        // resolve the inputformat, and create an recordreader instance that can feed this type of data
        Configuration conf = new Configuration(false);
        conf.set("fs.default.name", "file:///");
        try {
            Class<InputFormat<?,?>> inputFormatClass = (Class<InputFormat<?, ?>>) Class.forName(mapStep.getInputformat());
            InputFormat inputFormat = ReflectionUtils.newInstance(inputFormatClass, conf);
            File testFile = new File(mapStep.getInput());
            Path path = new Path(testFile.getAbsoluteFile().toURI());
            FileSplit split = new FileSplit(path, 0, testFile.length(), null);

            TaskAttemptContext context = new TaskAttemptContextImpl(conf, new TaskAttemptID());
            recordReader = inputFormat.createRecordReader(split, context);
            recordReader.initialize(split, context);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        // resolve the input

    }

    @Override
    public void postMapper(MapperRecipe mapStep) {

    }

    @Override
    public void preReduce(Recipe recipe) {
        stage = Stage.REDUCE;

        // reset variables
        resetVariables();

        // sort the values, as is done by hadoop
        for (Map.Entry<Writable, List<WritableComparable>> entry : mapEmits.entrySet()) {
            Collections.sort(entry.getValue());
        }

        // Create the reduce iterator
        reduceIterator = mapEmits.entrySet().iterator();

        // Open up output file (the localrunner only creates one)
        File file = new File(recipe.getOutput() + "/part-r-00000");

        // if file doesnt exists, then create it
        try {
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            reduceWriter = new BufferedWriter(fw);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    @Override
    public void postReduce(Recipe recipe) {
        try {
            reduceWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void emit(MrrrEmitKeyVal emit, int hashCode) {
        Writable key = simpleTypeToWritable(resolveArgument(emit.getKey()));
        WritableComparable value = (WritableComparable)simpleTypeToWritable(resolveArgument(emit.getValue()));

        if (stage == Stage.MAP) {
            mapEmit(key, value);
        } else {
            reduceEmit(key, value);
        }
    }

    private void mapEmit(Writable key, WritableComparable value) {
        List<WritableComparable> writables = mapEmits.get(key);
        if (writables == null) {
            writables = new ArrayList<WritableComparable>();
            mapEmits.put(key, writables);
        }
        writables.add(value);
    }

    private void reduceEmit(Writable key, WritableComparable value) {
        System.out.println(key + "," + value);
        try {
            reduceWriter.write(key + "," + value + "\n");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean mapHasNext() {
        try {
            boolean hasNext = recordReader.nextKeyValue();

            if (hasNext) {
                variables.put("key", recordReader.getCurrentKey());
                variables.put("value", recordReader.getCurrentValue());
            }

            return hasNext;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return false;
    }

    @Override
    public boolean reducerHasNext() {

        if (reduceIterator.hasNext()) {
            Map.Entry<Writable, List<WritableComparable>> next = reduceIterator.next();
            variables.put("key", next.getKey());
            variables.put("values", next.getValue());
            return true;
        } else {
            return false;
        }
    }

    private void resetVariables() {
        variables.clear();
        for (Map.Entry<String, Object> keyVal : globalVariables.entrySet()) {
            variables.put(keyVal.getKey(), keyVal);
        }
    }



    public Writable simpleTypeToWritable(Object in) {
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
