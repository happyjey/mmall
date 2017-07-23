package cc.mmall.service.impl;

import cc.mmall.service.IFileService;
import cc.mmall.util.FTPUtil;
import com.google.common.collect.Lists;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.UUID;

/**
 * Created by Administrator on 2017/6/18.
 */
@Service
public class FileServiceImpl implements IFileService {

    private static final Logger logger = LoggerFactory.getLogger(FileServiceImpl.class);

    public String upload(MultipartFile file, String path){
        logger.info("本地上传路径path:"+path);
        String fileName = file.getOriginalFilename();
        String fileExtensionName = fileName.substring(fileName.lastIndexOf(".")+1);
        String uploadFileName = UUID.randomUUID().toString()+"."+fileExtensionName;
        // 创建目录
        File dirFile = new File(path);
        if (!dirFile.exists()){
            dirFile.setWritable(true);
            dirFile.mkdirs();
        }
        // 创建文件
        File targetFile = new File(path,uploadFileName);
        try {
            file.transferTo(targetFile);
            // 文件上传成功
            // 上传至FTP
            List<File> fileList = Lists.newArrayList();
            fileList.add(targetFile);
            FTPUtil.uploadFile(fileList);
            // 删除原目录文件
            targetFile.delete();
        } catch (IOException e) {
            logger.error("上传文件异常",e);
            return null;
        }
        return targetFile.getName();
    }
}
