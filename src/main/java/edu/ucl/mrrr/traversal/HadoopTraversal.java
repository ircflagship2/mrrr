package edu.ucl.mrrr.traversal;

import edu.ucl.mrrr.callbacks.YamlJobCommonCallback;
import edu.ucl.mrrr.callbacks.YamlJobMapperCallback;
import edu.ucl.mrrr.callbacks.YamlJobReducerCallback;
import edu.ucl.mrrr.recipe.*;

/**
 * Created by jgeyti on 22/12/14.
 */
public class HadoopTraversal extends StepTraverser {

    public void traverseMapperSteps(YamlJobMapperCallback callback, MapperRecipe mapper) throws Exception {
        for (MrrrStep mapStep : mapper.getSteps()) {
            step(mapStep, callback);
        }
    }

    public void traverseReducer(Recipe recipe, YamlJobReducerCallback callback) throws Exception {
        ReducerRecipe reducer = recipe.getReducer();
        callback.preReduce(recipe);
        while( callback.reducerHasNext() ) {
            for (MrrrStep reduceStep : reducer.getSteps()) {
                step(reduceStep, callback);
            }
        }
        callback.postReduce(recipe);
    }


}