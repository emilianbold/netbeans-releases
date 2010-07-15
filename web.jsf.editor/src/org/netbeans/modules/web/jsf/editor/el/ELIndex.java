/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2010 Sun Microsystems, Inc.
 */
package org.netbeans.modules.web.jsf.editor.el;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.parsing.spi.indexing.support.QuerySupport;
import org.netbeans.modules.web.jsf.editor.el.ELIndexer.Fields;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Index for Expresion Language
 *
 * @author Erno Mononen
 */
public final class ELIndex {

    private final QuerySupport querySupport;

    private ELIndex(QuerySupport querySupport) {
        this.querySupport = querySupport;
    }

    public static ELIndex get(FileObject file) {
        Project project = FileOwnerQuery.getOwner(file);
        Collection<FileObject> sourceRoots = QuerySupport.findRoots(project,
                null,
                Collections.<String>emptyList(),
                Collections.<String>emptyList());
        try {
            QuerySupport support = QuerySupport.forRoots(ELIndexer.Factory.NAME,
                    ELIndexer.Factory.VERSION,
                    sourceRoots.toArray(new FileObject[]{}));

            return new ELIndex(support);

        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return null;
    }

    public Collection<? extends IndexResult> findManagedBeanReferences(String managedBeanName) {
        Collection<? extends IndexResult> queryResults = query(Fields.IDENTIFIER, managedBeanName, QuerySupport.Kind.EXACT);
        return queryResults;
//        List<IndexedIdentifier> result = new ArrayList<IndexedIdentifier>();
//        for (IndexResult ir : queryResults) {
//            for (String value : ir.getValues(Fields.IDENTIFIER)) {
//                IndexedIdentifier identifier = IndexedIdentifier.decode(value, ir);
//                if (identifier.getIdentifier().equals(managedBeanName)) {
//                    result.add(identifier);
//                }
//            }
//        }
//        return result;
    }

    public List<IndexedProperty> findPropertyReferences(String propertyName, String managedBeanName) {
        Collection<? extends IndexResult> queryResults = query(Fields.PROPERTY, propertyName, QuerySupport.Kind.PREFIX);
        List<IndexedProperty> result = new ArrayList<IndexedProperty>();
        for (IndexResult ir : queryResults) {
            for (String value : ir.getValues(Fields.PROPERTY)) {
                IndexedProperty property = IndexedProperty.decode(value, ir);
                if (property.getIdentifier().equals(managedBeanName) && property.getProperty().equals(propertyName)) {
                    result.add(property);
                }
            }
        }
        return result;
    }

    public Collection<? extends IndexResult> findPropertyReferences(String propertyName) {
        return query(Fields.PROPERTY, propertyName, QuerySupport.Kind.EXACT);
//        List<IndexedProperty> result = new ArrayList<IndexedProperty>();
//        for (IndexResult ir : queryResults) {
//            for (String value : ir.getValues(Fields.PROPERTY)) {
//                IndexedProperty property = IndexedProperty.decode(value, ir);
////                if (property.getIdentifier().equals(managedBeanName) && property.getProperty().equals(propertyName)) {
////                    result.add(property);
////                }
//            }
//        }
//        return result;
    }

    public Collection<? extends IndexResult> findMethodReferences(String methodName) {
        Collection<? extends IndexResult> queryResults = query(Fields.METHOD, methodName, QuerySupport.Kind.EXACT);
        return queryResults;
//        List<IndexedProperty> result = new ArrayList<IndexedProperty>();
//        for (IndexResult ir : queryResults) {
//            for (String value : ir.getValues(Fields.METHOD)) {
//                IndexedProperty property = IndexedProperty.decode(value, ir);
//                if (property.getIdentifier().equals(identifier) && property.getProperty().equals(methodName)) {
//                    result.add(property);
//                }
//            }
//        }
//        return result;
    }


    private Collection<? extends IndexResult> query(String field, String value, QuerySupport.Kind kind) {
        try {
            return querySupport.query(field, value, kind);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return Collections.emptySet();
    }
}
