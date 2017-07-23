package cc.mmall.util;

import org.apache.commons.net.ftp.FTPClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

/**
 * Created by Administrator on 2017/6/20.
 */
public class FTPUtil {

    private static String ftpIp = PropertiesUtil.getProperty("ftp.server.ip");
    private static String ftpUser = PropertiesUtil.getProperty("ftp.user");
    private static String ftpPass = PropertiesUtil.getProperty("ftp.pass");

    private String ip;
    private String user;
    private String pwd;
    private int port;
    private FTPClient ftpClient;

    private static final Logger logger = LoggerFactory.getLogger(FTPUtil.class);

    public FTPUtil(String ip,int port,String user,String pwd){
        this.ip = ip;
        this.port = port;
        this.user = user;
        this.pwd = pwd;
    }

    public static boolean uploadFile(List<File> fileList) throws IOException {
        FTPUtil ftpUtil = new FTPUtil(ftpIp,21,ftpUser,ftpPass);
        logger.info("开始连接FTP服务器");
        boolean result = ftpUtil.uploadFile(fileList,"img");
        logger.info("结束上传，上传结果{}",result);
        return result;
    }

    private boolean uploadFile(List<File> fileList, String remotePath) throws IOException {
        boolean uploaded = true;
        FileInputStream fis = null;
        if(ftpConnection(this.ip,this.port,this.user,this.pwd)){
            try {
                ftpClient.changeWorkingDirectory(remotePath);
                ftpClient.setBufferSize(1024);
                ftpClient.setControlEncoding("UTF-8");
                ftpClient.setFileType(FTPClient.BINARY_FILE_TYPE);
                ftpClient.enterLocalPassiveMode();
                for (File fileItem : fileList){
                    fis = new FileInputStream(fileItem);
                    ftpClient.storeFile(fileItem.getName(),fis);
                }
            } catch (IOException e) {
                uploaded = false;
                logger.error("上传文件异常",e);
            }finally {
                ftpClient.disconnect();
                fis.close();
            }
        }
        return uploaded;
    }

    private boolean ftpConnection(String ip,int port,String user,String pwd){
        boolean isSuccess = false;
        ftpClient = new FTPClient();
        try {
            ftpClient.connect(ip,port);
            isSuccess = ftpClient.login(user,pwd);
        } catch (IOException e) {
            logger.error("FTP连接异常",e);
        }
        return isSuccess;
    }

}
