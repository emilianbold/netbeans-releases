/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2009 Sun Microsystems, Inc. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.modelimpl.csm.core;

import java.lang.ref.SoftReference;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport.StateKey;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 * @author Alexander Simon
 */
/*package-local*/ class FileStateCache {
    private static final boolean TRACE = false;
    private static final boolean cacheStates = TraceFlags.CACHE_FILE_STATE;
    private static final int CACHE_SIZE = 10;
    private static final int MAX_KEY_SIZE = 1000;
    private static int stateCacheAttempt = 0;
    private static int stateCacheSuccessAttempt = 0;
    private final Map<StateKey, Value> stateCache = new LinkedHashMap<StateKey, Value>();
    private final ReadWriteLock stateCacheLock = new ReentrantReadWriteLock();
    private final FileImpl file;


    /*package-local*/ FileStateCache(FileImpl file){
        this.file = file;
    }
    void cacheVisitedState(APTPreprocHandler.State inputState, APTPreprocHandler outputHandler, FilePreprocessorConditionState pcState) {
        if (cacheStates && inputState.isCompileContext()) {
            stateCacheLock.writeLock().lock();
            try {
                if ((stateCache.isEmpty() || APTHandlersSupport.getIncludeStackDepth(inputState) == 1) && isCacheableState(inputState)) {
                    if (stateCache.size() == CACHE_SIZE) {
                        int min = Integer.MAX_VALUE;
                        StateKey key = null;
                        for (Map.Entry<StateKey, Value> entry : stateCache.entrySet()){
                            if (entry.getValue().value.get() == null) {
                                key = entry.getKey();
                                break;
                            }
                            if (entry.getValue().count < min){
                                key = entry.getKey();
                                min = entry.getValue().count;
                            }
                        }
                        stateCache.remove(key);
                    }
                    stateCache.put(createKey(inputState), new Value(new PreprocessorStatePair(outputHandler.getState(), pcState)));
                }
            } finally {
                stateCacheLock.writeLock().unlock();
            }
        }
    }

    /*package-local*/ PreprocessorStatePair getCachedVisitedState(APTPreprocHandler.State inputState) {
        PreprocessorStatePair res = null;
        if (cacheStates && inputState.isCompileContext()) {
            if (TRACE) {stateCacheAttempt++;}
            stateCacheLock.readLock().lock();
            StateKey key = null;
            try {
                if (isCacheableState(inputState)) {
                    key = createKey(inputState);
                    Value value = stateCache.get(key);
                    if (value != null) {
                        res = value.value.get();
                        value.count++;
                    }
                }
            } finally {
                stateCacheLock.readLock().unlock();
            }
            if (TRACE && res != null) {
                stateCacheSuccessAttempt++;
                System.err.println("State Cache Attempt="+stateCacheAttempt+" successful="+stateCacheSuccessAttempt+" cache size="+stateCache.size()+" in file "+file.getName());
                System.err.println("    Key="+key);
                System.err.println("    Res="+createKey(res.state));
            }
        }
        return res;
    }

    /*package-local*/ void clearStateCache() {
        if (cacheStates) {
            try {
                stateCacheLock.writeLock().lock();
                stateCache.clear();
                if (TRACE) {
                   System.err.println("Clear State Cache in file "+file.getName());
                }
            } finally {
                stateCacheLock.writeLock().unlock();
            }
        }
    }

    private static StateKey createKey(APTPreprocHandler.State inputState){
        return APTHandlersSupport.getMacroMapID(inputState);
    }

    private boolean isCacheableState(APTPreprocHandler.State inputState) {
        //return !APTHandlersSupport.isEmptyActiveMacroMap(inputState);
        return true;//APTHandlersSupport.getMacroSize(inputState) < MAX_KEY_SIZE;
    }

    private static class Value {
        private final SoftReference<PreprocessorStatePair> value;
        private int count;
        private Value(PreprocessorStatePair value){
            this.value = new SoftReference<PreprocessorStatePair>(value);
        }
    }
}
