package com.masterface.nxt.ae;

import nxt.Constants;

import java.util.Date;
import java.util.Map;

public class Utils {
    static String getUrlParams(Map<String, String> params) {
        if (params == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        for (String key : params.keySet()) {
            sb.append(key).append("=").append(params.get(key)).append("&");
        }
        String rc = sb.toString();
        if (rc.endsWith("&")) {
            rc = rc.substring(0, rc.length() - 1);
        }
        return rc;
    }

    static int getTimeStamp(Date date) {
        return (int)((date.getTime() - Constants.EPOCH_BEGINNING + 500) / 1000);
    }

    @SuppressWarnings("UnusedDeclaration")
    static int getTimeStamp() {
        return getTimeStamp(new Date());
    }
}
