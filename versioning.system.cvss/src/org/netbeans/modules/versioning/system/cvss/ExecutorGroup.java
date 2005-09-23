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

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * Context that carry data shared by several chained
 * executors.
 *
 * <p>It allows to create shared progress, logging
 * and cancelling.
 *
 * @author Petr Kuzel
 */
public final class ExecutorGroup {

    private final String name;
    private boolean ready = true;
    private int executorsToFinish;
    private boolean cancelled;
    private List listeners = new ArrayList(2);

    public ExecutorGroup(String displayName, int executors) {
        name = displayName;
        executorsToFinish = executors;
    }

    /**
     * Defines group display name.
     */
    public String getDisplayName() {
        return name;
    }

    boolean start() {
        boolean wasReady = ready;
        ready = false;
        return wasReady;
    }

    boolean finished() {
        return (--executorsToFinish) == 0;
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
}
