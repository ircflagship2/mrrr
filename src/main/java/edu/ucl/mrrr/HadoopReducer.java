package edu.ucl.mrrr;

import edu.ucl.mrrr.callbacks.*;
import edu.ucl.mrrr.recipe.Recipe;
import edu.ucl.mrrr.traversal.DefaultTraversal;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Created by jgeyti on 04/01/15.
 */
public class HadoopReducer extends Reducer {

    private HashMap<Integer, MethodInstance> methods;
    private DefaultTraversal traverser;
    private Recipe recipe;
    private HadoopReducerCallback callback;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        this.traverser = new DefaultTraversal();
        this.callback = new HadoopReducerCallback(context);

        // Get recipe from job context
        String mrrr_recipe = context.getConfiguration().get("mrrr_recipe");

        // create HadoopRunner, and get cached methods
        try {
            DummyRunner runner = new DummyRunner(mrrr_recipe);
            this.recipe = runner.recipe;
            this.methods = runner.methods;
            callback.methods = runner.methods;

            callback.preReduce(recipe);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        callback.postReduce(recipe);
    }

    @Override
    protected void reduce(Object key, Iterable values, Context context) throws IOException, InterruptedException {

        callback.variables.put("key", key);
        callback.variables.put("values", values);

//        for ()
        Iterator iterator = values.iterator();
        int i = 0;
        while (iterator.hasNext()) {
            iterator.next();
            i++;
        }
        context.getCounter("reduce_values", ""+i);

        context.getCounter("reducer", key.toString()).increment(1);

        try {
            traverser.traverseReduceSteps(callback, recipe.getReducer());
        } catch (Exception e) {
            throw new IOException(e.getMessage());
            //e.printStackTrace();
        }
    }
}
