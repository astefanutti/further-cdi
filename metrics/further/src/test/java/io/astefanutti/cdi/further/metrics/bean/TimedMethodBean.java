package io.astefanutti.cdi.further.metrics.bean;


import com.codahale.metrics.SlidingTimeWindowReservoir;
import com.codahale.metrics.Timer;
import com.codahale.metrics.annotation.Metric;
import com.codahale.metrics.annotation.Timed;

import javax.enterprise.inject.Produces;
import java.util.concurrent.TimeUnit;

public class TimedMethodBean {

    @Produces
    @Metric(name="myTimer")
    Timer timer = new Timer(new SlidingTimeWindowReservoir(1L, TimeUnit.MINUTES));

    @Timed(name="myTimer")
    public void timedMethod() throws InterruptedException {
        System.out.println("doing a pause");
        Thread.sleep(1000);
    }
}
