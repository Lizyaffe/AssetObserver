package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

import java.util.*;
import java.util.stream.Collectors;

class Asset {
    private final String assetId;
    private final String accountId;
    @SuppressWarnings({"FieldCanBeLocal", "UnusedDeclaration"})
    private final String accountIdRs;
    private final String name;
    private final String description;
    private final long quantityQNT;
    private final long decimals;
    private final long numberOfTrades;
    private final ArrayList<Transfer> transfers;
    private final Map<String, AccountBalance> accountBalancesMap;
    private final List<AccountBalance> accountBalancesList;
    private double lastPrice;

    Asset(JSONObject assetJson) {
        this.assetId = (String) assetJson.get("asset");
        this.accountId = (String) assetJson.get("account");
        this.accountIdRs = (String) assetJson.get("accountRS");
        this.name = (String) assetJson.get("name");
        this.description = (String) assetJson.get("description");
        this.quantityQNT = Long.parseLong(((String) assetJson.get("quantityQNT")));
        this.decimals = (Long) assetJson.get("decimals");
        this.numberOfTrades = (Long) assetJson.get("numberOfTrades");
        this.transfers = new ArrayList<>();
        this.accountBalancesMap = new HashMap<>();
        this.accountBalancesList = new ArrayList<>();
    }

    public String getId() {
        return assetId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getAccountId() {
        return accountId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getName() {
        return name;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getDescription() {
        return description;
    }

    public long getDecimals() {
        return decimals;
    }

    public double getQuantity() {
        return quantityQNT / (double)AssetObserver.MULTIPLIERS[(int)getDecimals()];
    }

    public long getNumberOfTrades() {
        return numberOfTrades;
    }

    public long getNumberOfTransfers() {
        return transfers.size();
    }

    public double getLastPrice() {
        return lastPrice / AssetObserver.NQT_IN_NXT * AssetObserver.MULTIPLIERS[((int) getDecimals())];
    }

    public void addTransfer(Transfer transfer) {
        transfers.add(transfer);
    }

    public void sortTransfers() {
        Collections.sort(transfers);
    }

    public long getAssetValue() {
        return (long) (getLastPrice() * getQuantity());
    }

    public void setAccountBalanceDistribution() {
        accountBalancesMap.put(accountId, new AccountBalance(accountId, this, quantityQNT, true));
        for (Transfer transfer : transfers) {
            String senderId = transfer.getSenderAccount();
            AccountBalance sender = accountBalancesMap.get(senderId);
            sender.send(transfer);

            String recipientId = transfer.getRecipientAccount();
            AccountBalance recipient = accountBalancesMap.get(recipientId);
            if (recipient == null) {
                recipient = new AccountBalance(recipientId, this, 0, false);
                accountBalancesMap.put(recipientId, recipient);
            }
            recipient.receive(transfer);
        }
        accountBalancesList.addAll(accountBalancesMap.values().stream().collect(Collectors.toList()));
        Collections.sort(accountBalancesList);
    }

    @Override
    public String toString() {
        return "Asset{" +
                "assetId=" + assetId +
                ", accountId=" + accountId +
                ", name='" + name + '\'' +
                ", quantityQNT=" + quantityQNT +
                ", decimals=" + decimals +
                ", numberOfTrades=" + numberOfTrades +
                '}';
    }

    public AccountBalance getAccountBalance(String accountId) {
        return accountBalancesMap.get(accountId);
    }

    /**
     * Make sure the sum of account quantities matches the asset quantity
     */
    public void verifyAccountBalances() {
        double accountQNT = 0;
        for (AccountBalance balance : accountBalancesMap.values()) {
            accountQNT += balance.getQuantity();
        }
        if (Math.abs(accountQNT - getQuantity()) < (1 / AssetObserver.MULTIPLIERS[7])) {
            throw new IllegalStateException("" + name);
        }
    }

    @SuppressWarnings("UnusedDeclaration")
    private double getQNTPercent(double qnt) {
        return qnt / quantityQNT * 100;
    }

    /**
        Set the asset price according to the most recent trade
     */
    public void setLastPrice() {
        for (int i = transfers.size() - 1; i > 0; i--) {
            if (transfers.get(i) instanceof Trade) {
                lastPrice = ((Trade)transfers.get(i)).getPriceNQT();
                break;
            }
        }
    }

    public AccountBalance getIssuerAccount() {
        return accountBalancesMap.get(accountId);
    }

    public double getTradedValue() {
        return (getQuantity() - getIssuerAccount().getQuantity()) * getLastPrice();
    }

    public List<AccountBalance> getAccountBalancesList() {
        return accountBalancesList;
    }

    @SuppressWarnings("UnusedDeclaration")
    public List<DividendPayment> payDividend(long dividendAmount, Date date, boolean shouldPayIssuer, long threshold) {
        int timeStamp = Utils.getTimeStamp(date);
        double totalQuantity = 0;
        for (AccountBalance balance : accountBalancesList) {
            if (!shouldPayIssuer && balance.getAccountId().equals(balance.getAsset().getIssuerAccount().getAccountId())) {
                continue;
            }
            totalQuantity += balance.getQuantity(timeStamp);
        }
        List<DividendPayment> payments = new ArrayList<>();
        int carey = 0;
        for (AccountBalance balance : accountBalancesList) {
            if (!shouldPayIssuer && balance.getAccountId().equals(balance.getAsset().getIssuerAccount().getAccountId())) {
                continue;
            }
            double payment = balance.getQuantity() / totalQuantity * dividendAmount;
            if (payment >= threshold) {
                payments.add(new DividendPayment(balance.getAccountId(), payment));
            } else {
                carey += payment;
            }
        }
        // recursively pay dividend for the carey amount
        return payments;
    }

    public Map<String, Object> getData() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("Name", name);
        map.put("Qty", String.format("%." + getDecimals() +"f", getQuantity()));
        map.put("IssuerAcctQty", String.format("%." + getDecimals() +"f", getIssuerAccount().getQuantity()));
        map.put("NxtPrice", getLastPrice());
        map.put("NxtValue", getAssetValue());
//        jsonObject.put("BtcVal", getAssetValue() * AssetObserver.nxtBtcPrice);
        map.put("UsdValue", (long) (getAssetValue() * AssetObserver.nxtUsdPrice));
//        jsonObject.put("CnyVal", (long)(getAssetValue() * AssetObserver.nxtCnyPrice));
//        jsonObject.put("issuedQuantity", (getQuantity() - getIssuerAccount().getQuantity()));
//        map.put("NxtVal2", getTradedValue());
//        jsonObject.put("issuedValueBtc", getTradedValue() * AssetObserver.nxtBtcPrice);
//        map.put("UsdVal2", (long) (getTradedValue() * AssetObserver.nxtUsdPrice));
//        jsonObject.put("issuedValueCny", (long)(getTradedValue() * AssetObserver.nxtCnyPrice));
        map.put("nofTrades", getNumberOfTrades());
        double tradeVolume = getTradeVolume();
        map.put("NxtVolume", Math.round(tradeVolume));
        map.put("UsdVolume", Math.round(tradeVolume * AssetObserver.nxtUsdPrice));
//        jsonObject.put("tradeVolume", getTradeVolume());
//        map.put("nofTransfers", getNumberOfTransfers());
//        jsonObject.put("transferVolume", getTransferVolume());
        map.put("id", assetId);
        return map;
    }

    public double getTradeVolume() {
        double volume = 0;
        for (Transfer transfer : transfers) {
            if (!transfer.isTrade()) {
                continue;
            }
            volume += transfer.getQuantityQNT() * ((Trade)transfer).getPriceNQT() / AssetObserver.NQT_IN_NXT;
        }
        return volume;
    }

    public Object getTransferVolume() {
        return 0;
    }
}
