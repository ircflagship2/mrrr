package edu.ucl.mrrr.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by jgeyti on 17/12/14.
 */
public class JsonStore {

    public String extractCity(String path) {
        return "London";
    }

    public List<String> splitJsonStoreStr(String jsonStr) {

        List<String> l = new ArrayList<String>();
        l.add("json1");
        l.add("json2");
        l.add("json3");
        return l;
    }

    public String toRegion(String json) {
        return json.substring(0, 1);
    }

    public boolean isUkRegion(String region) {
        return true;
    }
}
