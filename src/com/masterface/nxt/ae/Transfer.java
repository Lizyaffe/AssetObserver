package com.masterface.nxt.ae;

/**
* Created by lyaf on 6/6/2014.
*/
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

    public String getRecipientAccount() {
        return recipientAccount;
    }

    public void setSenderAccount(String senderAccount) {
        this.senderAccount = senderAccount;
    }

    public void setRecipientAccount(String recipientAccount) {
        this.recipientAccount = recipientAccount;
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
}
