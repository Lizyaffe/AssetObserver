package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

public class BterApi {

    private final BterClient bterClient;

    public BterApi(BterClient bterClient) {
        this.bterClient = bterClient;
    }

    public double getLastPrice(String fromSymbol, String toSymbol) {
        JSONObject jsonResponse = bterClient.getJsonResponse(fromSymbol + "_" + toSymbol, true);
        Object lastPrice = jsonResponse.get("last");
        if (lastPrice instanceof String) {
            return Double.parseDouble((String)lastPrice);
        }
        return (Double)lastPrice;
    }

}
