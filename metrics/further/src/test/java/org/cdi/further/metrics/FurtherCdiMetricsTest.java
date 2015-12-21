package org.cdi.further.metrics;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;
import org.cdi.further.metrics.bean.MetricRegistryFactoryBean;
import org.cdi.further.metrics.bean.TimedMethodBean;
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

import javax.enterprise.inject.spi.Extension;
import javax.inject.Inject;

@RunWith(Arquillian.class)
public class FurtherCdiMetricsTest {

    @Deployment
    public static Archive<?> deployment() {
        return ShrinkWrap.create(WebArchive.class, "test.war")
            .addClasses(TimedMethodBean.class, MetricRegistryFactoryBean.class)
            .addAsLibraries(Maven.resolver()
                .loadPomFromFile("pom.xml")
                .resolve("org.apache.deltaspike.core:deltaspike-core-api",
                    "io.dropwizard.metrics:metrics-core",
                    "io.dropwizard.metrics:metrics-annotation")
                .withTransitivity()
                .as(JavaArchive.class))
            .addPackage(MetricsExtension.class.getPackage())
            .addAsServiceProvider(Extension.class, MetricsExtension.class)
            .addAsWebInfResource(EmptyAsset.INSTANCE, "beans.xml");
    }

    @Inject
    @Metric(name = "myTimer")
    Timer timer;

    @Inject
    MetricRegistry registry;

    @Inject
    TimedMethodBean bean;

    @Test
    public void shouldMetricsBeTheSame() {
        Timer timer = registry.timer("myTimer");
        Assert.assertSame(timer, this.timer);
    }

    @Test
    public void shouldTimedInterceptorBeCalled() {
        bean.timedMethod();
        Assert.assertEquals(1, timer.getCount());
    }
}
