package io.astefanutti.cdi.further.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;
import java.util.Set;

class MetricProducer<X extends Metric> implements Producer<X> {

    private final Producer<X> delegate;

    private final BeanManager bm;

    private final String name;

    MetricProducer(Producer<X> delegate, BeanManager bm, String name) {
        this.delegate = delegate;
        this.bm = bm;
        this.name = name;
    }

    @Override
    public X produce(CreationalContext<X> ctx) {
        MetricRegistry registry = BeanProvider.getContextualReference(bm, MetricRegistry.class, false);
        if (!registry.getMetrics().containsKey(name))
            registry.register(name, delegate.produce(ctx));

        return (X) registry.getMetrics().get(name);
    }

    @Override
    public void dispose(X instance) {
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return delegate.getInjectionPoints();
    }
}