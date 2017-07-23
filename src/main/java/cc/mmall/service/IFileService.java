package cc.mmall.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * Created by Administrator on 2017/6/18.
 */
public interface IFileService {
    String upload(MultipartFile file, String path);
}
