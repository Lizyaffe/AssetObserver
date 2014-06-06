package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AssetObserver {

    public static final String ADDRESS = "localhost:7876";
    public static final int NQT_IN_NXT = 100000000;
    public static final int ASSET_EXCHANGE_BLOCK = 135000;
    public static final int COLORED_COINS = 2;
    public static final int COLORED_COINS_ASK = 2;
    public static final int COLORED_COINS_BID = 3;
    public static Logger log;

    public static void main(String[] args) {
        AssetObserver assetObserver = new AssetObserver();
        JsonProvider jsonProvider = JsonProviderFactory.getJsonProvider(false);
        try {
            Files.createFile(Paths.get(NxtClient.JSON_RESPONSE_JOURNAL_LOG));
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        Map<String, Asset> assets = assetObserver.load(jsonProvider);
        assetObserver.getMyBalance(assets, "13196039393619977660");
    }

    private static void initLogger() {
        log = Logger.getLogger("com.masterface.nxt.ae");
        log.setLevel(Level.ALL);
        ConsoleHandler handler = new ConsoleHandler();
        handler.setLevel(Level.ALL);
        log.addHandler(handler);
        handler.setFormatter(new BriefLogFormatter());
        log.fine("AssetObserver started");
    }

    public Map<String, Asset> load(JsonProvider jsonProvider) {
        initLogger();
        NxtApi nxtAPi = new NxtApi(jsonProvider);
        Map<String, Asset> assets = nxtAPi.getAllAssets();
        List<Trade> trades = nxtAPi.getAllTrades();
        for (Trade trade : trades) {
            JSONObject bidTransaction = nxtAPi.getTransaction(trade.getBidOrderId());
            if (log.isLoggable(Level.INFO)) {
                log.info("Bid:" + bidTransaction);
            }
            if (!((Long) bidTransaction.get("type") == COLORED_COINS) || !((Long) bidTransaction.get("subtype") == COLORED_COINS_BID)) {
                throw new IllegalStateException();
            }
            trade.setRecipientAccount((String) bidTransaction.get("sender"));

            JSONObject askTransaction = nxtAPi.getTransaction(trade.getAskOrderId());
            if (log.isLoggable(Level.INFO)) {
                log.info("Ask:" + askTransaction);
            }
            if (!((Long) askTransaction.get("type") == COLORED_COINS) || !((Long) askTransaction.get("subtype") == COLORED_COINS_ASK)) {
                throw new IllegalStateException();
            }
            trade.setSenderAccount((String) askTransaction.get("sender"));

            if (log.isLoggable(Level.INFO)) {
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
            asset.calcAccountQty();

            if (log.isLoggable(Level.INFO)) {
                log.info(String.format("Asset %s quantity %d price %f value %d\n",
                        asset.getName(), asset.getQuantityQNT(), asset.getLastPrice(), asset.getAssetValue()));
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
            if (accountBalance.getQuantityQNT() != 0) {
                long currentValue = (long) (accountBalance.getQuantityQNT() * asset.getLastPrice());
                long investment = accountBalance.getNxtBalance();
                if (AssetObserver.log.isLoggable(Level.INFO)) {
                    AssetObserver.log.info(String.format("LYLY Asset %s quantity %d price %f nxt value %d nxt balance %d percent %s\n",
                            asset.getName(), accountBalance.getQuantityQNT(), asset.getLastPrice(), currentValue, investment,
                            investment == 0 ? "N/A" : (double) ((double) currentValue / (double) -investment * 100)));
                }
                balances.add(accountBalance);
            }
        }
        return balances;
    }
}
