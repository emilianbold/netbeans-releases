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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.openide.nodes;

import java.awt.EventQueue;
import java.lang.Thread;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.openide.util.RequestProcessor;

/**
 * Children object which creates its keys on a background thread.  To use,
 * implement AsynchChildren.Provider and pass that to the constructor.
 *
 * @author Tim Boudreau
 */
final class AsynchChildren <T> extends Children.Keys <Object> implements 
                                                          ChildFactory.Observer, 
                                                          Runnable {
    private final ChildFactory<T> factory;
    private final RequestProcessor.Task task;
    private final RequestProcessor PROC = new RequestProcessor("Asynch " //NOI18N
             + "children creator " + this, Thread.NORM_PRIORITY, true); //NOI18N
    /**
     * Create a new AsyncChildren instance with the passed provider object
     * which will manufacture key objects and nodes.
     * @param provider An object which can provide a list of keys and make
     *        Nodes for them
     */ 
    AsynchChildren(ChildFactory<T> factory) {
        factory.setObserver (this);
        this.factory = factory;
        task = PROC.create(this, true);
    }
    
    volatile boolean initialized = false;
    protected void addNotify() {
        if ((!initialized && task.isFinished()) || cancelled) {
            cancelled = false;
            Node n = factory.getWaitNode();
            if (n != null) {
                setKeys (new Object[] { n });
            }
            task.schedule(0);
        }
    }
    
    protected void removeNotify() {
        cancelled = true;
        task.cancel();
        initialized = false;
        setKeys (Collections.<Object>emptyList());
    }
    
    /**
     * Notify this AsynchChildren that it should reconstruct its children,
     * calling <code>provider.asynchCreateKeys()</code> and setting the
     * keys to that.  Call this method if the list of child objects is known
     * or likely to have changed.
     * @param immediate If true, the keys are updated synchronously from the
     *  calling thread.  Set this to true only if you know that updating
     *  the keys will <i>not</i> be an expensive or time-consuming operation.
     */ 
    public void refresh(boolean immediate) {
        immediate &= !EventQueue.isDispatchThread();
        if (immediate) {
            boolean done;
            List <T> keys = new LinkedList <T> ();
            do {
                done = factory.createKeys(keys);
            } while (!done);
            setKeys (keys);
        } else {
            task.schedule (0);
        }
    }
    
    public Node[] getNodes(boolean optimalResult) {
        if (optimalResult) {
            task.waitFinished();
        }
        return super.getNodes();
    }
    
    @SuppressWarnings("unchecked") //NOI18N
    protected Node[] createNodes(Object key) {
        if (factory.isWaitNode(key)) {
            return new Node[] { (Node) key };
        } else {
            return factory.createNodesForKey ((T) key);
        }
    }

    volatile boolean cancelled = false;
    public void run() {
        if (Thread.interrupted()) {
            setKeys (Collections.<T>emptyList());
            return;
        }
        List <T> keys = new LinkedList <T> ();
        boolean done;
        do {
            done = factory.createKeys (keys);
            if (cancelled || Thread.interrupted()) {
                setKeys (Collections.<T>emptyList());
                return;
            }
            setKeys (new LinkedList <T> (keys));
        } while (!done && !Thread.interrupted());
        initialized = done;
    }
}
