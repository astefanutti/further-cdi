package io.astefanutti.cdi.further.metrics;


import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.inject.Inject;
import java.util.concurrent.TimeUnit;

public class TimedMethodBean {

    @Inject
    MetricRegistry registry;

    @PostConstruct
    void registerTimer() {
        registry.register("myTimer", new Timer(new SlidingTimeWindowReservoir(1L, TimeUnit.MINUTES)));
    }

    @Timed(name = "myTimer")
    public void timedMethod() {
        Timer timer = registry.timer("myTimer");
        Timer.Context time = timer.time();
        try {
            LoggerFactory.getLogger("BASIC METRICS").info("Timed method called, timer [{}] will be incremented", "myTimer");
        } finally {
            time.stop();
        }
    }
}
