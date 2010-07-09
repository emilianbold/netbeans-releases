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
import java.util.Collection;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.refactoring.spi.SimpleRefactoringElementImplementation;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.editor.el.ELIndex;
import org.netbeans.modules.web.jsf.editor.el.ELIndexer.Fields;
import org.openide.filesystems.FileObject;
import org.openide.text.PositionBounds;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
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
        Problem result = null;
        TreePathHandle handle = getHandle();
        if (handle == null || Kind.CLASS != handle.getKind()) {
            return null;
        }
        Element resElement = handle.resolveElement(RefactoringUtil.getCompilationInfo(handle, whereUsedQuery));
        TypeElement type = (TypeElement) resElement;
        String clazz = type.getQualifiedName().toString();
        FacesManagedBean managedBean = findManagedBeanByClass(clazz);
        ELIndex index = ELIndex.get(handle.getFileObject());
        Collection<? extends IndexResult> references = index.findManagedBeanReferences(managedBean.getManagedBeanName());
        for (IndexResult each : references) {
            for (String value : each.getValues(Fields.IDENTIFIER)) {
                if (managedBean.getManagedBeanName().equals(value)) {
                    refactoringElementsBag.add(whereUsedQuery, new WhereUsedQueryElement(each.getFile(), value));
                }
            }
        }

        return result;
    }

    private static class WhereUsedQueryElement extends SimpleRefactoringElementImplementation {

        private final FileObject file;
        private final String reference;

        public WhereUsedQueryElement(FileObject file, String reference) {
            this.file = file;
            this.reference = reference;
        }

        @Override
        public String getText() {
            return reference;
        }

        @Override
        public String getDisplayText() {
            return reference;
        }

        @Override
        public void performChange() {
        }

        @Override
        public Lookup getLookup() {
            return Lookups.singleton(file);
        }

        @Override
        public FileObject getParentFile() {
            return file;
        }

        @Override
        public PositionBounds getPosition() {
            return null;
        }
    }
}
