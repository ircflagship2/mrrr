package edu.ucl.mrrr.helpers;

import org.apache.hadoop.io.*;

/**
 * Created by jgeyti on 17/12/14.
 */
public class HadoopFormats {
    public String textToString(Text in) {
        return in.toString();
    }


}
