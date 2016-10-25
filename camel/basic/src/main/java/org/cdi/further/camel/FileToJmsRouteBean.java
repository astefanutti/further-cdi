package org.cdi.further.camel;

import org.apache.camel.builder.RouteBuilder;

public class FileToJmsRouteBean extends RouteBuilder {

    @Override
    public void configure() {
        from("file:target/input?delay=1s")
            .log("Sending message [${body}] to JMS...")
            .to("sjms:queue:output");
    }
}
