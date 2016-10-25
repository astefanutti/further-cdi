package org.cdi.further.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.PropertyInject;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.util.ObjectHelper;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.WithAnnotations;
import java.util.HashSet;
import java.util.Set;

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
            .produceWith(() -> new DefaultCamelContext(new CamelCdiRegistry(manager)))
            .disposeWith(context -> {
                try {
                    context.stop();
                } catch (Exception cause) {
                    throw ObjectHelper.wrapRuntimeCamelException(cause);
                }
            });
    }

    private void configureCamelContext(@Observes AfterDeploymentValidation adv, BeanManager manager) throws Exception {
        CamelContext context = (CamelContext) manager.getReference(manager.resolve(manager.getBeans(CamelContext.class)), CamelContext.class, manager.createCreationalContext(null));

        for (Bean<?> bean : manager.getBeans(RoutesBuilder.class))
            context.addRoutes((RoutesBuilder) manager.getReference(bean, RoutesBuilder.class, manager.createCreationalContext(bean)));

        context.start();
    }
}
