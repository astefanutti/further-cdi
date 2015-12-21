package org.cdi.further.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelBeanPostProcessor;

import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.InjectionException;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.inject.spi.InjectionTarget;
import java.util.Set;

class CamelInjectionTarget<T> implements InjectionTarget<T> {

    private final InjectionTarget<T> delegate;

    private final DefaultCamelBeanPostProcessor processor;

    CamelInjectionTarget(InjectionTarget<T> delegate, final BeanManager manager) {
        this.delegate = delegate;
        processor = new DefaultCamelBeanPostProcessor() {
            @Override
            public CamelContext getOrLookupCamelContext() {
                return (CamelContext) manager.getReference(manager.resolve(manager.getBeans(CamelContext.class)), CamelContext.class, manager.createCreationalContext(null));
            }
        };
    }

    @Override
    public void inject(T instance, CreationalContext<T> ctx) {
        try {
            processor.postProcessBeforeInitialization(instance, null);
        } catch (Exception cause) {
            throw new InjectionException(cause);
        }
        delegate.inject(instance, ctx);
    }

    @Override
    public void postConstruct(T instance) {
        delegate.postConstruct(instance);
    }

    @Override
    public void preDestroy(T instance) {
        delegate.preDestroy(instance);
    }

    @Override
    public T produce(CreationalContext<T> ctx) {
        return delegate.produce(ctx);
    }

    @Override
    public void dispose(T instance) {
        delegate.dispose(instance);
    }

    @Override
    public Set<InjectionPoint> getInjectionPoints() {
        return delegate.getInjectionPoints();
    }
}
