package com.masterface.nxt.ae;


import org.json.simple.JSONObject;

import java.util.*;

public class AssetObserver {

    public static final String ADDRESS = "localhost:7876";
    public static final int NQT_IN_NXT = 100000000;
    public static final int ASSET_EXCHANGE_BLOCK = 135000;
    public static final int COLORED_COINS = 2;
    public static final int COLORED_COINS_ASK = 2;
    public static final int COLORED_COINS_BID = 3;

    public static void main(String[] args) {
        AssetObserver assetObserver = new AssetObserver();
        assetObserver.load();
    }

    private void load() {
        Map<String, Asset> assets = NxtApi.getAllAssets();
        List<Trade> trades = NxtApi.getAllTrades();
        for (Trade trade : trades) {
            JSONObject bidTransaction = NxtApi.getTransaction(trade.getBidOrderId());
            System.out.println("Bid:" + bidTransaction);
            if (!((Long)bidTransaction.get("type") == COLORED_COINS) || !((Long) bidTransaction.get("subtype") == COLORED_COINS_BID)) {
                throw new IllegalStateException();
            }
            trade.setRecipientAccount((String) bidTransaction.get("sender"));

            JSONObject askTransaction = NxtApi.getTransaction(trade.getAskOrderId());
            System.out.println("Ask:" + askTransaction);
            if (!((Long)askTransaction.get("type") == COLORED_COINS) || !((Long) askTransaction.get("subtype") == COLORED_COINS_ASK)) {
                throw new IllegalStateException();
            }
            trade.setSenderAccount((String) askTransaction.get("sender"));

            System.out.println(trade);
            assets.get(trade.getAssetId()).addTransfer(trade);
        }
        for (Asset asset : assets.values()) {
            if (asset.getNumberOfTrades() != assets.get(asset.getId()).getNumberOfTransfers()) {
                throw new IllegalStateException(asset.toString());
            }
        }
        System.out.println("Trades loading - Ok");
        List<Transfer> assetTransfers = NxtApi.getAssetTransfers();
        for (Transfer transfer : assetTransfers) {
            assets.get(transfer.getAssetId()).addTransfer(transfer);
        }
        for (Asset asset : assets.values()) {
            asset.sortTransfers();
            asset.setLastPrice();
            asset.calcAccountQty();

            System.out.printf("Asset %s quantity %d price %f value %d\n",
                    asset.getName(), asset.getQuantityQNT(), asset.getLastPrice(), asset.getAssetValue());
            asset.analyzeAccountBalances();
        }
        for (Asset asset : assets.values()) {
            AccountBalance accountBalance = asset.getAccountBalance("13196039393619977660");
            if (accountBalance == null) {
                continue;
            }
            if (accountBalance.getQuantityQNT() != 0) {
                long currentValue = (long) (accountBalance.getQuantityQNT() * asset.getLastPrice());
                long investment = accountBalance.getNxtBalance();
                System.out.printf("LYLY Asset %s quantity %d price %f nxt value %d nxt balance %d percent %s\n",
                        asset.getName(), accountBalance.getQuantityQNT(), asset.getLastPrice(), currentValue, investment,
                        investment == 0 ? "N/A" : (double)((double)currentValue/(double)-investment*100));
            }
        }
    }
}
