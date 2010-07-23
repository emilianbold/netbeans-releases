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
package org.netbeans.modules.web.el.refactoring;

import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstMethodSuffix;
import com.sun.el.parser.AstPropertySuffix;
import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import com.sun.source.tree.Tree.Kind;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.el.ELException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.jsf.api.metamodel.FacesManagedBean;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELIndex;
import org.netbeans.modules.web.el.ELIndexer.Fields;
import org.netbeans.modules.web.el.ELParser;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;

/**
 * Finds usages of managed beans in Expression Language.
 *
 * @author Erno Mononen
 */
public class ELWhereUsedQuery extends ELRefactoringPlugin {

    private CompilationInfo info;

    ELWhereUsedQuery(AbstractRefactoring whereUsedQuery) {
        super(whereUsedQuery);
    }

    @Override
    public Problem prepare(RefactoringElementsBag refactoringElementsBag) {
        TreePathHandle handle = getHandle();
        if (handle == null) {
            return null;
        }
        this.info = RefactoringUtil.getCompilationInfo(handle, refactoring);
        Element element = handle.resolveElement(info);
        if (Kind.METHOD == handle.getKind()) {
            return handleProperty(refactoringElementsBag, handle, element);
        }
        if (Kind.CLASS == handle.getKind()) {
            return handleClass(refactoringElementsBag, handle, element);
        }
        return null;
    }

    protected Problem handleClass(RefactoringElementsBag refactoringElementsBag, TreePathHandle handle, Element targetType) {
        TypeElement type = (TypeElement) targetType;
        String clazz = type.getQualifiedName().toString();
        FacesManagedBean managedBean = findManagedBeanByClass(clazz);
        ELIndex index = ELIndex.get(handle.getFileObject());
        Collection<? extends IndexResult> result = index.findIdentifierReferences(managedBean.getManagedBeanName());
        String managedBeanName = managedBean.getManagedBeanName();
        for (ELElement elem : getMatchingElements(result)) {
            for (Node identifier : findMatchingIdentifierNodes(elem.getNode(), managedBeanName)) {
                WhereUsedQueryElement wuqe =
                        new WhereUsedQueryElement(elem.getParserResult().getFileObject(), managedBeanName, elem, identifier, getParserResult(elem.getParserResult().getFileObject()));
                refactoringElementsBag.add(refactoring, wuqe);
            }
        }
        return null;
    }

    protected Problem handleProperty(RefactoringElementsBag refactoringElementsBag, TreePathHandle handle, Element targetType) {
        String propertyName = RefactoringUtil.getPropertyName(targetType.getSimpleName().toString());
        ELIndex index = ELIndex.get(handle.getFileObject());
        final Set<IndexResult> result = new HashSet<IndexResult>();
        result.addAll(index.findPropertyReferences(propertyName));
        result.addAll(index.findMethodReferences(propertyName));

        // logic: first try to find all properties for which can resolve the type directly,
        // then search for occurrences in variables
        for (ELElement each : getMatchingElements(result)) {
            List<Node> matchingNodes = findMatchingPropertyNodes(each.getNode(), propertyName, targetType.getEnclosingElement().asType());
            addElements(each, matchingNodes, refactoringElementsBag);
            handleVariableReferences(each, targetType, refactoringElementsBag);
        }
        
        return null;
    }

    private void handleVariableReferences(ELElement elElement, Element targetType, RefactoringElementsBag refactoringElementsBag) {
        final List<Node> matchingNodes = new ArrayList<Node>();
        elElement.getNode().accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node instanceof AstPropertySuffix || node instanceof AstMethodSuffix) {
                    matchingNodes.add(node);
                }
            }
        });

        ELVariableResolver resolver = Lookup.getDefault().lookup(ELVariableResolver.class);
        for (Node n : matchingNodes) {
            String expression = resolver.getReferredExpression(elElement.getParserResult().getSnapshot(),
                    elElement.getOriginalOffset().getStart() + n.startOffset());
            if (expression != null) {
                Node expressionNode = ELParser.parse(expression);
                if (refersToType(expressionNode, targetType.getEnclosingElement().asType())) {
                    addElements(elElement, Collections.singletonList(n), refactoringElementsBag);
                }
            }
        }
    }

    protected void addElements(ELElement elem, List<Node> matchingNodes, RefactoringElementsBag refactoringElementsBag) {
        for (Node property : matchingNodes) {
            WhereUsedQueryElement wuqe =
                    new WhereUsedQueryElement(elem.getParserResult().getFileObject(), property.getImage(), elem, property, getParserResult(elem.getParserResult().getFileObject()));
            refactoringElementsBag.add(refactoring, wuqe);
        }
    }

    private List<Node> findMatchingPropertyNodes(Node root, final String targetName, final TypeMirror targetType) {

        final List<Node> result = new ArrayList<Node>();
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
                    TypeMirror enclosing = fmbType.asType();
                    for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                        Node child = parent.jjtGetChild(i);
                        if (child instanceof AstPropertySuffix || child instanceof AstMethodSuffix) {
                            if (targetName.equals(child.getImage()) && info.getTypes().isSameType(targetType, enclosing)) {
                                TypeMirror matching = getTypeForProperty(child, enclosing);
                                if (matching != null) {
                                    result.add(child);
                                }
                            } else {
                                enclosing = getTypeForProperty(child, enclosing);
                            }

                        }
                    }
                }
            }
        });
        return result;
    }

    /**
     * Returns true if {@code root} resolves to an expression that refers to the given
     * {@code targetType}.
     * @param root
     * @param targetType
     * @return
     */
    private boolean refersToType(Node root, final TypeMirror targetType) {

        final boolean[] result = new boolean[1];
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
                    TypeMirror enclosing = fmbType.asType();
                    Node current = parent;
                    for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                        current = parent.jjtGetChild(i);
                        if (current instanceof AstPropertySuffix || current instanceof AstMethodSuffix) {
                            enclosing = getTypeForProperty(current, enclosing);
                        }
                    }
                    //XXX: works just for generic collections, i.e. the assumption is 
                    // that variables refer to collections, which is not always the case
                    if (enclosing instanceof DeclaredType) {
                        List<? extends TypeMirror> typeArguments = ((DeclaredType) enclosing).getTypeArguments();
                        for (TypeMirror arg : typeArguments) {
                            if (info.getTypes().isSubtype(arg, targetType)) {
                                result[0] = true;
                                return;
                            }
                        }
                    }
                }
            }
        });

        return result[0];
    }

    private List<Node> findMatchingIdentifierNodes(Node root, final String identifierName) {
        final List<Node> result = new ArrayList<Node>();
        root.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node instanceof AstIdentifier) {
                    if (identifierName.equals(node.getImage())) {
                        result.add(node);
                    }
                }
            }
        });
        return result;
    }

    /**
     * Gets the element matching the given name from the given enclosing class.
     * @param name the name of the element to find.
     * @param enclosing
     * @return
     */
    private TypeMirror getTypeForProperty(Node property, TypeMirror enclosing) {
        String name = property.getImage();
        for (Element each : info.getTypes().asElement(enclosing).getEnclosedElements()) {
            // we're only interested in public methods
            // XXX: should probably include public fields too
            if (each.getKind() != ElementKind.METHOD || !each.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            ExecutableElement methodElem = (ExecutableElement) each;
            String methodName = methodElem.getSimpleName().toString();

            if (property instanceof AstMethodSuffix
                    && methodName.equals(name)
                    && haveSameParameters((AstMethodSuffix) property, methodElem)) {

                return getReturnType(methodElem);

            } else if (RefactoringUtil.getPropertyName(methodName).equals(name) || methodName.equals(name)) {
                return getReturnType(methodElem);
            }
        }
        return null;
    }

    private TypeMirror getReturnType(ExecutableElement method) {
        TypeKind returnTypeKind = method.getReturnType().getKind();
        if (returnTypeKind.isPrimitive()) {
            return info.getTypes().getPrimitiveType(returnTypeKind);
        } else {
            return method.getReturnType();
        }
    }

    private static boolean haveSameParameters(AstMethodSuffix methodNode, ExecutableElement method) {
        //XXX: need to do type matching here
        return method.getParameters().size() == methodNode.jjtGetNumChildren();
    }

    private List<ELElement> getMatchingElements(Collection<? extends IndexResult> indexResult) {
        // probably should store offsets rather than doing full expression comparison
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
}
