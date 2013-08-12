/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javascript2.editor.index;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import org.netbeans.modules.csl.api.Modifier;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsIndex {

    public static final String FIELD_BASE_NAME = "bn"; //NOI18N
    /**
     * In this field is in the lucene also coded, whether the object is anonymous (last char is 'A')
     * or normal object (last char is 'O'). If someone needs to access this field
     * directly, then has to be count with this.
     */
    public static final String FIELD_FQ_NAME = "fqn"; //NOI18N
    public static final String FIELD_OFFSET = "offset"; //NOI18N
    public static final String FIELD_ASSIGNMENTS = "assign"; //NOI18N
    public static final String FIELD_RETURN_TYPES = "return"; //NOI18N
    public static final String FIELD_PARAMETERS = "param"; //NOI18N
    public static final String FIELD_FLAG = "flag"; //NOI18N
    public static final String FIELD_ARRAY_TYPES = "array"; //NOI18N

    private static final String PROPERTIES_PATTERN = "\\.[^\\.]*[^" + IndexedElement.PARAMETER_POSTFIX + "]";
    
    @org.netbeans.api.annotations.common.SuppressWarnings("MS_MUTABLE_ARRAY")
    public static final String[] TERMS_BASIC_INFO = new String[] { FIELD_BASE_NAME, FIELD_FQ_NAME, FIELD_OFFSET,
        FIELD_RETURN_TYPES, FIELD_PARAMETERS, FIELD_FLAG, FIELD_ASSIGNMENTS, FIELD_ARRAY_TYPES};

    private static final Logger LOG = Logger.getLogger(JsIndex.class.getName());

    private static final ReentrantReadWriteLock LOCK = new ReentrantReadWriteLock();

    private static final Lock READ_LOCK = LOCK.readLock();

    private static final Lock WRITE_LOCK = LOCK.writeLock();

    private static final WeakHashMap<FileObject, JsIndex> INDEX_CACHE = new WeakHashMap<FileObject, JsIndex>();

    // empirical values (update if index is changed)
    private static final int MAX_ENTRIES_CACHE_INDEX_RESULT = 2000;

    private static final int MAX_CACHE_VALUE_SIZE = 1000000;

    private static final int AVERAGE_BASIC_INFO_SIZE = 60;

    // cache to keep latest index results. The cache is cleaned if a file is saved
    // or a file has to be reindexed due to an external change

    /* GuardedBy(LOCK) */
    private static final Map<CacheKey, SoftReference<CacheValue>> CACHE_INDEX_RESULT_SMALL = new LinkedHashMap<CacheKey, SoftReference<CacheValue>>(
            MAX_ENTRIES_CACHE_INDEX_RESULT + 1, 0.75F, true) {
        @Override
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_ENTRIES_CACHE_INDEX_RESULT;
        }
    };

    /* GuardedBy(LOCK) */
    private static final Map<CacheKey, SoftReference<CacheValue>> CACHE_INDEX_RESULT_LARGE = new LinkedHashMap<CacheKey, SoftReference<CacheValue>>(
            (MAX_ENTRIES_CACHE_INDEX_RESULT / 4) + 1, 0.75F, true) {
        @Override
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > (MAX_ENTRIES_CACHE_INDEX_RESULT / 4);
        }
    };

    private static final AtomicBoolean INDEX_CHANGED = new AtomicBoolean(true);

    private static final Map<StatsKey, StatsValue> QUERY_STATS = new HashMap<StatsKey, StatsValue>();

    /* GuardedBy(QUERY_STATS) */
    private static int cacheHit;

    /* GuardedBy(QUERY_STATS) */
    private static int cacheMiss;

    private final QuerySupport querySupport;

    private final boolean updateCache;

    private JsIndex(QuerySupport querySupport, boolean updateCache) {
        this.querySupport = querySupport;
        this.updateCache = updateCache;
    }

    public static JsIndex get(Collection<FileObject> roots) {
        // XXX no cache - is it needed?
        LOG.log(Level.FINE, "JsIndex for roots: {0}", roots); //NOI18N
        return new JsIndex(QuerySupportFactory.get(roots), false);
    }

    public static void changeInIndex() {
        INDEX_CHANGED.set(true);
    }

    public static JsIndex get(FileObject fo) {
        JsIndex index = INDEX_CACHE.get(fo);
        if (index == null) {
            LOG.log(Level.FINE, "Creating JsIndex for FileObject: {0}", fo); //NOI18N
            index = new JsIndex(QuerySupportFactory.get(fo), true);
            INDEX_CACHE.put(fo, index);
        }
        return index;
    }

    public Collection<? extends IndexResult> query(final String fieldName, final String fieldValue,
            final QuerySupport.Kind kind, final String... fieldsToLoad) {

        if (querySupport == null) {
            return Collections.<IndexResult>emptySet();
        }

        try {
            if (INDEX_CHANGED.get()) {
                WRITE_LOCK.lock();
                try {
                    CACHE_INDEX_RESULT_SMALL.clear();
                    CACHE_INDEX_RESULT_LARGE.clear();
                    //System.out.println("Cache cleared");
                    LOG.log(Level.FINEST, "Cache cleared");
                } finally {
                    WRITE_LOCK.unlock();
                }
                INDEX_CHANGED.set(false);
            }

            CacheKey key = new CacheKey(this, fieldName, fieldValue, kind);
            CacheValue value = getCachedValue(key, fieldsToLoad);

            if (value != null) {
                logStats(value.getResult(), true, fieldsToLoad);
                //System.out.println("Cache hit " + key + ": " + value.getResult().getClass().getName() + " " + value.getResult().size());
                return value.getResult();
            }

            Collection<? extends IndexResult> result = querySupport.query(
                    fieldName, fieldValue, kind, fieldsToLoad);
            if (updateCache) {
                WRITE_LOCK.lock();
                try {
                    value = getCachedValue(key, fieldsToLoad);
                    if (value != null) {
                        logStats(value.getResult(), false, fieldsToLoad);
                        //System.out.println("Lazy cache hit " + key + ": " + value.getResult().getClass().getName() + " " + value.getResult().size());
                        return value.getResult();
                    }

                    value = new CacheValue(fieldsToLoad, result);
                    if ((result.size() * AVERAGE_BASIC_INFO_SIZE) < MAX_CACHE_VALUE_SIZE) {
                        CACHE_INDEX_RESULT_SMALL.put(key, new SoftReference(value));
                    } else {
                        CACHE_INDEX_RESULT_LARGE.put(key, new SoftReference(value));
                    }
                    logStats(result, false, fieldsToLoad);
                    //System.out.println("Cache update " + key + ": " + value.getResult().getClass().getName() + " " + value.getResult().size());
                    return value.getResult();
                } finally {
                    WRITE_LOCK.unlock();
                }
            }

            logStats(result, false, fieldsToLoad);
            //System.out.println("No cache " + key + ": " + result.getClass().getName() + " " + result.size());
            return result;
        } catch (IOException ioe) {
            LOG.log(Level.WARNING, null, ioe);
        }

        return Collections.<IndexResult>emptySet();
    }

    public Collection<IndexedElement> getGlobalVar(String prefix) {
        prefix = prefix == null ? "" : prefix; //NOI18N
        ArrayList<IndexedElement> globals = new ArrayList<IndexedElement>();
        long start = System.currentTimeMillis();
        String indexPrefix = escapeRegExp(prefix) + "[^\\.]*[" + IndexedElement.OBJECT_POSFIX + "]";   //NOI18N
        Collection<? extends IndexResult> globalObjects = query(
                JsIndex.FIELD_FQ_NAME, indexPrefix, QuerySupport.Kind.REGEXP, TERMS_BASIC_INFO); //NOI18N
        for (IndexResult indexResult : globalObjects) {
            IndexedElement indexedElement = IndexedElement.create(indexResult);
            globals.add(indexedElement);
        }
        long end = System.currentTimeMillis();
        LOG.log(Level.FINE, "Obtaining globals from the index took: {0}", (end - start)); //NOI18N
        return globals;
    }

    private static CacheValue getCachedValue(CacheKey key, String... fieldsToLoad) {
        READ_LOCK.lock();
        try {
            CacheValue value = null;
            SoftReference<CacheValue> currentReference = CACHE_INDEX_RESULT_SMALL.get(key);
            if (currentReference != null) {
                value = currentReference.get();
            }
            if (value == null || !value.contains(fieldsToLoad)) {
                currentReference = CACHE_INDEX_RESULT_LARGE.get(key);
                if (currentReference != null) {
                    value = currentReference.get();
                }
                if (value == null || !value.contains(fieldsToLoad)) {
                    return null;
                } else {
                    return value;
                }
            } else {
                return value;
            }
        } finally {
            READ_LOCK.unlock();
        }
    }

    private static void logStats(Collection<? extends IndexResult> result, boolean hit, String... fieldsToLoad) {
        if (!LOG.isLoggable(Level.FINEST)) {
            return;
        }
        int size = 0;
        for (String field : fieldsToLoad) {
            for (IndexResult r : result) {
                String val = r.getValue(field);
                size += val == null ? 0 : val.length();
            }
        }

        synchronized (QUERY_STATS) {
            if (hit) {
                cacheHit++;
            } else {
                cacheMiss++;
            }

            StatsKey statsKey = new StatsKey(fieldsToLoad);
            StatsValue statsValue = QUERY_STATS.get(statsKey);
            if (statsValue == null) {
                QUERY_STATS.put(statsKey,
                        new StatsValue(1, result.size(), size));
            } else {
                QUERY_STATS.put(statsKey,
                        new StatsValue(statsValue.getRequests() + 1,
                            statsValue.getCount() + result.size(), statsValue.getSize() + size));
            }

            if ((cacheHit + cacheMiss) % 500 == 0) {
                LOG.log(Level.FINEST, "Cache hit: " + cacheHit + ", Cache miss: "
                        + cacheMiss + ", Ratio: " + (cacheHit  / cacheMiss));
                for (Map.Entry<StatsKey, StatsValue> entry : QUERY_STATS.entrySet()) {
                    LOG.log(Level.FINEST, entry.getKey() + ": " + entry.getValue());
                }
            }
        }
    }

    private static Collection<IndexedElement> getElementsByPrefix(String prefix, Collection<IndexedElement> items) {
        Collection<IndexedElement> result = new ArrayList<IndexedElement>();
        for (IndexedElement indexedElement : items) {
            if (indexedElement.getFQN().startsWith(prefix)) {
                result.add(indexedElement);
            }
        }
        return result;
    }

    public Collection <IndexedElement> getPropertiesWithPrefix(String fqn, String prexif) {
        return getElementsByPrefix(prexif, getProperties(fqn));
    }

    
    public Collection <IndexedElement> getProperties(String fqn) {
        return getProperties(fqn, 0, new ArrayList<String>());
    }

    private final int MAX_FIND_PROPERTIES_RECURSION = 15;
    
    private Collection <IndexedElement> getProperties(String fqn, int deepLevel, Collection<String> resolvedTypes) { 
        if (deepLevel > MAX_FIND_PROPERTIES_RECURSION) {
            return Collections.EMPTY_LIST;
        }
        Collection<IndexedElement> result = new ArrayList<IndexedElement>();
        if (!resolvedTypes.contains(fqn)) {
            resolvedTypes.add(fqn);
            deepLevel = deepLevel + 1;
            Collection<? extends IndexResult> results = findByFqn(fqn, JsIndex.FIELD_ASSIGNMENTS);
            for (IndexResult indexResult : results) {
                // find assignment to for the fqn
                Collection<TypeUsage> assignments = IndexedElement.getAssignments(indexResult);
                if (!assignments.isEmpty()) {
                    TypeUsage type = assignments.iterator().next();
                    if (!resolvedTypes.contains(type.getType())) {                    
                        result.addAll(getProperties(type.getType(), deepLevel, resolvedTypes));
                    }
                }
            }
            // find properties of the fqn
            String pattern = escapeRegExp(fqn) + PROPERTIES_PATTERN; //NOI18N
            results = query(
                    JsIndex.FIELD_FQ_NAME, pattern, QuerySupport.Kind.REGEXP, TERMS_BASIC_INFO); //NOI18N
            for (IndexResult indexResult : results) {
                IndexedElement property = IndexedElement.create(indexResult);
                if (!property.getModifiers().contains(Modifier.PRIVATE)) {
                    result.add(property);
                }
            }
        }
        return result;
    }

    public Collection<? extends IndexResult> findByFqn(String fqn, String... fields) {
        String pattern = escapeRegExp(fqn) + "."; // NOI18N
        Collection<? extends IndexResult> results = query(
                JsIndex.FIELD_FQ_NAME, pattern, QuerySupport.Kind.REGEXP, fields); //NOI18N

        return results;
    }
    
    private String escapeRegExp(String text) {
        return Pattern.quote(text);
    }

    private static class CacheKey {

        private final JsIndex index;

        private final String fieldName;

        private final String fieldValue;

        private final QuerySupport.Kind kind;

        public CacheKey(JsIndex index, String fieldName, String fieldValue, QuerySupport.Kind kind) {
            this.index = index;
            this.fieldName = fieldName;
            this.fieldValue = fieldValue;
            this.kind = kind;
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 41 * hash + (this.index != null ? this.index.hashCode() : 0);
            hash = 41 * hash + (this.fieldName != null ? this.fieldName.hashCode() : 0);
            hash = 41 * hash + (this.fieldValue != null ? this.fieldValue.hashCode() : 0);
            hash = 41 * hash + (this.kind != null ? this.kind.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final CacheKey other = (CacheKey) obj;
            if (this.index != other.index && (this.index == null || !this.index.equals(other.index))) {
                return false;
            }
            if ((this.fieldName == null) ? (other.fieldName != null) : !this.fieldName.equals(other.fieldName)) {
                return false;
            }
            if ((this.fieldValue == null) ? (other.fieldValue != null) : !this.fieldValue.equals(other.fieldValue)) {
                return false;
            }
            if (this.kind != other.kind) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "CacheKey{" + "index=" + index + ", fieldName=" + fieldName + ", fieldValue=" + fieldValue + ", kind=" + kind + '}';
        }
    }

    private static class CacheValue {

        private final Set<String> fields;

        private final Collection<? extends IndexResult> result;

        public CacheValue(String[] fields, Collection<? extends IndexResult> result) {
            this.fields = new HashSet<String>(Arrays.asList(fields));
            this.result = result;
        }

        public Collection<? extends IndexResult> getResult() {
            return result;
        }

        public boolean contains(String... fieldsToLoad) {
            return fields.containsAll(Arrays.asList(fieldsToLoad));
        }
    }

    private static class StatsKey {

        private final String[] fields;

        public StatsKey(String[] fields) {
            this.fields = fields.clone();
            Arrays.sort(this.fields);
        }

        @Override
        public int hashCode() {
            int hash = 3;
            hash = 97 * hash + Arrays.deepHashCode(this.fields);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final StatsKey other = (StatsKey) obj;
            if (!Arrays.deepEquals(this.fields, other.fields)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return Arrays.deepToString(fields);
        }
    }

    private static class StatsValue {

        private final int requests;

        private final int count;

        private final long size;

        public StatsValue(int requests, int count, long size) {
            this.requests = requests;
            this.count = count;
            this.size = size;
        }

        public int getRequests() {
            return requests;
        }

        public int getCount() {
            return count;
        }

        public long getSize() {
            return size;
        }

        @Override
        public String toString() {
            return "StatsValue{" + "requests=" + requests + ", average=" + (count != 0 ? (size / count) : 0)
                    + ", count=" + count + ", size=" + size + '}';
        }

    }
}
