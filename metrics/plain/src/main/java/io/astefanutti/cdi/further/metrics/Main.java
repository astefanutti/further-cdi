package io.astefanutti.cdi.further.metrics;

import org.slf4j.LoggerFactory;

public class Main {

    public static void main(String[] args) {
        TimedMethodClass timedMethodClass = new TimedMethodClass();
        timedMethodClass.timedMethod();

        LoggerFactory.getLogger("PLAIN METRICS").info("Timed method has been invoked [{}] times", MetricsHelper.REGISTRY.timer("timer").getCount());
    }
}
