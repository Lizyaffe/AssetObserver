package com.masterface.nxt.ae;


import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.Map;

public class UnitTestJsonProvider implements JsonProvider {

    private Iterator<String> iterator;

    @Override
    public JSONObject getJsonResponse(Map<String, String> params) {
        if (iterator == null) {
            try {
                iterator = Files.readAllLines(Paths.get("test.resources/JsonResponseJournal.log")).iterator();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        if (!Utils.getUrlParams(params).equals(iterator.next())) {
            throw new IllegalStateException();
        }
        return (JSONObject) JSONValue.parse(iterator.next());
    }
}
