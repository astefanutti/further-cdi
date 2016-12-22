package org.cdi.further.metrics;

import com.codahale.metrics.Metric;
import com.codahale.metrics.MetricRegistry;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.Producer;
import java.util.Set;

class MetricProducer<X extends Metric> implements Producer<X> {

    private final BeanManager manager;

    private final Producer<X> delegate;

    private final String name;

    MetricProducer(BeanManager manager, Producer<X> delegate, String name) {
        this.manager = manager;
        this.delegate = delegate;
        this.name = name;
    }

    @Override
    public X produce(CreationalContext<X> ctx) {
        MetricRegistry registry = manager.createInstance().select(MetricRegistry.class).get();
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