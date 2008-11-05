package org.netbeans.modules.parsing.api;

import java.util.Collection;
import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.Scheduler;

public class MyScheduler extends Scheduler {

    private static MyScheduler myScheduler;

    public MyScheduler() {
        super();
        myScheduler = this;
    }

    public static void schedule(Collection<Source> sources, SchedulerEvent event) {
        myScheduler.schedule(sources, event);
    }
}
