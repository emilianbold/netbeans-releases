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
import java.util.TreeMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.netbeans.modules.cnd.apt.support.APTHandlersSupport;
import org.netbeans.modules.cnd.apt.support.APTMacro;
import org.netbeans.modules.cnd.apt.support.APTPreprocHandler;
import org.netbeans.modules.cnd.modelimpl.debug.TraceFlags;

/**
 *
 * @author Alexander Simon
 */
/*package-local*/ class FileStateCache {
    private static final boolean cacheStates = TraceFlags.CACHE_FILE_STATE;
    private static final int CACHE_SIZE = 10;
    private static final int MAX_KEY_SIZE = 100;
    //private static int stateCacheAttempt = 0;
    //private static int stateCacheSuccessAttempt = 0;
    private Map<String, Value> stateCache = new LinkedHashMap<String, Value>();
    private final ReadWriteLock stateCacheLock = new ReentrantReadWriteLock();
    private final FileImpl file;


    /*package-local*/ FileStateCache(FileImpl file){
        this.file = file;
    }
    void cacheVisitedState(APTPreprocHandler.State inputState, APTPreprocHandler outputHandler) {
        if (cacheStates) {
            stateCacheLock.writeLock().lock();
            try {
                if ((stateCache.isEmpty() || APTHandlersSupport.getIncludeStackDepth(inputState) == 1) && isCacheableState(inputState)) {
                    if (stateCache.size() == CACHE_SIZE) {
                        int min = Integer.MAX_VALUE;
                        String key = null;
                        for (Map.Entry<String, Value> entry : stateCache.entrySet()){
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
                    Map<CharSequence, APTMacro> map = APTHandlersSupport.extractMacroMap(inputState);
                    stateCache.put(createKey(map), new Value(outputHandler.getState()));
                }
            } finally {
                stateCacheLock.writeLock().unlock();
            }
        }
    }

    /*package-local*/ APTPreprocHandler.State getCachedVisitedState(APTPreprocHandler.State inputState) {
        APTPreprocHandler.State res = null;
        if (cacheStates) {
            //stateCacheAttempt++;
            stateCacheLock.readLock().lock();
            try {
                if (isCacheableState(inputState)) {
                    Map<CharSequence, APTMacro> map = APTHandlersSupport.extractMacroMap(inputState);
                    Value value = stateCache.get(createKey(map));
                    if (value != null) {
                        res = value.value.get();
                        value.count++;
                    }
                }
            } finally {
                stateCacheLock.readLock().unlock();
            }
            //if (res != null) {
            //    stateCacheSuccessAttempt++;
            //    System.err.println("State Cache Attempt="+stateCacheAttempt+" successful="+stateCacheSuccessAttempt+" cache size="+stateCache.size()+" in file "+file.getName());
            //}
        }
        return res;
    }

    /*package-local*/ void clearStateCache() {
        if (cacheStates) {
            try {
                stateCacheLock.writeLock().lock();
                stateCache.clear();
            } finally {
                stateCacheLock.writeLock().unlock();
            }
        }
    }

    private String createKey(Map<CharSequence, APTMacro> map){
        TreeMap<CharSequence, APTMacro> tree = new TreeMap<CharSequence, APTMacro>(map);
        StringBuilder buf = new StringBuilder();
        for(Map.Entry<CharSequence, APTMacro> entry : tree.entrySet()){
            buf.append((char)entry.getValue().getKind().ordinal());
            buf.append(entry.getKey());
            buf.append('=');
            buf.append(entry.getValue().getBody());
            buf.append(';');
        }
        return buf.toString();
    }

    private boolean isCacheableState(APTPreprocHandler.State inputState) {
        return APTHandlersSupport.getMacroSize(inputState) < MAX_KEY_SIZE;
    }
    
    private static class Value {
        private final SoftReference<APTPreprocHandler.State> value;
        private int count;
        private Value(APTPreprocHandler.State value){
            this.value = new SoftReference<APTPreprocHandler.State>(value);
        }
    }
}
