package edu.ucl.mrrr.traversal;

import edu.ucl.mrrr.callbacks.YamlJobTraversalCallback;
import edu.ucl.mrrr.recipe.*;

/**
 * Created by jgeyti on 22/12/14.
 */
public class DefaultTraversal {
    public void traverse(Recipe recipe, YamlJobTraversalCallback callback) throws Exception {

        callback.preMappers(recipe);

        for (MapperRecipe mapper : recipe.getMappers()) {

            callback.preMapper(mapper);
            while(callback.mapHasNext()) {
                for (MrrrStep mapStep : mapper.getSteps()) {
                    step(mapStep, callback);
                }
            }
            callback.postMapper(mapper);
        }

        callback.postMappers(recipe);

        ReducerRecipe reducer = recipe.getReducer();
        callback.preReduce(recipe);
        while( callback.reducerHasNext() ) {
            for (MrrrStep reduceStep : reducer.getSteps()) {
                step(reduceStep, callback);
            }
        }
        callback.postReduce(recipe);
    }

    private void step(MrrrStep step, YamlJobTraversalCallback callback) {

        if (step.getIf() != null) {
            // is an IF conditional step
            callback.preIf(step.getIf(), step.hashCode());
            Boolean condition = callback.evaluateIf(step.getIf(), step.hashCode());

            // Run THEN?
            if (condition == null || condition == true) {
                callback.preThen(step.getThen(), condition, step.hashCode());
                for (MrrrStep thenstep : step.getThen()) {
                    step(thenstep, callback);
                }
                callback.postThen(step.getThen(), condition, step.hashCode());
            }

            // Run ELSE?
            if (step.getElse() != null && (condition == null || !condition)) {
                callback.preElse(step.getElse(), condition, step.hashCode());
                for (MrrrStep elsestep : step.getElse()) {
                    step(elsestep, callback);
                }
                callback.postElse(step.getElse(), condition, step.hashCode());
            }

            callback.postIf(step.getIf(), step.hashCode());
        } else if (step.getFor() != null) {
            // is a FOREACH LOOP?
            MrrrFor foreach = step.getFor();
            if (foreach != null) {
                //traverse for loop
                callback.preFor(foreach, step.hashCode());
                Iterable iter = callback.getForIterable(foreach, step.hashCode());
                String as = foreach.as;
                for (Object obj : iter) {
                    callback.setForValue(as, obj);
                    for (MrrrStep forStep : step.getSteps()) {
                        step(forStep, callback);
                    }
                }
                callback.postFor(step.getFor(), step.hashCode());
            }
        } else if (step.getEmit() != null) {
            // is an EMIT
            callback.emit(step.getEmit(), step.hashCode());
        } else {
            // this is a regular DO clause
            callback.call(step.getDo(), step.hashCode());
        }
    }
}
