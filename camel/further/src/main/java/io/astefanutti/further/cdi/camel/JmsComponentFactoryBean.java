package io.astefanutti.further.cdi.camel;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.component.sjms.SjmsComponent;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;
import javax.inject.Named;

public class JmsComponentFactoryBean {

    @Produces
    @Named("sjms")
    @ApplicationScoped
    SjmsComponent sjmsComponent() {
        SjmsComponent component = new SjmsComponent();
        component.setConnectionFactory(new ActiveMQConnectionFactory("vm://broker?broker.persistent=false&broker.useShutdownHook=false"));
        return component;
    }
}
