package com.masterface.nxt.ae;

import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class GetAccountBalance extends APIRequestHandler {

    static final GetAccountBalance instance = new GetAccountBalance();

    @Override
    String processRequest(AssetObserver assetObserver, HttpServletRequest req) {
        String accountIdParam = req.getParameter("accountId");
        if (accountIdParam == null || accountIdParam.equals("")) {
            return "Please specify the \"accountId\" parameter";
        }
        String accountId = Utils.toUnsignedLong(Utils.parseAccountId(accountIdParam));
        List<Asset> assetsList = assetObserver.getAllAssets();
        Map<String, Map<String, Object>> map = new LinkedHashMap<>();
        double value = 0;
        long fees = 0;
        for (Asset asset : assetsList) {
            AccountBalance accountBalance = asset.getAccountBalance(accountId);
            if (accountBalance == null) {
                // Account does not hold this asset
                continue;
            }
            Map<String, Object> data = accountBalance.getData();
            map.put(asset.getName(), data);
            value += Double.parseDouble((String) data.get("nxtValue"));
            fees += Long.parseLong((String) data.get("fees"));
        }
        List<Object> response = new ArrayList<>();
        Map<String, String> accountDetails = new LinkedHashMap<>();
        accountDetails.put("accountId", accountIdParam);
        accountDetails.put("nxtValue", String.format("%.2f", value));
        accountDetails.put("usdValue", String.format("%.2f", value * AssetObserver.nxtUsdPrice));
        accountDetails.put("btcValue", String.format("%.2f", value * AssetObserver.nxtBtcPrice));
        accountDetails.put("nxtFees", String.format("%d", fees));
        accountDetails.put("updateTime", String.format("%s", new Date(assetObserver.getUpdateTime())));
        response.add(accountDetails);
        response.add(map);
        return JSONValue.toJSONString(response);
    }

    @SuppressWarnings("UnusedDeclaration")
    static class AccountBalanceComparator implements Comparator<Asset> {

        @Override
        public int compare(Asset o1, Asset o2) {
            double tradeVolume1 = o1.getTradeVolume();
            double tradeVolume2 = o2.getTradeVolume();
            if (tradeVolume1 < tradeVolume2) {
                return -1;
            }
            if (tradeVolume1 > tradeVolume2) {
                return 1;
            }
            return 0;
        }
    }

}
