package org.cdi.further.camel;

import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.sjms.SjmsComponent;
import org.apache.camel.impl.DefaultCamelContext;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.enterprise.context.ApplicationScoped;
import javax.inject.Inject;

@ApplicationScoped
public class CamelContextBean extends DefaultCamelContext {

    @Inject
    CamelContextBean(FileToJmsRouteBean route, SjmsComponent sjmsComponent, PropertiesComponent propertiesComponent) throws Exception {
        addComponent("properties", propertiesComponent);
        addComponent("sjms", sjmsComponent);
        addRoutes(route);
    }

    @PostConstruct
    void startContext() {
        try {
            super.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    void stopContext() {
        try {
            super.stop();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
