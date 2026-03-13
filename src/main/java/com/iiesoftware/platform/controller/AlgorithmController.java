package com.iiesoftware.platform.controller;

import com.iiesoftware.platform.model.Algorithm;
import com.iiesoftware.platform.service.AlgorithmService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/algorithms")
@Tag(name = "算法管理", description = "算法的上传和查询接口")
public class AlgorithmController {

    private final AlgorithmService algorithmService;

    public AlgorithmController(AlgorithmService algorithmService) {
        this.algorithmService = algorithmService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "注册算法", description = "上传并注册一个新的算法")
    public ResponseEntity<Algorithm> registerAlgorithm(
            @Parameter(description = "算法名称") @RequestParam String name,
            @Parameter(description = "算法描述") @RequestParam(required = false) String description,
            @Parameter(description = "算法脚本文件") @RequestPart MultipartFile file) throws IOException {
        
        Algorithm algorithm = algorithmService.registerAlgorithm(name, description, file);
        return ResponseEntity.ok(algorithm);
    }

    @GetMapping
    @Operation(summary = "获取所有算法", description = "返回所有已注册算法的列表")
    public ResponseEntity<List<Algorithm>> listAllAlgorithms() {
        return ResponseEntity.ok(algorithmService.getAllAlgorithms());
    }
}
