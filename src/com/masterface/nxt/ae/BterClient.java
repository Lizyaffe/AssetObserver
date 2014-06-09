package com.masterface.nxt.ae;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;


public class BterClient {

    JSONObject getJsonResponse(String urlParams, boolean isLogRequests) {
        JSONObject response;
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet("http://data.bter.com/api/1/ticker/" + urlParams);
            HttpResponse httpResponse = client.execute(get);

            try (Reader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()))) {
                response = (JSONObject)JSONValue.parse(reader);
            }
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            throw new IllegalStateException(e);
        }
        if (isLogRequests) {
            ArrayList<String> lines = new ArrayList<>();
            lines.add(urlParams);
            lines.add(response.toJSONString());
            try {
                Files.write(Paths.get(JsonProvider.JSON_RESPONSE_JOURNAL + ".log"), lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return response;
    }

}
