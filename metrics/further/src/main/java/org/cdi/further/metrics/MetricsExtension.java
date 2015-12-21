package org.cdi.further.metrics;

import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;


public class MetricsExtension implements Extension {

    void addMetricQualifier(@Observes BeforeBeanDiscovery bbd) {
        bbd.addQualifier(Metric.class);
    }

    void addTimedInterceptorBinding(@Observes BeforeBeanDiscovery bbd) throws NoSuchMethodException {
        AnnotationLiteral<Nonbinding> NON_BINDING = new AnnotationLiteral<Nonbinding>() {};
        bbd.addInterceptorBinding(new AnnotatedTypeBuilder<Timed>()
            .readFromType(Timed.class)
            .addToMethod(Timed.class.getMethod("name"), NON_BINDING)
            .addToMethod(Timed.class.getMethod("absolute"), NON_BINDING)
            .create());
    }

    <T extends com.codahale.metrics.Metric> void decorateMetricProducer(@Observes ProcessProducer<?, T> pp) {
        if (pp.getAnnotatedMember().isAnnotationPresent(Metric.class)) {
            String name = pp.getAnnotatedMember().getAnnotation(Metric.class).name();
            pp.setProducer(new MetricProducer<>(pp.getProducer(), name));
        }
    }

    void registerProduceMetrics(@Observes AfterDeploymentValidation adv) {
        BeanProvider.getContextualReferences(com.codahale.metrics.Metric.class, true);
    }
}
