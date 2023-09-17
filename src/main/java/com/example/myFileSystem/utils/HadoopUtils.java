package com.example.myFileSystem.utils;

import com.example.myFileSystem.entity.File;
import lombok.extern.slf4j.Slf4j;
import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.fs.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;

@Component
@ConditionalOnBean(FileSystem.class)
@Slf4j
public class HadoopUtils {
    private static final int nameNodeLen = 19;
    @Autowired
    private FileSystem fileSystem;

    @Value("${hadoop.name-node}")
    private String nameNode;

    @Value("${hadoop.namespace}")
    private String nameSpace;

    public String uploadFile(boolean del, String srcFile, String destFile){
        return copyFileToHDFS(del, true, srcFile, destFile);
    }

    public void delFile(String filePath){
        rmdir(filePath) ;
    }

    /**
     * 将本地文件上传到HDFS
     * @param delSrc    是否删除源文件
     * @param overwrite 是否覆盖
     * @param srcFile   源文件在本地的地址
     * @param destFile  上传到HDFS的完整路径 以"/"开头且包含文件名，若中间文件夹不存在则自动创建
     */
    public String copyFileToHDFS(boolean delSrc, boolean overwrite, String srcFile, String destFile) {
        // 目的路径
        if(StringUtils.isNotBlank(nameNode)){
            destFile = nameNode + destFile;
        }

        Path srcPath = new Path(srcFile);
        Path destPath = new Path(destFile);

        try {
            fileSystem.copyFromLocalFile(delSrc, overwrite, srcPath, destPath);
            return "success";
        } catch (IOException e) {
            log.error("", e);
            return "error";
        }
    }

    /**
     * 删除文件或递归删除文件夹
     * @param path  HDFS上的完整路径 以"/"开头
     */
    public void rmdir(String path) {
        try {
            if(StringUtils.isNotBlank(nameNode)){
                path = nameNode + path;
            }
            // b=true表明递归删除子文件夹 delete参数是完整URI
            fileSystem.delete(new Path(path), true);
        } catch (IllegalArgumentException | IOException e) {
            log.error("", e);
        }
    }

    /**
     * 将HDFS上的文件下载到本地
     * @param srcFile   源文件在HDFS上的完整路径
     * @param destFile  本地目标路径
     */
    public String downloadFile(String srcFile, String destFile) {
        if(StringUtils.isNotBlank(nameNode)){
            srcFile = nameNode + srcFile;
        }

        Path srcPath = new Path(srcFile);
        Path destPath = new Path(destFile);

        try {
            fileSystem.copyToLocalFile(srcPath, destPath);
            return "success";
        } catch (IOException e) {
            log.error("", e);
            return "error";
        }
    }

    /**
     * 列出给定目录的一级子目录在HDFS上的完整路径 不包括该目录自身
     * @param path          给定的HDFS上的完整目录
     * @param pathFilter    过滤条件 由正则表达式给出
     * @return              返回子目录路径组成的字符串
     */
    public JSONArray listFile(String path, PathFilter pathFilter) {
        ArrayList<File> files = new ArrayList<>();
        try {
            if(StringUtils.isNotBlank(nameNode)){
                path = nameNode + path;
            }

            FileStatus[] status;
            if(pathFilter != null){
                // 根据filter列出目录内容
                status = fileSystem.listStatus(new Path(path), pathFilter);
            }else{
                // 列出目录内容
                status = fileSystem.listStatus(new Path(path));
            }
            // 获取目录下的所有文件路径
            Path[] listedPaths = FileUtil.stat2Paths(status);
            // 转换String[]
            if (listedPaths != null && listedPaths.length > 0){
                for (int i = 0; i < listedPaths.length; i++){
                    String filePath = listedPaths[i].toString().substring(nameNodeLen);
                    files.add(new File(filePath, existDir(filePath, false)));
                }
            }
        } catch (IllegalArgumentException | IOException e) {
            log.error("", e);
        }
        return JSONArray.fromObject(files);
    }

    /**
     * 判断给定路径是否为文件夹
     * @param filePath  在HDFS上的完整路径
     * @param create    为true时当文件夹不存在则自动创建
     */
    public boolean existDir(String filePath, boolean create){
        boolean flag = false;
        if(StringUtils.isEmpty(filePath)){
            throw new IllegalArgumentException("filePath不能为空");
        }
        try{
            Path path = new Path(filePath);
            if (create){
                if (!fileSystem.exists(path)){
                    fileSystem.mkdirs(path);
                }
            }
            if (fileSystem.isDirectory(path)){
                flag = true;
            }
        }catch (Exception e){
            log.error("", e);
        }
        return flag;
    }

}
