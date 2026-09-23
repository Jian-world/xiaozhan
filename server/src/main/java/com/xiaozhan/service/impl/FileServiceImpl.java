package com.xiaozhan.service.impl;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.xiaozhan.common.ResultCode;
import com.xiaozhan.config.XiaozhanProperties;
import com.xiaozhan.exception.BizException;
import com.xiaozhan.service.FileService;
import com.xiaozhan.vo.UploadVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * 本地磁盘文件服务
 * <p>
 * 目录结构：{basePath}/{bizType}/{yyyy}/{MM}/{uuid}.{ext}
 * 静态资源通过 {@code /files/**} 直接访问。
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileServiceImpl implements FileService {

    /** 各业务类型允许的扩展名 */
    private static final Map<String, Set<String>> ALLOW_EXT = new HashMap<>();

    /** 各业务类型体积上限（字节） */
    private static final Map<String, Long> MAX_SIZE = new HashMap<>();

    static {
        Set<String> image = Set.of("jpg", "jpeg", "png", "gif", "webp", "bmp", "svg");
        Set<String> doc = Set.of("pdf", "doc", "docx", "ppt", "pptx", "xls", "xlsx", "md", "txt", "zip", "rar", "7z");
        Set<String> video = Set.of("mp4", "webm", "mov", "m4v", "avi", "mkv");
        Set<String> source = Set.of("zip", "rar", "7z", "tar", "gz");

        ALLOW_EXT.put("AVATAR", image);
        ALLOW_EXT.put("COVER", image);
        ALLOW_EXT.put("IMAGE", image);
        ALLOW_EXT.put("VERIFY", union(image, Set.of("pdf")));
        ALLOW_EXT.put("DOC", doc);
        ALLOW_EXT.put("VIDEO", video);
        ALLOW_EXT.put("SOURCE", source);
        ALLOW_EXT.put("RICH_TEXT", image);
    }

    private final XiaozhanProperties properties;

    @SafeVarargs
    private static Set<String> union(Set<String>... sets) {
        Set<String> result = new HashSet<>();
        for (Set<String> s : sets) {
            result.addAll(s);
        }
        return Collections.unmodifiableSet(result);
    }

    @Override
    public UploadVO upload(MultipartFile file, String bizType) {
        if (file == null || file.isEmpty()) {
            throw new BizException(ResultCode.ASSET_UPLOAD_FAILED.getCode(), "请选择要上传的文件");
        }
        String type = StrUtil.blankToDefault(bizType, "DOC").toUpperCase();
        String originalName = StrUtil.blankToDefault(file.getOriginalFilename(), "unknown");
        String ext = FileUtil.extName(originalName).toLowerCase();

        // 类型白名单
        Set<String> allowed = ALLOW_EXT.getOrDefault(type, ALLOW_EXT.get("DOC"));
        if (StrUtil.isBlank(ext) || !allowed.contains(ext)) {
            throw new BizException(ResultCode.FILE_TYPE_NOT_ALLOWED.getCode(),
                    "不支持的文件格式：." + ext + "，支持 " + String.join("/", allowed));
        }

        // 体积限制
        long maxSize = resolveMaxSize(type);
        if (file.getSize() > maxSize) {
            throw new BizException(ResultCode.FILE_TOO_LARGE.getCode(),
                    "文件体积超出限制（最大 " + FileUtil.readableFileSize(maxSize) + "）");
        }

        // 生成相对路径：{bizType}/{yyyy}/{MM}/{uuid}.{ext}
        LocalDateTime now = LocalDateTime.now();
        String datePath = LocalDateTimeUtil.format(now, "yyyy/MM");
        String fileName = IdUtil.fastSimpleUUID() + "." + ext;
        String relativeDir = type.toLowerCase() + "/" + datePath;

        File destDir = new File(properties.getUpload().getBasePath(), relativeDir);
        if (!destDir.exists() && !destDir.mkdirs()) {
            throw new BizException(ResultCode.ASSET_UPLOAD_FAILED.getCode(), "无法创建上传目录");
        }

        File dest = new File(destDir, fileName);
        try {
            file.transferTo(dest.getAbsoluteFile());
        } catch (IOException e) {
            log.error("文件写入失败：{}", dest.getAbsolutePath(), e);
            throw new BizException(ResultCode.ASSET_UPLOAD_FAILED);
        }

        String url = StrUtil.format("{}/{}/{}", properties.getUpload().getUrlPrefix(), relativeDir, fileName);
        log.info("文件上传成功：{} -> {}", originalName, url);

        UploadVO vo = new UploadVO();
        vo.setUrl(url);
        vo.setFileName(originalName);
        vo.setSize(file.getSize());
        vo.setExt(ext);
        vo.setBizType(type);
        return vo;
    }

    private long resolveMaxSize(String bizType) {
        XiaozhanProperties.Upload upload = properties.getUpload();
        return switch (bizType) {
            case "VIDEO" -> upload.getMaxVideoSize();
            case "SOURCE" -> upload.getMaxSourceSize();
            default -> upload.getMaxDocSize();
        };
    }

    @Override
    public void delete(String url) {
        if (StrUtil.isBlank(url)) {
            return;
        }
        String prefix = properties.getUpload().getUrlPrefix();
        if (!url.startsWith(prefix)) {
            return;
        }
        String relative = url.substring(prefix.length());
        File target = new File(properties.getUpload().getBasePath(), relative);
        if (target.exists() && target.isFile() && FileUtil.del(target)) {
            log.info("文件已删除：{}", url);
        }
    }

    /**
     * 生成当天日期路径（供其他服务复用）
     */
    public static String currentDatePath() {
        return LocalDateTimeUtil.format(LocalDateTime.now(), DatePattern.NORM_DATE_PATTERN);
    }
}
