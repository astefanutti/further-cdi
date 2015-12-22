package org.cdi.further.metrics;


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
        registry.register("my_timer", new Timer(new SlidingTimeWindowReservoir(1L, TimeUnit.MINUTES)));
    }

    @Timed(name = "my_timer")
    public void timedMethod() {
        Timer timer = registry.timer("my_timer");
        Timer.Context time = timer.time();
        try {
            LoggerFactory.getLogger("BASIC METRICS").info("Timed method called, timer [{}] will be incremented", "my_timer");
        } finally {
            time.stop();
        }
    }
}
