package org.cdi.further.camel;

import org.apache.camel.spi.Registry;

import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import java.util.Collections;
import java.util.Map;
import java.util.Set;

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
        Bean<?> bean = manager.resolve(manager.getBeans(name));
        return bean == null ? null : (T) manager.getReference(bean, type, manager.createCreationalContext(bean));
    }

    @Override
    // Not used
    public <T> Map<String, T> findByTypeWithName(Class<T> type) {
        return Collections.emptyMap();
    }

    @Override
    // Deprecated
    public <T> Set<T> findByType(Class<T> type) {
        return null;
    }

    @Override
    // Deprecated
    public Object lookup(String name) {
        return null;
    }

    @Override
    // Deprecated
    public <T> T lookup(String name, Class<T> type) {
        return null;
    }

    @Override
    // Deprecated
    public <T> Map<String, T> lookupByType(Class<T> type) {
        return null;
    }
}
