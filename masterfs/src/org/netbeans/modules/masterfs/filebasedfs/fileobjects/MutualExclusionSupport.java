/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
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
