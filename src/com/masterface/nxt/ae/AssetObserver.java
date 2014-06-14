package com.masterface.nxt.ae;

import org.json.simple.JSONObject;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    public static final String CACHE_LOG = "cache.log";

    public static Logger log;

    public static String NXT_BTC = "NXT_BTC";

    public static String BTC_USD = "BTC_USD";
    public static String NXT_USD = "NXT_USD";
    public static String NXT_CNY = "NXT_CNY";

    private Map<String, Asset> assets;
    private Map<String, Double> exchangeRates = new HashMap<>();
    private long updateTime;

    static {
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tY-%1$tm-%1$td %1$tH:%1$tM:%1$tS %4$s %2$s %5$s%6$s%n");
        log = Logger.getGlobal();
        log.setLevel(Level.INFO);
        log.fine("AssetObserver started");
        if (log.isLoggable(Level.INFO)) {
            log.info("Loading started");
        }
        Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler());
    }

    public static void main(String[] args) {
        AssetObserver assetObserver = new AssetObserver();
        assetObserver.updateExchangeRates();
        assetObserver.loadCache();
        JsonProvider jsonProvider = JsonProviderFactory.getJsonProvider(null);
        ServerWrapper serverWrapper = new ServerWrapper();
        Runnable r = () -> {
            try {
                AssetObserver online = new AssetObserver();
                online.updateExchangeRates();
                online.updateTime = System.currentTimeMillis();
                online.load(jsonProvider);
                serverWrapper.setAssetObserver(online);
            } catch (Throwable t) {
                t.printStackTrace();
            }
        };
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
        executorService.scheduleWithFixedDelay(r, 0, 1, TimeUnit.MINUTES);
        serverWrapper.start(assetObserver);
    }

    private void updateExchangeRates() {
        BterClient bterClient = new BterClient();
        BterApi bterApi = new BterApi(bterClient);
        exchangeRates.put(NXT_BTC, bterApi.getLastPrice("NXT", "BTC"));
        exchangeRates.put(NXT_CNY, bterApi.getLastPrice("NXT", "CNY"));
        BitstampClient bitstampClient = new BitstampClient();
        BitstampApi bitstampApi = new BitstampApi(bitstampClient);
        exchangeRates.put(BTC_USD, bitstampApi.getBtcUsdLastPrice());
        exchangeRates.put(NXT_USD, exchangeRates.get(NXT_BTC) * exchangeRates.get(BTC_USD));
    }

    private void loadCache() {
        Path testResource = Paths.get(CACHE_LOG);
        if (!Files.exists(testResource)) {
            if (log.isLoggable(Level.INFO)) {
                log.info("Cache file does not exist, wait for reading data from the blockchain");
            }
            assets = new HashMap<>();
            return;
        }
        if (log.isLoggable(Level.INFO)) {
            log.info("Loading data from cache");
        }
        try {
            updateTime = Files.getLastModifiedTime(testResource, LinkOption.NOFOLLOW_LINKS).toMillis();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
        JsonProvider jsonProvider = JsonProviderFactory.getJsonProvider(testResource);
        assets = load(jsonProvider);
        if (log.isLoggable(Level.INFO)) {
            log.info("Loading from cache done");
        }
    }

    public Map<String, Asset> load(JsonProvider jsonProvider) {
        NxtApi nxtAPi = new NxtApi(jsonProvider);
        assets = nxtAPi.getAllAssets();
        loadTrades(nxtAPi);
        loadTransfers(nxtAPi);
        for (Asset asset : assets.values()) {
            asset.sortTransfers();
            asset.setLastPrice();
            asset.setAccountBalanceDistribution();

            if (log.isLoggable(Level.FINE)) {
                log.info(String.format("Asset %s quantity %.2f price %f value %d\n",
                        asset.getName(), asset.getQuantity(), asset.getLastPrice(), asset.getAssetValue()));
            }
            asset.verifyAccountBalances();
        }
        if (log.isLoggable(Level.INFO)) {
            log.info("Account analysis done");
        }
        try {
            ArrayList<String> lines = nxtAPi.getLines();
            if (lines != null) {
                Path path = Paths.get(CACHE_LOG);
                if (!Files.exists(path)) {
                    Files.createFile(path);
                }
                Files.write(path, lines, Charset.forName("UTF-8"), StandardOpenOption.WRITE);
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        } finally {
            nxtAPi.resetLines();
        }
        return assets;
    }

    private void loadTransfers(NxtApi nxtAPi) {
        BlockchainData blockchainData = nxtAPi.getAssetTransfers();
        List<Transfer> assetTransfers = blockchainData.getAssetTransfers();
        for (Transfer transfer : assetTransfers) {
            assets.get(transfer.getAssetId()).addTransfer(transfer);
        }
        List<Tuple3> assetCreation = blockchainData.getAssetCreation();
        for (Tuple3 assetExtraData : assetCreation) {
            @SuppressWarnings("RedundantCast") Asset asset = assets.get((String) assetExtraData.x);
            asset.setTimeStamp((Integer) assetExtraData.y);
            asset.setCreationFee((Long) assetExtraData.z);
        }
        if (log.isLoggable(Level.INFO)) {
            log.info("Transfer loading done");
        }
    }

    private void loadTrades(NxtApi nxtAPi) {
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
            log.info("Trades loading done");
        }
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

    public List<Asset> getAllAssets() {
        List<Asset> assetList = new ArrayList<>();
        for (Asset asset : assets.values()) {
            assetList.add(asset);

        }
        return assetList;
    }

    public Asset getAsset(String assetId) {
        return assets.get(assetId);
    }

    public long getUpdateTime() {
        return updateTime;
    }

    public Map<String, Double> getExchangeRates() {
        return exchangeRates;
    }
}
