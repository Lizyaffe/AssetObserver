package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

import java.util.*;
import java.util.logging.Level;
import java.util.stream.Collectors;

class Asset {
    private final String assetId;
    private final String accountId;
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

    public long getQuantityQNT() {
        return quantityQNT;
    }

    public long getNumberOfTrades() {
        return numberOfTrades;
    }

    public long getNumberOfTransfers() {
        return transfers.size();
    }

    public double getLastPrice() {
        return lastPrice / AssetObserver.NQT_IN_NXT;
    }

    public void addTransfer(Transfer transfer) {
        transfers.add(transfer);
    }

    public void sortTransfers() {
        Collections.sort(transfers);
    }

    public long getAssetValue() {
        return (long) (getLastPrice() * quantityQNT);
    }

    public void calcAccountQty() {
        accountBalancesMap.put(accountId, new AccountBalance(accountId, quantityQNT));
        for (Transfer transfer : transfers) {
            String senderId = transfer.getSenderAccount();
            AccountBalance sender = accountBalancesMap.get(senderId);
            sender.send(transfer);

            String recipientId = transfer.getRecipientAccount();
            AccountBalance recipient = accountBalancesMap.get(recipientId);
            if (recipient == null) {
                recipient = new AccountBalance(recipientId, 0);
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
        long accountQNT = 0;
        for (AccountBalance balance : accountBalancesMap.values()) {
            accountQNT += balance.getQuantityQNT();
        }
        if (accountQNT != quantityQNT) {
            throw new IllegalStateException("" + name);
        }
        if (AssetObserver.log.isLoggable(Level.INFO)) {
            AssetObserver.log.info(name + " issuer " + accountId + " balance:" + getQNTPercent(accountBalancesMap.get(accountId).getQuantityQNT()));
        }
        for (AccountBalance balance : accountBalancesList) {
            if (balance.getQuantityQNT() == 0) {
                break;
            }
            if (AssetObserver.log.isLoggable(Level.INFO)) {
                AssetObserver.log.info(String.format("Account %s quantity %d (%d%%) value %f nxt balance %d transfer quantity %d\n",
                        balance.getAccountId(), balance.getQuantityQNT(), getQNTPercent(balance.getQuantityQNT()),
                        balance.getQuantityQNT() * lastPrice / AssetObserver.NQT_IN_NXT, balance.getNxtBalance(), balance.getTransferBalance()));
            }
        }
    }

    private long getQNTPercent(long qnt) {
        return (long) ((double) qnt / quantityQNT * 100);
    }

    public void setLastPrice() {
        for (int i = transfers.size() - 1; i > -0; i--) {
            if (transfers.get(i) instanceof Trade) {
                lastPrice = ((Trade) transfers.get(i)).getPriceNQT();
                break;
            }
        }
    }
}
