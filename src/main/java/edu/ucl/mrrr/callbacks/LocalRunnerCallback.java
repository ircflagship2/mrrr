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

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;

/**
 * Created by jgeyti on 18/12/14.
 */
public class LocalRunnerCallback implements YamlJobTraversalCallback {

    private RecordReader recordReader;

    private enum Stage { MAP, REDUCE }
    private Stage stage;

    private HashMap<Integer, MethodInstance> methods;
    private Object currentValue;
    private Object currentKey;
    public Map<String, Object> globalVariables = new HashMap<String, Object>();
    public Map<String, Object> variables = new HashMap<String, Object>();

    private Map<Writable, List<WritableComparable>> mapEmits = new HashMap<Writable, List<WritableComparable>>();
    private Iterator<Map.Entry<Writable, List<WritableComparable>>> reduceIterator;

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

            // initialise variables


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
    }

    @Override
    public void postReduce(Recipe recipe) {

    }

    @Override
    public void preIf(MrrrMethodCall anIf, int hashCode) {

    }

    @Override
    public Boolean evaluateIf(MrrrMethodCall anIf, int hashCode) {
        // Get method and instance to call it on
        MethodInstance methodPair = methods.get(hashCode);
        Object instance = methodPair.instance;
        Method method = methodPair.method;

        // Get input
        Object[] arguments = resolveArguments(anIf.getArguments());

        try {
            return (Boolean)method.invoke(instance, arguments);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }

        System.err.println("No boolean returned"); //todo: err handling
        return false;
    }

    @Override
    public void preThen(List<MrrrStep> then, Boolean condition, int hashCode) {

    }

    @Override
    public void postThen(List<MrrrStep> then, Boolean condition, int hashCode) {

    }

    @Override
    public void preElse(List<MrrrStep> anElse, Boolean condition, int hashCode) {

    }

    @Override
    public void postElse(List<MrrrStep> anElse, Boolean condition, int hashCode) {

    }

    @Override
    public void postIf(MrrrMethodCall anIf, int hashCode) {

    }

    @Override
    public void preFor(MrrrFor foreach, int hashCode) {

    }

    @Override
    public Iterable getForIterable(MrrrFor foreach, int hashCode) {
        Object iter = variables.get(foreach.collection);

        return (Iterable)iter;

    }

    @Override
    public void setForValue(String variable, Object obj) {
        variables.put(variable, obj);
    }

    @Override
    public void postFor(MrrrFor foreach, int hashCode) {
        variables.remove(foreach.as);
    }

    @Override
    public void emit(MrrrEmitKeyVal emit, int hashCode) {
        Writable key = simpleTypeToWritable(resolveArgument(emit.getKey()));
        WritableComparable value = (WritableComparable)simpleTypeToWritable(resolveArgument(emit.getValue()));

        if (stage == Stage.MAP) {
            List<WritableComparable> writables = mapEmits.get(key);
            if (writables == null) {
                writables = new ArrayList<WritableComparable>();
                mapEmits.put(key, writables);
            }
            writables.add(value);
        } else {
            System.out.println(key + "," + value);
        }
    }

    @Override
    public void call(MrrrMethodCall do_, int hashCode) {
        // Get method and instance to call it on
        MethodInstance methodPair = methods.get(hashCode);
        Object instance = methodPair.instance;
        Method method = methodPair.method;

        // Get input
        Object[] arguments = resolveArguments(do_.getArguments());

        try {
            Object obj = method.invoke(instance, arguments);
            String retVar = do_.getReturnVariable();
            if (retVar != null) {
                variables.put(retVar, obj);
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            System.err.println("InvocationException: " + method);
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

    private Object[] resolveArguments(List<MrrrArgument> mrrrArguments) {
        List<Object> args = new ArrayList<Object>();
        for (MrrrArgument yamlArg : mrrrArguments) {
            args.add(resolveArgument(yamlArg));
        }
        return args.toArray(new Object[args.size()]);
    }

    private Object resolveArgument(MrrrArgument yamlArg) {
            if (yamlArg.getType() == MrrrArgument.Type.VARIABLE) {
                return variables.get((String) yamlArg.getValue());
            } else {
                return yamlArg.getValue();
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
