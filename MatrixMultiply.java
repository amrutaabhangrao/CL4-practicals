import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.*;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

public class MatrixMultiply {

    public static class MatrixMapper extends Mapper<LongWritable, Text, Text, Text> {
        public void map(LongWritable key, Text value, Context context)
                throws IOException, InterruptedException {
            String[] tokens = value.toString().split(" ");
            String matrix = tokens[0];
            int row = Integer.parseInt(tokens[1]);
            int col = Integer.parseInt(tokens[2]);
            int val = Integer.parseInt(tokens[3]);

            if (matrix.equals("A")) {
                for (int k = 0; k < 2; k++) { // change '2' if B has different column count
                    context.write(new Text(row + "," + k), new Text("A," + col + "," + val));
                }
            } else if (matrix.equals("B")) {
                for (int i = 0; i < 2; i++) { // change '2' if A has different row count
                    context.write(new Text(i + "," + col), new Text("B," + row + "," + val));
                }
            }
        }
    }

    public static class MatrixReducer extends Reducer<Text, Text, Text, IntWritable> {
        public void reduce(Text key, Iterable<Text> values, Context context)
                throws IOException, InterruptedException {
            Map<Integer, Integer> amap = new HashMap<>();
            Map<Integer, Integer> bmap = new HashMap<>();

            for (Text t : values) {
                String[] parts = t.toString().split(",");
                String matrixName = parts[0];
                int index = Integer.parseInt(parts[1]);
                int value = Integer.parseInt(parts[2]);

                if (matrixName.equals("A")) {
                    amap.put(index, value);
                } else {
                    bmap.put(index, value);
                }
            }

            int result = 0;
            for (int k : amap.keySet()) {
                if (bmap.containsKey(k)) {
                    result += amap.get(k) * bmap.get(k);
                }
            }

            context.write(key, new IntWritable(result));
        }
    }

    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "Matrix Multiplication");

        job.setJarByClass(MatrixMultiply.class);
        job.setMapperClass(MatrixMapper.class);
        job.setReducerClass(MatrixReducer.class);

        job.setMapOutputKeyClass(Text.class);
        job.setMapOutputValueClass(Text.class);

        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(IntWritable.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
