package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

public class BitstampApi {

    private final BitstampClient client;

    public BitstampApi(BitstampClient client) {
        this.client = client;
    }

    public double getBtcUsdLastPrice() {
        JSONObject jsonResponse = client.getJsonResponse(true);
        Object lastPrice = jsonResponse.get("last");
        if (lastPrice instanceof String) {
            return Double.parseDouble((String)lastPrice);
        }
        return (Double)lastPrice;
    }

}
