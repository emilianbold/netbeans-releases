/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
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

package org.netbeans.modules.cnd.modelimpl.uid;

import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.apt.utils.WeakSharedSet;

/**
 *
 * @author Vladimir Voskresensky
 */
public class UIDManager {
    private final WeakSharedSet<CsmUID> storage;
    private static final int UID_MANAGER_DEFAULT_CAPACITY=1024;
    
    private static final UIDManager instance = new UIDManager(UID_MANAGER_DEFAULT_CAPACITY);
    
    /** Creates a new instance of UIDManager */
    private UIDManager(int initialCapacity) {
        storage = new WeakSharedSet<CsmUID>(initialCapacity);
    }
    
    public static UIDManager instance() {
        return instance;
    }
    
    // we need exclusive copy of string => use "new String(String)" constructor
    private final String lock = new String("lock in UIDManager"); // NOI18N
    
    /**
     * returns shared uid instance equal to input one.
     *
     * @param uid - interested shared uid
     * @return the shared instance of uid
     * @exception NullPointerException If the <code>uid</code> parameter
     *                                 is <code>null</code>.
     */
    public final CsmUID getSharedUID(CsmUID uid) {
        if (uid == null) {
            throw new NullPointerException("null string is illegal to share"); // NOI18N
        }
        CsmUID outUID = null;
        synchronized (lock) {
            outUID = storage.addOrGet(uid);
        }
        assert (outUID != null);
        assert (outUID.equals(uid));
        return outUID;
    }
    
    public final void dispose() {
        storage.clear();
    }
}
