package com.masterface.nxt.ae;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

class AccountBalance implements Comparable<AccountBalance> {
    private final String accountId;
    private final Asset asset;
    private final boolean isIssuer;
    private long quantityQNT;
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
        return quantityQNT / (double) AssetObserver.MULTIPLIERS[(int) asset.getDecimals()];
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
                quantity += transfer.getQuantityQNT() / (double) AssetObserver.MULTIPLIERS[(int) asset.getDecimals()];
            } else {
                quantity -= transfer.getQuantityQNT() / (double) AssetObserver.MULTIPLIERS[(int) asset.getDecimals()];
            }
        }
        return quantity;
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
                totalValue += transfer.getQuantityQNT() * ((Trade) transfer).getPriceNQT() / AssetObserver.NQT_IN_NXT * AssetObserver.MULTIPLIERS[((int) asset.getDecimals())];
            } else {
                totalQty -= transfer.getQuantityQNT();
                totalValue -= transfer.getQuantityQNT() * ((Trade) transfer).getPriceNQT() / AssetObserver.NQT_IN_NXT * AssetObserver.MULTIPLIERS[((int) asset.getDecimals())];
            }
        }
        if (totalQty == 0) {
            return 0;
        }
        return (double) totalValue / totalQty;
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

    public Map<String, Object> getData() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("qty", String.format("%." + asset.getDecimals() + "f", getQuantity()));
        map.put("isIssuer", String.format("%b", isIssuer()));
        double nxtValue = getQuantity() * asset.getLastPrice();
        map.put("nxtValue", String.format("%.2f", nxtValue));
        map.put("usdValue", String.format("%.2f", nxtValue * AssetObserver.nxtUsdPrice));
        map.put("btcValue", String.format("%.2f", nxtValue * AssetObserver.nxtBtcPrice));
        map.put("buyQty", String.format("%." + asset.getDecimals() + "f", getQty(AccountTransfer.Type.INCOMING, true)));
        map.put("sellQty", String.format("%." + asset.getDecimals() + "f", getQty(AccountTransfer.Type.OUTGOING, true)));
        map.put("receiveQty", String.format("%." + asset.getDecimals() + "f", getQty(AccountTransfer.Type.INCOMING, false)));
        map.put("sendQty", String.format("%." + asset.getDecimals() + "f", getQty(AccountTransfer.Type.OUTGOING, false)));
        map.put("fees", String.format("%d", getFees()));
        map.put("id", asset.getId());
        return map;
    }

    public Map<String, Object> getDistributionData(int timeStamp, boolean ignoreIssuerAccount) {
        Map<String, Object> map = new LinkedHashMap<>();
        if (ignoreIssuerAccount && isIssuer) {
            return null;
        }
        double quantity = getQuantity();
        if (timeStamp > 0) {
            quantity = getQuantity(timeStamp);
        }
        if (Math.abs(quantity) < 1 / (double) AssetObserver.MULTIPLIERS[(int) asset.getDecimals()] / 2) {
            return null;
        }
        map.put("qty", String.format("%." + asset.getDecimals() + "f", quantity));
        double assetQuantity = ignoreIssuerAccount ? asset.getIssuedQuantity() : asset.getQuantity();
        map.put("percent", String.format("%.2f", quantity / assetQuantity * 100));
        map.put("isIssuer", String.format("%b", isIssuer()));
        return map;
    }

    public double getQty(AccountTransfer.Type type, boolean isTrade) {
        double volume = 0;
        for (AccountTransfer accountTransfer : transfers) {
            if (type != accountTransfer.getType()) {
                continue;
            }
            Transfer transfer = accountTransfer.getTransfer();
            if (transfer.isTrade() != isTrade) {
                continue;
            }
            volume += transfer.getQuantityQNT() / AssetObserver.MULTIPLIERS[((int) asset.getDecimals())];
        }
        return volume;
    }

}
