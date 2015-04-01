package io.astefanutti.further.cdi.camel;

import org.apache.camel.builder.RouteBuilder;

public class FileToJmsRouteBean extends RouteBuilder {

    @Override
    public void configure() {
        from("file:target/input?delay=1000").convertBodyTo(String.class).to("sjms:queue:output");
    }
}
