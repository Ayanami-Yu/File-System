package com.example.myFileSystem.controller;

import com.example.myFileSystem.mapreduce.WordCountDriver;
import net.sf.json.JSONArray;
import com.example.myFileSystem.utils.HadoopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/hadoop")
public class HadoopController {
    @Autowired
    private HadoopUtils hadoopUtils;

    @Autowired
    private WordCountDriver wordCountDriver;

    @GetMapping("/upload")
    public String upload(@RequestParam String srcFile, @RequestParam String destFile){
        return hadoopUtils.uploadFile(false, srcFile, destFile);
    }

    @DeleteMapping("/delFile")
    public String del(@RequestParam String filePath){
        hadoopUtils.delFile(filePath);
        return "delFile";
    }

    @RequestMapping("/download")
    public String download(@RequestParam String filePath, @RequestParam String savePath){
        return hadoopUtils.downloadFile(filePath, savePath);
    }

    @GetMapping("/listFiles")
    public JSONArray listFiles(@RequestParam String path) {
        return hadoopUtils.listFile(path, null);
    }

    @GetMapping("/wordCount")
    public String wordCount(@RequestParam String srcPath, @RequestParam String savePath) {
        return wordCountDriver.wordCount(srcPath, savePath);
    }

}
