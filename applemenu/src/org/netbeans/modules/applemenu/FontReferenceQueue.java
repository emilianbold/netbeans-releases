/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
