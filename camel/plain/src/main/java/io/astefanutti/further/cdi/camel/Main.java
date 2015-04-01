package io.astefanutti.further.cdi.camel;


import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.sjms.SjmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

public class Main {

    public static void main(String[] args) throws Exception {
        final CamelContext context = new DefaultCamelContext();

        context.addRoutes(new RouteBuilder() {
            @Override
            public void configure() throws Exception {
                from("file:target/input?delay=1000").convertBodyTo(String.class).to("sjms:queue:output");
            }
        });

        SjmsComponent component = new SjmsComponent();
        component.setConnectionFactory(new ActiveMQConnectionFactory("vm://broker?broker.persistent=false&broker.useShutdownHook=false"));
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
