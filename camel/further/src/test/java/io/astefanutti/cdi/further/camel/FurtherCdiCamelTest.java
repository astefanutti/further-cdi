package io.astefanutti.cdi.further.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.MessageHistory;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.LoggerFactory;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.Extension;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.TimeUnit;

@RunWith(Arquillian.class)
public class FurtherCdiCamelTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClasses(FileToJmsRouteBean.class, JmsComponentFactoryBean.class, PropertiesComponentFactoryBean.class)
            .addAsServiceProvider(Extension.class, CamelExtension.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Test
    public void sendMessage(CamelContext context) throws Exception {
        MockEndpoint output = context.getEndpoint("mock:output", MockEndpoint.class);
        output.expectedMessageCount(1);
        output.expectedHeaderReceived("advice", Boolean.TRUE);
        output.expectedBodiesReceived("HI DEVOXX");

        Files.write(Paths.get("target/input/msg"), "HI DEVOXX".getBytes());

        MockEndpoint.assertIsSatisfied(5L, TimeUnit.SECONDS, output);
    }

    private static void pointcut(@Observes @Node("join point") Exchange exchange) {
        List<MessageHistory> history = exchange.getProperty(Exchange.MESSAGE_HISTORY, List.class);
        LoggerFactory.getLogger("CAMEL DSL AOP").info("Sending message [{}] to [{}]...", exchange.getIn().getBody(), history.get(history.size() - 1).getNode().getLabel());
        exchange.getIn().setHeader("advice", Boolean.TRUE);
    }

    private static class JmsToMockRoute extends RouteBuilder {

        @Override
        public void configure() {
            from("sjms:queue:output").log("Message [${body}] sent to JMS").to("mock:output");
        }
    }
}
