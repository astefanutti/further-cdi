package io.astefanutti.cdi.further.metrics;

import com.codahale.metrics.Timer;
import org.slf4j.LoggerFactory;

public class TimedMethodClass {

    public void timedMethod() {
        Timer timer = MetricsHelper.REGISTRY.timer("timer");
        Timer.Context time = timer.time();
        try {
            LoggerFactory.getLogger("PLAIN METRICS").info("Timed method called, timer will be incremented");
        } finally {
            time.stop();
        }
    }
}
