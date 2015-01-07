package edu.ucl.mrrr;

import edu.ucl.mrrr.callbacks.MapperMetaData;
import edu.ucl.mrrr.callbacks.MethodInstance;
import edu.ucl.mrrr.callbacks.ReducerMetaData;

import java.util.HashMap;
import java.util.List;

/**
 * Created by jgeyti on 04/01/15.
 */
public class DummyRunner extends AbstractRunner {

    public DummyRunner(String yamlRecipe) throws Exception {
        super(yamlRecipe);
    }

    @Override
    public void setup(HashMap<Integer, MethodInstance> methods, List<MapperMetaData> mapperMetaData, ReducerMetaData reduceMetaData) throws Exception {

    }

    @Override
    public void run() {

    }
}
