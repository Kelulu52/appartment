package com.atguigu.lease.web.admin.service;

import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface FileService {

    String upload(MultipartFile file) throws MinioException, IOException;
}
