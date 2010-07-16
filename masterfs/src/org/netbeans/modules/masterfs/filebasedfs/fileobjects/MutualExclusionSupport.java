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

package org.netbeans.modules.masterfs.filebasedfs.fileobjects;


import org.openide.util.WeakSet;

import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.masterfs.filebasedfs.utils.FSException;

public final class MutualExclusionSupport {
    private static final MutualExclusionSupport DEFAULT = new MutualExclusionSupport();
    private static final Map exclusive = Collections.synchronizedMap(new WeakHashMap());
    private static final Map shared = Collections.synchronizedMap(new WeakHashMap());

    public static MutualExclusionSupport getDefault() {
        return MutualExclusionSupport.DEFAULT;
    }

    private MutualExclusionSupport() {
    }

    public synchronized Closeable addResource(final Object key, final boolean isShared) throws IOException {
        boolean isInUse = true;        
        final Map unexpected = (isShared) ? MutualExclusionSupport.exclusive : MutualExclusionSupport.shared;
        final Map expected = (isShared) ? MutualExclusionSupport.shared : MutualExclusionSupport.exclusive;

        final WeakSet unexpectedCounter = (WeakSet) unexpected.get(key);
        WeakSet expectedCounter = (WeakSet) expected.get(key);;

        for (int i = 0; i < 10 && isInUse; i++) {
            isInUse = unexpectedCounter != null && unexpectedCounter.size() > 0;

            if (!isInUse) {            
                if (expectedCounter == null) {
                    expectedCounter = new WeakSet();
                    expected.put(key, expectedCounter);
                }
                isInUse = !isShared && expectedCounter.size() > 0;            
            }
            
            if (isInUse) {
                try {
                    wait(200);
                } catch (InterruptedException e) {
                    break;
                }
            }
        }

        if (isInUse) {
            if (isShared) {
                FSException.io("EXC_CannotGetSharedAccess", key.toString()); // NOI18N        
            } else {
                FSException.io("EXC_CannotGetExclusiveAccess", key.toString()); // NOI18N        
            }
        }


        final Closeable retVal = new Closeable(key, isShared);
        expectedCounter.add(retVal);
        return retVal;
    }

    private synchronized void removeResource(final Object key, final Object value, final boolean isShared) {
        final Map expected = (isShared) ? MutualExclusionSupport.shared : MutualExclusionSupport.exclusive;

        final WeakSet expectedCounter = (WeakSet) expected.get(key);
        if (expectedCounter != null) {
            expectedCounter.remove(value);
        }
    }

    synchronized boolean isBeingWritten(FileObj file) {
        final WeakSet counter = (WeakSet) exclusive.get(file);
        return counter != null && !counter.isEmpty();
    }


    public final class Closeable {
        private final boolean isShared;
        private final Reference keyRef;
        private boolean isClosed = false;

        private Closeable(final Object key, final boolean isShared) {
            this.isShared = isShared;
            this.keyRef = new WeakReference(key);
        }


        public final void close() {
            if (!isClosed()) {
                isClosed = true;
                final Object key = keyRef.get();
                if (key != null) {
                    removeResource(key, this, isShared);
                }
            }
        }

        public final boolean isClosed() {
            return isClosed;
        }
    }
}
