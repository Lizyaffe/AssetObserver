package com.masterface.nxt.ae;

import org.json.simple.JSONObject;
import org.json.simple.JSONStreamAware;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.Writer;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public class APIServlet extends HttpServlet {

    public static final JSONStreamAware ERROR_INCORRECT_REQUEST;

    static {
        JSONObject response = new JSONObject();
        response.put("errorCode", 1);
        response.put("errorDescription", "Incorrect request");
        ERROR_INCORRECT_REQUEST = Utils.prepare(response);
    }

    static final Map<String, APIRequestHandler> apiRequestHandlers;

    static {

        Map<String, APIRequestHandler> map = new HashMap<>();
        map.put("getAllAssets", GetAllAssets.instance);
        map.put("getAccountBalance", GetAccountBalance.instance);
        map.put("getAssetDistribution", GetAssetDistribution.instance);
        apiRequestHandlers = Collections.unmodifiableMap(map);
    }

    private volatile AssetObserver assetObserver;

    public void setAssetObserver(AssetObserver assetObserver) {
        this.assetObserver = assetObserver;
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        process(req, resp);
    }

    private void process(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setHeader("Cache-Control", "no-cache, no-store, must-revalidate, private");
        resp.setHeader("Pragma", "no-cache");
        resp.setDateHeader("Expires", 0);

        JSONStreamAware response = Utils.emptyJSON;
        String strResponse = null;

        try {
            String requestType = req.getParameter("requestType");
            if (requestType == null) {
                response = ERROR_INCORRECT_REQUEST;
                return;
            }

            APIRequestHandler apiRequestHandler = apiRequestHandlers.get(requestType);
            if (apiRequestHandler == null) {
                response = ERROR_INCORRECT_REQUEST;
                return;
            }

            try {
                strResponse = apiRequestHandler.processRequest(assetObserver, req);
            } catch (RuntimeException e) {
                AssetObserver.log.log(Level.WARNING, "Error processing API request", e);
                response = ERROR_INCORRECT_REQUEST;
            }

        } finally {
            resp.setContentType("text/plain; charset=UTF-8");
            try (Writer writer = resp.getWriter()) {
                if (strResponse != null) {
                    writer.write(strResponse);
                } else {
                    response.writeJSONString(writer);
                }
            }
        }

    }

}
