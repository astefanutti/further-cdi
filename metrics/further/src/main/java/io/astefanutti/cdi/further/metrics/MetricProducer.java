package io.astefanutti.cdi.further.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;
import org.apache.deltaspike.core.api.provider.BeanProvider;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;
import java.util.Set;

class MetricProducer<T extends Metric> implements Producer<T> {

    private final Producer<T> delegate;

    private final BeanManager bm;

    private final String name;

    MetricProducer(Producer<T> delegate, String name, BeanManager bm) {
        this.delegate = delegate;
        this.bm = bm;
        this.name = name;
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        MetricRegistry registry = BeanProvider.getContextualReference(bm, MetricRegistry.class, false);
        if (!registry.getMetrics().containsKey(name))
            registry.register(name, delegate.produce(ctx));

        return (T) registry.getMetrics().get(name);
    }

    @Override
    public void dispose(Metric instance) {
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return delegate.getInjectionPoints();
    }
}