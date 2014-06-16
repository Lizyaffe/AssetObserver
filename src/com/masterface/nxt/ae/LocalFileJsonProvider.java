package com.masterface.nxt.ae;


import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;

public class LocalFileJsonProvider implements JsonProvider {

    private final Path testResource;
    private Iterator<String> iterator;

    public LocalFileJsonProvider(Path testResource) {
        this.testResource = testResource;
    }

    @Override
    public JSONObject getJsonResponse(Map<String, String> params) {
        if (iterator == null) {
            try {
                iterator = Files.readAllLines(testResource, Charset.forName("utf8")).iterator();
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
        return (JSONObject) JSONValue.parse(iterator.next());
    }

    @Override
    public ArrayList<String> getLines() {
        return null;
    }

    @Override
    public void resetLines() {
    }
}
