package io.astefanutti.further.cdi.camel;

import org.apache.camel.CamelContext;
import org.apache.camel.impl.DefaultCamelContext;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.inject.spi.InjectionPoint;
import javax.enterprise.util.AnnotationLiteral;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.Set;

class CamelContextBean implements Bean<CamelContext> {

    private final BeanManager manager;

    CamelContextBean(BeanManager manager) {
        this.manager = manager;
    }

    public Class<? extends Annotation> getScope() {
        return ApplicationScoped.class;
    }

    public Set<Annotation> getQualifiers() {
        return Collections.singleton((Annotation) new AnnotationLiteral<Default>() {});
    }

    public Set<Type> getTypes() {
        return Collections.singleton((Type) CamelContext.class);
    }

    public DefaultCamelContext create(CreationalContext<CamelContext> creational) {
        return new DefaultCamelContext(new CamelCdiRegistry(manager));
    }

    public void destroy(CamelContext instance, CreationalContext<CamelContext> creational) {
    }

    public Class<?> getBeanClass() {
        return DefaultCamelContext.class;
    }

    public Set<InjectionPoint> getInjectionPoints() {
        return Collections.emptySet();
    }

    public String getName() { // Only called for @Named bean
        return "";
    }

    public Set<Class<? extends Annotation>> getStereotypes() {
        return Collections.emptySet();
    }

    public boolean isAlternative() {
        return false;
    }

    public boolean isNullable() { // Deprecated since CDI 1.1
        return false;
    }
}