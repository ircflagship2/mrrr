package edu.ucl.mrrr.callbacks;

import edu.ucl.mrrr.recipe.*;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;
import java.util.List;

/**
 * Created by jgeyti on 22/12/14.
 */
public class HadoopRunnerCallback implements YamlJobTraversalCallback  {

    public class HadoopRunnerMapper extends Mapper<Writable, Writable, Writable, Writable> {

        @Override
        protected void setup(Context context) throws IOException, InterruptedException {
            String mrrr_recipe = context.getConfiguration().get("mrrr_recipe");

        }

        @Override
        protected void map(Writable key, Writable value, Context context) throws IOException, InterruptedException {
            super.map(key, value, context);
        }


    }

    @Override
    public void preMappers(Recipe job) {

    }

    @Override
    public void postMappers(Recipe job) {

    }

    @Override
    public void preMapper(MapperRecipe mapStep) {

    }

    @Override
    public void postMapper(MapperRecipe mapStep) {

    }

    @Override
    public void preReduce(Recipe recipe) {

    }

    @Override
    public void postReduce(Recipe recipe) {

    }

    @Override
    public void preIf(MrrrMethodCall anIf, int hashCode) {

    }

    @Override
    public Boolean evaluateIf(MrrrMethodCall anIf, int hashCode) {
        return null;
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
        return null;
    }

    @Override
    public void setForValue(String variable, Object obj) {

    }

    @Override
    public void postFor(MrrrFor foreach, int hashCode) {

    }

    @Override
    public void emit(MrrrEmitKeyVal emit, int hashCode) {

    }

    @Override
    public void call(MrrrMethodCall do_, int hashCode) {

    }

    @Override
    public boolean mapHasNext() {
        return false;
    }

    @Override
    public boolean reducerHasNext() {
        return false;
    }
}
