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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.util.Collection;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.modelimpl.uid.KeyBasedUID;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 *
 * @author Vladimir Voskresensky
 */
public class RepositoryUtils {
    
    /** Creates a new instance of RepositoryUtils */
    private RepositoryUtils() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // repository access wrappers
    
    public static CsmIdentifiable get(CsmUID uid) {
        Key key = UIDtoKey(uid);
        Persistent out = RepositoryAccessor.getRepository().get(key);
        assert out == null || (out instanceof CsmIdentifiable);
        return (CsmIdentifiable)out;
    }

    public static void remove(CsmUID uid) {
        Key key = UIDtoKey(uid);
        RepositoryAccessor.getRepository().remove(key);
    }

    public static void remove(Collection<CsmUID> uids) {
        if (uids != null) {
            for (CsmUID uid : uids) {
                remove(uid);
            }
        }
    }

    public static CsmUID put(CsmIdentifiable csmObj) {
        CsmUID uid = null;
        if (csmObj != null) {
            uid = csmObj.getUID();
            Key key = UIDtoKey(uid);
            Persistent obj = (Persistent)csmObj;
            assert key != null;
            assert obj != null;
            RepositoryAccessor.getRepository().put(key, obj);
        }
        return uid;
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // 
    public static Key UIDtoKey(CsmUID uid) {
        if (uid != null) {
            return ((KeyBasedUID)uid).getKey();
        } else {
            return null;
        }
    }
    
}
