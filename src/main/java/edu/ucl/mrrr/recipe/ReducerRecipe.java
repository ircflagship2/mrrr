package edu.ucl.mrrr.recipe;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jgeyti on 16/12/14.
 */
public class ReducerRecipe {
    private Map<String, MrrrObject> objects = new HashMap<String, MrrrObject>();
    private List<MrrrStep> steps;

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
