package it.r.microgateway.server.url;

import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.reactive.ServletHttpHandlerAdapter;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.testng.annotations.Test;

@Test
public class MessageRouterTest {

    private static final Logger LOGGER = LoggerFactory.getLogger(MessageRouterTest.class);

    public static void main(String[] args) throws InterruptedException, LifecycleException {
        RouterFunction<ServerResponse> httpHandler = MessageRouterBuilder.create()
            .handlerFor(RicercaPersona.class, RicercaPersona::toString)
            .build();

        Tomcat tomcatServer = new Tomcat();
        tomcatServer.setHostname("localhost");
        tomcatServer.setPort(8180);
        Context rootContext = tomcatServer.addContext("", System.getProperty("java.io.tmpdir"));
        ServletHttpHandlerAdapter servlet = new ServletHttpHandlerAdapter(
            RouterFunctions.toHttpHandler(httpHandler)
        );
        Tomcat.addServlet(rootContext, "httpHandlerServlet", servlet);

        rootContext.addServletMappingDecoded("/", "httpHandlerServlet");
        tomcatServer.start();
        Thread.sleep(10000000);

    }
    @Test
    public void test() {

    }
}
