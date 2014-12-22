package edu.ucl.mrrr;

import edu.ucl.mrrr.callbacks.LocalRunnerCallback;
import edu.ucl.mrrr.callbacks.MethodInstance;
import edu.ucl.mrrr.recipe.MapperRecipe;
import edu.ucl.mrrr.traversal.DefaultTraversal;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.InputFormat;
import org.apache.hadoop.mapreduce.RecordReader;
import org.apache.hadoop.mapreduce.TaskAttemptContext;
import org.apache.hadoop.mapreduce.TaskAttemptID;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;
import org.apache.hadoop.mapreduce.task.TaskAttemptContextImpl;
import org.apache.hadoop.util.ReflectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

/**
 * Created by jkg on 15/09/2014.
 */
public class YamlRunnerLocal extends YamlRunnerAbstract {

    LocalRunnerCallback callback;
    DefaultTraversal traverser;
    public YamlRunnerLocal(String yamlRecipePath) throws Exception {
        super(yamlRecipePath);
    }

    @Override
    public void run() {
        try {
            traverser.traverse(recipe, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setup(HashMap<Integer, MethodInstance> methods) {
        // create traversable input
        RecordReader recordReader;

        callback = new LocalRunnerCallback(methods);
        traverser = new DefaultTraversal();




//        try {
//            callback.preMappers(recipe);
//            for (Object mapperO : recipe.getMappers()) {
//
//                MapperRecipe mapper = (MapperRecipe)mapperO;
//                // resolve the inputformat, and create an recordreader instance that can feed this type of data
//                Configuration conf = new Configuration(false);
//                conf.set("fs.default.name", "file:///");
//
//                Class<InputFormat<?,?>> inputFormatClass = (Class<InputFormat<?, ?>>) Class.forName(mapper.getInputformat());
//                InputFormat inputFormat = ReflectionUtils.newInstance(inputFormatClass, conf);
//
//                // resolve the input
//                File testFile = new File(mapper.getInput());
//                Path path = new Path(testFile.getAbsoluteFile().toURI());
//                FileSplit split = new FileSplit(path, 0, testFile.length(), null);
//
//                TaskAttemptContext context = new TaskAttemptContextImpl(conf, new TaskAttemptID());
//                recordReader = inputFormat.createRecordReader(split, context);
//                recordReader.initialize(split, context);
//
//                // Initia
//                // iteratate through the input
//                while(recordReader.nextKeyValue()) {
//                    Object key = recordReader.getCurrentKey();
//                    Object val = recordReader.getCurrentValue();
//                    callback.setInput(key, val);
//
//                    mapper.traverse(callback);
//                }
//            }
//            callback.postMappers(recipe);
//
//            callback.preReduce(recipe);
//            recipe.getReducer().traverse(callback);
//            callback.postReduce(recipe);
//
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }


    }


    public static void main(String[] args) throws Exception {
        YamlRunnerAbstract yamlRunner = new YamlRunnerLocal(args[0]);
        yamlRunner.run();
    }

}
