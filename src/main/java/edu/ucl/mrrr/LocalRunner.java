package edu.ucl.mrrr;

import edu.ucl.mrrr.callbacks.LocalRunnerCallback;
import edu.ucl.mrrr.callbacks.MapperMetaData;
import edu.ucl.mrrr.callbacks.MethodInstance;
import edu.ucl.mrrr.callbacks.ReducerMetaData;
import edu.ucl.mrrr.traversal.DefaultTraversal;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.mapreduce.RecordReader;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jkg on 15/09/2014.
 */
public class LocalRunner extends AbstractRunner {

    LocalRunnerCallback callback;
    DefaultTraversal traverser;

    public LocalRunner(String yamlRecipePath) throws Exception {
        super(FileUtils.readFileToString(new File(yamlRecipePath)));
    }

    @Override
    public void setup(HashMap<Integer, MethodInstance> methods, List<MapperMetaData> mapperMetaData, ReducerMetaData reduceMetaData) throws Exception {
        // create traversable input
        RecordReader recordReader;

        callback = new LocalRunnerCallback(methods);
        traverser = new DefaultTraversal();
    }

    @Override
    public void run() {
        try {
            traverser.traverse(recipe, callback, callback);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    public static void main(String[] args) throws Exception {
        AbstractRunner yamlRunner = new LocalRunner(args[0]);
        yamlRunner.run();
    }

}
