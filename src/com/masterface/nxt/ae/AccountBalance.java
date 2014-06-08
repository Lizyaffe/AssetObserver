package com.masterface.nxt.ae;

import java.util.ArrayList;
import java.util.List;

class AccountBalance implements Comparable<AccountBalance> {
    private final String accountId;
    private final Asset asset;
    private long quantityQNT;
    private final boolean isIssuer;
    private long fees;
    private List<AccountTransfer> transfers;

    AccountBalance(String accountId, Asset asset, long quantityQNT, boolean isIssuer) {
        this.accountId = accountId;
        this.asset = asset;
        this.quantityQNT = quantityQNT;
        this.isIssuer = isIssuer;
        if (isIssuer) {
            fees += 1000;
        }
        this.transfers = new ArrayList<>();
    }

    public void send(Transfer transfer) {
        quantityQNT -= transfer.getQuantityQNT();
        transfers.add(new AccountTransfer(transfer, AccountTransfer.Type.OUTGOING));
        fees++;
    }

    public void receive(Transfer transfer) {
        quantityQNT += transfer.getQuantityQNT();
        transfers.add(new AccountTransfer(transfer, AccountTransfer.Type.INCOMING));
        if (transfer.isTrade()) {
            // The assumption is that to receive a trade the account had to issue an ask order
            // however to receive a transfer there are no fees for the receiver
            fees++;
        }
    }

    public double getQuantity() {
        return quantityQNT / (double)AssetObserver.MULTIPLIERS[(int)asset.getDecimals()];
    }

    public double getQuantity(int timeStamp) {
        double quantity = 0;
        if (isIssuer) {
            quantity += asset.getQuantity();
        }
        for (AccountTransfer accountTransfer : transfers) {
            Transfer transfer = accountTransfer.getTransfer();
            if (transfer.getTimestamp() > timeStamp) {
                break;
            }
            if (accountTransfer.getType() == AccountTransfer.Type.INCOMING) {
                quantity += transfer.getQuantityQNT();
            } else {
                quantity -= transfer.getQuantityQNT();
            }
        }
        return quantity / (double)AssetObserver.MULTIPLIERS[(int)asset.getDecimals()];
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
        for (AccountTransfer accountTransfer : transfers) {
            Transfer transfer = accountTransfer.getTransfer();
            if (!transfer.isTrade()) {
                continue;
            }
            if (accountTransfer.getType() == AccountTransfer.Type.INCOMING) {
                totalQty += transfer.getQuantityQNT();
                totalValue += transfer.getQuantityQNT() * ((Trade)transfer).getPriceNQT() / AssetObserver.NQT_IN_NXT * AssetObserver.MULTIPLIERS[((int) asset.getDecimals())];
            } else {
                totalQty -= transfer.getQuantityQNT();
                totalValue -= transfer.getQuantityQNT() * ((Trade)transfer).getPriceNQT() / AssetObserver.NQT_IN_NXT * AssetObserver.MULTIPLIERS[((int) asset.getDecimals())];
            }
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

    public double getValue() {
        return getQuantity() * asset.getLastPrice();
    }

    public boolean isIssuer() {
        return isIssuer;
    }

    public long getFees() {
        return fees;
    }
}
