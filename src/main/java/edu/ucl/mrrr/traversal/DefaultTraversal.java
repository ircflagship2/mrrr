package edu.ucl.mrrr.traversal;

import edu.ucl.mrrr.callbacks.YamlJobCommonCallback;
import edu.ucl.mrrr.callbacks.YamlJobMapperCallback;
import edu.ucl.mrrr.callbacks.YamlJobReducerCallback;
import edu.ucl.mrrr.recipe.*;

/**
 * Created by jgeyti on 22/12/14.
 */
public class DefaultTraversal extends StepTraverser {

    public void traverse(Recipe recipe, YamlJobMapperCallback mapperCallback, YamlJobReducerCallback reducerCallback) throws Exception {
        traverseMappers(recipe, mapperCallback);
        traverseReducer(recipe, reducerCallback);
    }

    public void traverseMappers(Recipe recipe, YamlJobMapperCallback callback) throws Exception {
        callback.preMappers(recipe);

        for (MapperRecipe mapper : recipe.getMappers()) {
            traverseMapper(callback, mapper);
        }

        callback.postMappers(recipe);
    }

    public void traverseMapper(YamlJobMapperCallback callback, MapperRecipe mapper) throws Exception {
        callback.preMapper(mapper);
        while(callback.mapHasNext()) {
            traverseMapperSteps(callback, mapper);
        }
        callback.postMapper(mapper);
    }

    public void traverseMapperSteps(YamlJobMapperCallback callback, MapperRecipe mapper) throws Exception {
        for (MrrrStep mapStep : mapper.getSteps()) {
            step(mapStep, callback);
        }
    }

    public void traverseReducer(Recipe recipe, YamlJobReducerCallback callback) throws Exception {
        ReducerRecipe reducer = recipe.getReducer();
        callback.preReduce(recipe);
        while( callback.reducerHasNext() ) {
            traverseReduceSteps(callback, reducer);
        }
        callback.postReduce(recipe);
    }

    public void traverseReduceSteps(YamlJobReducerCallback callback, ReducerRecipe reducer) throws Exception {
        for (MrrrStep reduceStep : reducer.getSteps()) {
            step(reduceStep, callback);
        }
    }

}
