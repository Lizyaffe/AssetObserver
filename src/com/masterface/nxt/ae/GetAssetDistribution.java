package com.masterface.nxt.ae;

import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class GetAssetDistribution extends APIRequestHandler {

    static final GetAssetDistribution instance = new GetAssetDistribution();

    @Override
    String processRequest(AssetObserver assetObserver, HttpServletRequest req) {
        String assetId = req.getParameter("assetId");
        if (assetId == null) {
            return "Please specify the \"assetId\" parameter";
        }
        boolean ignoreIssuerAccount = Boolean.parseBoolean(req.getParameter("ignoreIssuerAccount"));
        String balanceDate = req.getParameter("balanceDate");
        int timeStamp;
        if (balanceDate == null || balanceDate.equals("")) {
            timeStamp = 0;
        } else {
            DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
            dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
            try {
                Date date = dateFormat.parse(balanceDate);
                timeStamp = Utils.getEpochTime(date.getTime());
            } catch (ParseException e) {
                return String.format("Cannot parse date %s expected format is YYYYMMDD", balanceDate);
            }
        }
        Asset asset = assetObserver.getAsset(assetId);
        List<AccountBalance> accountBalances = asset.getAccountBalancesList();
        Collections.sort(accountBalances, Collections.reverseOrder(new AccountQuantityComparator()));
        Map<String, Object> map = new LinkedHashMap<>();
        double qty = 0;
        int count = 0;
        for (AccountBalance accountBalance : accountBalances) {
            Map<String, Object> distributionData = accountBalance.getDistributionData(timeStamp, ignoreIssuerAccount);
            if (distributionData == null) {
                continue;
            }
            map.put(accountBalance.getAccountId(), distributionData);
            qty += Double.parseDouble((String) distributionData.get("qty"));
            count++;
        }
        List<Object> response = new ArrayList<>();
        Map<String, String> assetDistribution = new LinkedHashMap<>();
        assetDistribution.put("assetName", asset.getName());
        assetDistribution.put("qty", String.format("%.2f", qty));
        assetDistribution.put("count", String.format("%d", count));
        assetDistribution.put("timeStamp", String.format("%d", timeStamp));
        assetDistribution.put("balanceDate", String.format("%s",
                (balanceDate == null || balanceDate.equals("")) ? "now" : balanceDate));
        assetDistribution.put("updateTime", String.format("%s", new Date(assetObserver.getCacheModificationTime())));
        response.add(assetDistribution);
        response.add(map);
        return JSONValue.toJSONString(response);
    }

    static class AccountQuantityComparator implements Comparator<AccountBalance> {

        @Override
        public int compare(AccountBalance o1, AccountBalance o2) {
            double qty1 = o1.getQuantity();
            double qty2 = o2.getQuantity();
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
