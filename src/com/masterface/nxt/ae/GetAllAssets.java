package com.masterface.nxt.ae;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.Comparator;

public class GetAllAssets extends APIRequestHandler {

    static final GetAllAssets instance = new GetAllAssets();

    @Override
    JSONStreamAware processRequest(AssetObserver assetObserver, HttpServletRequest req) {

        JSONObject response = new JSONObject();
        JSONArray assetsJSONArray = new JSONArray();
        response.put("assets", assetsJSONArray);
        for (Asset asset : assetObserver.getAllAssets()) {
            JSONObject jsonObject = asset.toJsonObject();
            assetsJSONArray.add(jsonObject);
        }
        Collections.sort(assetsJSONArray, Collections.reverseOrder(new AssetNumOfTradesComparator()));
        return response;
    }

    static class AssetNumOfTradesComparator implements Comparator<JSONObject> {

        @Override
        public int compare(JSONObject o1, JSONObject o2) {
            if ((Long)(o1.get("numberOfTrades")) < (Long)(o2.get("numberOfTrades"))) {
                return -1;
            }
            if ((Long)(o1.get("numberOfTrades")) > (Long)(o2.get("numberOfTrades"))) {
                return 1;
            }
            return 0;
        }
    }

}
