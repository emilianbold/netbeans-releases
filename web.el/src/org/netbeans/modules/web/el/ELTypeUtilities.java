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

import com.sun.el.parser.AstFalse;
import com.sun.el.parser.AstFloatingPoint;
import com.sun.el.parser.AstIdentifier;
import com.sun.el.parser.AstInteger;
import com.sun.el.parser.AstMethodSuffix;
import com.sun.el.parser.AstPropertySuffix;
import com.sun.el.parser.AstString;
import com.sun.el.parser.AstTrue;
import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.el.ELException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.Task;
import org.netbeans.api.java.source.TypeUtilities.TypeNameOptions;
import org.netbeans.modules.web.core.syntax.spi.ELImplicitObject;
import org.netbeans.modules.web.core.syntax.spi.ImplicitObjectProvider;
import org.netbeans.modules.web.el.refactoring.RefactoringUtil;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;

/**
 * Utility class for resolving elements/types for EL expressions.
 *
 * @author Erno Mononen
 */
public final class ELTypeUtilities {

    private static final String FACES_CONTEXT_CLASS = "javax.faces.context.FacesContext"; //NOI18N
    private static final String UI_COMPONENT_CLASS = "javax.faces.component.UIComponent";//NOI18N
    private final ClasspathInfo cpInfo;

    private static final Map<Class<? extends Node>, Set<TypeKind>> TYPES = new HashMap<Class<? extends Node>, Set<TypeKind>>();
    static {
        put(AstFloatingPoint.class, TypeKind.FLOAT, TypeKind.DOUBLE);
        put(AstTrue.class, TypeKind.BOOLEAN);
        put(AstFalse.class, TypeKind.BOOLEAN);
        put(AstInteger.class, TypeKind.INT, TypeKind.SHORT, TypeKind.LONG);
    }

    private static void put(Class<? extends Node> node, TypeKind... kinds) {
        Set<TypeKind> kindSet = new HashSet<TypeKind>();
        kindSet.addAll(Arrays.asList(kinds));
        TYPES.put(node, kindSet);
    }

    private ELTypeUtilities(ClasspathInfo cpInfo) {
        assert cpInfo != null;
        this.cpInfo = cpInfo;
    }

    public static ELTypeUtilities create(ClasspathInfo cpInfo) {
        return new ELTypeUtilities(cpInfo);
    }

    public static ELTypeUtilities create(FileObject context) {
        ClasspathInfo cp = ClasspathInfo.create(context);
        return create(cp);
    }

    public String getTypeNameFor(Element element) {
        final TypeMirror tm = getTypeMirrorFor(element);
        SourceTask<String> task = new SourceTask<String>() {

            @Override
            public void run(CompilationController info) throws Exception {
                setResult(info.getTypeUtilities().getTypeName(tm).toString());
            }
        };
        runTask(task);
        return task.getResult();
    }

    public Element getTypeFor(Element element) {
        final TypeMirror tm = getTypeMirrorFor(element);
        SourceTask<Element> task = new SourceTask<Element>() {

            @Override
            public void run(CompilationController info) throws Exception {
                setResult(info.getTypes().asElement(tm));
            }
        };
        runTask(task);
        return task.getResult();
    }

    /**
     * Resolves the element for the given {@code target}.
     * @param elem
     * @param target
     * @return the element or {@code null}.
     */
    public Element resolveElement(final ELElement elem, final Node target) {
        TypeResolverVisitor typeResolver = new TypeResolverVisitor(elem, target);
        elem.getNode().accept(typeResolver);
        return typeResolver.getResult();
    }

    /**
     * Gets the return type of the given {@code method}.
     * @param method
     * @return
     */
    public TypeMirror getReturnType(final ExecutableElement method) {
        SourceTask<TypeMirror> task = new SourceTask<TypeMirror>() {
            @Override
            public void run(CompilationController info) throws Exception {
                TypeKind returnTypeKind = method.getReturnType().getKind();
                if (returnTypeKind.isPrimitive()) {
                    setResult(info.getTypes().getPrimitiveType(returnTypeKind));
                } else if (returnTypeKind == TypeKind.VOID) {
                    setResult(info.getTypes().getNoType(returnTypeKind));
                } else {
                    setResult(method.getReturnType());
                }
            }
        };
        runTask(task);
        return task.getResult();
    }

    /**
     * @return true if {@code methodNode} and {@code method} have matching parameters;
     * false otherwise.
     */
    public boolean isSameMethod(Node methodNode, ExecutableElement method) {
        String image = methodNode.getImage();
        String methodName = method.getSimpleName().toString();
        if (image == null) {
            return false;
        }
        int methodParams = method.getParameters().size();
        if (methodNode instanceof AstMethodSuffix
                && (methodName.equals(image) || RefactoringUtil.getPropertyName(methodName).equals(image))) {
            int methodNodeParams = ((AstMethodSuffix) methodNode).jjtGetNumChildren();
            if (method.isVarArgs()) {
                return methodParams == 1 ? true : methodNodeParams >= methodParams;
            }
            return method.getParameters().size() == methodNodeParams
                    && haveSameParameters((AstMethodSuffix) methodNode, method);
        }

        if (methodNode instanceof AstPropertySuffix
                && (methodName.equals(image) || RefactoringUtil.getPropertyName(methodName).equals(image))) {

            // for validators params are passed automatically (they are not present in EL)
            if (isValidatorMethod(method)) {
                return true;
            }

            return method.isVarArgs()
                    ? method.getParameters().size() == 1
                    : method.getParameters().isEmpty();
        }
        return false;
    }

    public TypeElement getElementForType(final String clazz) {
        SourceTask<TypeElement> task = new SourceTask<TypeElement>() {
            @Override
            public void run(CompilationController info) throws Exception {
                TypeElement typeElement = info.getElements().getTypeElement(clazz);
                setResult(typeElement);
            }
        };

        runTask(task);
        return task.getResult();
    }

    public String getParametersAsString(final ExecutableElement method) {
        SourceTask<String> task = new SourceTask<String>() {

            @Override
            public void run(CompilationController info) throws Exception {
                StringBuilder result = new StringBuilder();
                for (VariableElement param : method.getParameters()) {
                    if (result.length() > 0) {
                        result.append(",");
                    }
                    String type = info.getTypeUtilities().getTypeName(param.asType()).toString();
                    result.append(type);
                    result.append(" ");
                    result.append(param.getSimpleName().toString());
                }

                if (result.length() > 0) {
                    result.insert(0, "(");
                    result.append(")");
                }
                setResult(result.toString());
            }
        };
        runTask(task);
        return task.getResult();
    }

    public static Collection<ELImplicitObject> getImplicitObjects() {
        Set<ELImplicitObject> result = new HashSet<ELImplicitObject>();
        Collection<? extends ImplicitObjectProvider> providers =
                Lookup.getDefault().lookupAll(ImplicitObjectProvider.class);
        
        for (ImplicitObjectProvider each : providers) {
            result.addAll(each.getImplicitObjects());
        }
        return result;
    }

    private TypeMirror getTypeMirrorFor(Element element) {
        if (element.getKind() == ElementKind.METHOD) {
            return getReturnType((ExecutableElement) element);
        }
        return element.asType();
    }
    
    private void runTask(SourceTask<?> task) {
        try {
            JavaSource.create(cpInfo).runUserActionTask(task, true);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
        task.setComputed(true);
    }

    private boolean isValidatorMethod(ExecutableElement method) {
        if (method.getParameters().size() != 3) {
            return false;
        }
        VariableElement param1 = method.getParameters().get(0);
        VariableElement param2 = method.getParameters().get(1);
        CharSequence param1Type = getTypeName(param1.asType());
        CharSequence param2Type = getTypeName(param2.asType());
        return FACES_CONTEXT_CLASS.equals(param1Type) && UI_COMPONENT_CLASS.equals(param2Type);
    }

    private CharSequence getTypeName(final TypeMirror type) {
        SourceTask<CharSequence> task = new SourceTask<CharSequence>() {

            @Override
            public void run(CompilationController info) throws Exception {
                setResult(info.getTypeUtilities().getTypeName(type, TypeNameOptions.PRINT_FQN));
            }
        };
        runTask(task);
        return task.getResult();
    }

    private boolean haveSameParameters(AstMethodSuffix methodNode, ExecutableElement method) {
        for (int i = 0; i < methodNode.jjtGetNumChildren(); i++) {
            Node paramNode = methodNode.jjtGetChild(i);
            if (!isSameType(paramNode, method.getParameters().get(i))) {
                return false;
            }
        }
        return true;
    }

    private boolean isSameType(final Node paramNode, final VariableElement param) {
        SourceTask<Boolean> task = new SourceTask<Boolean>() {

            @Override
            public void run(CompilationController info) throws Exception {
                TypeKind paramKind = param.asType().getKind();
                if (!paramKind.isPrimitive()) {
                    // try unboxing
                    try {
                        PrimitiveType unboxedType = info.getTypes().unboxedType(param.asType());
                        paramKind = unboxedType.getKind();
                    } catch (IllegalArgumentException iae) {
                        // not unboxable (isn't there a way to check this before trying to unbox??)
                    }

                }
                if (TYPES.containsKey(paramNode.getClass())) {
                    setResult(TYPES.get(paramNode.getClass()).contains(paramKind));
                }
                if (paramNode instanceof AstString) {
                    CharSequence typeName = info.getTypeUtilities().getTypeName(param.asType(), TypeNameOptions.PRINT_FQN);
                    setResult(String.class.getName().contentEquals(typeName));
                }
                // the ast param is an object whose real type we don't know
                // would need to further type inference for more exact matching
                setResult(true);
            }
        };
        runTask(task);
        return task.getResult();
    }

    /**
     * Gets the element matching the given name from the given enclosing class.
     * @param name the name of the element to find.
     * @param enclosing
     * @return
     */
    private ExecutableElement getElementForProperty(Node property, Element enclosing) {
        for (ExecutableElement each : ElementFilter.methodsIn(enclosing.getEnclosedElements())) {
            // we're only interested in public methods
            if (!each.getModifiers().contains(Modifier.PUBLIC)) {
                continue;
            }
            if (isSameMethod(property, each)) {
                return each;
            }
        }
        return null;
    }

    private Element getIdentifierType(final AstIdentifier identifier, final ELElement element) {
        String tempClass = null;
        // try implicit objects first
        for (ELImplicitObject implicitObject : getImplicitObjects()) {
            if (implicitObject.getName().equals(identifier.getImage())) {
                if (implicitObject.getClazz() == null || implicitObject.getClazz().isEmpty()) {
                    // the identiefier represents an implicit object whose type we don't know
                    tempClass = Object.class.getName();
                } else {
                    tempClass = implicitObject.getClazz();
                }
                break;
            }
        }
        if (tempClass == null) {
            // managed beans
            tempClass = ELVariableResolvers.findBeanClass(identifier.getImage(), element.getParserResult().getFileObject());
        }
        final String clazz = tempClass;
        SourceTask<Element> task = new SourceTask<Element>() {

            @Override
            public void run(CompilationController info) throws Exception {
                if (clazz != null) {
                    setResult(info.getElements().getTypeElement(clazz));
                }
                // probably a variable
                int offset = element.getOriginalOffset().getStart() + identifier.startOffset();
                Node expressionNode = ELVariableResolvers.getReferredExpression(element.getParserResult().getSnapshot(), offset);
                if (expressionNode != null) {
                    setResult(getReferredType(expressionNode, element.getParserResult().getFileObject(), info));
                }
            }
        };
        runTask(task);
        return task.getResult();
    }

    /**
     * @return the element for the type that that given {@code expression} refers to, i.e.
     * the return type of the last method in the expression.
     */
    private Element getReferredType(Node expression, final FileObject context, final CompilationController info) {

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
                            final ExecutableElement propertyType = getElementForProperty(child, enclosing);
                            if (propertyType == null) {
                                // give up
                                return;
                            }
                            if (child.equals(target)) {
                                result = propertyType;
                            } else {
                                SourceTask<Element> task = new SourceTask<Element>() {

                                    @Override
                                    public void run(CompilationController info) throws Exception {
                                        setResult(info.getTypes().asElement(getReturnType(propertyType)));
                                    }
                                };
                                runTask(task);
                                enclosing = task.getResult();
                            }
                        }
                    }
                }
            }
        }
    }

    private static abstract class SourceTask<T> implements Task<CompilationController> {

        private volatile T result;
        private volatile boolean computed;

        public void setComputed(boolean computed) {
            this.computed = computed;
        }

        public T getResult() {
            assert computed;
            return result;
        }

        public void setResult(T result) {
            this.result = result;
        }
    }
}
