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
 * TimeThreshold parameter allows to keep only values which took big creation time and skip 
 * caching fast calculated values.
 */
public final class CsmCacheMap implements CsmCacheManager.CsmClientCache {
    private final long initTime;
    private final Map<Object, Value> values;
    private final String name;
    private final int timeThreshold;
    private static final int DEFAULT_INITIAL_CAPACITY = 16;

    public CsmCacheMap(String name) {
        this(name, 0, DEFAULT_INITIAL_CAPACITY);
    }

    public CsmCacheMap(String name, int timeThreshold) {
        this(name, timeThreshold, DEFAULT_INITIAL_CAPACITY);
    }
    
    public CsmCacheMap(String name, int timeThreshold, int initialCapacity) {
        this.name = name;
        this.values = new HashMap<Object, Value>(initialCapacity);
        this.initTime = System.currentTimeMillis();
        this.timeThreshold = timeThreshold;
    }

    public final Value get(@NonNull Object key) {
        if (key == null) {
            throw new NullPointerException();
        }        
        Value res = values.get(key);
        if (res instanceof TraceValue) {
            TraceValue out = (TraceValue)res;
            out.onCacheHit();
            if (LOGGER.isLoggable(Level.FINE)) {
                LOGGER.log(Level.FINE, "HIT {0} (Hits {1}) {2}=>{3}\n", new Object[]{this.name, out.getHitsCount(), key, out.getResult()});
            }
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
        if (value == null) {
            return values.put(key, null);
        } else {
            if (timeThreshold == 0 || !(value instanceof TimeConsumingValue) || 
                (((TimeConsumingValue)value).getCalculationTime() >= timeThreshold)) {
                return values.put(key, value);
            }
        }
        return null;
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
                    if (value.getCalculationTime() != Integer.MAX_VALUE) {
                        hits += value.getHitsCount();
                        savedTime += value.getHitsCount() * value.getCalculationTime();
                        if (value.getResult() == null) {
                            nullResolved++;
                        }
                    }
                }
            }
            if ((hits > 0 && savedTime > 0) || LOGGER.isLoggable(Level.FINE)) {
                long usedTime = System.currentTimeMillis() - initTime;
                LOGGER.log(Level.INFO, "{0}: HITS={1}, Used {2}ms, SavedTime={3}ms, Cached {4} Values (NULLs={5}) ([{6}]{7})\n", new Object[]{name, hits, usedTime, savedTime, values.size(), nullResolved, Thread.currentThread().getId(), Thread.currentThread().getName()});
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
    
    /**
     * wrap cachedResult object calculated during resultCalculationTime.
     * @param cachedResult result to wrap
     * @param resultCalculationTime time needed to re-create value.
     * use Integer.MAX_VALUE to make sure value is always kept by map
     * @return value ready to be put into map
     */
    public static Value toValue(Object cachedResult, long resultCalculationTime) {
        if (CndTraceFlags.TRACE_CSM_CACHE) {
            return new TraceValueImpl(cachedResult, resultCalculationTime);
        } else {
            return new ValueImpl(cachedResult, resultCalculationTime);
        }
    }
    
    /**
     * help method to get result from cache
     * @param cache map-based cache instance or null
     * @param key key to access value
     * @param found if non-null is passed, then on return contains true if 
     *  cache has result for passed key. Can be used if 
     * @return cached result associated with key or null. Check parameter found
     * to distinguished between absent cache and cached null object
     */
    public static Object getFromCache(CsmCacheMap cache, Object key, boolean[] found) {
        if (found != null) {
            found[0] = false;
        }
        Object result = null;
        CsmCacheMap.Value cacheValue = null;
        if (cache != null) {
            cacheValue = cache.get(key);
        }
        if (cacheValue != null) {
            if (found != null) {
                found[0] = true;
            }
            result = cacheValue.getResult();
        }
        return result;
    }
    
    public interface Value {
        Object getResult();
    }
    
    public interface TimeConsumingValue extends Value {
        long getCalculationTime();
    }
    
    public interface TraceValue extends TimeConsumingValue {
        int onCacheHit();
        int getHitsCount();
    }
    
    private static final class ValueImpl implements TimeConsumingValue {
        private final Object cachedResult;
        private final int calculationTime;
        private ValueImpl(Object cachedResult, long calculationTime) {
            this.cachedResult = cachedResult;
            if (calculationTime < Integer.MAX_VALUE) {
                this.calculationTime = (int)calculationTime;
            } else {
                this.calculationTime = Integer.MAX_VALUE;
            }
        }

        @Override
        public String toString() {
            return "resolveTime=" + calculationTime + "result=" + getResult(); // NOI18N
        }
    
        @Override
        public final Object getResult() {
            return cachedResult;
        }

        @Override
        public long getCalculationTime() {
            return calculationTime;
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
