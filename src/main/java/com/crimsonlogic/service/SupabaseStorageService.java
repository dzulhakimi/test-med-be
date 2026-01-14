package com.crimsonlogic.service;

import org.apache.http.HttpEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@Service
public class SupabaseStorageService {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.api.key}")
    private String supabaseApiKey;

    @Value("${supabase.storage.bucket}")
    private String bucketName;

    /**
     * Uploads a file to Supabase Storage
     * @param file The file to upload
     * @return The public URL of the uploaded file
     */
    public String uploadFile(MultipartFile file) {
        try {
            String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String uploadUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filename;

            CloseableHttpClient httpClient = HttpClients.createDefault();
            HttpPost uploadFile = new HttpPost(uploadUrl);
            uploadFile.setHeader("Authorization", "Bearer " + supabaseApiKey);
            uploadFile.setHeader("apikey", supabaseApiKey);

            HttpEntity entity = MultipartEntityBuilder.create()
                    .addBinaryBody("file", file.getBytes(), org.apache.http.entity.ContentType.DEFAULT_BINARY, filename)
                    .build();
            uploadFile.setEntity(entity);

            CloseableHttpResponse response = httpClient.execute(uploadFile);
            int statusCode = response.getStatusLine().getStatusCode();

            if (statusCode == 200 || statusCode == 201) {
                // Return the public URL
                return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filename;
            } else {
                throw new RuntimeException("Failed to upload file to Supabase. Status: " + statusCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("Error uploading file to Supabase Storage", e);
        }
    }

    /**
     * Get the public URL for a file
     * @param filename The filename
     * @return The public URL
     */
    public String getPublicUrl(String filename) {
        return supabaseUrl + "/storage/v1/object/public/" + bucketName + "/" + filename;
    }

    /**
     * Delete a file from Supabase Storage
     * @param filename The filename to delete
     */
    public void deleteFile(String filename) {
        // Implementation for delete if needed
        try {
            String deleteUrl = supabaseUrl + "/storage/v1/object/" + bucketName + "/" + filename;
            CloseableHttpClient httpClient = HttpClients.createDefault();
            org.apache.http.client.methods.HttpDelete deleteRequest = new org.apache.http.client.methods.HttpDelete(deleteUrl);
            deleteRequest.setHeader("Authorization", "Bearer " + supabaseApiKey);
            deleteRequest.setHeader("apikey", supabaseApiKey);

            httpClient.execute(deleteRequest);
        } catch (Exception e) {
            System.err.println("Error deleting file from Supabase: " + e.getMessage());
        }
    }
}
