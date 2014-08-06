package com.masterface.nxt.ae;

import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.stream.Collectors;

public class GetAllAssets extends APIRequestHandler {

    static final GetAllAssets instance = new GetAllAssets();

    @Override
    String processRequest(AssetObserver assetObserver, HttpServletRequest req) {
        String sortOrder = req.getParameter("sortOrder");
        if (sortOrder == null || sortOrder.equals("")) {
            sortOrder = "-nxtVolume24h";
        }
        List<Asset> assetsList = assetObserver.getAllAssets();
        int timeStamp = assetObserver.getTimeStamp();
        List<Map<String, Object>> list = assetsList.stream().
                map(asset -> asset.getData(assetObserver.getExchangeRates(), timeStamp)).
                sorted(new ConfigurableComparator(sortOrder)).
                collect(Collectors.toList());
        long count = list.stream().count();
        long nofTrades = list.stream().mapToLong(data -> Long.parseLong((String) data.get("nofTrades"))).sum();
        double nxtVolume24h = list.stream().mapToDouble(data -> Double.parseDouble((String) data.get("nxtVolume24h"))).sum();

        List<Object> response = new ArrayList<>();
        Map<String, String> allAssets = new LinkedHashMap<>();
        allAssets.put("assetCount", String.format("%d", count));
        allAssets.put("nofTrades", String.format("%d", nofTrades));
        allAssets.put("nxtVolume24h", String.format("%.2f", nxtVolume24h));
        allAssets.put("usdVolume24h", String.format("%.2f", nxtVolume24h * assetObserver.getExchangeRates().get(AssetObserver.NXT_USD)));
        allAssets.put("btcVolume24h", String.format("%.2f", nxtVolume24h * assetObserver.getExchangeRates().get(AssetObserver.NXT_BTC)));
        allAssets.put("updateTime", String.format("%s", new Date(assetObserver.getUpdateTime())));
        response.add(allAssets);
        response.add(list);
        return JSONValue.toJSONString(response);
    }

}
