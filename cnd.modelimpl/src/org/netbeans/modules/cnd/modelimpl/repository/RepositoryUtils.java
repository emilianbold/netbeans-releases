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
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;

/**
 *
 * @author Vladimir Voskresensky
 */
public class RepositoryUtils {
    
    private static final boolean TRACE_REPOSITORY_ACCESS = DebugUtils.getBoolean("cnd.modelimpl.trace.repository", false);

    /**
     * the version of the persistency mechanism
     */
    private static int CURRENT_VERSION_OF_PERSISTENCY = 12;
    /** Creates a new instance of RepositoryUtils */
    private RepositoryUtils() {
    }
    
    ////////////////////////////////////////////////////////////////////////////
    // repository access wrappers
    private static volatile int counter = 0;
    public static <T extends CsmIdentifiable> T get(CsmUID<T> uid) {
        Key key = UIDtoKey(uid);
        Persistent out = get(key);
        assert out == null || (out instanceof CsmIdentifiable);
        return (T)out;
    }
    
    public static Persistent tryGet(Key key) {
	assert key != null;
	Persistent out = RepositoryAccessor.getRepository().tryGet(key);
	if (TRACE_REPOSITORY_ACCESS) {
	    System.err.printf("%d:trying key %s got %s", nextIndex(), key, out);
	}
	return out;
    }
    
    public static Persistent get(Key key) {
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
	return out;
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
            if (!TraceFlags.SAFE_REPOSITORY_ACCESS) {
                RepositoryAccessor.getRepository().remove(key);
            }
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
	    put(key, (Persistent) csmObj);
        }
        return uid;
    }    
    
    public static void put(Key key, Persistent obj) {
	if (key != null) {
	    long time = 0;
	    int index = 0;
	    if (TRACE_REPOSITORY_ACCESS) {
		index = nextIndex();
		time = System.currentTimeMillis();
		System.err.println(index + ":putting key " + key);
	    }
	    RepositoryAccessor.getRepository().put(key, obj);
	    if (TRACE_REPOSITORY_ACCESS) {
		time = System.currentTimeMillis() - time;
		System.err.println(index + ":put in " + time + "ms the key " + key);
	    }
	}
    }
    
    public static void hang(CsmIdentifiable csmObj) {
        CsmUID uid = null;
        if (csmObj != null) {
            uid = csmObj.getUID();
            assert uid != null;
            Key key = UIDtoKey(uid);
	    hang(key, (Persistent)csmObj);
        }
    }
    
    public static void hang(Key key, Persistent obj) {
	if (key != null) {
	    long time = 0;
	    int index = 0;
	    if (TRACE_REPOSITORY_ACCESS) {
		index = nextIndex();
		time = System.currentTimeMillis();
		System.err.println(index + ":hanging key " + key);
	    }
	    RepositoryAccessor.getRepository().hang(key, obj);
	    if (TRACE_REPOSITORY_ACCESS) {
		time = System.currentTimeMillis() - time;
		System.err.println(index + ":hung in " + time + "ms the key " + key);
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
    public static Key UIDtoKey(CsmUID uid) {
        if (uid instanceof KeyHolder) {
            return ((KeyHolder)uid).getKey();
        } else {
            return null;
        }
    }
    
    public static String getUnitName(CsmUID uid) {
        Key key = UIDtoKey(uid);
        assert key != null;
        String unitName = key.getUnit();

        return unitName;
    }
    
    public static void startup() {
	Repository repository = RepositoryAccessor.getRepository();
	repository.startup(CURRENT_VERSION_OF_PERSISTENCY);
	repository.unregisterRepositoryListener(RepositoryListenerImpl.instance());
	repository.registerRepositoryListener(RepositoryListenerImpl.instance());
    }
    
    public static void shutdown() {
	// we intentionally do not unregister listener here since it will be automatically 
	// unregistered as soon as shutdown (which is async.) finishes
        RepositoryAccessor.getRepository().shutdown();
    }
    
    public static void cleanCashes() {
        RepositoryAccessor.getRepository().cleanCaches();
    }

    public static void closeUnit(CsmUID uid, Set<String> requiredUnits, boolean cleanRepository) {
        closeUnit(UIDtoKey(uid), requiredUnits, cleanRepository);
    }
    
    public static void closeUnit(String unitName,  Set<String> requiredUnits) {
	closeUnit(unitName, requiredUnits, TraceFlags.PERSISTENT_REPOSITORY);
    }

    public static void closeUnit(String unitName,  Set<String> requiredUnits, boolean cleanRepository) {
	RepositoryListenerImpl.instance().onExplicitClose(unitName);
        RepositoryAccessor.getRepository().closeUnit(unitName, cleanRepository, requiredUnits);
    }
    
    public static void closeUnit(Key key, Set<String> requiredUnits) {
	closeUnit(key, requiredUnits, TraceFlags.PERSISTENT_REPOSITORY);
    }
    
    public static void closeUnit(Key key, Set<String> requiredUnits, boolean cleanRepository) {
        assert key != null;
        String unitName = key.getUnit();
        RepositoryAccessor.getRepository().closeUnit(unitName, cleanRepository, requiredUnits);
    }
    
    public static void onProjectDeleted(NativeProject nativeProject) {
	Key key = KeyUtilities.createProjectKey(nativeProject.getProjectRoot());
	RepositoryAccessor.getRepository().removeUnit(key.getUnit());
    }

    public static void openUnit(ProjectBase project) {
	CsmUID uid = project.getUID();
	assert uid != null;
	Key key = UIDtoKey(uid);
	openUnit(key);
    }
    
    public static void openUnit(Key key) {
	openUnit(key.getUnit());
    }
    
    private static void openUnit(String unitName) {
	RepositoryListenerImpl.instance().onExplicitOpen(unitName);
	// TODO explicit open should be called here: 
	// RepositoryAccess.getRepository().open(unitName);
    }
    
    public static void unregisterRepositoryListener(RepositoryListener listener) {
        RepositoryAccessor.getRepository().unregisterRepositoryListener(listener);
    }    

    static int getUnitId(String unitName) {
        return RepositoryAccessor.getTranslator().getUnitId(unitName);
    }

    static String getUnitName(int unitIndex) {
        return RepositoryAccessor.getTranslator().getUnitName(unitIndex);
    }

    static int getFileIdByName(int unitId, String fileName) {
        return RepositoryAccessor.getTranslator().getFileIdByName(unitId, fileName);
    }

    static String getFileNameByIdSafe(int unitId, int fileId) {
        return RepositoryAccessor.getTranslator().getFileNameByIdSafe(unitId, fileId);
    }

    static String getFileNameById(int unitId, int fileId) {
        return RepositoryAccessor.getTranslator().getFileNameById(unitId, fileId);
    }

}

