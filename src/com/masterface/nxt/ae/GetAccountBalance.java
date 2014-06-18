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
            return generateErrorResponse("Missing parameter %s", "accountId");
        }
        String accountId;
        try {
            accountId = Utils.toUnsignedLong(Utils.parseAccountId(accountIdParam));
        } catch (RuntimeException e) {
            return generateErrorResponse("Invalid account id %s", accountIdParam);
        }
        List<Asset> assetList = assetObserver.getAllAssets();
        List<Map<String, Object>> accountAssetList = new ArrayList<>();
        double value = 0;
        for (Asset asset : assetList) {
            AccountBalance accountBalance = asset.getAccountBalance(accountId);
            if (accountBalance == null) {
                // Account does not hold this asset
                continue;
            }
            Map<String, Object> data = accountBalance.getData(assetObserver.getExchangeRates());
            accountAssetList.add(data);
            value += Double.parseDouble((String) data.get("nxtValue"));
        }
        Collections.sort(accountAssetList, Collections.reverseOrder(new AssetDataComparator()));
        List<Object> response = new ArrayList<>();
        Map<String, String> accountDetails = new LinkedHashMap<>();
        accountDetails.put("accountId", accountIdParam);
        accountDetails.put("nxtValue", String.format("%.2f", value));
        accountDetails.put("usdValue", String.format("%.2f", value * assetObserver.getExchangeRates().get(AssetObserver.NXT_USD)));
        accountDetails.put("btcValue", String.format("%.2f", value * assetObserver.getExchangeRates().get(AssetObserver.NXT_BTC)));
        accountDetails.put("updateTime", String.format("%s", new Date(assetObserver.getUpdateTime())));
        response.add(accountDetails);
        response.add(accountAssetList);
        return JSONValue.toJSONString(response);
    }

    private class AssetDataComparator implements Comparator<Map<String, Object>> {

        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            double nxtValue1 = Double.parseDouble((String) o1.get("nxtValue"));
            double nxtValue2 = Double.parseDouble((String) o2.get("nxtValue"));
            if (nxtValue1 < nxtValue2) {
                return -1;
            }
            if (nxtValue1 > nxtValue2) {
                return 1;
            }
            return 0;
        }
    }
}
