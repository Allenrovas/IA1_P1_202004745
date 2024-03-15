package org.example.ia1_p1_202004745.controllers;


import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import java.util.Base64;
import com.google.cloud.vision.v1.AnnotateImageRequest;
import com.google.cloud.vision.v1.AnnotateImageResponse;
import com.google.cloud.vision.v1.BatchAnnotateImagesResponse;
import com.google.cloud.vision.v1.FaceAnnotation;
import com.google.cloud.vision.v1.Feature;
import com.google.cloud.vision.v1.Image;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.protobuf.ByteString;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.ServiceAccountCredentials;

import org.springframework.stereotype.Service;

@Service
public class GoogleCloudVisionService {

    @Value("${google.cloud.vision.api.key}")
    private String API_KEY;

    public String analyzeImage(byte[] image) {
        try (ImageAnnotatorClient vision = ImageAnnotatorClient.create()) {

            Image img = Image.newBuilder().setContent(ByteString.copyFrom(image)).build();
            Feature faceDetection = Feature.newBuilder().setType(Feature.Type.FACE_DETECTION).build();
            Feature safeSearchDetection = Feature.newBuilder().setType(Feature.Type.SAFE_SEARCH_DETECTION).build();

            AnnotateImageRequest request = AnnotateImageRequest.newBuilder().addFeatures(faceDetection).addFeatures(safeSearchDetection).setImage(img).build();

            List<AnnotateImageRequest> requests = new ArrayList<>();
            requests.add(request);

            BatchAnnotateImagesResponse responses = vision.batchAnnotateImages(requests);
            List<AnnotateImageResponse> responsesList = responses.getResponsesList();
            String JsonResult = "{\n";

            int ContadorRostros = 0;
            //Rostros detectados y su posicion x,y
            for (AnnotateImageResponse res : responsesList) {
                if (res.hasError()) {
                    JsonResult += "\"Error\": " + res.getError().getMessage() + "}";
                    return JsonResult;
                }

                JsonResult += "\"Rostros\": [\n";

                //Obtener la cantidad de rostros
                ContadorRostros = res.getFaceAnnotationsList().size();

                for (FaceAnnotation annotation : res.getFaceAnnotationsList()) {
                    //Obtener vertices del rostro
                    JsonResult += "\t{\n";
                    JsonResult += "\t\t\"Vertices\": [\n";
                    JsonResult += "\t\t\t{\"x\": " + annotation.getBoundingPoly().getVertices(0).getX() + ", \"y\": " + annotation.getBoundingPoly().getVertices(0).getY() + "},\n";
                    JsonResult += "\t\t\t{\"x\": " + annotation.getBoundingPoly().getVertices(1).getX() + ", \"y\": " + annotation.getBoundingPoly().getVertices(1).getY() + "},\n";
                    JsonResult += "\t\t\t{\"x\": " + annotation.getBoundingPoly().getVertices(2).getX() + ", \"y\": " + annotation.getBoundingPoly().getVertices(2).getY() + "},\n";
                    JsonResult += "\t\t\t{\"x\": " + annotation.getBoundingPoly().getVertices(3).getX() + ", \"y\": " + annotation.getBoundingPoly().getVertices(3).getY() + "}\n";

                    //Si es el ultimo rostro, no agregar coma
                    if (res.getFaceAnnotationsList().indexOf(annotation) == res.getFaceAnnotationsList().size() - 1) {
                        JsonResult += "\t\t]\n";
                        JsonResult += "\t}\n";
                    } else {
                        JsonResult += "\t\t]\n";
                        JsonResult += "\t},\n";
                    }

                }

                JsonResult += "],\n";

                JsonResult += "\"CantidadRostros\": " + ContadorRostros + ",\n";

                /*
                Parsear los valores de la deteccion de contenido inapropiado, segun:

                VeryUnlikely = 0
                Unlikely = 20
                Possible = 35
                Likely = 60
                VeryLikely = 80
                **/
                //Parsear los valores de la deteccion de contenido inapropiado y devolver el resultado


                String violenceDetection = String.valueOf(res.getSafeSearchAnnotation().getViolence());
                String adultDetection = String.valueOf(res.getSafeSearchAnnotation().getAdult());
                String spoofDetection = String.valueOf(res.getSafeSearchAnnotation().getSpoof());
                String medicalDetection = String.valueOf(res.getSafeSearchAnnotation().getMedical());
                String racyDetection = String.valueOf(res.getSafeSearchAnnotation().getRacy());

                //Agregar los string a una lista y correrla para parsear los valores
                List<String> detections = new ArrayList<String>();
                List<Integer> detectionsParse = new ArrayList<Integer>();
                detections.add(violenceDetection);
                detections.add(adultDetection);
                detections.add(spoofDetection);
                detections.add(medicalDetection);
                detections.add(racyDetection);

                //Iterar la lista y parsear los valores
                for (String detection : detections) {
                    if (detection.equals("VERY_UNLIKELY")) {
                        detectionsParse.add(0);
                    } else if (detection.equals("UNLIKELY")) {
                        detectionsParse.add(20);
                    } else if (detection.equals("POSSIBLE")) {
                        detectionsParse.add(35);
                    } else if (detection.equals("LIKELY")) {
                        detectionsParse.add(60);
                    } else if (detection.equals("VERY_LIKELY")) {
                        detectionsParse.add(80);
                    }
                }

                //Agregar los valores parseados a un string
                JsonResult += "\"Violencia\": " + detectionsParse.get(0) + ",\n";
                JsonResult += "\"Adulto\": " + detectionsParse.get(1) + ",\n";
                JsonResult += "\"Spoof\": " + detectionsParse.get(2) + ",\n";
                JsonResult += "\"Medico\": " + detectionsParse.get(3) + ",\n";
                JsonResult += "\"Racy\": " + detectionsParse.get(4) + ",\n";

                //Sumar valores violencia, adulto y picante
                int suma = detectionsParse.get(0) + detectionsParse.get(1) + detectionsParse.get(4);

                //Si la suma es mayor a 45, devolver que la imagen es inapropiada
                if (suma > 45) {
                    JsonResult += "\"Resultado\": \"Imagen inapropiada\"\n";
                } else {
                    JsonResult += "\"Resultado\": \"Imagen apropiada\"\n";
                }

                JsonResult += "}";

            }
            return JsonResult;
        } catch (Exception  e) {
            return "Error al procesar la imagen" + e.getMessage();
        }
    }
}
