package edu.ucl.mrrr;

import edu.ucl.mrrr.callbacks.MapperMetaData;
import edu.ucl.mrrr.callbacks.MethodInstance;
import edu.ucl.mrrr.callbacks.ReducerMetaData;
import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.GlobFilter;
import org.apache.hadoop.fs.GlobPattern;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.MultipleInputs;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.File;
import java.util.HashMap;
import java.util.List;

/**
 * Created by jkg on 15/09/2014.
 */
public class HadoopRunner extends AbstractRunner {



    public HadoopRunner(String yamlRecipePath) throws Exception {
        super(FileUtils.readFileToString(new File(yamlRecipePath)));
    }

    @Override
    public void run() {

    }

    @Override
    public void setup(HashMap<Integer, MethodInstance> methods, List<MapperMetaData> mapperMetaData, ReducerMetaData reduceMetaData) throws Exception {

        Configuration conf = new Configuration();
        conf.set("mrrr_recipe", this.rawRecipe);

        // set all path->mrrr_mapper relationships
        for (int i = 0; i < mapperMetaData.size(); i++) {
            MapperMetaData mapper = mapperMetaData.get(i);
            String pathMapperIdx = new Path(mapper.inputPath).toString() + ";" + i;
            String mappers = conf.get(HadoopMapper.DIR_MRRR_MAPPERS);
            conf.set(HadoopMapper.DIR_MRRR_MAPPERS, mappers == null ? pathMapperIdx : mappers + "," + pathMapperIdx);
        }

        Job job = Job.getInstance(conf, recipe.getJobname());
        job.setJarByClass(HadoopRunner.class);

        // We need a number of mappers, and one reducer. Parse the yaml
        // file to find out which.
        System.out.println("Adding Mappers");

        for (MapperMetaData mapper : mapperMetaData){
            System.out.println("Adding mapper for input path: " + mapper.inputPath);
            MultipleInputs.addInputPath(job, new Path(mapper.inputPath), mapper.inputFormat, HadoopMapper.class);
        }

        //job.setCombinerClass(IntSumReducer.class);
        job.setReducerClass(HadoopReducer.class);

        job.setMapOutputKeyClass(mapperMetaData.get(0).emitKeyType);
        job.setMapOutputValueClass(mapperMetaData.get(0).emitValType);

        job.setOutputKeyClass(reduceMetaData.emitKeyType);
        job.setOutputValueClass(reduceMetaData.emitValType);

        FileOutputFormat.setOutputPath(job, new Path(recipe.getOutput()));

        System.out.println("Starting job:");
        System.exit(job.waitForCompletion(true) ? 0 : 1);

    }


    public static void main(String[] args) throws Exception {
        AbstractRunner yamlRunner = new HadoopRunner(args[0]);
        yamlRunner.run();
    }

}
