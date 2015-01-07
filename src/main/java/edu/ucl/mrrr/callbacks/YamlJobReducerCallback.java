package edu.ucl.mrrr.callbacks;

import edu.ucl.mrrr.recipe.*;

import java.util.List;

/**
 * Created by jkg on 16/09/2014.
 */
public interface YamlJobReducerCallback extends YamlJobCommonCallback {

    public void preReduce(Recipe recipe);
    public void postReduce(Recipe recipe);

    // Regular Step
//    void call(MrrrMethodCall do_, int hashCode);

    boolean reducerHasNext();
}
