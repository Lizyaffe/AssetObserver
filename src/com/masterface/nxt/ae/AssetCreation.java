package com.masterface.nxt.ae;

public class AssetCreation {

    private final String assetId;
    private final Integer timeStamp;
    private final Long feeNqt;

    public AssetCreation(String assetId, Integer timeStamp, Long feeNqt) {
        this.assetId = assetId;
        this.timeStamp = timeStamp;
        this.feeNqt = feeNqt;
    }

    public String getAssetId() {
        return assetId;
    }

    public Integer getTimeStamp() {
        return timeStamp;
    }

    public Long getFeeNqt() {
        return feeNqt;
    }
}
