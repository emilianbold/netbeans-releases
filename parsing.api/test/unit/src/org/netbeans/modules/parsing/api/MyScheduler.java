package org.netbeans.modules.parsing.api;

import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.Scheduler;

public class MyScheduler extends Scheduler {

    private static MyScheduler myScheduler;

    public static void schedule2 (Source source, SchedulerEvent event) {
        if (myScheduler == null)
            myScheduler = new MyScheduler ();
        myScheduler.schedule(source, event);
    }
}
