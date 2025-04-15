package com.autumnia.rag.movierag.util;

import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class URLTest {
    // Helper method to fetch YouTube link using ChatClient
    public static List<String> searchYouTube(String movieTitle) throws Exception {

        String url = "https://www.googleapis.com/youtube/v3/search?part=snippet&type=video&q="
                + movieTitle
                + "&order=relevance"
                + "&key=AIzaSyAbyCfTNakLnu7hsg5xRr4SM16AWOwQZHw";

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);

        List<String> videoUrls = new ArrayList<>();
        JSONObject jsonResponse = new JSONObject(response.getBody());
        JSONArray items = jsonResponse.getJSONArray("items");

        for (int i = 0; i < items.length(); i++) {
            JSONObject item = items.getJSONObject(i);
            String videoId = item.getJSONObject("id").getString("videoId");
            videoUrls.add(videoId);
        }
        return videoUrls;
    }
}