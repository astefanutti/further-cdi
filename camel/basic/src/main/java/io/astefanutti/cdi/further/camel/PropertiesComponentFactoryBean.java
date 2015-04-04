package io.astefanutti.cdi.further.camel;

import org.apache.camel.component.properties.PropertiesComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class PropertiesComponentFactoryBean {

    @Produces
    @ApplicationScoped
    PropertiesComponent propertiesComponent() {
        PropertiesComponent component = new PropertiesComponent();
        component.setLocation("classpath:camel.properties");
        return component;
    }
}
