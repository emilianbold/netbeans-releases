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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TreePathHandle;
import org.netbeans.api.java.source.TreeUtilities;
import org.netbeans.modules.parsing.spi.indexing.support.IndexResult;
import org.netbeans.modules.refactoring.api.AbstractRefactoring;
import org.netbeans.modules.refactoring.api.Problem;
import org.netbeans.modules.refactoring.spi.RefactoringElementsBag;
import org.netbeans.modules.web.el.ELElement;
import org.netbeans.modules.web.el.ELIndex;
import org.netbeans.modules.web.el.ELIndexer.Fields;
import org.netbeans.modules.web.el.ELParser;
import org.netbeans.modules.web.el.ELTypeUtilities;
import org.netbeans.modules.web.el.ELVariableResolvers;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Finds usages of managed beans in Expression Language.
 *
 * @author Erno Mononen
 */
public class ELWhereUsedQuery extends ELRefactoringPlugin {

    private static final Logger LOGGER = Logger.getLogger(ELWhereUsedQuery.class.getName());
    protected CompilationInfo info;
    protected ELTypeUtilities typeUtilities;

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
        this.typeUtilities = ELTypeUtilities.create(info.getFileObject(), refactoring.getContext().lookup(ClasspathInfo.class));
        Element element = resolveElement(handle);
        if (element == null) {
            LOGGER.log(Level.INFO, "Could not resolve Element for TPH: {0}", handle);
            return null;
        }
        if ((Kind.METHOD == handle.getKind() || Kind.MEMBER_SELECT == handle.getKind())
                && element instanceof ExecutableElement) {
            return handleProperty(refactoringElementsBag, handle, (ExecutableElement) element);
        }
        if (TreeUtilities.CLASS_TREE_KINDS.contains(handle.getKind())) {
            return handleClass(refactoringElementsBag, handle, element);
        }
        return null;
    }

    private Element resolveElement(final TreePathHandle handle) {
        final ClasspathInfo cpInfo = refactoring.getContext().lookup(ClasspathInfo.class);
        JavaSource source = JavaSource.create(cpInfo, new FileObject[]{handle.getFileObject()});
        final Element[] result = new Element[1];
        try {
            source.runUserActionTask(new Task<CompilationController>() {
                @Override
                public void run(CompilationController cc) throws Exception {
                    cc.toPhase(JavaSource.Phase.RESOLVED);
                    result[0] = handle.resolveElement(cc);
                }
            }, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return result[0];
    }
    
    protected Problem handleClass(RefactoringElementsBag refactoringElementsBag, TreePathHandle handle, Element targetType) {
        TypeElement type = (TypeElement) targetType;
        String clazz = type.getQualifiedName().toString();
        String beanName = ELVariableResolvers.findBeanName(clazz, getFileObject());
        if (beanName != null) {
            ELIndex index = ELIndex.get(handle.getFileObject());
            Collection<? extends IndexResult> result = index.findIdentifierReferences(beanName);
            for (ELElement elem : getMatchingElements(result)) {
                addElements(elem, findMatchingIdentifierNodes(elem.getNode(), beanName), refactoringElementsBag);
            }
        }
        return null;
    }

    protected Problem handleProperty(RefactoringElementsBag refactoringElementsBag, TreePathHandle handle, ExecutableElement targetType) {
        String propertyName = RefactoringUtil.getPropertyName(targetType.getSimpleName().toString());
        ELIndex index = ELIndex.get(handle.getFileObject());
        final Set<IndexResult> result = new HashSet<IndexResult>();
        // search for property nodes only if the method has no params (or accepts one vararg)
        if (targetType.getParameters().isEmpty() ||
                (targetType.getParameters().size() == 1 && targetType.isVarArgs())) {
            result.addAll(index.findPropertyReferences(propertyName));
        }
        result.addAll(index.findMethodReferences(propertyName));

        // logic: first try to find all properties for which can resolve the type directly,
        // then search for occurrences in variables
        for (ELElement each : getMatchingElements(result)) {
            List<Node> matchingNodes = findMatchingPropertyNodes(each.getNode(),
                    targetType,
                    each.getSnapshot().getSource().getFileObject());
            addElements(each, matchingNodes, refactoringElementsBag);
            handleVariableReferences(each, targetType, refactoringElementsBag);
        }
        
        return null;
    }

    private void handleVariableReferences(ELElement elElement, Element targetType, RefactoringElementsBag refactoringElementsBag) {
        //fix by using ELVariableResolver.getVariables(...)

//        final List<Node> matchingNodes = new ArrayList<Node>();
//        elElement.getNode().accept(new NodeVisitor() {
//
//            @Override
//            public void visit(Node node) throws ELException {
//                if (node instanceof AstPropertySuffix || node instanceof AstMethodSuffix) {
//                    matchingNodes.add(node);
//                }
//            }
//        });
//
//        for (ELVariableResolver resolver : getResolvers()) {
//            for (Node n : matchingNodes) {
//                String expression = resolver.getReferredExpression(elElement.getSnapshot(),
//                        elElement.getOriginalOffset().getStart() + n.startOffset());
//                if (expression != null) {
//                    Node expressionNode = ELParser.parse(expression);
//                    if (refersToType(expressionNode,
//                            targetType.getEnclosingElement().asType(),
//                            elElement.getSnapshot().getSource().getFileObject())) {
//                        addElements(elElement, Collections.singletonList(n), refactoringElementsBag);
//                    }
//                }
//            }
//        }
    }

    private Collection<? extends ELVariableResolver> getResolvers() {
        return Lookup.getDefault().lookupAll(ELVariableResolver.class);
    }

    protected void addElements(ELElement elem, List<Node> matchingNodes, RefactoringElementsBag refactoringElementsBag) {
        for (Node property : matchingNodes) {
            WhereUsedQueryElement wuqe =
                    new WhereUsedQueryElement(elem.getSnapshot().getSource().getFileObject(), property.getImage(), elem, property, getParserResult(elem.getSnapshot().getSource().getFileObject()));
            refactoringElementsBag.add(refactoring, wuqe);
        }
    }

    private List<Node> findMatchingPropertyNodes(Node root,
            final ExecutableElement targetMethod,
            final FileObject context) {

        final List<Node> result = new ArrayList<Node>();
        final TypeMirror targetType = targetMethod.getEnclosingElement().asType();
        root.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node instanceof AstIdentifier) {
                    Node parent = node.jjtGetParent();
                    String beanClass = ELVariableResolvers.findBeanClass(node.getImage(), context);
                    if (beanClass == null) {
                        return;
                    }
                    TypeElement fmbType = info.getElements().getTypeElement(beanClass);
                    TypeMirror enclosing = fmbType.asType();
                    for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                        Node child = parent.jjtGetChild(i);
                        if (!(child instanceof AstPropertySuffix || child instanceof AstMethodSuffix)) {
                            continue;
                        }
                        if (enclosing == null) {
                            break;
                        }
                        if (info.getTypes().isSameType(targetType, enclosing) && typeUtilities.isSameMethod(child, targetMethod)) {
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
    private boolean refersToType(Node root, final TypeMirror targetType, final FileObject context) {

        final boolean[] result = new boolean[1];
        root.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node instanceof AstIdentifier) {
                    Node parent = node.jjtGetParent();
                    String beanClass = ELVariableResolvers.findBeanClass(node.getImage(), context);
                    if (beanClass == null) {
                        return;
                    }
                    TypeElement fmbType = info.getElements().getTypeElement(beanClass);
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
        List<? extends Element> enclosedElements = info.getTypes().asElement(enclosing).getEnclosedElements();
        for (Element each : ElementFilter.methodsIn(enclosedElements)) {
            // we're only interested in public methods
            // XXX: should probably include public fields too
            if (!each.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            ExecutableElement methodElem = (ExecutableElement) each;
            String methodName = methodElem.getSimpleName().toString();

            if (typeUtilities.isSameMethod(property, methodElem)) {
                return typeUtilities.getReturnType(methodElem);

            } else if (RefactoringUtil.getPropertyName(methodName).equals(name) || methodName.equals(name)) {
                return typeUtilities.getReturnType(methodElem);
            }
        }
        return null;
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
                    if (!result.contains(element)) {
                        result.add(element);
                    }
                }
            }
        }
        return result;

    }
}
