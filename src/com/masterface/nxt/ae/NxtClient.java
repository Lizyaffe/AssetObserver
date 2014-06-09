package com.masterface.nxt.ae;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Map;

public class NxtClient implements JsonProvider {

    HttpClient client;
    ArrayList<String> lines = new ArrayList<>();

    public NxtClient() {
        this.client = HttpClientBuilder.create().build();
        this.lines = new ArrayList<>();
    }

    @Override
    public JSONObject getJsonResponse(Map<String, String> params) {
        return getJsonResponse(params, true);
    }

    JSONObject getJsonResponse(Map<String, String> params, boolean isLogRequests) {
        JSONObject response;
        String urlParams = Utils.getUrlParams(params);
        try {
            String spec = "http://" + AssetObserver.ADDRESS + "/nxt?" + urlParams;
            HttpPost post = new HttpPost(spec);
            HttpResponse httpResponse = client.execute(post);
            try (Reader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent(), "UTF-8"))) {
                response = (JSONObject) JSONValue.parse(reader);
            }
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        if (isLogRequests && response != null) {
            lines.add(urlParams);
            lines.add(response.toJSONString());
        }
        return response;
    }

    public ArrayList<String> getLines() {
        return lines;
    }
}
