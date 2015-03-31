package io.astefanutti.further.cdi.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.ProducerTemplate;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.apache.camel.impl.DefaultCamelContext;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Produces;
import javax.enterprise.inject.spi.Extension;
import java.util.concurrent.TimeUnit;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

@RunWith(Arquillian.class)
public class CamelRouteInstrumentationTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class)
            // Camel extension
            .addAsServiceProvider(Extension.class, CamelExtension.class)
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Produces
    @ApplicationScoped
    private CamelContext camelContext = new DefaultCamelContext();

    @Test
    @InSequence(1)
    public void addRoutes(CamelContext context) throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:inbound").routeId("inbound")
                    .setBody(constant("body")).id("setBody")
                    .to("direct:outbound").id("toOutbound");

                from("direct:outbound").routeId("outbound")
                    .to("mock:outbound");
            }
        });
        context.start();
    }

    @Test
    @InSequence(2)
    public void sendMessage(CamelContext context) throws Exception {
        MockEndpoint outbound = context.getEndpoint("mock:outbound", MockEndpoint.class);
        outbound.expectedMessageCount(1);
        outbound.expectedHeaderReceived("advice", Boolean.TRUE);

        ProducerTemplate inbound = context.createProducerTemplate();
        inbound.setDefaultEndpointUri("direct:inbound");
        inbound.sendBody("test");

        assertIsSatisfied(1L, TimeUnit.SECONDS, outbound);
    }

    private static void advice(@Observes @Node("setBody") Exchange exchange) {
        exchange.getIn().setHeader("advice", Boolean.TRUE);
    }
}
