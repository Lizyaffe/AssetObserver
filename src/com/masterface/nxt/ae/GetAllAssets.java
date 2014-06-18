package com.masterface.nxt.ae;

import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class GetAllAssets extends APIRequestHandler {

    static final GetAllAssets instance = new GetAllAssets();

    @Override
    String processRequest(AssetObserver assetObserver, HttpServletRequest req) {
        List<Asset> assetsList = assetObserver.getAllAssets();
        List<Map<String, Object>> list = new ArrayList<>();
        int count = 0;
        long nofTrades = 0;
        double nxtVolume = 0;
        for (Asset asset : assetsList) {
            Map<String, Object> data = asset.getData(assetObserver.getExchangeRates());
            list.add(data);
            count++;
            nofTrades += Long.parseLong((String) data.get("nofTrades"));
            nxtVolume += Double.parseDouble((String) data.get("nxtVolume"));
        }
        Collections.sort(list, Collections.reverseOrder(new AssetTradeVolumeComparator()));
        List<Object> response = new ArrayList<>();
        Map<String, String> allAssets = new LinkedHashMap<>();
        allAssets.put("assetCount", String.format("%d", count));
        allAssets.put("nofTrades", String.format("%d", nofTrades));
        allAssets.put("nxtVolume", String.format("%.2f", nxtVolume));
        allAssets.put("usdVolume", String.format("%.2f", nxtVolume * assetObserver.getExchangeRates().get(AssetObserver.NXT_USD)));
        allAssets.put("btcVolume", String.format("%.2f", nxtVolume * assetObserver.getExchangeRates().get(AssetObserver.NXT_BTC)));
        allAssets.put("updateTime", String.format("%s", new Date(assetObserver.getUpdateTime())));
        response.add(allAssets);
        response.add(list);
        return JSONValue.toJSONString(response);
    }

    static class AssetTradeVolumeComparator implements Comparator<Map<String, Object>> {

        @Override
        public int compare(Map<String, Object> o1, Map<String, Object> o2) {
            double tradeVolume1 = Double.parseDouble((String) o1.get("nxtVolume"));
            double tradeVolume2 = Double.parseDouble((String) o2.get("nxtVolume"));
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
