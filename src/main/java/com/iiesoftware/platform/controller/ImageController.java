package com.iiesoftware.platform.controller;

import com.iiesoftware.platform.model.Image;
import com.iiesoftware.platform.service.ImageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/images")
@Tag(name = "镜像管理", description = "Docker镜像的查询接口")
public class ImageController {

    private final ImageService imageService;

    public ImageController(ImageService imageService) {
        this.imageService = imageService;
    }

    @GetMapping
    @Operation(summary = "获取所有镜像", description = "返回所有可用镜像的列表")
    public ResponseEntity<List<Image>> listAllImages() {
        return ResponseEntity.ok(imageService.getAllImages());
    }
}
