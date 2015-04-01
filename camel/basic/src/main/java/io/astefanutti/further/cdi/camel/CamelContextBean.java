package io.astefanutti.further.cdi.camel;

import org.apache.camel.component.sjms.SjmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CamelContextBean extends DefaultCamelContext {

    @Inject
    CamelContextBean(FileToJmsRouteBean route, SjmsComponent sjmsComponent) throws Exception {
        addComponent("sjms", sjmsComponent);
        addRoutes(route);
    }

    @PostConstruct
    void postConstruct() {
        try {
            super.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    void preDestroy() {
        try {
            super.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
