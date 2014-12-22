package edu.ucl.mrrr;

import edu.ucl.mrrr.callbacks.MethodCachingValidateCallback;
import edu.ucl.mrrr.callbacks.MethodInstance;
import edu.ucl.mrrr.recipe.Recipe;
import edu.ucl.mrrr.traversal.DefaultTraversal;
import org.apache.commons.io.FileUtils;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jkg on 15/09/2014.
 */
public abstract class YamlRunnerAbstract {

    protected String yamlRecipePath;
    protected Recipe recipe;

    /**
     * Create new instance, and parse YAML file immediately. Only validates
     * that input is a valid YAML file. Use validate() to validate recipe
     * semantics.
     *
     * @param yamlRecipePath
     * @throws IOException
     */
    public YamlRunnerAbstract(String yamlRecipePath) throws Exception {
        this.yamlRecipePath = yamlRecipePath;

        // Read YAML recipe (firstly, just to regular YAML)
        String rawYaml = FileUtils.readFileToString(new File(yamlRecipePath));
        Yaml yaml = new Yaml();
        recipe = yaml.loadAs(rawYaml, Recipe.class);

        // Validate it and get cached methods
        MethodCachingValidateCallback callback = new MethodCachingValidateCallback();
        DefaultTraversal traverser = new DefaultTraversal();
        traverser.traverse(recipe, callback);

        // Get the cached methods
        HashMap<Integer, MethodInstance> methods = callback.methods;
        //Map<String, Object> objects = callback.objects;

        setup(methods);
    }

    public abstract void setup(HashMap<Integer, MethodInstance> methods);
    public abstract void run();
}
