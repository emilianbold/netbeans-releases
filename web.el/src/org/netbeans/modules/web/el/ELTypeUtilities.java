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

import com.sun.el.parser.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.el.ELException;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import org.netbeans.api.java.source.TypeUtilities.TypeNameOptions;
import org.netbeans.modules.web.el.completion.ELStreamCompletionItem;
import org.netbeans.modules.web.el.spi.ELVariableResolver.VariableInfo;
import org.netbeans.modules.web.el.spi.ImplicitObject;
import org.netbeans.modules.web.el.refactoring.RefactoringUtil;
import org.netbeans.modules.web.el.spi.ELPlugin;
import org.netbeans.modules.web.el.spi.ELVariableResolver;
import org.netbeans.modules.web.el.spi.Function;
import org.netbeans.modules.web.el.spi.ImplicitObjectType;
import org.openide.filesystems.FileObject;

/**
 * Utility class for resolving elements/types for EL expressions.
 *
 * @author Erno Mononen
 */
public final class ELTypeUtilities {

    private static final Logger LOG = Logger.getLogger(ELTypeUtilities.class.getName());

    private static final String FACES_CONTEXT_CLASS = "javax.faces.context.FacesContext"; //NOI18N
    private static final String UI_COMPONENT_CLASS = "javax.faces.component.UIComponent";//NOI18N

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

    private ELTypeUtilities() {
        //do not create instancies
    }

    public static String getTypeNameFor(CompilationContext info, Element element) {
        final TypeMirror tm = getTypeMirrorFor(info, element);
        return info.info().getTypeUtilities().getTypeName(tm).toString();
    }

    public static Element getTypeFor(CompilationContext info, Element element) {
        TypeMirror tm = getTypeMirrorFor(info, element);
        return info.info().getTypes().asElement(tm);
    }

    public static List<Element> getSuperTypesFor(CompilationContext info, Element element) {
        return getSuperTypesFor(info, element, null, null);
    }
    
    /**
     * 
     * @param element
     * @return a list of Element-s representing all the superclasses of the element. 
     * The list starts with the given element itself and ends with java.lang.Object
     */
    public static List<Element> getSuperTypesFor(CompilationContext info, Element element, ELElement elElement, List<Node> rootToNode) {
        final TypeMirror tm = getTypeMirrorFor(info, element, elElement, rootToNode);
        List<Element> types = new ArrayList<Element>();
        TypeMirror mirror = tm;
        while (mirror.getKind() == TypeKind.DECLARED) {
            Element el = info.info().getTypes().asElement(mirror);
            types.add(el);

            if (el.getKind() == ElementKind.CLASS) {
                TypeElement tel = (TypeElement) el;
                mirror = tel.getSuperclass();
            } else {
                break;
            }
        }

        return types;
    }

    /**
     * Resolves the element for the given {@code target}.
     * @param elem
     * @param target
     * @return the element or {@code null}.
     */
    public static Element resolveElement(CompilationContext info, final ELElement elem, final Node target) {
        return resolveElement(info, elem, target, Collections.<AstIdentifier, Node>emptyMap());
    }

    /**
     * Resolves the element for the given {@code target}.
     * @param elem
     * @param target
     * @return the element or {@code null}.
     */
    public static Element resolveElement(CompilationContext info, final ELElement elem, final Node target, Map<AstIdentifier, Node> assignments) {
        TypeResolverVisitor typeResolver = new TypeResolverVisitor(info, elem, target, assignments);
        elem.getNode().accept(typeResolver);
        return typeResolver.getResult();
    }

    public static TypeMirror getReturnType(CompilationContext info, final ExecutableElement method) {
        return getReturnType(info, method, null, null);
    }    
    
    /**
     * Gets the return type of the given {@code method}.
     * @param method
     * @return
     */
    public static TypeMirror getReturnType(CompilationContext info, final ExecutableElement method, ELElement elElement, List<Node> rootToNode) {
        TypeKind returnTypeKind = method.getReturnType().getKind();
        if (returnTypeKind.isPrimitive()) {
            return info.info().getTypes().getPrimitiveType(returnTypeKind);
        } else if (returnTypeKind == TypeKind.VOID) {
            return info.info().getTypes().getNoType(returnTypeKind);
        } else if (returnTypeKind == TypeKind.TYPEVAR && rootToNode != null && elElement != null) {
            return getReturnTypeForGenericClass(info, method, elElement, rootToNode);
        } else {
            return method.getReturnType();
        }
    }
    
    public static TypeMirror getReturnTypeForGenericClass(CompilationContext info, final ExecutableElement method, ELElement elElement, List<Node> rootToNode) {
        Node node = null;
        for (int i = rootToNode.size() - 1; i > 0; i--) {
            node = rootToNode.get(i);
            if (node instanceof AstIdentifier) {
                break;
            }
        }
        if (node != null) {
            TypeMirror type = ELTypeUtilities.resolveElement(info, elElement, node).asType();
            // interfaces are at the end of the List - first parameter has to be superclass
            TypeMirror directSupertype = info.info().getTypes().directSupertypes(type).get(0);
            if (directSupertype instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType)directSupertype;
                // index of involved type argument
                int indexOfTypeArgument = -1;
                // list of all type arguments
                List <? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

                // search for the same method in the generic class
                for (Element enclosedElement : declaredType.asElement().getEnclosedElements()) {
                    if (method.equals(enclosedElement)) {
                        TypeMirror returnType = ((ExecutableElement)enclosedElement).getReturnType();
                        // get index of type argument which is returned by involved method
                        indexOfTypeArgument = info.info().getElementUtilities().enclosingTypeElement(method).
                                getTypeParameters().indexOf(((TypeVariable) returnType).asElement());
                        break;
                    }
                }
                if (indexOfTypeArgument != -1 && indexOfTypeArgument < typeArguments.size()) {
                    return typeArguments.get(indexOfTypeArgument);
                }
            }
        } 
        
        return method.getReturnType();
    }

    
    private static List<Node> getMethodParameters(Node methodNode) {
        assert NodeUtil.isMethodCall(methodNode);
        
        if (methodNode.jjtGetNumChildren() == 0) {
            return Collections.emptyList();
        }

        Node firstChild = methodNode.jjtGetChild(0);
        if (!(firstChild instanceof AstMethodArguments)) {
            return Collections.emptyList();
        }
        
        List<Node> parameters = new ArrayList<Node>();
        for (int i = 0; i < firstChild.jjtGetNumChildren(); i++) {
            parameters.add(firstChild.jjtGetChild(i));
        }
        return parameters;
    }

    
    /**
     * @return true if {@code methodNode} and {@code method} have matching parameters;
     * false otherwise.
     */
    public static boolean isSameMethod(CompilationContext info, Node methodNode, ExecutableElement method) {
        String image = methodNode.getImage();
        String methodName = method.getSimpleName().toString();
        TypeMirror methodReturnType = method.getReturnType();
        if (image == null) {
            return false;
        }
        int methodParams = method.getParameters().size();
        if (NodeUtil.isMethodCall(methodNode) &&
                (methodName.equals(image) || RefactoringUtil.getPropertyName(methodName, methodReturnType).equals(image))) {
            //now we are in AstDotSuffix or AstBracketSuffix
            
            //lets check if the parameters are equal
            List<Node> parameters = getMethodParameters(methodNode);
            int methodNodeParams = parameters.size();
            if (method.isVarArgs()) {
                return methodParams == 1 ? true : methodNodeParams >= methodParams;
            }
            return method.getParameters().size() == methodNodeParams && haveSameParameters(info, methodNode, method);
        }

        if (methodNode instanceof AstDotSuffix
                && (methodName.equals(image) || RefactoringUtil.getPropertyName(methodName, methodReturnType).equals(image))) {

            if (methodNode.jjtGetNumChildren() > 0) {
                for (int i = 0; i < method.getParameters().size(); i++) {
                    final VariableElement methodParameter = method.getParameters().get(i);
                    final Node methodNodeParameter = methodNode.jjtGetChild(i);
                    
                    if (!isSameType(info, methodNodeParameter, methodParameter)) {
                        return false;
                    }
                }
            }
            
            if (image.equals(methodName)) {
                return true;
            }
            
            return method.isVarArgs()
                    ? method.getParameters().size() == 1
                    : method.getParameters().isEmpty();
        }
        return false;
    }

    public static TypeElement getElementForType(CompilationContext info, final String clazz) {
        return info.info().getElements().getTypeElement(clazz);
    }

    public static List<String> getParameterNames(CompilationContext info, final ExecutableElement method) {
        List<String> result = new ArrayList<String>();
        for (VariableElement param : method.getParameters()) {
            result.add(param.getSimpleName().toString());
        }
        return result;
    }

    public static String getParametersAsString(CompilationContext info, ExecutableElement method) {
        StringBuilder result = new StringBuilder();
        for (VariableElement param : method.getParameters()) {
            if (result.length() > 0) {
                result.append(",");
            }
            String type = info.info().getTypeUtilities().getTypeName(param.asType()).toString();
            result.append(type);
            result.append(" ");
            result.append(param.getSimpleName().toString());
        }

        if (result.length() > 0) {
            result.insert(0, "(");
            result.append(")");
        }
        return result.toString();
    }

    public static Collection<ImplicitObject> getImplicitObjects(CompilationContext info) {
        return ELPlugin.Query.getImplicitObjects(info.file());
    }

    public static Collection<Function> getELFunctions(CompilationContext info) {
        return ELPlugin.Query.getFunctions(info.file());
    }

    public static boolean isScopeObject(CompilationContext info, Node target) {
        if (!(target instanceof AstIdentifier)) {
            return false;
        }
        for (ImplicitObject each : getImplicitObjects(info)) {
            if (each.getType() == ImplicitObjectType.SCOPE_TYPE
                    && each.getName().equals(target.getImage())) {
                return true;
            }
        }
        return false;
    }

    public static boolean isRawObjectReference(CompilationContext info, Node target) {
//        Parse tree for #{cc.attrs.muj} expression
//
//        CompositeExpression
//            DeferredExpression
//                Value
//                    Identifier[cc]
//                    PropertySuffix[attrs]
//                    PropertySuffix[muj]

        do {
            if (target instanceof AstIdentifier) {
                for (ImplicitObject each : getImplicitObjects(info)) {
                    if (each.getType() == ImplicitObjectType.RAW
                            && each.getName().equals(target.getImage())) {
                        return true;
                    }
                }
            } 
            
            target = NodeUtil.getSiblingBefore(target);
            
        } while (target != null);
        
        return false;
    }
    
    public static boolean isResourceBundleVar(CompilationContext info, Node target) {
        if (!(target instanceof AstIdentifier)) {
            return false;
        }
        ResourceBundles resourceBundles = ResourceBundles.get(info.file());
        if (!resourceBundles.canHaveBundles()) {
            return false;
        }
        String bundleVar = target.getImage();
        return resourceBundles.isResourceBundleIdentifier(bundleVar, info.context());
    }

    private static TypeMirror getTypeMirrorFor(CompilationContext info, Element element) {
        return getTypeMirrorFor(info, element, null, null);
    }

    private static TypeMirror getTypeMirrorFor(CompilationContext info, Element element, ELElement elElement, List<Node> rootToNode) {
        if (element.getKind() == ElementKind.METHOD) {
            return getReturnType(info, (ExecutableElement) element, elElement, rootToNode);
        }
        return element.asType();
    }

    private static boolean isValidatorMethod(CompilationContext info, ExecutableElement method) {
        if (method.getParameters().size() != 3) {
            return false;
        }
        VariableElement param1 = method.getParameters().get(0);
        VariableElement param2 = method.getParameters().get(1);
        CharSequence param1Type = getTypeName(info, param1.asType());
        CharSequence param2Type = getTypeName(info, param2.asType());
        return FACES_CONTEXT_CLASS.equals(param1Type) && UI_COMPONENT_CLASS.equals(param2Type);
    }

    private static CharSequence getTypeName(CompilationContext info, TypeMirror type) {
        return info.info().getTypeUtilities().getTypeName(type, TypeNameOptions.PRINT_FQN);
    }

    private static boolean haveSameParameters(CompilationContext info, Node methodNode, ExecutableElement method) {
        List<Node> methodNodeParameters = getMethodParameters(methodNode);
        for (int i = 0; i < methodNodeParameters.size(); i++) {
            Node paramNode = methodNodeParameters.get(i);
            if (!isSameType(info, paramNode, method.getParameters().get(i))) {
                return false;
            }
        }
        return true;
    }

    private static boolean isSameType(CompilationContext info, final Node paramNode, final VariableElement param) {
        TypeKind paramKind = param.asType().getKind();
        if (!paramKind.isPrimitive()) {
            // try unboxing
            try {
                PrimitiveType unboxedType = info.info().getTypes().unboxedType(param.asType());
                paramKind = unboxedType.getKind();
            } catch (IllegalArgumentException iae) {
                // not unboxable (isn't there a way to check this before trying to unbox??)
            }

        }
        if (TYPES.containsKey(paramNode.getClass())) {
            return TYPES.get(paramNode.getClass()).contains(paramKind);
        }
        if (paramNode instanceof AstString) {
            CharSequence typeName = info.info().getTypeUtilities().getTypeName(param.asType(), TypeNameOptions.PRINT_FQN);
            return String.class.getName().contentEquals(typeName);
        }
        // the ast param is an object whose real type we don't know
        // would need to further type inference for more exact matching
        return true;

    }

    /**
     * Gets the element matching the given name from the given enclosing class.
     * @param name the name of the element to find.
     * @param enclosing
     * @return
     */
    private static ExecutableElement getElementForProperty(CompilationContext info, Node property, Element enclosing) {
        for (Element element : getSuperTypesFor(info, enclosing)) {
            for (ExecutableElement each : ElementFilter.methodsIn(element.getEnclosedElements())) {
                // we're only interested in public methods
                if (!each.getModifiers().contains(Modifier.PUBLIC)) {
                    continue;
                }
                if (isSameMethod(info, property, each)) {
                    return each;
                }
            }
        }
        return null;
    }

    private static Element getIdentifierType(CompilationContext info, AstIdentifier identifier, ELElement element) {
        if (info.file() == null) {
            // Strange case - file was deleted? Try to find out whether it's invalid.
            FileObject fileObject = element.getSnapshot().getSource().getFileObject();
            LOG.log(Level.WARNING, "FileObject to resolve doesn''t exist: {0}, isValid: {1}",
                    new Object[]{fileObject, fileObject != null ? fileObject.isValid() : "null"});
            return null;
        }
        String tempClass = null;
        // try implicit objects first
        for (ImplicitObject implicitObject : getImplicitObjects(info)) {
            if (implicitObject.getName().equals(identifier.getImage())) {
                if (implicitObject.getClazz() == null || implicitObject.getClazz().isEmpty()) {
                    // the identiefier represents an implicit object whose type we don't know
//                    tempClass = Object.class.getName();
                } else {
                    tempClass = implicitObject.getClazz();
                }
                break;
            }
        }
        if (tempClass == null) {
            // managed beans
            tempClass = ELVariableResolvers.findBeanClass(info, identifier.getImage(), element.getSnapshot().getSource().getFileObject());
        }
        if (tempClass != null) {
            return info.info().getElements().getTypeElement(tempClass);
        }

        // probably a variable
        int offset = element.getOriginalOffset().getStart() + identifier.startOffset();

        Collection<ELVariableResolver.VariableInfo> vis = ELVariableResolvers.getVariables(info, element.getSnapshot(), offset);
        for (ELVariableResolver.VariableInfo vi : vis) {
            if (identifier.getImage().equals(vi.name)) {
                try {
                    ELPreprocessor elp = new ELPreprocessor(vi.expression, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
                    Node expressionNode = ELParser.parse(elp);
                    if (expressionNode != null) {
                        return getReferredType(info, expressionNode, element.getSnapshot().getSource().getFileObject());
                    }
                } catch (ELException e) {
                    //invalid expression
                }
            }
        }

        return null;

    }

    /**
     * Resolves the given variable type
     * @param vi the variable to be resolved
     * @return source Element representing the variable
     */
    public static Element getReferredType(CompilationContext info, VariableInfo vi, FileObject context) {
        //resolved variable
        if (vi.clazz != null) {
            return info.info().getElements().getTypeElement(vi.clazz);
        }

        //unresolved variable
        assert vi.expression != null;
        try {
            ELPreprocessor elp = new ELPreprocessor(vi.expression, ELPreprocessor.XML_ENTITY_REFS_CONVERSION_TABLE);
            Node expressionNode = ELParser.parse(elp);
            if (expressionNode != null) {
                return getReferredType(info, expressionNode, context);
            }
        } catch (ELException e) {
            //invalid expression
        }

        return null;
    }

    /**
     * @return the element for the type that that given {@code expression} refers to, i.e.
     * the return type of the last method in the expression.
     * 
     * The method can ONLY be used for resolved expressions, i.e. the base object must be a known bean,
     * not a variable!
     */
    public static Element getReferredType(final CompilationContext info, Node expression, final FileObject context) {

        final Element[] result = new Element[1];
        expression.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                if (node instanceof AstIdentifier) {
                    Node parent = node.jjtGetParent();
                    String beanClass = ELVariableResolvers.findBeanClass(info, node.getImage(), context);
                    if (beanClass == null) {
                        return;
                    }
                    Element enclosing = info.info().getElements().getTypeElement(beanClass);
                    if (enclosing == null) {
                        //no such class on the classpath
                        return;
                    }
                    ExecutableElement method = null;
                    Node current = parent;
                    for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                        current = parent.jjtGetChild(i);
                        if (current instanceof AstDotSuffix || NodeUtil.isMethodCall(current)) {
                            method = getElementForProperty(info, current, enclosing);
                            if (method == null) {
                                continue;
                            }
                            enclosing = info.info().getTypes().asElement(getReturnType(info, method));
                        }
                    }
                    if (method == null) {
                        return;
                    }
                    TypeMirror returnType = getReturnType(info, method);
                    //XXX: works just for generic collections, i.e. the assumption is
                    // that variables refer to collections, which is not always the case

                    if (returnType.getKind() == TypeKind.DECLARED) {
                        if (isSubtypeOf(info, returnType, "java.lang.Iterable")) { //NOI18N
                            List<? extends TypeMirror> typeArguments = ((DeclaredType) returnType).getTypeArguments();
                            for (TypeMirror arg : typeArguments) {
                                result[0] = info.info().getTypes().asElement(arg);
                                return;
                            }
                            //use the returned type itself
                            result[0] = info.info().getTypes().asElement(returnType);
                        }
                    } else if (returnType.getKind() == TypeKind.ARRAY) {
                        TypeMirror componentType = ((ArrayType) returnType).getComponentType();
                        result[0] = info.info().getTypes().asElement(componentType);
                    }
                }
            }
        });

        return result[0];
    }

    /**
     * Whether the given node represents static {@link Iterable} field where can be used operators.
     * @param ccontext compilation context
     * @param node node to examine
     * @return {@code true} if the object is static {@link Iterable} field, {@code false} otherwise
     * @since 1.26
     */
    public static boolean isStaticIterableElement(CompilationContext ccontext, Node node) {
        return (node instanceof AstListData || node instanceof AstMapData);
    }

    /**
     * Whether the given node represents static {@link Iterable} field where can be used operators.
     * @param ccontext compilation context
     * @param element element to examine
     * @return {@code true} if the object is static {@link Iterable} field, {@code false} otherwise
     * @since 1.26
     */
    public static boolean isIterableElement(CompilationContext ccontext, Element element) {
        if (element.getKind() == ElementKind.METHOD) {
            TypeMirror returnType = ELTypeUtilities.getReturnType(ccontext, (ExecutableElement) element);
            if (returnType.getKind() == TypeKind.ARRAY
                    || isSubtypeOf(ccontext, returnType, "java.lang.Iterable")) { //NOI18N
                return true;
            }
        } else if (element.getKind() == ElementKind.INTERFACE) {
            return isSubtypeOf(ccontext, element.asType(), "java.lang.Iterable"); //NOI18N
        }
        return false;
    }

    /**
     * Whether the given node represents Map field.
     * @param ccontext compilation context
     * @param element element to examine
     * @return {@code true} if the element extends {@link Map} interface, {@code false} otherwise
     * @since 1.28
     */
    public static boolean isMapElement(CompilationContext ccontext, Element element) {
        return isSubtypeOf(ccontext, element.asType(), "java.util.Map"); //NOI18N
    }

    private static boolean isSubtypeOf(CompilationContext info, TypeMirror tm, CharSequence typeName) {
        Element element = info.info().getElements().getTypeElement(typeName);
        if (element == null) {
            return false;
        }
        TypeMirror type = element.asType(); //NOI18N
        TypeMirror erasedType = info.info().getTypes().erasure(type);
        TypeMirror tmErasure = info.info().getTypes().erasure(tm);

        return info.info().getTypes().isSubtype(tmErasure, erasedType);
    }

    private static TypeElement getTypeFor(CompilationContext info, String clazz) {
        return info.info().getElements().getTypeElement(clazz);
    }

    private static class TypeResolverVisitor implements NodeVisitor {

        private final ELElement elem;
        private final Node target;
        private final Map<AstIdentifier, Node> assignments;
        private Element result;
        private CompilationContext info;

        public TypeResolverVisitor(CompilationContext info, ELElement elem, Node target, Map<AstIdentifier, Node> assignments) {
            this.info = info;
            this.elem = elem;
            this.target = target;
            this.assignments = assignments;
        }

        public Element getResult() {
            return result;
        }

        @Override
        public void visit(Node node) {
            Element enclosing = null;

            // look for possible assignments to the identifier
            Node evalNode;
            if (node instanceof AstIdentifier && assignments.containsKey((AstIdentifier) node)) {
                evalNode = assignments.get((AstIdentifier) node);
            } else {
                evalNode = node;
            }

            // traverses AST resolving types for each property starting from
            // an identifier until the target is found
            if (evalNode instanceof AstIdentifier) {
                enclosing = getIdentifierType(info, (AstIdentifier) evalNode, elem);
                if (enclosing != null) {
                    if (node.equals(target)) {
                        result = enclosing;
                        return;
                    }
                    Node parent = node.jjtGetParent();
                    for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                        Node child = parent.jjtGetChild(i);
                        if (child instanceof AstDotSuffix || NodeUtil.isMethodCall(child)) {
                            Element propertyType = getElementForProperty(info, child, enclosing);
                            if (propertyType == null) {
                                // special case handling for scope objects; their types don't help
                                // in resolving the beans they contain. The code below handles cases
                                // like sessionScope.myBean => sessionScope is in position parent.jjtGetChild(i - 1)
                                if (i > 0 && isScopeObject(info, parent.jjtGetChild(i - 1))) {
                                    final String clazz = ELVariableResolvers.findBeanClass(info, child.getImage(), elem.getSnapshot().getSource().getFileObject());
                                    if (clazz == null) {
                                        return;
                                    }
                                    // it's a managed bean in a scope
                                    propertyType = getTypeFor(info, clazz);
                                }

                                // maps
                                if (ELTypeUtilities.isMapElement(info, enclosing)) {
                                    result = info.info().getElements().getTypeElement("java.lang.Object"); //NOI18N
                                    return;
                                }

                                // stream method
                                if (ELTypeUtilities.isIterableElement(info, enclosing)) {
                                    propertyType = enclosing = info.info().getElements().getTypeElement("com.sun.el.stream.Stream"); //NOI18N
                                }
                            }
                            if (propertyType == null) {
                                return;
                            }
                            if (child.equals(target)) {
                                result = propertyType;
                            } else if (propertyType.getKind() == ElementKind.METHOD) {
                                final ExecutableElement method = (ExecutableElement) propertyType;
                                TypeMirror returnType = getReturnType(info, method);
                                if (returnType.getKind() == TypeKind.ARRAY) {
                                    // for array try to look like Iterable (operators for array return type)
                                    enclosing = info.info().getElements().getTypeElement("java.lang.Iterable"); //NOI18N
                                } else {
                                    enclosing = info.info().getTypes().asElement(returnType);
                                }
                                if (enclosing == null) {
                                    return;
                                }
                            } else {
                                enclosing = propertyType;
                            }
                        }
                    }
                }
            } else if (evalNode instanceof AstListData || evalNode instanceof AstMapData) {
                Node parent = node.jjtGetParent();
                for (int i = 0; i < parent.jjtGetNumChildren(); i++) {
                    Node child = parent.jjtGetChild(i);
                    if (child instanceof AstDotSuffix) {
                        if (ELStreamCompletionItem.STREAM_METHOD.equals(child.getImage())) {
                            if (target.getImage() != null && target.getImage().equals(child.getImage())) {
                                result = info.info().getElements().getTypeElement("com.sun.el.stream.Stream"); //NOI18N
                                return;
                            } else {
                                enclosing = info.info().getElements().getTypeElement("com.sun.el.stream.Stream"); //NOI18N
                            }
                        } else {
                            if (enclosing != null) {
                                Element propertyType = getElementForProperty(info, child, enclosing);
                                if (child.equals(target)) {
                                    result = propertyType;
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
