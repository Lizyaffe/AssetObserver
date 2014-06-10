package com.masterface.nxt.ae;

import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class GetAllAssets extends APIRequestHandler {

    static final GetAllAssets instance = new GetAllAssets();

    @Override
    String processRequest(AssetObserver assetObserver, HttpServletRequest req) {
        List<Asset> assetsList = assetObserver.getAllAssets();
        Collections.sort(assetsList, Collections.reverseOrder(new AssetNumOfTradesComparator()));
        Map<String, Object> map = new LinkedHashMap<>();
        for (Asset asset : assetsList) {
            map.put(asset.getName(), asset.getData());
        }
        return JSONValue.toJSONString(map);
    }

    static class AssetNumOfTradesComparator implements Comparator<Asset> {

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
