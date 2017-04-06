package org.cdi.further.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.PropertyInject;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.impl.DefaultCamelContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.WithAnnotations;
import java.util.HashSet;
import java.util.Set;

import static org.cdi.further.camel.Exceptions.rethrow;

public class CamelExtension implements Extension {

    private final Set<AnnotatedType<?>> camelBeans = new HashSet<>();

    private void camelAnnotatedTypes(@Observes @WithAnnotations(PropertyInject.class) ProcessAnnotatedType<?> pat) {
        camelBeans.add(pat.getAnnotatedType());
    }

    private <T> void camelBeanPostProcessor(@Observes ProcessInjectionTarget<T> pit, BeanManager manager) {
        if (camelBeans.contains(pit.getAnnotatedType()))
            pit.setInjectionTarget(new CamelInjectionTarget<>(pit.getInjectionTarget(), manager));
    }

    private void addCamelContext(@Observes AfterBeanDiscovery abd, BeanManager manager) {
        abd.addBean()
            .types(CamelContext.class)
            .scope(ApplicationScoped.class)
            .produceWith(instance -> new DefaultCamelContext(new CamelCdiRegistry(manager)))
            .disposeWith(rethrow((context, instance) -> context.stop()));
    }

    private void configureCamelContext(@Observes AfterDeploymentValidation adv, BeanManager manager) throws Exception {
        CamelContext context = manager.createInstance().select(CamelContext.class).get();
        manager.createInstance().select(RoutesBuilder.class).forEach(rethrow(context::addRoutes));
        context.start();
    }
}