import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.Mapper;
import org.apache.hadoop.mapreduce.Reducer;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;

import java.io.IOException;
import java.util.StringTokenizer;

public class StudentGrades {

    // Mapper Class
    public static class GradeMapper extends Mapper<Object, Text, Text, Text> {

        public void map(Object key, Text value, Context context) throws IOException, InterruptedException {
            String line = value.toString();
            String[] fields = line.split(",");

            if (fields.length == 3) {
                String studentID = fields[0].trim();
                String studentName = fields[1].trim();
                int marks;

                try {
                    marks = Integer.parseInt(fields[2].trim());
                } catch (NumberFormatException e) {
                    return; // Skip invalid records
                }

                // Assign Grade
                String grade;
                if (marks >= 90) {
                    grade = "A";
                } else if (marks >= 80) {
                    grade = "B";
                } else if (marks >= 70) {
                    grade = "C";
                } else if (marks >= 60) {
                    grade = "D";
                } else {
                    grade = "F";
                }

                // Output: (StudentID, Name - Grade)
                context.write(new Text(studentID), new Text(studentName + " - " + grade));
            }
        }
    }

    // Reducer Class
    public static class GradeReducer extends Reducer<Text, Text, Text, Text> {
        public void reduce(Text key, Iterable<Text> values, Context context) throws IOException, InterruptedException {
            for (Text val : values) {
                context.write(key, val);
            }
        }
    }

    // Driver Class
    public static void main(String[] args) throws Exception {
        Configuration conf = new Configuration();
        Job job = Job.getInstance(conf, "student grades");

        job.setJarByClass(StudentGrades.class);
        job.setMapperClass(GradeMapper.class);
        job.setReducerClass(GradeReducer.class);
        job.setOutputKeyClass(Text.class);
        job.setOutputValueClass(Text.class);

        FileInputFormat.addInputPath(job, new Path(args[0]));
        FileOutputFormat.setOutputPath(job, new Path(args[1]));

        System.exit(job.waitForCompletion(true) ? 0 : 1);
    }
}
