package com.yang.ftp;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPClientConfig;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

public class FtpUtils {

    private String userName;
    private String password;
    private String ip;
    private int port;

    public FTPClient ftpClient;

    private static final Logger logger = LoggerFactory.getLogger(FtpUtils.class);

    public FtpUtils() {
    }

    public FtpUtils(String userName, String password, String ip, int port) {
        this.userName = userName;
        this.password = password;
        this.ip = ip;
        this.port = port;
        this.ftpClient = new FTPClient();
    }

    public boolean login() {

        FTPClientConfig ftpClientConfig = new FTPClientConfig();
        ftpClientConfig.setServerTimeZoneId(TimeZone.getDefault().getID());
        ftpClient.configure(ftpClientConfig);

        // 尝试连接服务器，如果连接不成功，则关闭连接
        try {
            ftpClient.connect(ip, port);

            // 如果来自服务器的应答是消极的，说明没有连上服务器
            int replyCode = ftpClient.getReplyCode();
            if (!FTPReply.isPositiveCompletion(replyCode)) {
                ftpClient.disconnect();
                logger.error("FTPReply.isPositiveCompletion失败");
                return false;
            }

            boolean login = ftpClient.login(userName, password);
            if (!login) {
                logger.info("用户名或者密码不正确，用户名={}，密码={}", userName, password);
                return false;
            }

            // 如果登陆成功，则对ftp进行一些设置
            ftpClient.setControlEncoding("UTF-8");
            ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();
            ftpClient.setBufferSize(1024 * 2);
            ftpClient.setDataTimeout(30 * 1000);
            return true;
        } catch (Exception e) {
            logger.error("连接FTP服务器时出错", e);
            this.close();
        }
        return false;
    }

    public InputStream download(String remoteFileName, String remoteFileDir) {

        try {
            ftpClient.changeWorkingDirectory(remoteFileDir);
            InputStream in = ftpClient.retrieveFileStream(remoteFileName);

            //如果这个目录中没有，则在父目录中寻找
            if (in == null) {
                ftpClient.changeToParentDirectory();
                in = ftpClient.retrieveFileStream(remoteFileName);
                if (in == null) {
                    logger.info("此文件不存在：{}", remoteFileName);
                }
            }
            return in;
        } catch (IOException e) {
            logger.error("下载文件时出错：" + remoteFileName, e);
        }
        return null;
    }

    public void rename(String remoteFileName, String remoteFileDir, String remoteNewName) {

        try {
            FTPFile[] ftpFiles = null;
            ftpClient.changeWorkingDirectory(remoteFileDir);
            boolean ret = dorename(remoteFileName, remoteFileDir, remoteNewName);
            if (ret) {
                return;
            }
            //如果这个目录中没有，则在父目录中寻找
            ftpClient.changeToParentDirectory();
            ret = dorename(remoteFileName, remoteFileDir, remoteNewName);
            if (!ret) {
                logger.info("文件重命名失败：{}", remoteFileName);
            }
        } catch (IOException e) {
            logger.error("文件重命名失败：" + remoteFileName, e);
        }
    }

    private boolean dorename(String remoteFileName, String remoteFileDir, String remoteNewName) {
        try {
            FTPFile[] ftpFiles = ftpClient.listFiles();
            for (FTPFile f : ftpFiles) {
                //如果在这个目录下找到了
                if (f.getName().equals(remoteFileName)) {
                    boolean ret = ftpClient.rename(remoteFileName, remoteNewName);
                    if (ret) {
                        logger.info("文件重命名成功：{}", remoteNewName);
                        return true;
                    } else {
                        logger.info("文件重命名失败：{}", remoteNewName);
                        return false;
                    }
                }
            }
        } catch (IOException e) {
            logger.error("文件重命名失败：" + remoteFileName, e);
        }
        return false;
    }

    /**
     * 关闭连接
     */
    private void close() {

        if (ftpClient != null && ftpClient.isConnected()) {
            try {
                boolean logout = ftpClient.logout();
                if (!logout) {
                    logger.info("ftpClient.logout失败：{}", Thread.currentThread().getName());
                }
            } catch (IOException e) {
                logger.error("ftpClient.logout出现异常", e);
            } finally {
                try {
                    ftpClient.disconnect();
                } catch (IOException e) {
                    logger.error("ftpClient.disconnect exception:" + Thread.currentThread().getName(), e);
                }
            }
        }
    }

}
