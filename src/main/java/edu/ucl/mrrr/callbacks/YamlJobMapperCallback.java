package edu.ucl.mrrr.callbacks;

import edu.ucl.mrrr.recipe.*;

import java.util.List;

/**
 * Created by jkg on 16/09/2014.
 */
public interface YamlJobMapperCallback extends YamlJobCommonCallback {
    public void preMappers(Recipe job);
    public void preMapper(MapperRecipe mapStep);
    public void postMapper(MapperRecipe mapStep);
    public void postMappers(Recipe job);

    boolean mapHasNext();
}
