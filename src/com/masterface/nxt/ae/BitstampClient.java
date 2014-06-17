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
import java.util.logging.Level;


public class BitstampClient {

    JSONObject getJsonResponse(boolean isLogRequests) {
        JSONObject response;
        try {
            HttpClient client = HttpClientBuilder.create().build();
            HttpGet get = new HttpGet("https://www.bitstamp.net/api/ticker/");
            HttpResponse httpResponse = client.execute(get);

            try (Reader reader = new BufferedReader(new InputStreamReader(httpResponse.getEntity().getContent()))) {
                response = (JSONObject) JSONValue.parse(reader);
            }
        } catch (RuntimeException | IOException e) {
            AssetObserver.log.log(Level.SEVERE, e.getMessage(), e);
            throw new IllegalStateException(e);
        }
        if (isLogRequests) {
            ArrayList<String> lines = new ArrayList<>();
            lines.add("");
            lines.add(response.toJSONString());
            try {
                Files.write(Paths.get(JsonProvider.EXCHANGE_RATES_LOG), lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return response;
    }

}
