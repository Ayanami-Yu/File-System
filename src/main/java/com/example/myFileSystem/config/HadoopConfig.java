package com.example.myFileSystem.config;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileSystem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.URI;

@Configuration
@ConditionalOnProperty(name="hadoop.name-node")
@Slf4j
public class HadoopConfig {
    @Value("${hadoop.name-node}")
    private String nameNode;

    // Configuration构造方法会默认加载hdfs-site.xml及core-site.xml
    // 获取nameNode对应的文件系统
    @Bean("fileSystem")
    public FileSystem createFs() throws IOException {
        //读取配置文件
        org.apache.hadoop.conf.Configuration conf = new org.apache.hadoop.conf.Configuration();

        FileSystem fs = FileSystem.get(conf);

        // 返回指定的文件系统
        try {
            // trim方法删除字符串的头尾空白符
            fs = FileSystem.get(new URI(nameNode.trim()), conf, "root");
        } catch (Exception e) {
            log.error("", e);
        }

        System.out.println("fs.defaultFS: "+conf.get("fs.defaultFS"));
        return  fs;
    }
}
