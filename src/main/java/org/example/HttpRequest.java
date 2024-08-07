package org.example;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.util.concurrent.CompletableFuture;

public class HttpRequest {

    private static final String serverUrl = "https://api.barisyasaman.com/patchVotes"; // Sunucu URL'si
    private static final String AUTHORIZATION_KEY = "df96a2ac-cd2a-4721-a556-c8adf7b1fc7f"; // Authorization Key

    public static CompletableFuture<Boolean> sendDataToServer(String voteType,String data) {
        return CompletableFuture.supplyAsync(() -> {
            try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
                HttpPost post = new HttpPost(serverUrl);
                post.setHeader("Content-Type", "application/json"); // JSON formatında veri gönderiyoruz
                post.setHeader("authorization",  AUTHORIZATION_KEY); // Authorization başlığı

                // JSON formatında veri hazırlama
                String jsonData = "{\"type\":\"" + voteType + "\",\"data\":\"" + data + "\"}";
                System.out.println(jsonData);
                post.setEntity(new StringEntity(jsonData, "UTF-8"));

                HttpResponse response = httpClient.execute(post);
                HttpEntity entity = response.getEntity();
                String responseBody = EntityUtils.toString(entity);

                System.out.println("Response: " + responseBody);
                return true;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        });
    }
}
