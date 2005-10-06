/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.versioning.system.cvss;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Allows to synchronize tasks (performed in multiple
 * ClientRuntime threads) in ExecutorSupport group.
 *
 * <p>Example usage:
 * <pre>
 * ExecutorGroup group = ...;
 * group.addExecutor(...);
 * group.addExecutor(...);
 * group.addExecutor(...);
 *
 * // once executed waits until above executors finish
 * group.addBarrier(Runnable action);
 *
 * // then continue
 * group.addExecutor(...);
 * group.addExecutor(...);
 *
 * // dispatch into execution queues
 * group.execute();
 * </pre>
 *
 * @author Petr Kuzel
 */
final class ExecutorGroupBar implements ExecutorGroup.Groupable {

    private final Runnable action;
    private final ExecutorSupport[] bar;
    private ExecutorGroup group;

    /**
     * Creates barrier, with optional action
     * @param executorsBar collection of ExecutorSupports to wait for.
     *        Actually all other Groupables are relaxed, silently ignored.  
     */
    public ExecutorGroupBar(Collection executorsBar, Runnable action) {
        this.action = action;

        // ExecutorSupport.wait(...); works only for ExecutorSupports
        List filtered = new ArrayList(executorsBar.size());
        Iterator it = executorsBar.iterator();
        while (it.hasNext()) {
            ExecutorGroup.Groupable groupable = (ExecutorGroup.Groupable) it.next();
            if (groupable instanceof ExecutorSupport) {
                filtered.add(groupable);
            }
        }

        bar = (ExecutorSupport[]) filtered.toArray(new ExecutorSupport[filtered.size()]);
    }

    public void joinGroup(ExecutorGroup group) {
        this.group = group;
        group.enqueued(null, this);
    }

    /**
     * This one is blocking. It returns when all bar
     * executors and action finish (successfuly or fail).
     */
    public void execute() {
        ExecutorSupport.wait(bar);
        if (action != null) {
            action.run();
        }
        group.finished(null, this);
    }
}
