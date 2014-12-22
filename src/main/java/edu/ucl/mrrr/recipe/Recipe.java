package edu.ucl.mrrr.recipe;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by jkg on 28/03/2014.
 */
public class Recipe implements Serializable {
    private String jobname;
    private String output;
    private Map<String, Object> conf = new HashMap<String, Object>();
    private List<MapperRecipe> mappers;
    private ReducerRecipe reducer;

    public String getJobname() {
        return jobname;
    }
    public void setJobname(String jobname) {
        this.jobname = jobname;
    }

    public String getOutput() {
        return output;
    }
    public void setOutput(String output) {
        this.output = output;
    }

    public Map<String, Object> getConf() {
        return conf;
    }
    public void setConf(Map<String, Object> conf) {
        this.conf = conf;
    }

    public List<MapperRecipe> getMappers() {
        return mappers;
    }
    public void setMappers(List<MapperRecipe> mappers) {
        this.mappers = mappers;
    }

    public ReducerRecipe getReducer() { return reducer; }
    public void setReducer(ReducerRecipe reducer) { this.reducer = reducer; }



    private void traverseMapStep(MrrrStep mapStep) {

    }
}