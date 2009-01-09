package org.netbeans.modules.parsing.api;

import org.netbeans.modules.parsing.spi.SchedulerEvent;
import org.netbeans.modules.parsing.spi.Scheduler;
import org.netbeans.modules.parsing.spi.SourceModificationEvent;

public class MyScheduler extends Scheduler {

    private static MyScheduler myScheduler;

    public static void schedule2 (Source source, SchedulerEvent event) {
        if (myScheduler == null)
            myScheduler = new MyScheduler ();
        myScheduler.schedule(source, event);
    }

    @Override
    protected SchedulerEvent createSchedulerEvent (SourceModificationEvent event) {
        return new SchedulerEvent (this) {};
    }
}
