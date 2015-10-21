package io.astefanutti.cdi.further.camel.bean;

import org.apache.camel.builder.RouteBuilder;

public class FileToJmsRouteBean extends RouteBuilder {

    @Override
    public void configure() {
        from("file:target/input?delay=1000")
            .convertBodyTo(String.class)
            // Logged with Camel DSL AOP
            //.log("Sending message [${body}] to JMS...")
            .to("sjms:queue:output").id("join point");
    }
}
