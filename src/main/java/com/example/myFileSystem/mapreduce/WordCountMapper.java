package com.example.myFileSystem.mapreduce;

import org.apache.hadoop.io.IntWritable;
import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.mapreduce.Mapper;

import java.io.IOException;

public class WordCountMapper extends Mapper<LongWritable, Text, Text, IntWritable> {

    @Override
    protected void map(LongWritable key, Text value, Context context)
            throws IOException, InterruptedException {
        // 获取每一行的文本内容
        String lines = value.toString();
        // 按空格切分
        String[] words = lines.split(" ");

        for (String word :words) {
            context.write(new Text(word), new IntWritable(1));
        }
    }
}
