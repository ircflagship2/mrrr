package edu.ucl.mrrr.callbacks;

import org.apache.hadoop.mapreduce.InputFormat;

/**
 * Created by jgeyti on 04/01/15.
 */
public class MapperMetaData {

    public String inputPath;
    public Class<InputFormat<?, ?>> inputFormat;
    public Class emitKeyType;
    public Class emitValType;
}
