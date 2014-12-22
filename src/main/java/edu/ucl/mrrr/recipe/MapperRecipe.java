package edu.ucl.mrrr.recipe;

import edu.ucl.mrrr.callbacks.YamlJobTraversalCallback;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jgeyti on 28/10/2014.
 */
public class MapperRecipe {
    private String input;
    private String inputformat = "org.apache.hadoop.mapreduce.lib.input.TextInputFormat";
    private Map<String, MrrrObject> objects = new HashMap<String, MrrrObject>();
    private List<MrrrStep> steps;

    public String getInput() {
        return input;
    }
    public void setInput(String input) {
        this.input = input;
    }

    public String getInputformat() {
        return inputformat;
    }
    public void setInputformat(String inputformat) {
        this.inputformat = inputformat;
    }

    public List<MrrrStep> getSteps() {
        return steps;
    }
    public void setSteps(List<MrrrStep> steps) {
        this.steps = steps;
    }

    public Map<String, MrrrObject> getObjects() {
        return objects;
    }
    public void setObjects(Map<String, MrrrObject> objects) {
        this.objects = objects;
    }

}
