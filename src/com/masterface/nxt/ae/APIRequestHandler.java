package com.masterface.nxt.ae;

import org.json.simple.JSONObject;
import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;

public abstract class APIRequestHandler {
    static String generateErrorResponse(String format, Object... args) {
        JSONObject response = new JSONObject();
        response.put("response", String.format(format, args));
        return JSONValue.toJSONString(response);
    }

    abstract String processRequest(AssetObserver assetObserver, HttpServletRequest request);
}
