package org.cdi.further.camel;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.processor.DelegateAsyncProcessor;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.WithAnnotations;
import java.util.HashSet;
import java.util.Set;

import static org.cdi.further.camel.Exceptions.rethrow;

public class CamelExtension implements Extension {

    private final Set<AnnotatedType<?>> camelBeans = new HashSet<>();

    private final Set<Node> nodePointcuts = new HashSet<>();

    private void camelAnnotatedTypes(@Observes @WithAnnotations(PropertyInject.class) ProcessAnnotatedType<?> pat) {
        camelBeans.add(pat.getAnnotatedType());
    }

    private <T> void camelBeanPostProcessor(@Observes ProcessInjectionTarget<T> pit, BeanManager manager) {
        if (camelBeans.contains(pit.getAnnotatedType()))
            pit.setInjectionTarget(new CamelInjectionTarget<>(pit.getInjectionTarget(), manager));
    }

    private void camelNodePointcuts(@Observes ProcessObserverMethod<Exchange, ?> pom) {
        pom.getObserverMethod().getObservedQualifiers().stream()
            .filter(q -> q instanceof Node).map(Node.class::cast).forEach(nodePointcuts::add);
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
        context.addInterceptStrategy((camel, definition, target, next) -> definition.hasCustomIdAssigned()
            ? nodePointcuts.stream().filter(node -> definition.getId().equals(node.value())).findFirst()
                .map(node -> (Processor) new DelegateAsyncProcessor(target) {
                    public boolean process(Exchange exchange, AsyncCallback callback) {
                        manager.fireEvent(exchange, node);
                        return super.process(exchange, callback);
                    }
                }).orElse(target)
            : target);
        manager.createInstance().select(RoutesBuilder.class).forEach(rethrow(context::addRoutes));
        context.start();
    }
}
