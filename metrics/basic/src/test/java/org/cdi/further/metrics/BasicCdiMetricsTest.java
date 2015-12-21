package org.cdi.further.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.EmptyAsset;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.jboss.shrinkwrap.resolver.api.maven.Maven;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

import javax.inject.Inject;

@RunWith(Arquillian.class)
public class BasicCdiMetricsTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClasses(TimedMethodBean.class, MetricRegistryFactoryBean.class)
            .addAsLibraries(Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("io.dropwizard.metrics:metrics-core")
                .withTransitivity()
                .as(JavaArchive.class))
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    MetricRegistry registry;

    @Inject
    TimedMethodBean bean;

    @Test
    public void shouldTimedInterceptorBeCalled() {
        bean.timedMethod();

        Timer timer = registry.getTimers().get("myTimer");
        Assert.assertEquals(1, timer.getCount());
    }
}
