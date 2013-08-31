/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.api.model.services;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import org.netbeans.api.annotations.common.NonNull;
import static org.netbeans.modules.cnd.api.model.services.CsmCacheManager.LOGGER;
import org.netbeans.modules.cnd.debug.CndTraceFlags;

/**
 * map-based implementation for CsmCacheManager.CsmCacheEntry.
 * @author Vladimir Voskresensky
 */
public final class CsmCacheMap implements CsmCacheManager.CsmClientCache {
    private final long initTime;
    private final Map<Object, Value> values;
    private final String name;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public CsmCacheMap(String name) {
        this(name, DEFAULT_INITIAL_CAPACITY);
    }

    public CsmCacheMap(String name, int initialCapacity) {
        this.name = name;
        this.values = new HashMap<Object, Value>(initialCapacity);
        this.initTime = System.currentTimeMillis();
    }

    public final Value get(@NonNull Object key) {
        if (key == null) {
            throw new NullPointerException();
        }        
        Value res = values.get(key);
        if (res instanceof TraceValue) {
            TraceValue out = (TraceValue)res;
            out.onCacheHit();
            LOGGER.log(Level.FINE, "HIT {0} (Hits {1}) {2}=>{3}\n", new Object[]{this.name, out.getHitsCount(), key, out.getResult()});
        }
        return res;
    }

    /**
     * Associates the specified value with the specified key in this map.
     *
     * @param key key with which the specified value is to be associated
     * @param value value to be associated with the specified key
     * @return the previous value associated with <tt>key</tt>, or
     * <tt>null</tt> if there was no mapping for <tt>key</tt>.
     */
    public final Value put(@NonNull Object key, Value value) {
        if (key == null) {
            throw new NullPointerException();
        }
        return values.put(key, value);
    }

    @Override
    public void cleanup() {
        if (CndTraceFlags.TRACE_CSM_CACHE || LOGGER.isLoggable(Level.FINE)) {
            int hits = 0;
            int savedTime = 0;
            int nullResolved = 0;
            for (Map.Entry<Object, Value> entry : values.entrySet()) {
                Value v = entry.getValue();
                if (v instanceof TraceValue) {
                    TraceValue value = (TraceValue) v;
                    hits += value.getHitsCount();
                    savedTime += value.getHitsCount() * value.getCalculationTime();
                    if (value.getResult() == null) {
                        nullResolved++;
                    }
                }
            }
            if (hits > 0) {
                long usedTime = System.currentTimeMillis() - initTime;
                LOGGER.log(Level.INFO, "{0}: HITS={1}, Used {2}ms, SavedTime={3}ms, Cached {4} Values (NULLs={5}) ({6})\n", new Object[]{name, hits, usedTime, savedTime, values.size(), nullResolved, Thread.currentThread().getName()});
            }
        }        
        values.clear();
    }
    
    @Override
    public String toString() {
        StringBuilder out = new StringBuilder();
        for (Map.Entry<Object, Value> entry : values.entrySet()) {
            out.append(entry.getKey()).append("=").append(entry.getValue()).append("\n"); // NOI18N
        }
        return out.toString();
    }
    
    public static Value toValue(Object cachedResult) {
        return toValue(cachedResult, 0);
    }
    
    public static Value toValue(Object cachedResult, long resultCalculationTime) {
        if (CndTraceFlags.TRACE_CSM_CACHE) {
            return new TraceValueImpl(cachedResult, resultCalculationTime);
        } else {
            return new ValueImpl(cachedResult);
        }
    }
    
    public interface Value {
        Object getResult();
    }
    
    public interface TraceValue extends Value {
        int onCacheHit();
        int getHitsCount();
        long getCalculationTime();
    }
    
    private static final class ValueImpl implements Value {
        private final Object cachedResult;
        private ValueImpl(Object cachedResult) {
            this.cachedResult = cachedResult;
        }

        @Override
        public String toString() {
            return "result=" + getResult(); // NOI18N
        }
    
        @Override
        public final Object getResult() {
            return cachedResult;
        }
    }
    
    private static final class TraceValueImpl implements TraceValue {
        private final Object cachedResult;
        private final long calculationTime;
        private int hits;

        private TraceValueImpl(Object cachedResult, long resultCalculationTime) {
            this.cachedResult = cachedResult;
            this.calculationTime = resultCalculationTime;
            this.hits = 0;
        }

        @Override
        public String toString() {
            String saved = "";// NOI18N
            if (hits > 0 && calculationTime > 0) {
                saved = ", saved=" + (hits*calculationTime) + "ms";// NOI18N
            }
            return "HITS=" + hits + ", resolveTime=" + calculationTime + saved + ", result=" + cachedResult; // NOI18N
        }

        @Override
        public Object getResult() {
            return cachedResult;
        }

        @Override
        public int onCacheHit() {
            return ++hits;
        }

        @Override
        public int getHitsCount() {
            return hits;
        }

        @Override
        public long getCalculationTime() {
            return calculationTime;
        }
    }
}
