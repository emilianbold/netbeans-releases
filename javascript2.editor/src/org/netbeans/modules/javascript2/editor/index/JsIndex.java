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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.modules.javascript2.editor.model.TypeUsage;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.openide.filesystems.FileObject;

/**
 *
 * @author Petr Pisl
 */
public class JsIndex {

    private static final Logger LOG = Logger.getLogger(JsIndex.class.getName());
    private final QuerySupport querySupport;
    private static final JsIndex EMPTY = new JsIndex(null);

    public static final String FIELD_IS_GLOBAL = "isglobal"; //NOI18N
    public static final String FIELD_BASE_NAME = "bn"; //NOI18N
    public static final String FIELD_FQ_NAME = "fqn"; //NOI18N
    public static final String FIELD_PROPERTY = "prop"; //NOI18N
    public static final String FIELD_OFFSET = "offset"; //NOI18N
    public static final String FIELD_ASSIGNMENS = "assign"; //NOI18N
    public static final String FIELD_RETURN_TYPES = "return"; //NOI18N
    public static final String FIELD_PARAMETERS = "param"; //NOI18N
    public static final String FIELD_FLAG = "flag"; //NOI18N

    @org.netbeans.api.annotations.common.SuppressWarnings("MS_MUTABLE_ARRAY")
    public static final String[] TERMS_BASIC_INFO = new String[] { FIELD_BASE_NAME, FIELD_FQ_NAME, FIELD_OFFSET, FIELD_RETURN_TYPES, FIELD_PARAMETERS, FIELD_FLAG, FIELD_IS_GLOBAL, FIELD_ASSIGNMENS};
    static final String[] TERMS_PROPERTIES = new String[] { FIELD_PROPERTY, FIELD_ASSIGNMENS, FIELD_RETURN_TYPES, FIELD_FLAG};
    
    private JsIndex(QuerySupport querySupport) {
        this.querySupport = querySupport;
    }

    public static JsIndex get(Collection<FileObject> roots) {
        LOG.log(Level.FINE, "JsIndex for roots: {0}", roots); //NOI18N
        return new JsIndex(QuerySupportFactory.get(roots));
    }
    
    public static JsIndex get(FileObject fo) {
        LOG.log(Level.FINE, "JsIndex for FileObject: {0}", fo); //NOI18N
        return new JsIndex(QuerySupportFactory.get(fo));
    }

    public Collection<? extends IndexResult> query(
            final String fieldName, final String fieldValue,
            final QuerySupport.Kind kind, final String... fieldsToLoad) {
        if (querySupport != null) {
            try {
                return querySupport.query(fieldName, fieldValue, kind, fieldsToLoad);
            } catch (IOException ioe) {
                LOG.log(Level.WARNING, null, ioe);
            }
        }

        return Collections.<IndexResult>emptySet();
    }
    
    public Collection <IndexedElement> getGlobalVar(String prefix) {
        Collection<IndexedElement> allGlobalItems = new ArrayList<IndexedElement>();
        prefix = prefix == null ? "" : prefix; //NOI18N

        Collection<? extends IndexResult> globalObjects = query(
                JsIndex.FIELD_IS_GLOBAL, "1", QuerySupport.Kind.EXACT, TERMS_BASIC_INFO); //NOI18N
        for (IndexResult indexResult : globalObjects) {
            IndexedElement indexedElement = IndexedElement.create(indexResult);
            allGlobalItems.add(indexedElement);
        }

        // enhance results for all window properties - see issue #218412, #215863, #218122, ...
        allGlobalItems.addAll(getProperties("window")); //NOI18N

        return getElementsByPrefix(prefix, allGlobalItems);
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
    
    public Collection <IndexedElement> getProperties(String fqn) {
        Collection<? extends IndexResult> results = query(
                JsIndex.FIELD_FQ_NAME, fqn, QuerySupport.Kind.EXACT, TERMS_PROPERTIES); //NOI18N
        Collection<IndexedElement> result = new ArrayList<IndexedElement>();
        for (IndexResult indexResult : results) {
            Collection<TypeUsage> assignments = IndexedElement.getAssignments(indexResult);
            if (!assignments.isEmpty()) {
                TypeUsage type = assignments.iterator().next();
                if (!type.getType().equals(fqn)) {
                    result.addAll(getProperties(type.getType()));
                }
            }
            for (IndexedElement indexedElement : IndexedElement.createProperties(indexResult, fqn)) {
                result.add(indexedElement);
            }
        }
        return result;
    }
    
    public Collection<? extends IndexResult> findFQN(String fqn) {
        Collection<? extends IndexResult> results = query(
                JsIndex.FIELD_FQ_NAME, fqn, QuerySupport.Kind.EXACT, TERMS_PROPERTIES); //NOI18N
        
        return results;
    }
}
