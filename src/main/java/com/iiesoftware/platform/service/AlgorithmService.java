package com.iiesoftware.platform.service;

import com.iiesoftware.platform.config.PlatformProperties;
import com.iiesoftware.platform.model.Algorithm;
import com.iiesoftware.platform.repository.AlgorithmRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Service
public class AlgorithmService {

    private final AlgorithmRepository algorithmRepository;
    private final PlatformProperties properties;

    public AlgorithmService(AlgorithmRepository algorithmRepository, PlatformProperties properties) {
        this.algorithmRepository = algorithmRepository;
        this.properties = properties;
    }

    public Algorithm registerAlgorithm(String name, String description, MultipartFile file) throws IOException {
        Path uploadDir = Paths.get(properties.getPaths().getAlgorithms()).toAbsolutePath();
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path targetPath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Algorithm algorithm = new Algorithm(name, description, targetPath.toString());
        return algorithmRepository.save(algorithm);
    }

    public List<Algorithm> getAllAlgorithms() {
        return algorithmRepository.findAll();
    }
    
    public Algorithm getAlgorithm(Long id) {
        return algorithmRepository.findById(id).orElseThrow(() -> new RuntimeException("Algorithm not found"));
    }
}
