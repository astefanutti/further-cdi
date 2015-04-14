package io.astefanutti.cdi.further.camel.bean;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.PropertyInject;
import org.apache.camel.component.sjms.SjmsComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

public class JmsComponentFactoryBean {

    @PropertyInject(value = "jms.maxConnections", defaultValue = "10")
    private int maxConnections;

    @Produces
    @Named("sjms")
    @ApplicationScoped
    SjmsComponent sjmsComponent() {
        SjmsComponent component = new SjmsComponent();
        component.setConnectionFactory(new ActiveMQConnectionFactory("vm://broker?broker.persistent=false&broker.useShutdownHook=false"));
        component.setConnectionCount(maxConnections);
        return component;
    }
}
