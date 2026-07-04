package com.atguigu.lease.web.admin.controller.apartment;


import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.web.admin.service.FileService;
import io.minio.errors.MinioException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


@Tag(name = "文件管理")
@RequestMapping("/admin/file")
@RestController
public class FileUploadController {

    @Autowired
    private FileService fileService;
    @Operation(summary = "上传文件")
    @PostMapping(value = "upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<String> upload(@RequestPart("file") MultipartFile file) throws MinioException, IOException {
        String url = fileService.upload(file);
        return Result.ok(url);
    }
}
