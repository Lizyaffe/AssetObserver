package com.masterface.nxt.ae;


import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

public class UnitTestJsonProvider implements JsonProvider {

    private final String testResource;
    private Iterator<String> iterator;

    public UnitTestJsonProvider(String testResource) {
        this.testResource = testResource;
    }

    @Override
    public JSONObject getJsonResponse(Map<String, String> params) {
        if (iterator == null) {
            try {
                iterator = Files.readAllLines(Paths.get("test.resources/" + JSON_RESPONSE_JOURNAL + "." + testResource + ".log")).iterator();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        String urlParams = Utils.getUrlParams(params);
        if (!iterator.hasNext()) {
            throw new IllegalStateException(String.format("No match for request %s", urlParams));
        }
        String expectedUrlParams = iterator.next();
        if (!urlParams.equals(expectedUrlParams)) {
            throw new IllegalStateException(String.format("Unexpected request %s was expecting %s", urlParams, expectedUrlParams));
        }
        return (JSONObject)JSONValue.parse(iterator.next());
    }
}
