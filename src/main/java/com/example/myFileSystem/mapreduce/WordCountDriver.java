package com.example.myFileSystem.mapreduce;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Job;
import org.apache.hadoop.mapreduce.lib.input.FileInputFormat;
import org.apache.hadoop.mapreduce.lib.input.TextInputFormat;
import org.apache.hadoop.mapreduce.lib.output.FileOutputFormat;
import org.apache.hadoop.mapreduce.lib.output.TextOutputFormat;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

// 主类，指定job参数并提交job
@Component
@Slf4j
public class WordCountDriver {
    @Value("${hadoop.name-node}")
    private String nameNode;
    public String wordCount(String srcPath, String savePath) {
        try {
            // 获取job对象信息
            Configuration conf = new Configuration();
            Job job = Job.getInstance(conf);

            // jar包位置
            job.setJarByClass(WordCountDriver.class);

            // 指定mapper类和reducer类
            job.setMapperClass(WordCountMapper.class);
            job.setReducerClass(WordCountReducer.class);

            // mapper输出的数据类型
            job.setMapOutputKeyClass(Text.class);
            job.setMapOutputValueClass(IntWritable.class);
            // 最终端输出的数据类型
            job.setOutputKeyClass(Text.class);
            job.setOutputValueClass(IntWritable.class);

            // 数据读取组件和输出组件
            job.setInputFormatClass(TextInputFormat.class);
            job.setOutputFormatClass(TextOutputFormat.class);

            // 输入数据的路径
            FileInputFormat.setInputPaths(job, new Path(nameNode + srcPath));

            // 处理结果存放路径（如已存在需删除）
            FileOutputFormat.setOutputPath(job, new Path(savePath));

            //向yarn集群提交job
            boolean res = job.waitForCompletion(true);
            // System.exit(res ? 0 : 1);

            return "success";
        } catch (Exception e) {
            log.error("", e);
            return "error";
        }

    }
}
