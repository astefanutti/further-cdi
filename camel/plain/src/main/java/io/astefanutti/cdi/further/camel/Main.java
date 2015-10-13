package io.astefanutti.cdi.further.camel;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.sjms.SjmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class Main {

    public static void main(String[] args) throws Exception {
        final CamelContext context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() {
                from("file:target/input?delay=1000")
                    .convertBodyTo(String.class)
                    .log("Sending message [${body}] to JMS ...")
                    .to("sjms:queue:output");
            }
        });

        PropertiesComponent properties = new PropertiesComponent();
        properties.setLocation("classpath:camel.properties");
        context.addComponent("properties", properties);

        SjmsComponent component = new SjmsComponent();
        component.setConnectionFactory(new ActiveMQConnectionFactory("vm://broker?broker.persistent=false&broker.useShutdownHook=false&broker.useJmx=false"));
        component.setConnectionCount(Integer.valueOf(context.resolvePropertyPlaceholders("{{jms.maxConnections}}")));
        context.addComponent("sjms", component);

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    context.stop();
                } catch (Exception cause) {
                    cause.printStackTrace();
                }
            }
        });

        context.start();
    }
}
