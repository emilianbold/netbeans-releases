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
package org.netbeans.modules.web.jsf.editor.refactoring;

import com.sun.source.tree.Tree.Kind;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.swing.text.BadLocationException;
import javax.swing.text.Position.Bias;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.csl.api.OffsetRange;
import org.netbeans.modules.csl.spi.GsfUtilities;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.editor.el.ELElement;
import org.netbeans.modules.web.jsf.editor.el.ELIndex;
import org.netbeans.modules.web.jsf.editor.el.ELIndexer.Fields;
import org.netbeans.modules.web.jsf.editor.el.IndexedIdentifier;
import org.netbeans.modules.web.jsf.editor.el.IndexedProperty;
import org.openide.filesystems.FileObject;
import org.openide.text.CloneableEditorSupport;
import org.openide.text.PositionBounds;
import org.openide.text.PositionRef;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * Finds usages of managed beans in Expression Language.
 *
 * @author Erno Mononen
 */
public class ELWhereUsedQuery extends JsfELRefactoringPlugin {

    private final WhereUsedQuery whereUsedQuery;

    ELWhereUsedQuery(WhereUsedQuery whereUsedQuery) {
        super(whereUsedQuery);
        this.whereUsedQuery = whereUsedQuery;
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        TreePathHandle handle = getHandle();
        if (handle == null) {
            return null;
        }
        Element element = handle.resolveElement(RefactoringUtil.getCompilationInfo(handle, whereUsedQuery));
        if (Kind.METHOD == handle.getKind()) {
            return handleProperty(refactoringElementsBag, handle, element);
        }
        if (Kind.CLASS == handle.getKind()) {
            return handleClass(refactoringElementsBag, handle, element);
        }
        return null;
    }

    private Problem handleClass(RefactoringElementsBag refactoringElementsBag, TreePathHandle handle, Element element) {
        TypeElement type = (TypeElement) element;
        String clazz = type.getQualifiedName().toString();
        FacesManagedBean managedBean = findManagedBeanByClass(clazz);
        ELIndex index = ELIndex.get(handle.getFileObject());
        List<IndexedIdentifier> identifiers = index.findManagedBeanReferences(managedBean.getManagedBeanName());
        for (WhereUsedQueryElement elem : createElements(identifiers, managedBean.getManagedBeanName())) {
            refactoringElementsBag.add(whereUsedQuery, elem);
        }
        return null;
    }

    private Problem handleProperty(RefactoringElementsBag refactoringElementsBag, TreePathHandle handle, Element element) {
        String clazz = element.getEnclosingElement().asType().toString();
        FacesManagedBean managedBean = findManagedBeanByClass(clazz);
        if (managedBean == null) {
            return null;
        }
        String propertyName = RefactoringUtil.getPropertyName(element.getSimpleName().toString());
        ELIndex index = ELIndex.get(handle.getFileObject());
        List<IndexedProperty> properties = index.findPropertyReferences(propertyName, managedBean.getManagedBeanName());
        for (WhereUsedQueryElement elem : createElements(properties, propertyName)) {
            refactoringElementsBag.add(whereUsedQuery, elem);
        }
        return null;
    }


    private static List<WhereUsedQueryElement> createElements(List<? extends IndexedIdentifier> identifiers, String reference) {

        if (identifiers.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<WhereUsedQueryElement> result = new ArrayList<WhereUsedQueryElement>(identifiers.size());
        for (IndexedIdentifier identifier : identifiers) {
            ParserResultHolder parserResultHolder = getParserResult(identifier.getFile());
            if (parserResultHolder.parserResult == null) {
                continue;
            }
            List<ELElement> elements = new ArrayList(parserResultHolder.parserResult.getElements());
            for (Iterator<ELElement> it = elements.iterator(); it.hasNext();) {
                ELElement eLElement = it.next();
                if (identifier.getExpression().equals(eLElement.getExpression())) {
                    WhereUsedQueryElement wuqe =
                            new WhereUsedQueryElement(identifier.getFile(), reference, eLElement, parserResultHolder);
                    result.add(wuqe);
                    it.remove();
                }
            }
        }
        return result;
    }
}
