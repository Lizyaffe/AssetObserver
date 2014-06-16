package com.masterface.nxt.ae;

import java.util.LinkedHashMap;
import java.util.Map;

class Transfer implements Comparable<Transfer> {
    private final String assetId;
    private final Long timestamp;
    private final Long quantityQNT;
    private final String block;
    private String senderAccount;
    private String recipientAccount;

    Transfer(String assetId, Long timestamp, Long quantityQNT, String block, String senderAccount, String recipientAccount) {
        this.assetId = assetId;
        this.timestamp = timestamp;
        this.quantityQNT = quantityQNT;
        this.block = block;
        this.senderAccount = senderAccount;
        this.recipientAccount = recipientAccount;
    }

    public String getAssetId() {
        return assetId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public Long getQuantityQNT() {
        return quantityQNT;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getBlock() {
        return block;
    }

    public String getSenderAccount() {
        return senderAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public void setRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
    }

    public boolean isTrade() {
        return false;
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") Transfer t) {
        if (getTimestamp() < t.getTimestamp()) {
            return -1;
        }
        if (getTimestamp() > t.getTimestamp()) {
            return 1;
        }
        return 0;
    }

    @Override
    public String toString() {
        return "Transfer{" +
                "assetId='" + assetId + '\'' +
                ", timestamp=" + timestamp +
                ", quantityQNT=" + quantityQNT +
                ", block='" + block + '\'' +
                ", senderAccount='" + senderAccount + '\'' +
                ", recipientAccount='" + recipientAccount + '\'' +
                '}';
    }

    public Map<String, Object> getData(Asset asset) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("type", String.format("%s", isTrade() ? "trade" : "transfer"));
        double qty = quantityQNT / (double) AssetObserver.MULTIPLIERS[(int) asset.getDecimals()];
        map.put("qty", String.format("%." + asset.getDecimals() + "f", qty));
        double nxtValue = qty * asset.getLastPrice();
        map.put("nxtValue", String.format("%.2f", nxtValue));
        map.put("timeStamp", String.format("%d", timestamp));
        map.put("date", String.format("%s", Utils.fromEpochTime((int) (long) timestamp)));
        map.put("sender", String.format("%s", senderAccount));
        map.put("recipient", String.format("%s", recipientAccount));
        return map;
    }
}
