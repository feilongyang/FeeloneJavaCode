package com.yang.hdfs;

import java.util.List;

public class HdfsUtilsTest {

    public static void main(String[] args) {

        List<String> list = HdfsUtils.list("/");
        System.out.println(list);
    }
}
