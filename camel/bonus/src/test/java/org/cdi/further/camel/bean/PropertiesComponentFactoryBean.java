package org.cdi.further.camel.bean;

import org.apache.camel.component.properties.PropertiesComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

public class PropertiesComponentFactoryBean {

    @Produces
    @Named("properties")
    @ApplicationScoped
    PropertiesComponent propertiesComponent() {
        PropertiesComponent component = new PropertiesComponent();
        component.setLocation("classpath:camel.properties");
        return component;
    }
}
