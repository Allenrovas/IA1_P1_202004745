package org.example.ia1_p1_202004745.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@RestController
@CrossOrigin(origins = "*")
public class ImageAnalysisController {

    @Autowired
    private GoogleCloudVisionService visionService;

    //Configuracion de CORS
    @Configuration
    public class CorsConfig implements WebMvcConfigurer {
        @Override
        public void addCorsMappings(CorsRegistry registry) {
            registry.addMapping("/**").allowedMethods("GET", "POST", "PUT", "DELETE").allowedOrigins("*")
                    .allowedHeaders("*");
        }
    }

    @PostMapping("/analyze")
    public ResponseEntity<?> analyzeImage(@RequestPart("file") MultipartFile image) {
        try {
            String analysisResult = visionService.analyzeImage(image.getBytes());
            //Parsear el string a JSON
            return ResponseEntity.ok(analysisResult);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error al analizar la imagen: " + e.getMessage());
        }
    }

    @GetMapping("/hello")
    public String hello() {
        return "Hello from user";
    }
}
