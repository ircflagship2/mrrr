package edu.ucl.mrrr;

import edu.ucl.mrrr.callbacks.MapperMetaData;
import edu.ucl.mrrr.callbacks.MethodCachingValidateCallback;
import edu.ucl.mrrr.callbacks.MethodInstance;
import edu.ucl.mrrr.callbacks.ReducerMetaData;
import edu.ucl.mrrr.recipe.Recipe;
import edu.ucl.mrrr.traversal.DefaultTraversal;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jkg on 15/09/2014.
 */
public abstract class AbstractRunner {

    public String rawRecipe;
    public Recipe recipe;
    public HashMap<Integer, MethodInstance> methods;

    public AbstractRunner(String yamlRecipe) throws Exception {
        parseYaml(yamlRecipe);
    }

    private void parseYaml(String rawYaml) throws Exception {
        Yaml yaml = new Yaml();
        rawRecipe = rawYaml;
        recipe = yaml.loadAs(rawYaml, Recipe.class);

        // Validate it and get cached methods
        MethodCachingValidateCallback callback = new MethodCachingValidateCallback();
        DefaultTraversal traverser = new DefaultTraversal();
        traverser.traverse(recipe, callback, callback);

        // Get the cached methods
        this.methods = callback.methods;

        // Store other information, required to set up a hadoop instance

        setup(methods, callback.mapperMetaData, callback.reduceMetaData);
    }

    public abstract void setup(HashMap<Integer, MethodInstance> methods, List<MapperMetaData> mapperMetaData, ReducerMetaData reduceMetaData) throws Exception;
    public abstract void run();
}
