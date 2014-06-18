package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

import java.util.*;

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
    private double lastPrice;
    private int creationTimeStamp;
    private long creationFee;

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
        return quantityQNT / (double) AssetObserver.MULTIPLIERS[(int) getDecimals()];
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
        accountBalancesMap.put(accountId, new AccountBalance(accountId, this, quantityQNT));
        for (Transfer transfer : transfers) {
            String senderId = transfer.getSenderAccount();
            AccountBalance sender = accountBalancesMap.get(senderId);
            if (sender == null) {
                sender = new AccountBalance(senderId, this, 0);
            }
            sender.send(transfer);

            String recipientId = transfer.getRecipientAccount();
            AccountBalance recipient = accountBalancesMap.get(recipientId);
            if (recipient == null) {
                recipient = new AccountBalance(recipientId, this, 0);
                accountBalancesMap.put(recipientId, recipient);
            }
            recipient.receive(transfer);
        }
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
     * Set the asset price according to the most recent trade
     */
    public void setLastPrice() {
        for (int i = transfers.size() - 1; i > 0; i--) {
            if (transfers.get(i) instanceof Trade) {
                lastPrice = ((Trade) transfers.get(i)).getPriceNQT();
                break;
            }
        }
    }

    public AccountBalance getIssuerAccount() {
        return accountBalancesMap.get(accountId);
    }

    public List<AccountBalance> getAccountBalancesList() {
        List<AccountBalance> accountBalancesList = new ArrayList<>();
        accountBalancesList.addAll(accountBalancesMap.values());
        Collections.sort(accountBalancesList, new AccountBalanceComparator());
        return Collections.unmodifiableList(accountBalancesList);
    }

    public Map<String, Object> getData(Map<String, Double> exchangeRates) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("qty", String.format("%." + getDecimals() + "f", getQuantity()));
        map.put("issuerAccount", String.format("%s", getIssuerAccount().getAccountId()));
        map.put("issuedQty", String.format("%." + getDecimals() + "f", getQuantity() - getIssuerAccount().getQuantity()));
        map.put("nxtPrice", String.format("%.2f", getLastPrice()));
        map.put("nxtValue", String.format("%d", getAssetValue()));
        map.put("usdValue", String.format("%.2f", getAssetValue() * exchangeRates.get(AssetObserver.NXT_USD)));
        map.put("nofTrades", String.format("%d", getNumberOfTrades()));
        double tradeVolume = getTradeVolume();
        map.put("nxtVolume", String.format("%d", Math.round(tradeVolume)));
        map.put("usdVolume", String.format("%d", Math.round(tradeVolume * exchangeRates.get(AssetObserver.NXT_USD))));
        map.put("creationTime", String.format("%s", Utils.fromEpochTime(creationTimeStamp)));
        map.put("creationFee", String.format("%d", creationFee / AssetObserver.NQT_IN_NXT));
        map.put("decimals", String.format("%d", decimals));
        map.put("description", description);
        map.put("id", assetId);
        return map;
    }

    public double getTradeVolume() {
        double volume = 0;
        for (Transfer transfer : transfers) {
            if (!transfer.isTrade()) {
                continue;
            }
            volume += transfer.getQuantityQNT() * ((Trade) transfer).getPriceNQT() / AssetObserver.NQT_IN_NXT;
        }
        return volume;
    }

    public void setTimeStamp(Integer timeStamp) {
        this.creationTimeStamp = timeStamp;
    }

    public void setCreationFee(Long creationFee) {
        this.creationFee = creationFee;
    }

    public ArrayList<Transfer> getTransfers() {
        return transfers;
    }

    static class AccountBalanceComparator implements Comparator<AccountBalance> {

        @Override
        public int compare(AccountBalance o1, AccountBalance o2) {
            if (o1.getQuantity() > o2.getQuantity()) {
                return -1;
            }
            if (o1.getQuantity() < o2.getQuantity()) {
                return 1;
            }
            return 0;
        }
    }
}
