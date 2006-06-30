/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
    }

    /**
     * This one is blocking. It returns when all bar
     * executors and action finish (successfuly or fail).
     */
    public void execute() {
        group.enqueued(null, this);
        ExecutorSupport.wait(bar);
        if (action != null) {
            action.run();
        }
        group.finished(null, this);
    }
}
