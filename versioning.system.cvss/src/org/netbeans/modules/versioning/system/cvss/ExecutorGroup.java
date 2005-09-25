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

import org.openide.util.Cancellable;
import org.openide.ErrorManager;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;

import java.util.*;

/**
 * Support for actions that run multiple commands.
 * Represents context that carry data shared by
 * action commands executors. It can manage execution
 * in multiple ClientRuntimes (threads).
 *
 * <p>Implements shared progress, logging support
 * and cancelling.
 *
 * @author Petr Kuzel
 */
public final class ExecutorGroup {

    private final String name;
    private boolean executed;
    private boolean cancelled;
    private List listeners = new ArrayList(2);
    private List executors = new ArrayList(2);
    private Map started = new HashMap();
    private ProgressHandle progressHandle;
    private long dataCounter;

    public ExecutorGroup(String displayName) {
        name = displayName;
    }

    /**
     * Defines group display name.
     */
    public String getDisplayName() {
        return name;
    }

    /**
     * Called by ExecutorSupport on enqueue.
     * @return true for the first command in given queue
     */
    synchronized boolean start(ClientRuntime queue) {
        if (started.isEmpty()) {
            progressHandle = ProgressHandleFactory.createHandle(name);
            progressHandle.start();
        }

        int i = 1;
        Integer counter = (Integer) started.get(queue);
        if (counter != null) {
            i = counter.intValue() + 1;
        }
        counter = new Integer(i);
        started.put(queue, counter);
        return i == 1;
    }

    /**
     * Called by ExecutorSupport after processing.
     * @return true for last command in given queue
     */
    synchronized boolean finished(ClientRuntime queue) {
        Integer counter = (Integer) started.get(queue);
        int i = counter.intValue() - 1;
        counter = new Integer(i);
        if (i == 0) {
            started.remove(queue);
            if (started.isEmpty()) {
                progressHandle.finish();
            }
        } else {
            started.put(queue, counter);
        }
        return i == 0;
    }

    boolean isCancelled() {
        return cancelled;
    }

    void cancel() {
        cancelled = true;
        Iterator it;
        synchronized(listeners) {
            it = new ArrayList(listeners).iterator();
        }
        while (it.hasNext()) {
            try {
                Cancellable cancellable = (Cancellable) it.next();
                cancellable.cancel();
            } catch (RuntimeException ex) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
            }
        }
    }

    /**
     * Add a cancelaable in chain of cancellable performers.
     */
    public void addCancellable(Cancellable cancellable) {
        synchronized(listeners) {
            listeners.add(cancellable);
        }
    }

    public void removeCancellable(Cancellable cancellable) {
        synchronized(listeners) {
            listeners.remove(cancellable);
        }
    }

    /**
     * Add executor into this group.
     */
    public synchronized void addExecutor(ExecutorSupport executor) {
        assert executed == false;
        executor.joinGroup(this);
        executors.add(executor);
    }

    /**
     * Asynchronously executes all added executors. Executors
     * are grouped according to CVSRoot and serialized in
     * particular ClientRuntime (thread) queue.
     *
     * <p>Do not call {@link ExecutorSupport#execute} if you
     * use grouping.
     */
    public void execute() {
        synchronized(this) {
            executed = true;
        }
        Iterator it = executors.iterator();
        while (it.hasNext()) {
            ExecutorSupport support = (ExecutorSupport) it.next();
            support.execute();
        }
    }


    void increaseDataCounter(long bytes) {
        dataCounter += bytes;
        progressHandle.progress("" + name + " " + format(dataCounter));
    }

    private static String format(long counter) {
        if (counter < 1024*16) {
            return "" + counter + " bytes";
        }
        counter /= 1024;
        return "" + counter + " kbytes";

        // do not go to megabytes as user want to see CHANGING number
        // it can be solved by average speed in last 5sec, as it drops to zero
        // something is wrong
    }


}
