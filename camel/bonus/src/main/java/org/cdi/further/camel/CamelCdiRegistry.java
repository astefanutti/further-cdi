package org.cdi.further.camel;

import org.apache.camel.spi.Registry;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

class CamelCdiRegistry implements Registry {

    private final BeanManager manager;

    CamelCdiRegistry(BeanManager manager) {
        this.manager = manager;
    }

    @Override
    public Object lookupByName(String name) {
        return lookupByNameAndType(name, Object.class);
    }

    @Override
    public <T> T lookupByNameAndType(String name, Class<T> type) {
        return Optional.of(manager.getBeans(name))
            .map(manager::resolve)
            .map(bean -> manager.getReference(bean, type, manager.createCreationalContext(bean)))
            .map(type::cast)
            .orElse(null);
    }

    @Override
    // Not used in the examples
    public <T> Map<String, T> findByTypeWithName(Class<T> type) {
        return manager.getBeans(type, Any.Literal.INSTANCE).stream()
            .filter(bean -> bean.getName() != null)
            .collect(toMap(Bean::getName, bean -> type.cast(manager.getReference(bean, type, manager.createCreationalContext(bean)))));
    }

    @Override
    // Not used in the examples
    public <T> Set<T> findByType(Class<T> type) {
        return manager.getBeans(type, Any.Literal.INSTANCE).stream()
            .map(bean -> manager.getReference(bean, type, manager.createCreationalContext(bean)))
            .map(type::cast)
            .collect(toSet());
    }

    @Override
    // Deprecated
    public Object lookup(String name) {
        return lookupByName(name);
    }

    @Override
    // Deprecated
    public <T> T lookup(String name, Class<T> type) {
        return lookupByNameAndType(name, type);
    }

    @Override
    // Deprecated
    public <T> Map<String, T> lookupByType(Class<T> type) {
        return findByTypeWithName(type);
    }
}
