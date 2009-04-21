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
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.modelimpl.csm.core.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmUID;
import org.netbeans.modules.cnd.api.project.NativeProject;
import org.netbeans.modules.cnd.apt.debug.DebugUtils;
import org.netbeans.modules.cnd.modelimpl.csm.core.ProjectBase;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;
import org.netbeans.modules.cnd.modelimpl.uid.UIDProviderIml;
import org.netbeans.modules.cnd.repository.api.Repository;
import org.netbeans.modules.cnd.repository.api.RepositoryAccessor;
import org.netbeans.modules.cnd.repository.spi.Key;
import org.netbeans.modules.cnd.repository.spi.Persistent;
import org.netbeans.modules.cnd.repository.spi.RepositoryListener;
import org.netbeans.modules.cnd.utils.CndUtils;

/**
 *
 * @author Vladimir Voskresensky
 */
public final class RepositoryUtils {

    private static final boolean TRACE_ARGS = CndUtils.getBoolean("cnd.repository.trace.args", false); //NOI18N;
    private static final boolean TRACE_REPOSITORY_ACCESS = TRACE_ARGS || DebugUtils.getBoolean("cnd.modelimpl.trace.repository", false);
    private static final Repository repository = RepositoryAccessor.getRepository();
    /**
     * the version of the persistency mechanism
     */
    private static int CURRENT_VERSION_OF_PERSISTENCY = 69;

    /** Creates a new instance of RepositoryUtils */
    private RepositoryUtils() {
    }
    ////////////////////////////////////////////////////////////////////////////
    // repository access wrappers
    private static volatile int counter = 0;

    public static <T> T get(CsmUID<T> uid) {
        Key key = UIDtoKey(uid);
        Persistent obj = get(key);
        assert obj == null || (obj instanceof CsmIdentifiable) : "unexpected object with class " + obj.getClass() + obj; // NOI18N
        // we are sure in type, because of uid type
        @SuppressWarnings("unchecked")
        T out = (T)obj;
        return out;
    }

    public static Persistent tryGet(Key key) {
        assert key != null;
        Persistent out = repository.tryGet(key);
        if (TRACE_REPOSITORY_ACCESS && isTracingKey(key)) {
            System.err.printf("%d:trying key %s got %s", nextIndex(), key, out);
        }
        return out;
    }

    public static Persistent get(Key key) {
        assert key != null;
        if (TRACE_REPOSITORY_ACCESS && isTracingKey(key)) {
            long time = System.currentTimeMillis();
            int index = nextIndex();
            System.err.println(index + ": " + System.identityHashCode(key) + "@getting key " + key);
            Persistent out = repository.get(key);
            time = System.currentTimeMillis() - time;
            System.err.println(index + ": " + System.identityHashCode(key) + "@got" + (out == null ? " - NULL":"") + " in " + time + "ms the key " + key);
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
            if (TRACE_REPOSITORY_ACCESS && isTracingKey(key)) {
                long time = System.currentTimeMillis();
                int index = nextIndex();
                System.err.println(index + ": " + System.identityHashCode(key) + "@removing key " + key);
                if (!TraceFlags.SAFE_REPOSITORY_ACCESS) {
                    repository.remove(key);
                }
                time = System.currentTimeMillis() - time;
                System.err.println(index + ": " + System.identityHashCode(key) + "@removed in " + time + "ms the key " + key);
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

    public static <T> CsmUID<T> put(T csmObj) {
        CsmUID<T> uid = null;
        if (csmObj != null) {
            // during put we suppress check for null
            uid = UIDProviderIml.get(csmObj, false);
            assert uid != null;
            Key key = UIDtoKey(uid);
            put(key, (Persistent) csmObj);
            if (!((csmObj instanceof CsmNamespace)||(csmObj instanceof CsmProject))){
                assert uid.getObject() != null;
            }
        }
        return uid;
    }

    public static void put(Key key, Persistent obj) {
        if (key != null) {
            if (TRACE_REPOSITORY_ACCESS && isTracingKey(key)) {
                long time = System.currentTimeMillis();
                int index = nextIndex();
                System.err.println(index + ": " + System.identityHashCode(key) + "@putting key " + key);
                repository.put(key, obj);
                time = System.currentTimeMillis() - time;
                System.err.println(index + ": " + System.identityHashCode(key) + "@put in " + time + "ms the key " + key);
                return;
            }
            repository.put(key, obj);
        }
    }

    public static void hang(Object csmObj) {
        CsmUID uid = null;
        if (csmObj != null) {
            // during hang we suppress check for null
            uid = UIDProviderIml.get(csmObj, false);
            assert uid != null;
            Key key = UIDtoKey(uid);
            hang(key, (Persistent) csmObj);
            if (!((csmObj instanceof CsmNamespace)||(csmObj instanceof CsmProject))){
                assert uid.getObject() != null;
            }
        }
    }

    public static void hang(Key key, Persistent obj) {
        if (key != null) {
            if (TRACE_REPOSITORY_ACCESS && isTracingKey(key)) {
                long time = System.currentTimeMillis();
                int index = nextIndex();
                System.err.println(index + ": " + System.identityHashCode(key) + "@hanging key " + key);
                repository.hang(key, obj);
                time = System.currentTimeMillis() - time;
                System.err.println(index + ": " + System.identityHashCode(key) + "@hung in " + time + "ms the key " + key);
                return;
            }
            repository.hang(key, obj);
        }
    }

    public static <T> Collection<CsmUID<T>> put(Collection<T> decls) {
        assert decls != null;
        List<CsmUID<T>> uids = new ArrayList<CsmUID<T>>(decls.size());
        for (T decl : decls) {
            if (decl instanceof CsmIdentifiable) {
                CsmUID<T> uid = put(decl);
                uids.add(uid);
            }
        }
        return uids;
    }

    public static <T extends CsmDeclaration> void setSelfUIDs(Collection<T> decls) {
        assert decls != null;
        for (T decl : decls) {
            org.netbeans.modules.cnd.modelimpl.csm.core.Utils.setSelfUID(decl);
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    //
    public static Key UIDtoKey(CsmUID uid) {
        if (uid instanceof KeyHolder) {
            return ((KeyHolder) uid).getKey();
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

    public static void debugClear() {
        repository.debugClear();
    }

    public static void closeUnit(CsmUID uid, Set<String> requiredUnits, boolean cleanRepository) {
        closeUnit(UIDtoKey(uid), requiredUnits, cleanRepository);
    }

    public static void closeUnit(String unitName, Set<String> requiredUnits) {
        closeUnit(unitName, requiredUnits, !TraceFlags.PERSISTENT_REPOSITORY);
    }

    public static void closeUnit(String unitName, Set<String> requiredUnits, boolean cleanRepository) {
        RepositoryListenerImpl.instance().onExplicitClose(unitName);
        repository.closeUnit(unitName, cleanRepository, requiredUnits);
    }

    public static void closeUnit(Key key, Set<String> requiredUnits) {
        closeUnit(key, requiredUnits, !TraceFlags.PERSISTENT_REPOSITORY);
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

    private static boolean isTracingKey(Key key) {
        if (TRACE_ARGS) {
            if (key.getDepth() == 3 &&
                    ("argc".contentEquals(key.getAt(2)) || // NOI18N
                    "main".contentEquals(key.getAt(2)))) { // NOI18N
                return true;
            }
            return false;
        } else {
            return true;
        }
    }

}

