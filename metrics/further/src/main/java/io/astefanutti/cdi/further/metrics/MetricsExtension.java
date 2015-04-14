package io.astefanutti.cdi.further.metrics;

import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;
import org.apache.deltaspike.core.api.literal.AnyLiteral;
import org.apache.deltaspike.core.api.provider.BeanProvider;
import org.apache.deltaspike.core.util.metadata.builder.AnnotatedTypeBuilder;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.BeforeBeanDiscovery;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessProducer;
import javax.enterprise.util.AnnotationLiteral;
import javax.enterprise.util.Nonbinding;
import java.lang.annotation.Annotation;


public class MetricsExtension implements Extension {

    private static final AnnotationLiteral<Nonbinding> NON_BINDING_LITERAL = new AnnotationLiteral<Nonbinding>() {};

    void addMetricQualifier(@Observes BeforeBeanDiscovery bbd, BeanManager bm) {
        bbd.addQualifier(bm.createAnnotatedType(Metric.class));
    }

    void addTimedInterceptorBinding(@Observes BeforeBeanDiscovery bbd) throws NoSuchMethodException {
        bbd.addInterceptorBinding(new AnnotatedTypeBuilder<Timed>()
            .readFromType(Timed.class)
            .addToMethod(Timed.class.getMethod("name"), NON_BINDING_LITERAL)
            .addToMethod(Timed.class.getMethod("absolute"), NON_BINDING_LITERAL)
            .create());
    }

    <T extends com.codahale.metrics.Metric> void decorateMetricProducer(@Observes ProcessProducer<?, T> pp, BeanManager bm) {
        if (pp.getAnnotatedMember().getAnnotation(Metric.class) != null)
            pp.setProducer(new MetricProducer<>(pp.getProducer(), pp.getAnnotatedMember().getAnnotation(Metric.class).name(), bm));
    }

    void registerProduceMetrics(@Observes AfterDeploymentValidation adv, BeanManager bm) {
        for (Bean<?> bean : bm.getBeans(com.codahale.metrics.Metric.class, new AnyLiteral()))
            for (Annotation qualifier : bean.getQualifiers())
                if (qualifier instanceof Metric)
                    BeanProvider.getContextualReference(bm, com.codahale.metrics.Metric.class, false, qualifier);
    }
}
