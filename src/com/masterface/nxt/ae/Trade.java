package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

import java.util.Map;

class Trade extends Transfer {
    private final Long priceNQT;
    private final String askOrder;
    private final String bidOrder;

    Trade(JSONObject assetJson) {
        super((String) assetJson.get("asset"), (Long) assetJson.get("timestamp"), Long.parseLong((String) assetJson.get("quantityQNT")),
                (String) assetJson.get("block"), null, null);
        this.priceNQT = Long.parseLong((String) assetJson.get("priceNQT"));
        this.askOrder = (String) assetJson.get("askOrder");
        this.bidOrder = (String) assetJson.get("bidOrder");
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
    public boolean isTrade() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "Trade{" +
                "priceNQT=" + priceNQT +
                ", askOrder=" + askOrder +
                ", bidOrder=" + bidOrder +
                '}';
    }

    @Override
    public Map<String, Object> getData(Asset asset) {
        Map<String, Object> map = super.getData(asset);
        double price = (double) getPriceNQT() / AssetObserver.NQT_IN_NXT * (double) AssetObserver.MULTIPLIERS[(int) asset.getDecimals()];
        map.put("price", String.format("%.8f", price));
        double qty = getQuantityQNT() / (double) AssetObserver.MULTIPLIERS[(int) asset.getDecimals()];
        double nxtValue = qty * price;
        map.put("nxtValue", String.format("%.2f", nxtValue));
        return map;
    }

    public long getVolume() {
        return getQuantityQNT() * getPriceNQT() / AssetObserver.NQT_IN_NXT;
    }
}
