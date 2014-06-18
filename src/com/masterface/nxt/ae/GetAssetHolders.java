package com.masterface.nxt.ae;

import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class GetAssetHolders extends APIRequestHandler {

    static final GetAssetHolders instance = new GetAssetHolders();

    @Override
    String processRequest(AssetObserver assetObserver, HttpServletRequest req) {
        String assetId = req.getParameter("assetId");
        if (assetId == null || assetId.equals("")) {
            return generateErrorResponse("Missing parameter %s", "assetId");
        }
        Asset asset = assetObserver.getAsset(assetId);
        if (asset == null) {
            return generateErrorResponse("Asset id %s not found", assetId);
        }
        List<AccountBalance> accountBalances = asset.getAccountBalancesList();
        List<Map<String, Object>> list = new ArrayList<>();
        int count = 0;
        for (AccountBalance accountBalance : accountBalances) {
            Map<String, Object> data = accountBalance.getData(assetObserver.getExchangeRates());
            list.add(data);
            count++;
        }

        List<Object> response = new ArrayList<>();
        Map<String, String> assetDistribution = new LinkedHashMap<>();
        assetDistribution.put("assetName", asset.getName());
        assetDistribution.put("count", String.format("%d", count));
        assetDistribution.put("updateTime", String.format("%s", new Date(assetObserver.getUpdateTime())));
        response.add(assetDistribution);
        response.add(list);
        return JSONValue.toJSONString(response);
    }

}
