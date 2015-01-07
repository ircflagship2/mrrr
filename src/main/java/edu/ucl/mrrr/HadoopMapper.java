package edu.ucl.mrrr;

import edu.ucl.mrrr.callbacks.HadoopMapperCallback;
import edu.ucl.mrrr.callbacks.MethodInstance;
import edu.ucl.mrrr.callbacks.YamlJobMapperCallback;
import edu.ucl.mrrr.recipe.MapperRecipe;
import edu.ucl.mrrr.recipe.MrrrStep;
import edu.ucl.mrrr.recipe.Recipe;
import edu.ucl.mrrr.traversal.DefaultTraversal;
import edu.ucl.mrrr.traversal.HadoopTraversal;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.GlobFilter;
import org.apache.hadoop.fs.GlobPattern;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.PathFilter;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.InputSplit;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.lib.input.FileSplit;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jgeyti on 04/01/15.
 */
public class HadoopMapper extends Mapper {

    public static final String DIR_MRRR_MAPPERS = "mrrr.conf.mappers";

    private HashMap<Integer, MethodInstance> methods;
    private HadoopTraversal traverser;
    private Recipe recipe;
    private MapperRecipe mrrrMapper;
    private HadoopMapperCallback callback;

    @Override
    protected void setup(Context context) throws IOException, InterruptedException {
        this.traverser = new HadoopTraversal();
        this.callback = new HadoopMapperCallback(context);

        Configuration conf = context.getConfiguration();
        // Get recipe from job context
        String mrrr_recipe = conf.get("mrrr_recipe");

        Integer mapperIdx = getMapperIndex(context);

        // create HadoopRunner, and get cached methods
        try {
            DummyRunner runner = new DummyRunner(mrrr_recipe);
            this.recipe = runner.recipe;
            this.methods = runner.methods;
            callback.methods = runner.methods;

            // Get the mrrr mapper recipe to run for this input
            this.mrrrMapper = runner.recipe.getMappers().get(mapperIdx);
            callback.preMapper(this.mrrrMapper);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void cleanup(Context context) throws IOException, InterruptedException {
        callback.postMapper(mrrrMapper);
    }

    @Override
    protected void map(Object key, Object value, Context context) throws IOException, InterruptedException {
        // Send this key and value pair through the pipeline
        callback.variables.put("key", key);
        callback.variables.put("value", value);

        try {
            traverser.traverseMapperSteps(callback, mrrrMapper);
        } catch (Exception e) {
            throw new IOException(e.getMessage());
            //e.printStackTrace();
        }
    }

    /**
     * http://stackoverflow.com/questions/11130145/hadoop-multipleinputs-fails-with-classcastexception
     * todo: this is some nasty shit, using reflection to access the private class TaggedInputsplit
     *       but I can't think of an alternative as it is.
     *
     * @param inputSplit
     * @return
     */
    private FileSplit inputSplitToFileSplit(InputSplit inputSplit) throws IOException {

        Class<? extends InputSplit> splitClass = inputSplit.getClass();

        FileSplit fileSplit = null;
        if (splitClass.equals(FileSplit.class)) {
            fileSplit = (FileSplit) inputSplit;
        } else if (splitClass.getName().equals("org.apache.hadoop.mapreduce.lib.input.TaggedInputSplit")) {
            try {
                Method getInputSplitMethod = splitClass.getDeclaredMethod("getInputSplit");
                getInputSplitMethod.setAccessible(true);
                fileSplit = (FileSplit) getInputSplitMethod.invoke(inputSplit);
            } catch (Exception e) {
                // wrap and re-throw error
                throw new IOException(e);
            }
        }

        return fileSplit;
    }

    private Integer getMapperIndex(Context context) throws IOException {
        // Get path and determine mapper recipe to run from that
        FileSplit fileSplit = inputSplitToFileSplit(context.getInputSplit());
        String inputPath = fileSplit.getPath().toString();

        context.getCounter("inputPaths", inputPath).increment(1);

        for (String pathMapperNo : context.getConfiguration().get(DIR_MRRR_MAPPERS).split(",")) {
            String[] pathMapperNoArr = pathMapperNo.split(";");
            String pathFilter = pathMapperNoArr[0];
            Integer mapperNo = Integer.parseInt(pathMapperNoArr[1]);

            boolean accept = new GlobPattern(pathFilter).matches(inputPath);

            if (accept) {
                context.getCounter("paths", pathFilter + " => " + mapperNo).increment(1);
                return mapperNo;
            }
        }

        return null;
    }

}
