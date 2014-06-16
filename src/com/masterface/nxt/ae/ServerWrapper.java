package com.masterface.nxt.ae;

import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.*;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.util.ssl.SslContextFactory;

public class ServerWrapper {

    private APIServlet apiServlet;

    public void start(AssetObserver assetObserver) {
        final int port = PropertiesStorage.getIntProperty("ServerPort");
        final String host = PropertiesStorage.getStringProperty("ServerHost");
        final Server apiServer = new Server();
        ServerConnector connector;

        boolean enableSSL = PropertiesStorage.getBooleanProperty("apiSSL");
        if (enableSSL) {
            AssetObserver.log.info("Using SSL (https) for the API server");
            HttpConfiguration https_config = new HttpConfiguration();
            https_config.setSecureScheme("https");
            https_config.setSecurePort(port);
            https_config.addCustomizer(new SecureRequestCustomizer());
            SslContextFactory sslContextFactory = new SslContextFactory();
            sslContextFactory.setKeyStorePath(PropertiesStorage.getStringProperty("keyStorePath"));
            sslContextFactory.setKeyStorePassword(PropertiesStorage.getStringProperty("keyStorePassword"));
            sslContextFactory.setExcludeCipherSuites("SSL_RSA_WITH_DES_CBC_SHA", "SSL_DHE_RSA_WITH_DES_CBC_SHA",
                    "SSL_DHE_DSS_WITH_DES_CBC_SHA", "SSL_RSA_EXPORT_WITH_RC4_40_MD5", "SSL_RSA_EXPORT_WITH_DES40_CBC_SHA",
                    "SSL_DHE_RSA_EXPORT_WITH_DES40_CBC_SHA", "SSL_DHE_DSS_EXPORT_WITH_DES40_CBC_SHA");
            connector = new ServerConnector(apiServer, new SslConnectionFactory(sslContextFactory, "http/1.1"),
                    new HttpConnectionFactory(https_config));
        } else {
            connector = new ServerConnector(apiServer);
        }

        connector.setPort(port);
        connector.setHost(host);
        connector.setIdleTimeout(PropertiesStorage.getIntProperty("apiServerIdleTimeout"));
        apiServer.addConnector(connector);

        HandlerList apiHandlers = new HandlerList();

        String apiResourceBase = PropertiesStorage.getStringProperty("apiResourceBase");
        if (apiResourceBase != null) {
            ResourceHandler apiFileHandler = new ResourceHandler();
            apiFileHandler.setDirectoriesListed(true);
            apiFileHandler.setWelcomeFiles(new String[]{"index.html"});
            apiFileHandler.setResourceBase(apiResourceBase);
            apiHandlers.addHandler(apiFileHandler);
        }

        ContextHandlerCollection contextHandlerCollection = new ContextHandlerCollection();
        ServletContextHandler servletContext = new ServletContextHandler(contextHandlerCollection, "/*", ServletContextHandler.SESSIONS);

        apiServlet = new APIServlet();
        apiServlet.setAssetObserver(assetObserver);
        ServletHolder apiServletHolder = new ServletHolder(apiServlet);
        servletContext.addServlet(apiServletHolder, "/api/*");

        apiHandlers.addHandler(servletContext);

        NCSARequestLog requestLog = new NCSARequestLog("access_yyyy_mm_dd.log");
        int days = 14;
        requestLog.setRetainDays(days);
        requestLog.setAppend(true);
        requestLog.setExtended(true);
        requestLog.setLogCookies(true);
        requestLog.setLogServer(true);
        requestLog.setLogLatency(true);
        requestLog.setPreferProxiedForAddress(true);
        requestLog.setLogTimeZone(System.getProperty("user.timezone"));
        RequestLogHandler requestLogHandler = new RequestLogHandler();
        requestLogHandler.setRequestLog(requestLog);
        apiHandlers.addHandler(requestLogHandler);
        apiHandlers.addHandler(new DefaultHandler());

        apiServer.setHandler(apiHandlers);
        apiServer.setStopAtShutdown(true);

        Runnable r = new Runnable() {
            public void run() {
                try {
                    apiServer.start();
                    AssetObserver.log.info("API server started listening on " + host + ":" + port);
                } catch (Exception e) {
                    AssetObserver.log.info("Failed to start API server");
                    throw new RuntimeException(e.toString(), e);
                }
            }
        };
        new Thread(r).start();
    }

    public void setAssetObserver(AssetObserver assetObserver) {
        apiServlet.setAssetObserver(assetObserver);
    }
}
