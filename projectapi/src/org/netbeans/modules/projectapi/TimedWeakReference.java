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

package org.netbeans.modules.projectapi;

// XXX COPIED from org.openide.util w/ changes:
//     weak -> soft
//     timeout
//     removed map key functionality

import java.lang.ref.WeakReference;
import org.openide.util.RequestProcessor;
import org.openide.util.Utilities;

/**
 * A weak reference which is held strongly for a while after last access.
 * Lifecycle:
 * <ol>
 * <li>Created. Referent held strongly. A task is scheduled into the request
 *     processor for some time in the future (currently 15 seconds).</li>
 * <li>Expired. After the timeout, the reference switches to a normal weak
 *     reference.</li>
 * <li>Touched. If the value is accessed before it is garbage collected,
 *     whether the reference is expired or not, the reference is "touched".
 *     This means that the referent is again held strongly and the timeout
 *     is started from scratch.</li>
 * <li>Dead. If after expiry there is no access before the next full GC cycle,
 *     the GC algorithm may reclaim the reference. In this case the reference
 *     of course dies.</li>
 * </ol>
 * @author Jesse Glick
 */
public final class TimedWeakReference<T> extends WeakReference<T> implements Runnable {
    
    public static int TIMEOUT = 15000;
    
    private static final RequestProcessor RP = new RequestProcessor("TimedWeakReference"); // NOI18N
    
    private RequestProcessor.Task task;
    
    private T o;
    
    /** Time when the object was last time touched */
    private long touched;
    
    /**
     * Create a weak reference with timeout.
     * @param o the referent
     */
    public TimedWeakReference(T o) {
        super(o, Utilities.activeReferenceQueue());
        this.o = o;
        task = RP.create(this);
        task.schedule(TIMEOUT);
    }
    
    public synchronized void run() {
        if (o != null) {
            //System.err.println("Expire " + k);
            // how long we've really been idle
            long unused  = System.currentTimeMillis() - touched;
            if (unused > TIMEOUT / 2) {
                o = null;
                touched = 0;
            } else {
                task.schedule(TIMEOUT - (int) unused);
            }
        }
    }
    
    public synchronized T get() {
        if (o == null) {
            o = super.get();
        }
        if (o != null) {
            // touch me
            //System.err.println("Touch " + k);
            if (touched == 0) {
                task.schedule(TIMEOUT);
            } 
            touched = System.currentTimeMillis();
            return o;
        } else {
            return null;
        }
    }
    
}
