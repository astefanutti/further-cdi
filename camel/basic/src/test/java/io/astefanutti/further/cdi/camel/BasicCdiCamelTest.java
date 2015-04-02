package io.astefanutti.further.cdi.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.junit.InSequence;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

@RunWith(Arquillian.class)
public class BasicCdiCamelTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(JavaArchive.class)
            .addClasses(CamelContextBean.class, FileToJmsRouteBean.class, JmsComponentFactoryBean.class, PropertiesComponentFactoryBean.class)
            .addAsManifestResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    private CamelContext context;

    @Test
    @InSequence(1)
    public void jmsToMockRoute() throws Exception {
        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("sjms:queue:output").log("Message [${body}] sent to JMS").to("mock:output");
            }
        });
    }

    @Test
    @InSequence(2)
    public void sendMessage() throws Exception {
        MockEndpoint output = context.getEndpoint("mock:output", MockEndpoint.class);
        output.expectedMessageCount(1);
        output.expectedBodiesReceived("HI DEVOXX");

        Files.write(Paths.get("target/input/msg"), "HI DEVOXX".getBytes());

        MockEndpoint.assertIsSatisfied(5L, TimeUnit.SECONDS, output);
    }
}
