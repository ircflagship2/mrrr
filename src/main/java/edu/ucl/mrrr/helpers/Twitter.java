package edu.ucl.mrrr.helpers;

import org.apache.hadoop.io.Text;

import java.util.Arrays;
import java.util.List;

/**
 * Created by jgeyti on 17/12/14.
 */
public class Twitter {

    public List<String> parseJsons(List<String> jsons) {
        return Arrays.asList(jsons.toString().split(","));
    }

}
