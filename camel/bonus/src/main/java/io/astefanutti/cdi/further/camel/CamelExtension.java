/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.astefanutti.cdi.further.camel;

import org.apache.camel.AsyncCallback;
import org.apache.camel.CamelContext;
import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.PropertyInject;
import org.apache.camel.RoutesBuilder;
import org.apache.camel.model.ProcessorDefinition;
import org.apache.camel.processor.DelegateAsyncProcessor;
import org.apache.camel.spi.InterceptStrategy;

import javax.enterprise.event.Observes;
import javax.enterprise.inject.spi.AfterBeanDiscovery;
import javax.enterprise.inject.spi.AfterDeploymentValidation;
import javax.enterprise.inject.spi.AnnotatedType;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.Extension;
import javax.enterprise.inject.spi.ProcessAnnotatedType;
import javax.enterprise.inject.spi.ProcessInjectionTarget;
import javax.enterprise.inject.spi.ProcessObserverMethod;
import javax.enterprise.inject.spi.WithAnnotations;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

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
        abd.addBean(new CamelContextBean(manager));
    }

    private void configureCamelContext(@Observes AfterDeploymentValidation adv, final BeanManager manager) throws Exception {
        CamelContext context = (CamelContext) manager.getReference(manager.resolve(manager.getBeans(CamelContext.class)), CamelContext.class, manager.createCreationalContext(null));

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

        for (Bean<?> bean : manager.getBeans(RoutesBuilder.class))
            context.addRoutes((RoutesBuilder) manager.getReference(bean, RoutesBuilder.class, manager.createCreationalContext(bean)));

        context.start();
    }
}
