package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

/**
* Created by lyaf on 6/6/2014.
*/
class Trade extends Transfer {
    private final Long priceNQT;
    private final String askOrder;
    private final String bidOrder;

    Trade(JSONObject assetJson) {
        super((String)assetJson.get("asset"), (Long)assetJson.get("timestamp"), Long.parseLong((String)assetJson.get("quantityQNT")),
                (String)assetJson.get("block"), null, null);
        this.priceNQT = Long.parseLong((String) assetJson.get("priceNQT"));
        this.askOrder = (String)assetJson.get("askOrder");
        this.bidOrder = (String)assetJson.get("bidOrder");
    }

    public Long getPriceNQT() {
        return priceNQT;
    }

    public String getAskOrderId() {
        return askOrder;
    }

    public String getBidOrderId() {
        return bidOrder;
    }

    @Override
    public String toString() {
        return super.toString() + "Trade{" +
                "priceNQT=" + priceNQT +
                ", askOrder=" + askOrder +
                ", bidOrder=" + bidOrder +
                '}';
    }
}
