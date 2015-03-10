package com.yang.ftp;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FtpUtilsTest {

    private static final Logger logger = LoggerFactory.getLogger(FtpUtilsTest.class);

    static String ip = "192.168.1.90";
    static int port = 21;
    static String username = "oracle";
    static String password = "oracle";

    static String remoteFileDir = "/home/oracle/test/";
    static String remoteFileName = "111.archive";
    static String localFileDir = "e:/test";//文件存储在本地的路径

    public static void main(String[] args) {

        testDownToLocal();
    }

    private static void testDownToLocal() {

        FtpUtils ftp = new FtpUtils(username, password, ip, port);
        boolean login = ftp.login();
        if (login) {
            ftp.download(remoteFileName, remoteFileDir);
        } else {
            logger.info("下载失败");
        }
    }
}
