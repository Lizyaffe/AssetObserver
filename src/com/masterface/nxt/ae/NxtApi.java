package com.masterface.nxt.ae;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NxtApi {


    static JSONObject getTransaction(String transactionId) {
        Map<String, String> params = new HashMap<>();
        params.put("requestType", "getTransaction");
        params.put("transaction", transactionId);
        return NxtClient.getJsonResponse(params);
    }

    private static JSONArray getBlockTransactions(String blockId) {
        JSONObject response = getBlock(blockId);
        return (JSONArray) response.get("transactions");
    }

    private static JSONObject getBlock(String blockId) {
        Map<String, String> params = new HashMap<>();
        params.put("requestType", "getBlock");
        params.put("block", blockId);
        return NxtClient.getJsonResponse(params);
    }

    static Map<String, Asset> getAllAssets() {
        Map<String, String> params = new HashMap<>();
        params.put("requestType", "getAllAssets");
        JSONObject response = NxtClient.getJsonResponse(params);
        JSONArray assets = (JSONArray)response.get("assets");
        Map<String, Asset> assetMap = new HashMap<>();
        for (Object assetJson : assets) {
            Asset asset = new Asset((JSONObject)assetJson);
            assetMap.put(asset.getId() ,asset);
        }
        for (Asset asset : assetMap.values()) {
            System.out.println(asset.toString());
        }
        return assetMap;
    }

    public static List<Trade> getAllTrades() {
        Map<String, String> params = new HashMap<>();
        params.put("requestType", "getAllTrades");
        JSONObject response = NxtClient.getJsonResponse(params);
        JSONArray trades = (JSONArray)response.get("trades");
        List<Trade> tradeList = new ArrayList<>();
        for (Object trade : trades) {
            tradeList.add(new Trade((JSONObject) trade));
        }
        return tradeList;
    }

    public static List<Transfer> getAssetTransfers() {
        String blockId = getLastBlockId();
        List<Transfer> assetTransfers = new ArrayList<>();
        while(true) {
            JSONObject block = getBlock(blockId);
            long height = (Long) block.get("height");
            if (height < AssetObserver.ASSET_EXCHANGE_BLOCK) {
                break;
            }
            JSONArray blockTransactions = getBlockTransactions(blockId);
            for (Object transactionObj : blockTransactions) {
                String transactionId = (String)transactionObj;
                JSONObject transaction = getTransaction(transactionId);
                if ((Long)transaction.get("type") != 2) {
                    continue;
                }
                if ((Long)transaction.get("subtype") != 1) {
                    continue;
                }
                JSONObject attachment = (JSONObject)transaction.get("attachment");
                Transfer transfer = new Transfer((String)attachment.get("asset"), (Long)transaction.get("timestamp"),
                        Long.parseLong((String)attachment.get("quantityQNT")), blockId,
                        (String)transaction.get("sender"), (String)transaction.get("recipient"));
                System.out.println("Transfer:" + transfer);
                assetTransfers.add(transfer);
            }
            blockId = (String)block.get("previousBlock");
        }
        return assetTransfers;
    }

    public static String getLastBlockId() {
        Map<String, String> params = new HashMap<>();
        params.put("requestType", "getBlockchainStatus");
        JSONObject response = NxtClient.getJsonResponse(params);
        return (String)response.get("lastBlock");
    }
}
