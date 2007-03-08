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
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;

/**
 *
 * @author Vladimir Voskresensky
 */
public class RepositoryUtils {
    
    private static final boolean TRACE_REPOSITORY_ACCESS = DebugUtils.getBoolean("cnd.modelimpl.trace.repository", false);
    /** Creates a new instance of RepositoryUtils */
    private RepositoryUtils() {
    }

    ////////////////////////////////////////////////////////////////////////////
    // repository access wrappers
    private static volatile int counter = 0;
    public static <T extends CsmIdentifiable> T get(CsmUID<T> uid) {
        Key key = UIDtoKey(uid);
        assert key != null;
        long time = 0;
        int index = 0;
        if (TRACE_REPOSITORY_ACCESS) {
            index = nextIndex();
            time = System.currentTimeMillis();
            System.err.println(index + ":getting key " + key);
        }
        Persistent out = RepositoryAccessor.getRepository().get(key);
        if (TRACE_REPOSITORY_ACCESS) {
            time = System.currentTimeMillis() - time;
            System.err.println(index + ":got in " + time + "ms the key " + key);
        }
        assert out == null || (out instanceof CsmIdentifiable);
        return (T)out;
    }
    
    private static synchronized int nextIndex() {
        return counter++;
    }
    
    public static void remove(CsmUID uid) {
        Key key = UIDtoKey(uid);
        if (key != null) {
            long time = 0;
            int index = 0;
            if (TRACE_REPOSITORY_ACCESS) {
                index = nextIndex();
                time = System.currentTimeMillis();
                System.err.println(index + ":removing key " + key);
            }            
            RepositoryAccessor.getRepository().remove(key);
            if (TRACE_REPOSITORY_ACCESS) {
                time = System.currentTimeMillis() - time;
                System.err.println(index + ":removed in " + time + "ms the key " + key);
            }            
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
                long time = 0;
                int index = 0;
                if (TRACE_REPOSITORY_ACCESS) {
                    index = nextIndex();
                    time = System.currentTimeMillis();
                    System.err.println(index + ":putting key " + key);
                }                  
                RepositoryAccessor.getRepository().put(key, (Persistent)csmObj);
                if (TRACE_REPOSITORY_ACCESS) {
                    time = System.currentTimeMillis() - time;
                    System.err.println(index + ":put in " + time + "ms the key " + key);
                }                  
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
                long time = 0;
                int index = 0;
                if (TRACE_REPOSITORY_ACCESS) {
                    index = nextIndex();
                    time = System.currentTimeMillis();
                    System.err.println(index + ":hanging key " + key);
                }                  
                RepositoryAccessor.getRepository().hang(key, (Persistent)csmObj);
                if (TRACE_REPOSITORY_ACCESS) {
                    time = System.currentTimeMillis() - time;
                    System.err.println(index + ":hung in " + time + "ms the key " + key);
                }                 
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
    
    public static void shutdown() {
	RepositoryAccessor.getRepository().shutdown();
    }
    
}
