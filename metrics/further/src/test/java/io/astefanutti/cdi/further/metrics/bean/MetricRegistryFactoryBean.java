package io.astefanutti.cdi.further.metrics.bean;

import com.codahale.metrics.MetricRegistry;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

public class MetricRegistryFactoryBean {

    @Produces
    @ApplicationScoped
    MetricRegistry registry = new MetricRegistry();
}
