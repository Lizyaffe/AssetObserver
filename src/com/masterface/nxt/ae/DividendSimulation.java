package com.masterface.nxt.ae;

import org.json.simple.JSONValue;

import javax.servlet.http.HttpServletRequest;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class DividendSimulation extends APIRequestHandler {

    static final DividendSimulation instance = new DividendSimulation();

    @Override
    String processRequest(AssetObserver assetObserver, HttpServletRequest req) {
        String assetId = req.getParameter("assetId");
        if (assetId == null || assetId.equals("")) {
            return generateErrorResponse("Missing parameter %s", "assetId");
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
                return generateErrorResponse("Cannot parse date %s expected format is YYYYMMDD", balanceDate);
            }
        }
        String dividendAmountStr = req.getParameter("dividendAmount");
        long totalDividendAmount;
        if (dividendAmountStr == null || dividendAmountStr.equals("")) {
            totalDividendAmount = 0;
        } else {
            totalDividendAmount = Long.parseLong(dividendAmountStr);
        }
        String thresholdStr = req.getParameter("threshold");
        long threshold;
        if (thresholdStr == null || thresholdStr.equals("")) {
            threshold = 0;
        } else {
            threshold = Long.parseLong(thresholdStr);
        }
        Asset asset = assetObserver.getAsset(assetId);
        if (asset == null) {
            return generateErrorResponse("Asset id %s not found", assetId);
        }
        return calcDividendPayments(asset.getAccountBalancesList(), ignoreIssuerAccount, balanceDate, timeStamp,
                totalDividendAmount, threshold, assetObserver.getUpdateTime(), asset.getName(), asset.getIssuerAccount());
    }

    String calcDividendPayments(List<AccountBalance> accountBalances, boolean ignoreIssuerAccount, String balanceDate,
                                int timeStamp, long totalDividendAmount,
                                long threshold, long updateTime,
                                String assetName, AccountBalance issuerAccount) {
        if (ignoreIssuerAccount) {
            accountBalances.remove(issuerAccount);
        }
        double totalQty = 0;
        int activeAccountCounter = 0;
        for (AccountBalance accountBalance : accountBalances) {
            double qty = accountBalance.getQuantity(timeStamp);
            if (qty > 0) {
                activeAccountCounter++;
            }
            totalQty += qty;
        }
        Map<AccountBalance, Double> accountDividends = new LinkedHashMap<>();
        double carey = 0;
        double distributedQty = 0;
        for (AccountBalance accountBalance : accountBalances) {
            double qty = accountBalance.getQuantity(timeStamp);
            double dividendAmount = qty / totalQty * totalDividendAmount;
            if (dividendAmount < threshold) {
                carey += dividendAmount;
            } else {
                accountDividends.put(accountBalance, dividendAmount);
                distributedQty += qty;
            }
        }
        for (Map.Entry<AccountBalance, Double> entry : accountDividends.entrySet()) {
            AccountBalance accountBalance = entry.getKey();
            double dividendAmount = accountDividends.get(accountBalance);
            dividendAmount += accountBalance.getQuantity(timeStamp) / distributedQty * carey;
            accountDividends.put(accountBalance, dividendAmount);
        }
        List<Map<String, Object>> data = new ArrayList<>();
        double calcDividendAmount = 0;
        int count = 0;
        for (Map.Entry<AccountBalance, Double> entry : accountDividends.entrySet()) {
            AccountBalance accountBalance = entry.getKey();
            Map<String, Object> distributionData = accountBalance.getDistributionData(timeStamp);
            if (distributionData == null) {
                continue;
            }
            Double dividendAmount = accountDividends.get(accountBalance);
            calcDividendAmount += dividendAmount;
            distributionData.put("dividendAmount", String.format("%.2f", dividendAmount));
            data.add(distributionData);
            count++;
        }
        List<Object> response = new ArrayList<>();
        Map<String, String> assetDistribution = new LinkedHashMap<>();
        assetDistribution.put("assetName", assetName);
        assetDistribution.put("qty", String.format("%.2f", totalQty));
        assetDistribution.put("dividendAmount", String.format("%.2f", calcDividendAmount));
        assetDistribution.put("feesPercent", String.format("%.2f", 100 - 100 * (calcDividendAmount - count) / calcDividendAmount));
        assetDistribution.put("accountsReceivingDividend", String.format("%d", count));
        assetDistribution.put("accountsReceivingDividendPercent", String.format("%.2f", 100 * (count / (double) activeAccountCounter)));
        assetDistribution.put("timeStamp", String.format("%d", timeStamp));
        assetDistribution.put("balanceDate", String.format("%s",
                (balanceDate == null || balanceDate.equals("")) ? "now" : balanceDate));
        assetDistribution.put("updateTime", String.format("%s", new Date(updateTime)));
        response.add(assetDistribution);
        response.add(data);
        return JSONValue.toJSONString(response);
    }
}
