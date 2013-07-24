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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
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
    public static final String FIELD_ASSIGNMENS = "assign"; //NOI18N
    public static final String FIELD_RETURN_TYPES = "return"; //NOI18N
    public static final String FIELD_PARAMETERS = "param"; //NOI18N
    public static final String FIELD_FLAG = "flag"; //NOI18N

    private static final String PROPERTIES_PATTERN = "\\.[^\\.]*[^" + IndexedElement.PARAMETER_POSTFIX + "]";
    
    @org.netbeans.api.annotations.common.SuppressWarnings("MS_MUTABLE_ARRAY")
    public static final String[] TERMS_BASIC_INFO = new String[] { FIELD_BASE_NAME, FIELD_FQ_NAME, FIELD_OFFSET, FIELD_RETURN_TYPES, FIELD_PARAMETERS, FIELD_FLAG, FIELD_ASSIGNMENS};

    private static final Logger LOG = Logger.getLogger(JsIndex.class.getName());

    private static final WeakHashMap<FileObject, JsIndex> CACHE = new WeakHashMap<FileObject, JsIndex>();

    private static final int MAX_ENTRIES_CACHE_INDEX_RESULT = 300;
    // cache to keep latest index results. The cache is cleaned if a file is saved
    // or a file has to be reindexed due to an external change
    private static final Map <CacheKey, Collection<? extends IndexResult>> CACHE_INDEX_RESULT = new LinkedHashMap<CacheKey, Collection<? extends IndexResult>>(MAX_ENTRIES_CACHE_INDEX_RESULT + 1, 0.75F, true) {
        @Override
        public boolean removeEldestEntry(Map.Entry eldest) {
            return size() > MAX_ENTRIES_CACHE_INDEX_RESULT;
        }
    };

    private static final AtomicBoolean IS_INDEX_CHANGED = new AtomicBoolean(true);

    private final QuerySupport querySupport;

    private JsIndex(QuerySupport querySupport) {
        this.querySupport = querySupport;
    }

    public static JsIndex get(Collection<FileObject> roots) {
        // XXX no cache - is it needed?
        LOG.log(Level.FINE, "JsIndex for roots: {0}", roots); //NOI18N
        return new JsIndex(QuerySupportFactory.get(roots));
    }

    public static void changeInIndex() {
        IS_INDEX_CHANGED.set(true);
    }

    public static JsIndex get(FileObject fo) {
        JsIndex index = CACHE.get(fo);
        if (index == null) {
            LOG.log(Level.FINE, "Creating JsIndex for FileObject: {0}", fo); //NOI18N
            index = new JsIndex(QuerySupportFactory.get(fo));
            CACHE.put(fo, index);
        }
        return index;
    }

    public Collection<? extends IndexResult> query(
            final String fieldName, final String fieldValue,
            final QuerySupport.Kind kind, final String... fieldsToLoad) {
        if (querySupport != null) {
            try {
                if (IS_INDEX_CHANGED.get()) {
                    CACHE_INDEX_RESULT.clear();
                    IS_INDEX_CHANGED.set(false);
                }
                CacheKey key = new CacheKey(this, fieldName, fieldValue, kind);
                Collection<? extends IndexResult> result = CACHE_INDEX_RESULT.get(key);
                if (result == null) {
                    result = querySupport.query(fieldName, fieldValue, kind, fieldsToLoad);
                    CACHE_INDEX_RESULT.put(key, result);
                }
                return result;
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
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

    private static Collection<IndexedElement> getElementsByPrefix(String prefix, Collection<IndexedElement> items) {
        Collection<IndexedElement> result = new ArrayList<IndexedElement>();
        for (IndexedElement indexedElement : items) {
            if (indexedElement.getName().startsWith(prefix)) {
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
            Collection<? extends IndexResult> results = findFQN(fqn);
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
    
    public Collection<? extends IndexResult> findFQN(String fqn) {
        String pattern = escapeRegExp(fqn)+ "."; //NOI18N
        Collection<? extends IndexResult> results = query(
                JsIndex.FIELD_FQ_NAME, pattern, QuerySupport.Kind.REGEXP, TERMS_BASIC_INFO); //NOI18N
        
        return results;
    }
    
    private String escapeRegExp(String text) {
        return Pattern.quote(text);
    }

    private static class CacheKey {

        final JsIndex index;
        
        final String fieldName;

        final String fieldValue;

        final QuerySupport.Kind kind;

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
    }
}
