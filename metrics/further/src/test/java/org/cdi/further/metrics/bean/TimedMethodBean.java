package org.cdi.further.metrics.bean;


import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.Produces;
import java.util.concurrent.TimeUnit;

public class TimedMethodBean {

    @Produces
    @Metric(name = "my_timer")
    Timer timer = new Timer(new SlidingTimeWindowReservoir(1L, TimeUnit.MINUTES));

    @Timed(name = "my_timer")
    public void timedMethod() {
        LoggerFactory.getLogger("FURTHER METRICS").info("Timed method called, timer [{}] will be incremented", "my_timer");
    }
}
