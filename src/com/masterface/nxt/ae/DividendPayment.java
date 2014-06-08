package com.masterface.nxt.ae;

public class DividendPayment {
    String accountId;
    double dividendAmount;

    public DividendPayment(String accountId, double dividendAmount) {
        this.accountId = accountId;
        this.dividendAmount = dividendAmount;
    }

    @SuppressWarnings("UnusedDeclaration")
    public String getAccountId() {
        return accountId;
    }

    @SuppressWarnings("UnusedDeclaration")
    public double getDividendAmount() {
        return dividendAmount;
    }
}
