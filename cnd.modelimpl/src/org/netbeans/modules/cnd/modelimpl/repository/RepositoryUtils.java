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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
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
    
    public static <T extends CsmIdentifiable> T get(CsmUID<T> uid) {
        Key key = UIDtoKey(uid);
        assert key != null;
        Persistent out = RepositoryAccessor.getRepository().get(key);
        assert out == null || (out instanceof CsmIdentifiable);
        return (T)out;
    }

    public static void remove(CsmUID uid) {
        Key key = UIDtoKey(uid);
        if (key != null) {
            RepositoryAccessor.getRepository().remove(key);
        }
    }

    public static void remove(Collection<? extends CsmUID> uids) {
        if (uids != null) {
            for (CsmUID uid : uids) {
                remove(uid);
            }
            uids.clear();
        }
    }

    public static CsmUID put(CsmIdentifiable csmObj) {
        CsmUID uid = null;
        if (csmObj != null) {
            uid = csmObj.getUID();
            assert uid != null;
            Key key = UIDtoKey(uid);
            if (key != null) {
                RepositoryAccessor.getRepository().put(key, (Persistent)csmObj);
            }
        }
        return uid;
    }
    
    public static void hang(CsmIdentifiable csmObj) {
        CsmUID uid = null;
        if (csmObj != null) {
            uid = csmObj.getUID();
            assert uid != null;
            Key key = UIDtoKey(uid);
            if (key != null) {
                RepositoryAccessor.getRepository().hang(key, (Persistent)csmObj);
            }
        }
    }
    
    public static <T extends CsmOffsetableDeclaration> List<CsmUID<T>> put(List<T> decls) {
        assert decls != null;
        List<CsmUID<T>> uids = new ArrayList<CsmUID<T>>(decls.size());
        for (T decl: decls) {
            CsmUID<T> uid = put(decl);
            uids.add(uid);
        }
        return uids;
    }
        
    ////////////////////////////////////////////////////////////////////////////
    // 
    private static Key UIDtoKey(CsmUID uid) {
        if (uid instanceof KeyHolder) {
            return ((KeyHolder)uid).getKey();
        } else {
            return null;
        }
    }
}
