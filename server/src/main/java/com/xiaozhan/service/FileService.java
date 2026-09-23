package com.xiaozhan.service;

import com.xiaozhan.vo.UploadVO;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务（本地磁盘存储）
 */
public interface FileService {

    /**
     * 通用上传
     *
     * @param file     文件
     * @param bizType  业务类型：AVATAR/COVER/SOURCE/DOC/VIDEO/IMAGE/VERIFY
     * @return 上传结果
     */
    UploadVO upload(MultipartFile file, String bizType);

    /**
     * 按业务类型删除文件
     */
    void delete(String url);
}
