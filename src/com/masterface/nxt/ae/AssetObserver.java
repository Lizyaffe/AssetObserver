package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssetObserver {

    public static final String ADDRESS = "localhost:7876";
    public static final int NQT_IN_NXT = 100000000;
    public static final int ASSET_EXCHANGE_BLOCK = 135000;
    public static final int COLORED_COINS = 2;
    public static final int COLORED_COINS_ASK = 2;
    public static final int COLORED_COINS_BID = 3;
    public static final long[] MULTIPLIERS = {1, 10, 100, 1000, 10000, 100000, 1000000, 10000000, 100000000};
    public static Logger log;

    public static void main(String[] args) {
        AssetObserver assetObserver = new AssetObserver();
        JsonProvider jsonProvider = JsonProviderFactory.getJsonProvider(null);
        try {
            String logFile = NxtClient.JSON_RESPONSE_JOURNAL + ".log";
            Files.deleteIfExists(Paths.get(logFile));
            Files.createFile(Paths.get(logFile));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        Map<String, Asset> assets = assetObserver.load(jsonProvider);
        assetObserver.getMyBalance(assets, "13196039393619977660");
        assetObserver.getMyBalance(assets, "9747151086038883973");
        assetObserver.getMyBalance(assets, "3041433146235555849");
        Asset nemStake = assets.get("12465186738101000735");
        AccountBalance issuerAccount = nemStake.getIssuerAccount();
        double totalQuantity = nemStake.getQuantity();
        double totalValue = totalQuantity * nemStake.getLastPrice();
        double tradedQuantity = totalQuantity - issuerAccount.getQuantity();
        if (log.isLoggable(Level.INFO)) {
            log.info(String.format("Asset %s totalQuantity %f totalValue %f tradedQuantity %f tradedValue %f",
                    nemStake, totalQuantity, totalValue, tradedQuantity, nemStake.getTradedValue()));
        }
    }

    private static void initLogger() {
        log = Logger.getGlobal();
        log.setLevel(Level.INFO);
        log.fine("AssetObserver started");
    }

    public Map<String, Asset> load(JsonProvider jsonProvider) {
        initLogger();
        NxtApi nxtAPi = new NxtApi(jsonProvider);
        Map<String, Asset> assets = nxtAPi.getAllAssets();
        List<Trade> trades = nxtAPi.getAllTrades();
        for (Trade trade : trades) {
            JSONObject bidTransaction = nxtAPi.getTransaction(trade.getBidOrderId());
            if (log.isLoggable(Level.FINE)) {
                log.info("Bid:" + bidTransaction);
            }
            if (!((Long) bidTransaction.get("type") == COLORED_COINS) || !((Long) bidTransaction.get("subtype") == COLORED_COINS_BID)) {
                throw new IllegalStateException();
            }
            trade.setRecipientAccount((String) bidTransaction.get("sender"));

            JSONObject askTransaction = nxtAPi.getTransaction(trade.getAskOrderId());
            if (log.isLoggable(Level.FINE)) {
                log.info("Ask:" + askTransaction);
            }
            if (!((Long) askTransaction.get("type") == COLORED_COINS) || !((Long) askTransaction.get("subtype") == COLORED_COINS_ASK)) {
                throw new IllegalStateException();
            }
            trade.setSenderAccount((String) askTransaction.get("sender"));

            if (log.isLoggable(Level.FINE)) {
                log.info("" + trade);
            }
            assets.get(trade.getAssetId()).addTransfer(trade);
        }
        for (Asset asset : assets.values()) {
            if (asset.getNumberOfTrades() != assets.get(asset.getId()).getNumberOfTransfers()) {
                throw new IllegalStateException(asset.toString());
            }
        }
        if (log.isLoggable(Level.INFO)) {
            log.info("Trades loading - Ok");
        }
        List<Transfer> assetTransfers = nxtAPi.getAssetTransfers();
        for (Transfer transfer : assetTransfers) {
            assets.get(transfer.getAssetId()).addTransfer(transfer);
        }
        for (Asset asset : assets.values()) {
            asset.sortTransfers();
            asset.setLastPrice();
            asset.calcAccountQty(asset);

            if (log.isLoggable(Level.FINE)) {
                log.info(String.format("Asset %s quantity %.2f price %f value %d\n",
                        asset.getName(), asset.getQuantity(), asset.getLastPrice(), asset.getAssetValue()));
            }
            asset.analyzeAccountBalances();
        }
        return assets;
    }

    List<AccountBalance> getMyBalance(Map<String, Asset> assets, String accountId) {
        List<AccountBalance> balances = new ArrayList<>();
        for (Asset asset : assets.values()) {
            AccountBalance accountBalance = asset.getAccountBalance(accountId);
            if (accountBalance == null) {
                continue;
            }
            if (accountBalance.getQuantity() != 0) {
                double quantityQNT = accountBalance.getQuantity();
                double currentValue = accountBalance.getValue();
                // double currentValue = quantityQNT * asset.getLastPrice();
                double avcoPrice = accountBalance.getFifoPrice();
                if (AssetObserver.log.isLoggable(Level.FINE)) {
                    AssetObserver.log.info(String.format("Asset %s Account %s quantity %.2f current price %.2f value %.2f AVCO price %.2f change %.2f%% " +
                                    "average cost %d profit %d%n",
                            asset.getName(), accountId, quantityQNT, asset.getLastPrice(), currentValue, avcoPrice,
                            avcoPrice == 0 ? 0 : (asset.getLastPrice() - avcoPrice) / avcoPrice * 100,
                            (long)(avcoPrice * quantityQNT),
                            (long)((asset.getLastPrice() - avcoPrice) * quantityQNT)));
                }
                balances.add(accountBalance);
            }
        }
        return balances;
    }
}
