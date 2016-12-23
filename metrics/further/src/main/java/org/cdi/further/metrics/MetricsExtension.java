package org.cdi.further.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.inject.spi.Producer;
import javax.enterprise.util.Nonbinding;

public class MetricsExtension implements Extension {

    void addMetricQualifier(@Observes BeforeBeanDiscovery bbd) {
        bbd.addQualifier(Metric.class);
    }

    void addTimedInterceptorBinding(@Observes BeforeBeanDiscovery bbd) {
        bbd.configureInterceptorBinding(Timed.class).methods().forEach(method -> method.add(Nonbinding.Literal.INSTANCE));
    }

    <T extends com.codahale.metrics.Metric> void decorateMetricProducer(@Observes ProcessProducer<?, T> pp, BeanManager manager) {
        if (pp.getAnnotatedMember().isAnnotationPresent(Metric.class)) {
            String name = pp.getAnnotatedMember().getAnnotation(Metric.class).name();
            Producer<T> producer = pp.getProducer();
            pp.configureProducer().produceWith(context -> {
                MetricRegistry registry = manager.createInstance().select(MetricRegistry.class).get();
                if (registry.getMetrics().containsKey(name))
                    return (T) registry.getMetrics().get(name);
                return registry.register(name, producer.produce(context));
            });
        }
    }

    void registerProduceMetrics(@Observes AfterDeploymentValidation adv, BeanManager manager) {
        manager.createInstance().select(com.codahale.metrics.Metric.class, Any.Literal.INSTANCE).forEach(Object::toString);
    }
}
