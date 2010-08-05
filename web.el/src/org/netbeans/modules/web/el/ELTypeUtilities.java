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
package org.netbeans.modules.web.el;

import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstMethodSuffix;
import com.sun.el.parser.AstPropertySuffix;
import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import java.io.IOException;
import java.util.List;
import javax.el.ELException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.CompilationInfo;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.modules.web.el.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Utility class for resolving elements/types for EL expressions.
 *
 * @author Erno Mononen
 */
public final class ELTypeUtilities {

    private final CompilationInfo info;

    private ELTypeUtilities(CompilationInfo info) {
        assert info != null;
        this.info = info;
    }

    public static ELTypeUtilities create(CompilationInfo info) {
        return new ELTypeUtilities(info);
    }

    public static ELTypeUtilities create(FileObject context) {
        ClasspathInfo cp = ClasspathInfo.create(context);
        final CompilationInfo[] info = new CompilationInfo[1];
        JavaSource source = JavaSource.create(cp);
        try {
            source.runUserActionTask(new Task<CompilationController>() {

                public void run(CompilationController co) throws Exception {
                    co.toPhase(JavaSource.Phase.RESOLVED);
                    info[0] = co;
                }
            }, false);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        return create(info[0]);
    }

    public Element resolveElement(final ELElement elem, final Node target) {
        TypeResolverVisitor typeResolver = new TypeResolverVisitor(elem, target);
        elem.getNode().accept(typeResolver);
        return typeResolver.getResult();
    }

    public TypeMirror getReturnType(ExecutableElement method) {
        TypeKind returnTypeKind = method.getReturnType().getKind();
        if (returnTypeKind.isPrimitive()) {
            return info.getTypes().getPrimitiveType(returnTypeKind);
        } else {
            return method.getReturnType();
        }
    }

    /**
     * @return true if {@code methodNode} and {@code method} have the same parameters; 
     * false otherwise.
     */
    public static boolean haveSameParameters(AstMethodSuffix methodNode, ExecutableElement method) {
        //XXX: need to do type matching here
        return method.getParameters().size() == methodNode.jjtGetNumChildren();
    }

    /**
     * Gets the element matching the given name from the given enclosing class.
     * @param name the name of the element to find.
     * @param enclosing
     * @return
     */
    private ExecutableElement getElementForProperty(Node property, Element enclosing) {
        String name = property.getImage();
        for (ExecutableElement each : ElementFilter.methodsIn(enclosing.getEnclosedElements())) {
            // we're only interested in public methods
            if (!each.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            String methodName = each.getSimpleName().toString();

            if (property instanceof AstMethodSuffix
                    && methodName.equals(name)
                    && haveSameParameters((AstMethodSuffix) property, each)) {

                return each;

            } else if (RefactoringUtil.getPropertyName(methodName).equals(name) || methodName.equals(name)) {
                return each;
            }
        }
        return null;
    }

    private Element getIdentifierType(AstIdentifier identifier, ELElement element) {
        String beanClass = ELVariableResolvers.findBeanClass(identifier.getImage(), element.getParserResult().getFileObject());
        if (beanClass != null) {
            return info.getElements().getTypeElement(beanClass);
        }
        // probably a variable
        int offset = element.getOriginalOffset().getStart() + identifier.startOffset();
        Node expressionNode = ELVariableResolvers.getReferredExpression(element.getParserResult().getSnapshot(), offset);
        if (expressionNode != null) {
            return getReferredType(expressionNode, element.getParserResult().getFileObject());
        }
        return null;
    }
    /**
     * @return the element for the type that that given {@code expression} refers to, i.e.
     * the return type of the last method in the expression.
     */
    private Element getReferredType(Node expression, final FileObject context) {

        final Element[] result = new Element[1];
        expression.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node instanceof AstIdentifier) {
                    Node parent = node.jjtGetParent();
                    String beanClass = ELVariableResolvers.findBeanClass(node.getImage(), context);
                    if (beanClass == null) {
                        return;
                    }
                    Element enclosing = info.getElements().getTypeElement(beanClass);
                    ExecutableElement method = null;
                    Node current = parent;
                    for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                        current = parent.jjtGetChild(i);
                        if (current instanceof AstPropertySuffix || current instanceof AstMethodSuffix) {
                            method = getElementForProperty(current, enclosing);
                            enclosing = info.getTypes().asElement(getReturnType(method));
                        }
                    }
                    if (method == null) {
                        return;
                    }
                    TypeMirror returnType = getReturnType(method);
                    //XXX: works just for generic collections, i.e. the assumption is
                    // that variables refer to collections, which is not always the case
                    if (returnType instanceof DeclaredType) {
                        List<? extends TypeMirror> typeArguments = ((DeclaredType) returnType).getTypeArguments();
                        for (TypeMirror arg : typeArguments) {
                            result[0] = info.getTypes().asElement(arg);
                            return;
                        }
                    }

                }
            }
        });

        return result[0];
    }

    private class TypeResolverVisitor implements NodeVisitor {

        private final ELElement elem;
        private final Node target;
        private Element result;

        public TypeResolverVisitor(ELElement elem, Node target) {
            this.elem = elem;
            this.target = target;
        }

        public Element getResult() {
            return result;
        }

        @Override
        public void visit(Node node) {
            Element enclosing = null;
            // traverses AST resolving types for each property starting from
            // an identifier until the target is found
            if (node instanceof AstIdentifier) {
                enclosing = getIdentifierType((AstIdentifier) node, elem);
                if (enclosing != null) {
                    if (node.equals(target)) {
                        result = enclosing;
                        return;
                    }
                    Node parent = node.jjtGetParent();
                    for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                        Node child = parent.jjtGetChild(i);
                        if (child instanceof AstPropertySuffix || child instanceof AstMethodSuffix) {
                            ExecutableElement propertyType = getElementForProperty(child, enclosing);
                            if (propertyType == null) {
                                // give up
                                return;
                            }
                            if (child.equals(target)) {
                                result = propertyType;
                            } else {
                                enclosing = info.getTypes().asElement(getReturnType(propertyType));
                            }
                        }
                    }
                }
            }
        }
    }
}
