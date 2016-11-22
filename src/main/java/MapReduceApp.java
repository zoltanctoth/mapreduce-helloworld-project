import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.Random;
import java.util.StringTokenizer;

public class MapReduceApp {

    public static class MyMapper
            extends Mapper<Object, Text, Text, IntWritable>{

        private Text word = new Text();

        public void map(Object key, Text value, Context context
        ) throws IOException, InterruptedException {
            StringTokenizer itr = new StringTokenizer(value.toString());
            while (itr.hasMoreTokens()) {
                word.set(itr.nextToken());
                context.write(word, new IntWritable(1));
            }
        }
    }

    public static class MyReducer
            extends Reducer<Text,IntWritable,Text,IntWritable> {
        private IntWritable result = new IntWritable();

        public void reduce(Text key, Iterable<IntWritable> values,
                           Context context
        ) throws IOException, InterruptedException {
            int sum = 0;
            for (IntWritable val : values) {
                sum += val.get();
            }
            result.set(sum);
            context.write(key, result);
        }
    }

    public static void main(String[] args) throws Exception {
        org.apache.log4j.BasicConfigurator.configure();

        FileSystem fs = FileSystem.get(new Configuration());
        try {
            fs.delete(new Path("output"), true);
        } catch (Exception e){

        }
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "MapReduceApp");
        job.setJarByClass(MapReduceApp.class);
        job.setMapperClass(MyMapper.class);
        job.setReducerClass(MyReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);
        FileInputFormat.addInputPath(job, new Path("input"));
        FileOutputFormat.setOutputPath(job, new Path("output"));
        Boolean r = job.waitForCompletion(true);

        if (r) {
            System.out.println("Success! :)");
        } else {
            System.err.println("Oh no!.");
        }
    }
}
