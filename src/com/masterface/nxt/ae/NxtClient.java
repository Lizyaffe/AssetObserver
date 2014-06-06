package com.masterface.nxt.ae;

import nxt.util.CountingInputStream;
import nxt.util.CountingOutputStream;
import nxt.util.JSON;
import nxt.util.Logger;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;
import org.json.simple.JSONValue;

import java.io.*;
import java.net.*;
import java.util.Map;

public class NxtClient {

    static JSONObject getJsonResponse(Map<String, String> params) {
        JSONStreamAware request = JSON.prepareRequest(new JSONObject());
        JSONObject response;
        HttpURLConnection connection = null;
        try {
            String urlParams = getUrlParams(params);
            URL url = new URL("http://" + AssetObserver.ADDRESS + "/nxt?" + urlParams);
            connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("POST");
            connection.setDoOutput(true);

            CountingOutputStream cos = new CountingOutputStream(connection.getOutputStream());
            try (Writer writer = new BufferedWriter(new OutputStreamWriter(cos, "UTF-8"))) {
                request.writeJSONString(writer);
            }

            if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                CountingInputStream cis = new CountingInputStream(connection.getInputStream());
                try (Reader reader = new BufferedReader(new InputStreamReader(cis, "UTF-8"))) {
                    response = (JSONObject) JSONValue.parse(reader);
                }
            } else {
                response = null;
            }
        } catch (RuntimeException|IOException e) {
            if (! (e instanceof UnknownHostException || e instanceof SocketTimeoutException || e instanceof SocketException)) {
                Logger.logDebugMessage("Error sending JSON request", e);
            }
            response = null;
        }
        if (connection != null) {
            connection.disconnect();
        }
        return response;
    }

    private static String getUrlParams(Map<String, String> params) {
        if (params == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            sb.append(key).append("=").append(params.get(key)).append("&");
        }
        String rc = sb.toString();
        if (rc.endsWith("&")) {
            rc = rc.substring(0, rc.length() - 1);
        }
        return rc;
    }
}
