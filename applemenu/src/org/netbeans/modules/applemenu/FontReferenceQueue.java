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

package org.netbeans.modules.applemenu;

import java.awt.Font;
import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.reflect.Field;

// #57664: Random crash on Mac OS X.  Apple JDK apparently has a bug in
// the native part of the font handling code.  Font instances seems to share
// native data structures which are released when the instances are finalized.
// Because the native data are shared, their premature release causes random
// JVM crashes.
//
// This evil hack forces Font.finalize() not to run.  The native font data
// are leaked but it's still better than total crash

class FontReferenceQueue extends ReferenceQueue {
    /**
     * Polls this queue to see if a reference object is available.  If one is
     * available without further delay then it is removed from the queue and
     * returned.  Otherwise this method immediately returns <tt>null</tt>.
     * 
     * @return  A reference object, if one was immediately available,
     *          otherwise <code>null</code>
     */
    public Reference poll() {
        Reference ref;
        
        for (;;) {
            ref = super.poll();
            if (ref == null)
                break;
            
            Object obj = ref.get();
            if (! (obj instanceof Font))
                break;
        }
        return ref;
    }

    /**
     * Removes the next reference object in this queue, blocking until either
     * one becomes available or the given timeout period expires.
     * 
     * <p> This method does not offer real-time guarantees: It schedules the
     * timeout as if by invoking the {@link Object#wait(long)} method.
     * 
     * @param  timeout  If positive, block for up <code>timeout</code>
     *                  milliseconds while waiting for a reference to be
     *                  added to this queue.  If zero, block indefinitely.
     * 
     * @return  A reference object, if one was available within the specified
     *          timeout period, otherwise <code>null</code>
     * 
     * @throws  IllegalArgumentException
     *          If the value of the timeout argument is negative
     * 
     * @throws  InterruptedException
     *          If the timeout wait is interrupted
     */
    public Reference remove(long timeout) throws IllegalArgumentException, InterruptedException {
        // timeout is not handled correctly, but good enough for our purposes
        
        Reference ref;
        
        for (;;) {
            ref = super.remove(timeout);
            if (ref == null)
                break;
            
            Object obj = ref.get();
            if (! (obj instanceof Font))
                break;
        }
        return ref;
    }
        
    static void install() {
        try {
            Class clzz = Class.forName("java.lang.ref.Finalizer");  // NOI18N
            Field fld = clzz.getDeclaredField("queue");  // NOI18N
            fld.setAccessible(true);
            fld.set(null, new FontReferenceQueue());
        } catch (NoClassDefFoundError ex) {
            // ex.printStackTrace();
        } catch (ClassNotFoundException ex) {
            // ex.printStackTrace();
        } catch (Exception ex) {
            // ex.printStackTrace();
        }
        
    }
}
