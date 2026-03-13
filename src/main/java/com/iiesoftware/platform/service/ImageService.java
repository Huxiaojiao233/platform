package com.iiesoftware.platform.service;

import com.iiesoftware.platform.model.Image;
import com.iiesoftware.platform.repository.ImageRepository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class ImageService {

    private final ImageRepository imageRepository;

    public ImageService(ImageRepository imageRepository) {
        this.imageRepository = imageRepository;
    }

    @PostConstruct
    public void init() {
        // 初始化一些默认镜像，如果数据库为空
        if (imageRepository.count() == 0) {
            imageRepository.save(new Image("Python 3.8", "python:3.8"));
            imageRepository.save(new Image("Node.js 14", "node:14"));
            imageRepository.save(new Image("Ubuntu 20.04", "ubuntu:20.04"));
            imageRepository.save(new Image("algo-runner", "algo-runner:1.0"));
        }
    }

    public List<Image> getAllImages() {
        return imageRepository.findAll();
    }

    public Image getImage(Long id) {
        return imageRepository.findById(id).orElseThrow(() -> new RuntimeException("Image not found"));
    }
}
