package org.cdi.further.camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.CamelContext;
import org.apache.camel.component.properties.PropertiesComponent;
import org.apache.camel.component.sjms.SjmsComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class JmsComponentFactoryBean {

    @Produces
    @ApplicationScoped
    SjmsComponent sjmsComponent(PropertiesComponent properties) throws Exception {
        SjmsComponent component = new SjmsComponent();
        component.setConnectionFactory(new ActiveMQConnectionFactory("vm://broker?broker.persistent=false&broker.useShutdownHook=false&broker.useJmx=false"));
        component.setConnectionCount(Integer.valueOf(properties.parseUri("{{jms.maxConnections}}")));
        return component;
    }
}
