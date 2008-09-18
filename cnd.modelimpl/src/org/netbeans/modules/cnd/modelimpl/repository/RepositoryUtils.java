/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.cnd.modelimpl.repository;

import java.util.ArrayList;
import java.util.Collection;
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
public final class RepositoryUtils {
    
    private static final boolean TRACE_REPOSITORY_ACCESS = DebugUtils.getBoolean("cnd.modelimpl.trace.repository", false);
    private static final Repository repository = RepositoryAccessor.getRepository();

    /**
     * the version of the persistency mechanism
     */
    private static int CURRENT_VERSION_OF_PERSISTENCY = 46;
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
        Persistent out = repository.tryGet(key);
        if (TRACE_REPOSITORY_ACCESS) {
            System.err.printf("%d:trying key %s got %s", nextIndex(), key, out);
        }
        return out;
    }
    
    public static Persistent get(Key key) {
        assert key != null;
        if (TRACE_REPOSITORY_ACCESS) {
            long time = System.currentTimeMillis();
            int index = nextIndex();
            System.err.println(index + ":getting key " + key);
            Persistent out = repository.get(key);
            time = System.currentTimeMillis() - time;
            System.err.println(index + ":got in " + time + "ms the key " + key);
            return out;
        }
        return repository.get(key);
    }
    
    private static synchronized int nextIndex() {
        return counter++;
    }
    
    public static void remove(CsmUID uid) {
        Key key = UIDtoKey(uid);
        if (key != null) {
            if (TRACE_REPOSITORY_ACCESS) {
                long time = System.currentTimeMillis();
                int index = nextIndex();
                System.err.println(index + ":removing key " + key);
                if (!TraceFlags.SAFE_REPOSITORY_ACCESS) {
                    repository.remove(key);
                }
                time = System.currentTimeMillis() - time;
                System.err.println(index + ":removed in " + time + "ms the key " + key);
                return;
            }
            if (!TraceFlags.SAFE_REPOSITORY_ACCESS) {
                repository.remove(key);
            }
        }
    }
    
    public static void remove(Collection<? extends CsmUID> uids) {
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
            assert uid != null;
            Key key = UIDtoKey(uid);
	    put(key, (Persistent) csmObj);
        }
        return uid;
    }    
    
    public static void put(Key key, Persistent obj) {
	    if (key != null) {
            if (TRACE_REPOSITORY_ACCESS) {
                long time = System.currentTimeMillis();
                int index = nextIndex();
                System.err.println(index + ":putting key " + key);
                repository.put(key, obj);
                // A workaround for #131701
                if( key instanceof FileKey ) {
                    repository.hang(key, obj);
                }
                time = System.currentTimeMillis() - time;
                System.err.println(index + ":put in " + time + "ms the key " + key);
                return;
            }
            repository.put(key, obj);
            // A workaround for #131701
            if( key instanceof FileKey ) {
                repository.hang(key, obj);
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
            if (TRACE_REPOSITORY_ACCESS) {
                long time = System.currentTimeMillis();
                int index = nextIndex();
                System.err.println(index + ":hanging key " + key);
                repository.hang(key, obj);
                time = System.currentTimeMillis() - time;
                System.err.println(index + ":hung in " + time + "ms the key " + key);
                return;
            }
            repository.hang(key, obj);
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
    
    public static CharSequence getUnitName(CsmUID uid) {
        Key key = UIDtoKey(uid);
        assert key != null;
        CharSequence unitName = key.getUnit();

        return unitName;
    }
    
    public static void startup() {
    	repository.startup(CURRENT_VERSION_OF_PERSISTENCY);
        repository.unregisterRepositoryListener(RepositoryListenerImpl.instance());
        repository.registerRepositoryListener(RepositoryListenerImpl.instance());
    }
    
    public static void shutdown() {
        // we intentionally do not unregister listener here since it will be automatically
        // unregistered as soon as shutdown (which is async.) finishes
        repository.shutdown();
    }
    
    public static void cleanCashes() {
        repository.cleanCaches();
    }

    public static void closeUnit(CsmUID uid, Set<String> requiredUnits, boolean cleanRepository) {
        closeUnit(UIDtoKey(uid), requiredUnits, cleanRepository);
    }
    
    public static void closeUnit(String unitName,  Set<String> requiredUnits) {
        closeUnit(unitName, requiredUnits, ! TraceFlags.PERSISTENT_REPOSITORY);
    }

    public static void closeUnit(String unitName,  Set<String> requiredUnits, boolean cleanRepository) {
        RepositoryListenerImpl.instance().onExplicitClose(unitName);
        repository.closeUnit(unitName, cleanRepository, requiredUnits);
    }
    
    public static void closeUnit(Key key, Set<String> requiredUnits) {
        closeUnit(key, requiredUnits, ! TraceFlags.PERSISTENT_REPOSITORY);
    }
    
    public static void closeUnit(Key key, Set<String> requiredUnits, boolean cleanRepository) {
        assert key != null;
        repository.closeUnit(key.getUnit().toString(), cleanRepository, requiredUnits);
    }
    
    public static void onProjectDeleted(NativeProject nativeProject) {
        Key key = KeyUtilities.createProjectKey(nativeProject);
        repository.removeUnit(key.getUnit().toString());
    }

    public static void openUnit(ProjectBase project) {
        CsmUID uid = project.getUID();
        assert uid != null;
        Key key = UIDtoKey(uid);
        openUnit(key);
    }
    
    public static void openUnit(Key key) {
	openUnit(key.getUnitId(), key.getUnit().toString());
    }
    
    private static void openUnit(int unitId, String unitName) {
        // TODO explicit open should be called here:
        RepositoryListenerImpl.instance().onExplicitOpen(unitName);
        repository.openUnit(unitId, unitName);
    }
    
    public static void unregisterRepositoryListener(RepositoryListener listener) {
        repository.unregisterRepositoryListener(listener);
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

