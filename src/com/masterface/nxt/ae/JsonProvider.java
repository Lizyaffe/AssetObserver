package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.Map;

public interface JsonProvider {

    public static final String EXCHANGE_RATES_LOG = "exchangeRates.log";

    public JSONObject getJsonResponse(Map<String, String> params);

    ArrayList<String> getLines();

    void resetLines();
}
