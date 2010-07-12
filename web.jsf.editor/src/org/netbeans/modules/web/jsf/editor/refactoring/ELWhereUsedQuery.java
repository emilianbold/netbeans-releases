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
        for (IndexResult indexResult : references) {
            for (WhereUsedQueryElement elem : createElements(indexResult.getFile(), managedBean.getManagedBeanName(), indexResult)) {
                refactoringElementsBag.add(whereUsedQuery, elem);
            }
        }
        return result;
    }

    private static List<WhereUsedQueryElement> createElements(FileObject file, String reference, IndexResult indexResult) {
        String[] identifiers = indexResult.getValues(Fields.IDENTIFIER);
        String[] expressions = indexResult.getValues(Fields.FULL_EXPRESSION);
        assert identifiers.length == expressions.length;

        ParserResultHolder parserResultHolder = getParserResult(file);
        // XXX: happens due to x-el lexing bug
        if (parserResultHolder.parserResult == null) {
            return Collections.emptyList();
        }
        List<ELElement> elements = new ArrayList(parserResultHolder.parserResult.getElements());
        List<WhereUsedQueryElement> result = new ArrayList<WhereUsedQueryElement>();
        for (int i = 0; i < identifiers.length; i++) {
            if (reference.equals(identifiers[i])) {
                String expression = expressions[i];
                for (Iterator<ELElement> it = elements.iterator(); it.hasNext();) {
                    ELElement eLElement = it.next();
                    if (expression.equals(eLElement.getExpression())) {
                        WhereUsedQueryElement wuqe = new WhereUsedQueryElement(file, reference, eLElement, parserResultHolder);
                        result.add(wuqe);
                        it.remove();
                    }
                }
            }
        }
        return result;
    }

    private static class WhereUsedQueryElement extends SimpleRefactoringElementImplementation {

        private final FileObject file;
        private final String reference;
        private final ELElement eLElement;
        private final ParserResultHolder parserResult;

        public WhereUsedQueryElement(FileObject file, String reference, ELElement eLElement, ParserResultHolder parserResult) {
            this.file = file;
            this.reference = reference;
            this.eLElement = eLElement;
            this.parserResult = parserResult;
        }

        @Override
        public String getText() {
            return reference;
        }

        @Override
        public String getDisplayText() {
            try {
                CharSequence text = parserResult.topLevelSnapshot.getText();
                OffsetRange orig = eLElement.getOriginalOffset();
                int astLineStart = GsfUtilities.getRowStart(text, orig.getStart());
                int astLineEnd = GsfUtilities.getRowEnd(text, orig.getStart());
                // TODO: this is not accurate, need to do highlighning based on AST offsets
                return RefactoringUtil.encodeAndHighlight(reference, text.subSequence(astLineStart, astLineEnd).toString()).trim();
            } catch (BadLocationException ex) {
                Exceptions.printStackTrace(ex);
                return eLElement.getExpression();
            }
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
            CloneableEditorSupport editor = GsfUtilities.findCloneableEditorSupport(file);
            PositionRef start = editor.createPositionRef(eLElement.getOriginalOffset().getStart(), Bias.Forward);
            PositionRef end = editor.createPositionRef(eLElement.getOriginalOffset().getEnd(), Bias.Backward);
            return new PositionBounds(start, end);
        }
    }
}
