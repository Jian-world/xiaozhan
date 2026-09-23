package com.xiaozhan.controller;

import com.xiaozhan.common.Result;
import com.xiaozhan.service.FileService;
import com.xiaozhan.vo.UploadVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件上传接口（本地磁盘存储）
 */
@Tag(name = "02-文件", description = "通用文件上传与删除")
@RestController
@RequestMapping("/api/file")
@RequiredArgsConstructor
public class FileController extends BaseController {

    private final FileService fileService;

    @Operation(summary = "上传文件",
            description = "bizType 可选：AVATAR/COVER/IMAGE/VERIFY/DOC/VIDEO/SOURCE/RICH_TEXT")
    @PostMapping("/upload")
    public Result<UploadVO> upload(
            @Parameter(description = "文件") @RequestParam("file") MultipartFile file,
            @Parameter(description = "业务类型") @RequestParam(value = "bizType", defaultValue = "DOC") String bizType) {
        return Result.success(fileService.upload(file, bizType));
    }

    @Operation(summary = "删除文件")
    @DeleteMapping
    public Result<Void> delete(@RequestParam("url") String url) {
        fileService.delete(url);
        return Result.success();
    }
}
