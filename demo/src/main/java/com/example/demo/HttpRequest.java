package com.example.demo;

import java.util.HashMap;
import java.util.Map;

public class HttpRequest {

    private Map<String, String> queryParams = new HashMap<>();

    public HttpRequest(String query) {
        if (query != null) {
            String[] pairs = query.split("&");
            for (String pair : pairs) {
                String[] keyValue = pair.split("=");
                if (keyValue.length == 2) {
                    queryParams.put(keyValue[0], keyValue[1]);
                }
            }
        }
    }

    public String getValue(String key) {
        return queryParams.get(key);
    }
}