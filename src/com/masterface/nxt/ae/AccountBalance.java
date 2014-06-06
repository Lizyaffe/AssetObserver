package com.masterface.nxt.ae;

import java.util.ArrayList;
import java.util.List;

class AccountBalance implements Comparable<AccountBalance> {
    private final String accountId;
    private long quantityQNT;
    @SuppressWarnings("MismatchedQueryAndUpdateOfCollection")
    private List<Trade> incomingTrade;
    private List<Trade> outgoingTrade;
    private List<Transfer> incomingTransfer;
    private List<Transfer> outgoingTransfer;

    AccountBalance(String accountId, long quantityQNT) {
        this.accountId = accountId;
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

    public long getQuantityQNT() {
        return quantityQNT;
    }

    public String getAccountId() {
        return accountId;
    }

    public long getNxtBalance() {
        long nxt = 0;
        for (Trade trade : incomingTrade) {
            nxt -= trade.getQuantityQNT() * trade.getPriceNQT() / AssetObserver.NQT_IN_NXT;
        }
        for (Trade trade : outgoingTrade) {
            nxt += trade.getQuantityQNT() * trade.getPriceNQT() / AssetObserver.NQT_IN_NXT;
        }
        return nxt;
    }

    public long getTransferBalance() {
        long qty = 0;
        for (Transfer transfer : incomingTransfer) {
            qty += transfer.getQuantityQNT();
        }
        for (Transfer transfer : outgoingTransfer) {
            qty -= transfer.getQuantityQNT();
        }
        return qty;
    }

    @Override
    public int compareTo(@SuppressWarnings("NullableProblems") AccountBalance balance) {
        if (quantityQNT > balance.getQuantityQNT()) {
            return -1;
        }
        if (quantityQNT < balance.getQuantityQNT()) {
            return 1;
        }
        return 0;
    }
}
