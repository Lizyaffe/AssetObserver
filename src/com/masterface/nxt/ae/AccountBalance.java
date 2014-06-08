package com.masterface.nxt.ae;

import java.util.ArrayList;
import java.util.List;

class AccountBalance implements Comparable<AccountBalance> {
    private final String accountId;
    private final Asset asset;
    private long quantityQNT;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<Trade> incomingTrade;
    private List<Trade> outgoingTrade;
    private List<Transfer> incomingTransfer;
    private List<Transfer> outgoingTransfer;

    AccountBalance(String accountId, Asset asset, long quantityQNT) {
        this.accountId = accountId;
        this.asset = asset;
        this.quantityQNT = quantityQNT;
        this.incomingTrade = new ArrayList<>();
        this.outgoingTrade = new ArrayList<>();
        this.incomingTransfer = new ArrayList<>();
        this.outgoingTransfer = new ArrayList<>();
    }

    public void send(Transfer transfer) {
        quantityQNT -= transfer.getQuantityQNT();
        if (transfer instanceof Trade) {
            outgoingTrade.add((Trade) transfer);
        } else {
            outgoingTransfer.add(transfer);
        }
    }

    public void receive(Transfer transfer) {
        quantityQNT += transfer.getQuantityQNT();
        if (transfer instanceof Trade) {
            incomingTrade.add((Trade) transfer);
        } else {
            incomingTransfer.add(transfer);
        }
    }

    public double getQuantity() {
        return quantityQNT / (double)AssetObserver.MULTIPLIERS[(int)asset.getDecimals()];
    }

    public String getAccountId() {
        return accountId;
    }

    public Asset getAsset() {
        return asset;
    }

    public double getFifoPrice() {
        long totalQty = 0;
        long totalValue = 0;
        for (Trade trade : incomingTrade) {
            totalQty += trade.getQuantityQNT();
            totalValue += trade.getQuantityQNT() * trade.getPriceNQT() / AssetObserver.NQT_IN_NXT * AssetObserver.MULTIPLIERS[((int) asset.getDecimals())];
        }
        for (Trade trade : outgoingTrade) {
            totalQty -= trade.getQuantityQNT();
            totalValue -= trade.getQuantityQNT() * trade.getPriceNQT() / AssetObserver.NQT_IN_NXT * AssetObserver.MULTIPLIERS[((int) asset.getDecimals())];
        }
        if (totalQty == 0) {
            return 0;
        }
        return (double)totalValue/totalQty;
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") AccountBalance balance) {
        if (getQuantity() > balance.getQuantity()) {
            return -1;
        }
        if (getQuantity() < balance.getQuantity()) {
            return 1;
        }
        return 0;
    }
}
