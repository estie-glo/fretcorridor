package com.flysoft.fretcorridor.common.service;

import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Stockage objet MinIO pour justificatifs KYC (EF-IDA-02 niveau 2).
 * Les objets restent privés ; l'accès se fait via URLs présignées à durée limitée.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentStorageService {

    private final MinioClient minioClient;

    @Value("${minio.bucket}")
    private String bucket;

    @Value("${minio.endpoint}")
    private String endpoint;

    @Value("${minio.presign-expiry-minutes:15}")
    private int presignExpiryMinutes;

    /**
     * Upload un document KYC et retourne la clé objet (stockée en BDD, jamais d'URL publique).
     */
    public String uploadKycDocument(
            String tenantId,
            UUID chauffeurId,
            String typeDocument,
            MultipartFile fichier) {
        try {
            String extension = extensionOf(fichier.getOriginalFilename());
            String objectKey = tenantId + "/" + chauffeurId + "/"
                    + typeDocument.toLowerCase() + "_" + System.currentTimeMillis() + extension;

            minioClient.putObject(PutObjectArgs.builder()
                    .bucket(bucket)
                    .object(objectKey)
                    .stream(fichier.getInputStream(), fichier.getSize(), -1)
                    .contentType(fichier.getContentType() != null
                            ? fichier.getContentType()
                            : "application/octet-stream")
                    .build());

            log.info("Document KYC uploadé MinIO : {}", objectKey);
            return objectKey;
        } catch (Exception e) {
            log.error("Échec upload MinIO : {}", e.getMessage());
            throw new RuntimeException("STOCKAGE_INDISPONIBLE");
        }
    }

    /**
     * Génère une URL présignée pour consultation temporaire (back-office authentifié).
     */
    public String resolveAccessUrl(String storedReference) {
        if (storedReference == null || storedReference.isBlank()) {
            return null;
        }

        String objectKey = extractObjectKey(storedReference);
        if (objectKey == null || objectKey.isBlank()) {
            return null;
        }

        try {
            return minioClient.getPresignedObjectUrl(
                    GetPresignedObjectUrlArgs.builder()
                            .method(Method.GET)
                            .bucket(bucket)
                            .object(objectKey)
                            .expiry(presignExpiryMinutes, TimeUnit.MINUTES)
                            .build());
        } catch (Exception e) {
            log.warn("Impossible de générer URL présignée pour {} : {}", objectKey, e.getMessage());
            return null;
        }
    }

    private String extractObjectKey(String storedReference) {
        String trimmed = storedReference.trim();

        // Clé objet directe (format courant après migration)
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return trimmed;
        }

        // Rétrocompatibilité : URL complète stockée avant sécurisation MinIO
        String marker = "/" + bucket + "/";
        int index = trimmed.indexOf(marker);
        if (index >= 0) {
            return trimmed.substring(index + marker.length());
        }

        return null;
    }

    private static String extensionOf(String filename) {
        if (filename == null || !filename.contains(".")) {
            return ".bin";
        }
        return filename.substring(filename.lastIndexOf('.'));
    }
}
