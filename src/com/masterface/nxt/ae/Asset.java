package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

import java.util.*;
import java.util.logging.Level;
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

    public void calcAccountQty(Asset asset) {
        accountBalancesMap.put(accountId, new AccountBalance(accountId, asset, quantityQNT, true));
        for (Transfer transfer : transfers) {
            String senderId = transfer.getSenderAccount();
            AccountBalance sender = accountBalancesMap.get(senderId);
            sender.send(transfer);

            String recipientId = transfer.getRecipientAccount();
            AccountBalance recipient = accountBalancesMap.get(recipientId);
            if (recipient == null) {
                recipient = new AccountBalance(recipientId, asset, 0, false);
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

    public void analyzeAccountBalances() {
        double accountQNT = 0;
        for (AccountBalance balance : accountBalancesMap.values()) {
            accountQNT += balance.getQuantity();
        }
        if (Math.abs(accountQNT - getQuantity()) < (1 / AssetObserver.MULTIPLIERS[7])) {
            throw new IllegalStateException("" + name);
        }
        if (AssetObserver.log.isLoggable(Level.FINE)) {
            AssetObserver.log.info(name + " issuer " + accountId + " balance " + getQNTPercent(getIssuerAccount().getQuantity()));
        }
        for (AccountBalance balance : accountBalancesList) {
            if (balance.getQuantity() == 0) {
                break;
            }
            if (AssetObserver.log.isLoggable(Level.FINE)) {
                AssetObserver.log.info(String.format("Account %s quantity %.2f (%.2f%%) value %.2f fifo price %.2f%n",
                        balance.getAccountId(), balance.getQuantity(), getQNTPercent(balance.getQuantity()),
                        balance.getQuantity() * lastPrice / AssetObserver.NQT_IN_NXT, balance.getFifoPrice()));
            }
        }
    }

    private double getQNTPercent(double qnt) {
        return qnt / quantityQNT * 100;
    }

    public void setLastPrice() {
        for (int i = transfers.size() - 1; i > -0; i--) {
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

    public JSONObject toJsonObject() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", assetId);
        jsonObject.put("name", name);
        jsonObject.put("quantity", getQuantity());
        jsonObject.put("price", getLastPrice());
        jsonObject.put("totalValueNxt", getAssetValue());
        jsonObject.put("totalValueBtc", getAssetValue() * AssetObserver.nxtBtcPrice);
        jsonObject.put("totalValueUsd", (long)(getAssetValue() * AssetObserver.nxtUsdPrice));
        jsonObject.put("totalValueCny", (long)(getAssetValue() * AssetObserver.nxtCnyPrice));
        jsonObject.put("issuedQuantity", (getQuantity() - getIssuerAccount().getQuantity()));
        jsonObject.put("issuedValueNxt", getTradedValue());
        jsonObject.put("issuedValueBtc", getTradedValue() * AssetObserver.nxtBtcPrice);
        jsonObject.put("issuedValueUsd", (long)(getTradedValue() * AssetObserver.nxtUsdPrice));
        jsonObject.put("issuedValueCny", (long)(getTradedValue() * AssetObserver.nxtCnyPrice));
        jsonObject.put("numberOfTrades", getNumberOfTrades());
        jsonObject.put("tradeVolume", getTradeVolume());
        jsonObject.put("transfers", getNumberOfTransfers());
        jsonObject.put("transferVolume", getTransferVolume());
        return jsonObject;
    }

    public Object getTradeVolume() {
        return 0;
    }

    public Object getTransferVolume() {
        return 0;
    }
}
