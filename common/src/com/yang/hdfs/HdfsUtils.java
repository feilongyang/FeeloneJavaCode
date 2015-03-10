package com.yang.hdfs;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.*;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class HdfsUtils {

    private static final Logger logger = LoggerFactory.getLogger(HdfsUtils.class);
    private static FileSystem fs;
    private static Configuration conf;


    static {
        conf = new Configuration();
        conf.addResource("hadoop.xml");
        try {
            fs = FileSystem.get(conf);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取hdfs中的一个文件
     *
     * @param path
     */
    public static InputStream getStream(String path) {
        FSDataInputStream in = null;
        try {
            in = fs.open(new Path(path));
            return in;
        } catch (IOException e) {
            logger.error("", e);
        }
        return in;
    }

    /**
     * 存放一个文件
     *
     * @param in
     * @param hdfspath
     */
    public static void put(InputStream in, String hdfspath) {

        Path path = new Path(hdfspath);
        try {
            FSDataOutputStream out = fs.create(path);
            //最后一个参数用于指定，在复制完毕以后是否自动关闭流
            IOUtils.copyBytes(in, out, 4096, true);
            logger.info("文件存放完毕：{}", hdfspath);
        } catch (IOException e) {
            logger.error("", e);
        }
    }

    /**
     * 删除一个文件或者目录
     *
     * @param hdfspath
     */
    public static void del(String hdfspath) {
        try {

            Path path = new Path(hdfspath);

            if (fs.exists(path)) {
                boolean b = fs.delete(path, true);
                logger.info("是否删除：{},{}", hdfspath, b);
            } else {
                logger.info("文件不存在：{}", hdfspath);
            }
        } catch (IOException e) {
            logger.error("删除文件失败", e);
        }
    }

    public static List<String> list(String hdfsdir) {

        List<String> list = new ArrayList<String>();
        try {
            FileStatus[] status = fs.listStatus(new Path(hdfsdir));
            Path[] paths = FileUtil.stat2Paths(status);
            for (Path p : paths) {
                list.add(p.toString());
            }
        } catch (IOException e) {
            logger.error("", e);
        }
        return list;
    }

    public static void main(String[] args) throws Exception {

        List<String> list = list("/");
        System.out.println(list);

    }
}
