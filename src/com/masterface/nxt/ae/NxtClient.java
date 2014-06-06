package com.masterface.nxt.ae;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Map;

public class NxtClient implements JsonProvider {

    @Override
    public JSONObject getJsonResponse(Map<String, String> params) {
        return getJsonResponse(params, true);
    }

    JSONObject getJsonResponse(Map<String, String> params, boolean isLogRequests) {
        JSONObject response;
        HttpURLConnection connection = null;
        String urlParams = Utils.getUrlParams(params);
        try {
            String spec = "http://" + AssetObserver.ADDRESS + "/nxt?" + urlParams;
            URL url = new URL(spec);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);
            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                try (Reader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"))) {
                    response = (JSONObject) JSONValue.parse(reader);
                }
            } else {
                response = null;
            }
        } catch (RuntimeException | IOException e) {
            e.printStackTrace();
            if (connection != null) {
                connection.disconnect();
            }
            throw new IllegalStateException(e);
        }
        if (isLogRequests && response != null) {
            ArrayList<String> lines = new ArrayList<>();
            lines.add(urlParams);
            lines.add(response.toJSONString());
            try {
                Files.write(Paths.get(JSON_RESPONSE_JOURNAL_LOG), lines, Charset.forName("UTF-8"), StandardOpenOption.APPEND);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        return response;
    }

}
