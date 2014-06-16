package com.masterface.nxt.ae;

import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import java.util.*;

public class GetAssetMovements extends APIRequestHandler {

    static final GetAssetMovements instance = new GetAssetMovements();

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
        String type = req.getParameter("type");
        boolean showTrades = (type == null || type.equals("") || type.equalsIgnoreCase("trade"));
        boolean showTransfers = (type == null || type.equals("") || type.equalsIgnoreCase("transfer"));
        List<Transfer> assetTransfers = asset.getTransfers();
        Collections.sort(assetTransfers, Collections.reverseOrder(new AssetQuantityComparator()));
        List<Object> list = new ArrayList<>();
        double tradeQty = 0;
        double transferQty = 0;
        int tradeCount = 0;
        int transferCount = 0;
        for (Transfer transfer : assetTransfers) {
            Map<String, Object> transferData = transfer.getData(asset);
            if (transferData == null) {
                continue;
            }
            if (transfer.isTrade() && showTrades || !transfer.isTrade() && showTransfers) {
                list.add(transferData);
            }
            if (transfer.isTrade() && showTrades) {
                tradeQty += Double.parseDouble((String) transferData.get("qty"));
                tradeCount++;
            }
            if (!transfer.isTrade() && showTransfers) {
                transferQty += Double.parseDouble((String) transferData.get("qty"));
                transferCount++;
            }
        }
        List<Object> response = new ArrayList<>();
        Map<String, String> assetDistribution = new LinkedHashMap<>();
        assetDistribution.put("assetName", asset.getName());
        if (showTrades) {
            assetDistribution.put("tradeQty", String.format("%.2f", tradeQty));
            assetDistribution.put("tradeCount", String.format("%d", tradeCount));
        }
        if (showTransfers) {
            assetDistribution.put("transferQty", String.format("%.2f", transferQty));
            assetDistribution.put("transferCount", String.format("%d", transferCount));
        }
        assetDistribution.put("updateTime", String.format("%s", new Date(assetObserver.getUpdateTime())));
        response.add(assetDistribution);
        response.add(list);
        return JSONValue.toJSONString(response);
    }

    static class AssetQuantityComparator implements Comparator<Transfer> {

        @Override
        public int compare(Transfer t1, Transfer t2) {
            double qty1 = t1.getQuantityQNT();
            double qty2 = t2.getQuantityQNT();
            if (qty1 < qty2) {
                return -1;
            }
            if (qty1 > qty2) {
                return 1;
            }
            return 0;
        }
    }

}
