package com.masterface.nxt.ae;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import java.io.IOException;
import java.io.Writer;
import java.math.BigInteger;
import java.util.Date;
import java.util.Map;

public class Utils {


    public final static JSONStreamAware emptyJSON = prepare(new JSONObject());
    public static final BigInteger two64 = new BigInteger("18446744073709551616");

    public static JSONStreamAware prepare(final JSONObject json) {
        return new JSONStreamAware() {
            private final char[] jsonChars = json.toJSONString().toCharArray();

            @Override
            public void writeJSONString(Writer out) throws IOException {
                out.write(jsonChars);
            }
        };
    }

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
        return (int) ((date.getTime() - Constants.EPOCH_BEGINNING + 500) / 1000);
    }

    @SuppressWarnings("UnusedDeclaration")
    static int getTimeStamp() {
        return getTimeStamp(new Date());
    }

    public static Long parseAccountId(String account) {
        if (account == null) {
            return null;
        }
        account = account.toUpperCase();
        if (account.startsWith("NXT-")) {
            return zeroToNull(Utils.rsDecode(account.substring(4)));
        } else {
            return parseUnsignedLong(account);
        }
    }

    public static Long zeroToNull(long l) {
        return l == 0 ? null : l;
    }

    public static long nullToZero(Long l) {
        return l == null ? 0 : l;
    }

    public static String toUnsignedLong(Long objectId) {
        return toUnsignedLong(nullToZero(objectId));
    }

    public static Long parseUnsignedLong(String number) {
        if (number == null) {
            return null;
        }
        BigInteger bigInt = new BigInteger(number.trim());
        if (bigInt.signum() < 0 || bigInt.compareTo(two64) != -1) {
            throw new IllegalArgumentException("overflow: " + number);
        }
        return zeroToNull(bigInt.longValue());
    }

    public static long rsDecode(String rsString) {
        rsString = rsString.toUpperCase();
        try {
            long id = ReedSolomon.decode(rsString);
            if (!rsString.equals(ReedSolomon.encode(id))) {
                throw new RuntimeException("ERROR: Reed-Solomon decoding of " + rsString
                        + " not reversible, decoded to " + id);
            }
            return id;
        } catch (ReedSolomon.DecodeException e) {
            AssetObserver.log.info("Reed-Solomon decoding failed for " + rsString + ": " + e.toString());
            throw new RuntimeException(e.toString(), e);
        }
    }

    public static String toUnsignedLong(long objectId) {
        if (objectId >= 0) {
            return String.valueOf(objectId);
        }
        BigInteger id = BigInteger.valueOf(objectId).add(two64);
        return id.toString();
    }

    public static int getEpochTime(long time) {
        return (int) ((time - Constants.EPOCH_BEGINNING + 500) / 1000);
    }

    public static Date getDate(int epochTime) {
        return new Date(getTimeMillis(epochTime));
    }

    public static long getTimeMillis(int epochTime) {
        return epochTime * 1000L + Constants.EPOCH_BEGINNING - 500L;
    }
}
