
package com.eusleats.eusleats.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/food-images")
public class FoodImageController {

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private static final Set<String> ALLOWED_EXTENSIONS =
            Set.of("jpg", "jpeg", "png", "gif", "webp");

    @PostMapping(
            value = "/upload",
            consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> uploadImage(
            @RequestParam("image") MultipartFile image) {

        try {
            if (image == null || image.isEmpty()) {
                return ResponseEntity.badRequest()
                        .body("Please choose an image.");
            }

            if (image.getSize() > 5 * 1024 * 1024) {
                return ResponseEntity.badRequest()
                        .body("Image must be 5 MB or smaller.");
            }

            String contentType = image.getContentType();

            if (contentType == null || !contentType.startsWith("image/")) {
                return ResponseEntity.badRequest()
                        .body("Please upload a valid image file.");
            }

            String originalName = image.getOriginalFilename();

            String extension = "";
            if (originalName != null && originalName.contains(".")) {
                extension = originalName.substring(
                        originalName.lastIndexOf('.') + 1)
                        .toLowerCase();
            }

            if (!ALLOWED_EXTENSIONS.contains(extension)) {
                return ResponseEntity.badRequest()
                        .body("Use a JPG, PNG, GIF, or WEBP image.");
            }

            Path folder = Paths.get(uploadDir)
                    .toAbsolutePath()
                    .normalize()
                    .resolve("food");

            Files.createDirectories(folder);

            String filename = UUID.randomUUID() + "." + extension;
            Path destination = folder.resolve(filename).normalize();

            if (!destination.startsWith(folder)) {
                return ResponseEntity.badRequest()
                        .body("Invalid image filename.");
            }

            Files.copy(image.getInputStream(), destination,
                    StandardCopyOption.REPLACE_EXISTING);

            return ResponseEntity.ok(
                    Map.of("imageUrl", "/uploads/food/" + filename));

        } catch (IOException e) {
            return ResponseEntity.internalServerError()
                    .body("Could not save the uploaded image.");
        }
    }
}
