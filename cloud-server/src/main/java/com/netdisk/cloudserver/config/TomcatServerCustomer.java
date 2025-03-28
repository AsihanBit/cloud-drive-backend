package com.netdisk.cloudserver.config;

import org.apache.catalina.connector.Connector;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.embedded.tomcat.TomcatServletWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.stereotype.Component;

/**
 * 增加额外 http 的连接器
 */
//@Component
public class TomcatServerCustomer implements WebServerFactoryCustomizer<TomcatServletWebServerFactory> {

    @Value("${server.port}")
    private Integer serverPort;

    @Override
    public void customize(TomcatServletWebServerFactory factory) {
        final Connector httpConn = new Connector("HTTP/1.1");
//        httpConn.setPort(8081); //
        httpConn.setPort(8081);
        httpConn.setRedirectPort(serverPort);  // 将HTTP请求重定向到yml中的端口
        factory.addAdditionalTomcatConnectors(httpConn);
    }
}
