package com.masterface.nxt.ae;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class NxtApi {

    private final JsonProvider jsonProvider;

    public NxtApi(JsonProvider jsonProvider) {
        this.jsonProvider = jsonProvider;
    }

    public JSONObject getTransaction(String transactionId) {
        Map<String, String> params = new HashMap<>();
        params.put("requestType", "getTransaction");
        params.put("transaction", transactionId);
        return jsonProvider.getJsonResponse(params);
    }

    public JSONArray getBlockTransactions(String blockId) {
        JSONObject response = getBlock(blockId);
        return (JSONArray) response.get("transactions");
    }

    public JSONObject getBlock(String blockId) {
        Map<String, String> params = new HashMap<>();
        params.put("requestType", "getBlock");
        params.put("block", blockId);
        return jsonProvider.getJsonResponse(params);
    }

    public Map<String, Asset> getAllAssets() {
        Map<String, String> params = new HashMap<>();
        params.put("requestType", "getAllAssets");
        JSONObject response = jsonProvider.getJsonResponse(params);
        JSONArray assets = (JSONArray) response.get("assets");
        Map<String, Asset> assetMap = new HashMap<>();
        for (Object assetJson : assets) {
            Asset asset = new Asset((JSONObject) assetJson);
            assetMap.put(asset.getId(), asset);
        }
        for (Asset asset : assetMap.values()) {
            if (AssetObserver.log.isLoggable(Level.FINE)) {
                AssetObserver.log.info(asset.toString());
            }
        }
        return assetMap;
    }

    public List<Trade> getAllTrades() {
        Map<String, String> params = new HashMap<>();
        params.put("requestType", "getAllTrades");
        JSONObject response = jsonProvider.getJsonResponse(params);
        JSONArray trades = (JSONArray) response.get("trades");
        List<Trade> tradeList = new ArrayList<>();
        for (Object trade : trades) {
            tradeList.add(new Trade((JSONObject) trade));
        }
        return tradeList;
    }

    public BlockchainData getAssetTransfers(String blockId) {
        List<Transfer> assetTransfers = new ArrayList<>();
        List<AssetCreation> assetCreations = new ArrayList<>();
        while (true) {
            JSONObject block = getBlock(blockId);
            if (block == null) {
                AssetObserver.log.info("Can't getBlock for blockId " + blockId);
                break;
            }
            Object heightObj = block.get("height");
            if (heightObj != null) {
                long height = (Long) heightObj;
                if (height < AssetObserver.ASSET_EXCHANGE_BLOCK) {
                    break;
                }
            }
            JSONArray blockTransactions = getBlockTransactions(blockId);
            if (blockTransactions == null) {
                AssetObserver.log.info("Can't get block transactions for blockId " + blockId);
                break;
            }
            for (Object transactionObj : blockTransactions) {
                String transactionId = (String) transactionObj;
                JSONObject transaction = getTransaction(transactionId);
                if (transaction == null) {
                    AssetObserver.log.info("Can't get transaction for for transactionId " + transactionId);
                    continue;
                }
                Object type = transaction.get("type");
                if (type == null) {
                    AssetObserver.log.info("Can't get transaction type for transactionId " + transactionId);
                    continue;
                }
                if ((Long) type != 2) {
                    continue;
                }
                Object subtype = transaction.get("subtype");
                if (subtype == null) {
                    AssetObserver.log.info("Can't get transaction subtype for transactionId " + transactionId);
                    continue;
                }
                if ((Long) subtype == 0) {
                    String assetId = (String) transaction.get("transaction");
                    //noinspection RedundantCast
                    Integer timeStamp = Integer.parseInt(((Long) transaction.get("timestamp")).toString());
                    long feeNQT = Long.parseLong((String) transaction.get("feeNQT"));
                    AssetCreation assetCreation = new AssetCreation(assetId, timeStamp, feeNQT);
                    assetCreations.add(assetCreation);
                }
                if ((Long) subtype == 1) {
                    JSONObject attachment = (JSONObject) transaction.get("attachment");
                    Transfer transfer = new Transfer((String) attachment.get("asset"), (Long) transaction.get("timestamp"),
                            Long.parseLong((String) attachment.get("quantityQNT")), blockId,
                            (String) transaction.get("sender"), (String) transaction.get("recipient"), (String) attachment.get("comment"));
                    if (AssetObserver.log.isLoggable(Level.FINE)) {
                        AssetObserver.log.info("Transfer:" + transfer);
                    }
                    assetTransfers.add(transfer);
                }
            }
            blockId = (String) block.get("previousBlock");
        }
        return new BlockchainData(assetTransfers, assetCreations);
    }

    public JSONObject getLastBlock() {
        Map<String, String> params = new HashMap<>();
        params.put("requestType", "getBlockchainStatus");
        return jsonProvider.getJsonResponse(params);
    }

    public ArrayList<String> getLines() {
        return jsonProvider.getLines();
    }

    public void resetLines() {
        jsonProvider.resetLines();
    }
}
