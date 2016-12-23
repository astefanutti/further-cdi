package org.cdi.further.camel;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.impl.DefaultCamelContext;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.InterceptStrategy;

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
import java.lang.annotation.Annotation;
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
        for (Annotation annotation : pom.getObserverMethod().getObservedQualifiers())
            if (annotation instanceof Node)
                nodePointcuts.add(Node.class.cast(annotation));
    }

    private void addCamelContext(@Observes AfterBeanDiscovery abd, BeanManager manager) {
        abd.<CamelContext>addBean()
            .types(CamelContext.class)
            .scope(ApplicationScoped.class)
            .produceWith(instance -> new DefaultCamelContext(new CamelCdiRegistry(manager)))
            .disposeWith(rethrow((context, instance) -> context.stop()));
    }

    private void configureCamelContext(@Observes AfterDeploymentValidation adv, final BeanManager manager) throws Exception {
        CamelContext context = manager.createInstance().select(CamelContext.class).get();

        if (!nodePointcuts.isEmpty()) {
            context.addInterceptStrategy(new InterceptStrategy() {
                @Override
                public Processor wrapProcessorInInterceptors(CamelContext context, ProcessorDefinition<?> definition, Processor target, Processor nextTarget) throws Exception {
                    if (definition.hasCustomIdAssigned()) {
                        for (final Node node : nodePointcuts)
                            if (definition.getId().equals(node.value())) {
                                return new DelegateAsyncProcessor(target) {
                                    @Override
                                    public boolean process(Exchange exchange, AsyncCallback callback) {
                                        manager.fireEvent(exchange, node);
                                        return super.process(exchange, callback);
                                    }
                                };
                            }
                    }
                    return target;
                }
            });
        }

        manager.createInstance().select(RoutesBuilder.class).forEach(rethrow(context::addRoutes));

        context.start();
    }
}
