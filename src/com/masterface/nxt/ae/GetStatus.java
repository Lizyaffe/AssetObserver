package com.masterface.nxt.ae;

import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class GetStatus extends APIRequestHandler {

    static final GetStatus instance = new GetStatus();

    @Override
    String processRequest(AssetObserver assetObserver, HttpServletRequest req) {
        Map<String, String> status = new LinkedHashMap<>();
        status.put("timeSinceUpdateSec", String.format("%d", (System.currentTimeMillis() - assetObserver.getUpdateTime()) / 1000));
        status.put("nxtBtcRateBter", String.format("%.8f", assetObserver.getExchangeRates().get(AssetObserver.NXT_BTC)));
        status.put("btcUsdRateBitstamp", String.format("%.4f", assetObserver.getExchangeRates().get(AssetObserver.BTC_USD)));
        status.put("nxtUsdRateCalc", String.format("%.4f", assetObserver.getExchangeRates().get(AssetObserver.NXT_USD)));
        status.put("numberOfAssets", String.format("%d", assetObserver.getAllAssets().size()));
        List<Object> response = new ArrayList<>();
        response.add(status);
        return JSONValue.toJSONString(response);
    }
}