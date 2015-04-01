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
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

import static org.apache.camel.component.mock.MockEndpoint.assertIsSatisfied;

@RunWith(Arquillian.class)
public class FurtherCdiCamelTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClasses(FileToJmsRouteBean.class, JmsComponentFactoryBean.class)
            // Camel extension
            .addAsServiceProvider(Extension.class, CamelExtension.class)
            // Bean archive deployment descriptor
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void sendMessage(CamelContext context) throws Exception {
        MockEndpoint output = context.getEndpoint("mock:output", MockEndpoint.class);
        output.expectedMessageCount(1);
        output.expectedHeaderReceived("advice", Boolean.TRUE);
        output.expectedBodiesReceived("HI DEVOXX");

        Files.write(Paths.get("target/input/msg"), "HI DEVOXX".getBytes());

        assertIsSatisfied(5L, TimeUnit.SECONDS, output);
    }

    private static void pointcut(@Observes @Node("join point") Exchange exchange) {
        exchange.getIn().setHeader("advice", Boolean.TRUE);
    }

    private static class JmsToMockRoute extends RouteBuilder {

        @Override
        public void configure() {
            from("sjms:queue:output").log("Message [${body}] received").to("mock:output");
        }
    }
}
