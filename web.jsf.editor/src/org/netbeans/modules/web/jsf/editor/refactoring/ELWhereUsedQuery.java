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

import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstPropertySuffix;
import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import com.sun.source.tree.Tree.Kind;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import javax.el.ELException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.ExecutableType;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.api.WhereUsedQuery;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.jsf.editor.el.ELElement;
import org.netbeans.modules.web.jsf.editor.el.ELIndex;
import org.netbeans.modules.web.jsf.editor.el.ELIndexer.Fields;
import org.openide.filesystems.FileObject;

/**
 * Finds usages of managed beans in Expression Language.
 *
 * @author Erno Mononen
 */
public class ELWhereUsedQuery extends JsfELRefactoringPlugin {

    private final WhereUsedQuery whereUsedQuery;
    private CompilationInfo info;

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
        this.info = RefactoringUtil.getCompilationInfo(handle, whereUsedQuery);
        Element element = handle.resolveElement(info);
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
        Collection<? extends IndexResult> identifiers = index.findManagedBeanReferences(managedBean.getManagedBeanName());
        for (WhereUsedQueryElement elem : createElements(identifiers, managedBean.getManagedBeanName())) {
            refactoringElementsBag.add(whereUsedQuery, elem);
        }
        return null;
    }

    private Problem handleProperty(RefactoringElementsBag refactoringElementsBag, TreePathHandle handle, Element element) {
        String propertyName = RefactoringUtil.getPropertyName(element.getSimpleName().toString());
        ELIndex index = ELIndex.get(handle.getFileObject());
        final Set<IndexResult> result = new HashSet<IndexResult>();
        result.addAll(index.findPropertyReferences(propertyName));

        for (ELElement e : getMatchingElements(result)) {
            Node node = findMatchingNode(e.getNode(), propertyName, element.getEnclosingElement());
            if (node != null) {
                WhereUsedQueryElement wuqe =
                        new WhereUsedQueryElement(e.getParserResult().getFileObject(), propertyName, e, node, getParserResult(e.getParserResult().getFileObject()));
                refactoringElementsBag.add(whereUsedQuery, wuqe);
            }

        }
        return null;
    }

    private Node findMatchingNode(Node root, final String targetName, final Element targetType) {
        final Node[] result = new Node[1];
        root.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node instanceof AstIdentifier) {
                    Node parent = node.jjtGetParent();
                    FacesManagedBean fmb = findManagedBeanByName(node.getImage());
                    if (fmb == null) {
                        return;
                    }
                    TypeElement fmbType = info.getElements().getTypeElement(fmb.getManagedBeanClass());
                    Element enclosing = fmbType;
                    for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                        Node child = parent.jjtGetChild(i);
                        if (child instanceof AstPropertySuffix) {
                            if (targetName.equals(child.getImage()) && enclosing.equals(targetType)) {
                                Element matching = getElementForProperty(child.getImage(), enclosing);
                                if (matching != null) {
                                    result[0] = child;
                                    return;
                                }
                            } else {
                                enclosing = getElementForProperty(child.getImage(), enclosing);
                            }

                        }
                    }
                }
            }
        });
        return result[0];

    }

    /**
     * Gets the element matching the given name from the given enclosing class.
     * @param name the name of the element to find.
     * @param enclosing
     * @return
     */
    private Element getElementForProperty(String name, Element enclosing) {
        for (Element each : enclosing.getEnclosedElements()) {
            // we're only interested in public methods
            // XXX: should probably include public fields too
            if (each.getKind() != ElementKind.METHOD || !each.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            String methodName = each.getSimpleName().toString();
            if (RefactoringUtil.getPropertyName(methodName).equals(name) || methodName.equals(name)) {
                ExecutableType returnType = (ExecutableType) each.asType();
                return info.getTypes().asElement(returnType.getReturnType());
            }
        }
        return null;
    }

    private List<ELElement> getMatchingElements(Collection<? extends IndexResult> indexResult)  {
        List<ELElement> result = new ArrayList<ELElement>();
        for (IndexResult ir : indexResult) {
            FileObject file = ir.getFile();
            ParserResultHolder parserResultHolder = getParserResult(file);
            if (parserResultHolder.parserResult == null) {
                continue;
            }
            String expression = ir.getValue(Fields.EXPRESSION);
            for (ELElement element : parserResultHolder.parserResult.getElements()) {
                if (expression.equals(element.getExpression())) {
                    result.add(element);
                }
            }
        }
        return result;

    }

    private static List<WhereUsedQueryElement> createElements(Collection<? extends IndexResult> results, String reference) {

        if (results.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<WhereUsedQueryElement> result = new ArrayList<WhereUsedQueryElement>(results.size());
        for (IndexResult ir : results) {
            FileObject file = ir.getFile();
            ParserResultHolder parserResultHolder = getParserResult(file);
            if (parserResultHolder.parserResult == null) {
                continue;
            }
            String expression = ir.getValue(Fields.EXPRESSION);
            List<ELElement> elements = new ArrayList(parserResultHolder.parserResult.getElements());
            for (Iterator<ELElement> it = elements.iterator(); it.hasNext();) {
                ELElement eLElement = it.next();
                if (expression.equals(eLElement.getExpression())) {
                    WhereUsedQueryElement wuqe =
                            new WhereUsedQueryElement(file, reference, eLElement, eLElement.getNode(), parserResultHolder);
                    result.add(wuqe);
                    it.remove();
                }
            }
        }
        return result;
    }
}
