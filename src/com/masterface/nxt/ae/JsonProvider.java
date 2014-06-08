package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

import java.util.Map;

public interface JsonProvider {

    public static final String JSON_RESPONSE_JOURNAL = "JsonResponseJournal";

    public JSONObject getJsonResponse(Map<String, String> params);

}
