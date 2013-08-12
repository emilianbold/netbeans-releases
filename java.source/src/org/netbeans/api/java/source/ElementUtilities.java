/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */
package org.netbeans.api.java.source;

import com.sun.javadoc.Doc;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Scope;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacScope;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.code.Type.MethodType;
import com.sun.tools.javac.comp.AttrContext;
import com.sun.tools.javac.comp.Enter;
import com.sun.tools.javac.comp.Env;
import com.sun.tools.javac.comp.Resolve;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;
import com.sun.tools.javadoc.DocEnv;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.type.TypeVariable;
import javax.lang.model.util.ElementFilter;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.api.annotations.common.CheckForNull;

import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.java.source.builder.ElementsService;
import org.netbeans.modules.java.source.JavadocEnv;

/**
 *
 * @author Jan Lahoda, Dusan Balek, Tomas Zezula
 */
public final class ElementUtilities {
    
    private final Context ctx;
    private final ElementsService delegate;
    private final CompilationInfo info;
    
    /** Creates a new instance of ElementUtilities */
    ElementUtilities(@NonNull final CompilationInfo info) {
        this((JavacTaskImpl)info.impl.getJavacTask(),info);
    }

    ElementUtilities(@NonNull final JavacTaskImpl jt) {
        this(jt,null);
    }

    private ElementUtilities(
        @NonNull final JavacTaskImpl jt,
        @NullAllowed final CompilationInfo info) {
        this.ctx = jt.getContext();
        this.delegate = ElementsService.instance(ctx);
        this.info = info;
    }
    /**
     * Returns the type element within which this member or constructor
     * is declared. Does not accept packages
     * If this is the declaration of a top-level type (a non-nested class
     * or interface), returns null.
     *
     * @return the type declaration within which this member or constructor
     * is declared, or null if there is none
     * @throws IllegalArgumentException if the provided element is a package element
     */
    public TypeElement enclosingTypeElement( Element element ) throws IllegalArgumentException {
        return enclosingTypeElementImpl(element);
    }
    
    static TypeElement enclosingTypeElementImpl( Element element ) throws IllegalArgumentException {
	
	if( element.getKind() == ElementKind.PACKAGE ) {
	    throw new IllegalArgumentException();
	}

        element = element.getEnclosingElement();
	
        if (element.getKind() == ElementKind.PACKAGE) {
            //element is a top level class, returning null according to the contract:
            return null;
        }
        
	while(element != null && !(element.getKind().isClass() || element.getKind().isInterface())) {
	    element = element.getEnclosingElement();
	}
	
	return (TypeElement)element;
    }
    
    /**
     * 
     * The outermost TypeElement which indirectly encloses this element.
     */
    public TypeElement outermostTypeElement(Element element) {
        return delegate.outermostTypeElement(element);
    }
    
    /**
     * Returns the implementation of a method in class origin; null if none exists.
     */
    public Element getImplementationOf(ExecutableElement method, TypeElement origin) {
        return delegate.getImplementationOf(method, origin);
    }
    
    /**Returns true if the given element is synthetic.
     * 
     *  @param element to check
     *  @return true if and only if the given element is synthetic, false otherwise
     */
    public boolean isSynthetic(Element element) {
        return (((Symbol) element).flags() & Flags.SYNTHETIC) != 0 || (((Symbol) element).flags() & Flags.GENERATEDCONSTR) != 0;
    }
    
    /**
     * Returns true if this element represents a method which overrides a
     * method in one of its superclasses.
     */
    public boolean overridesMethod(ExecutableElement element) {
        return delegate.overridesMethod(element);
    }
    
    /**
     * Returns a binary name of a type.
     * @param element for which the binary name should be returned
     * @return the binary name, see Java Language Specification 13.1
     * @throws IllegalArgumentException when the element is not a javac element
     */
    public static String getBinaryName (TypeElement element) throws IllegalArgumentException {
        if (element instanceof Symbol.TypeSymbol) {
            return ((Symbol.TypeSymbol)element).flatName().toString();
        }
        else {
            throw new IllegalArgumentException ();
        } 
    }
    
    /**Get javadoc for given element.
     */
    public Doc javaDocFor(Element element) {
        if (element != null) {
            DocEnv env = DocEnv.instance(ctx);
            switch (element.getKind()) {
                case ANNOTATION_TYPE:
                case CLASS:
                case ENUM:
                case INTERFACE:
                    return env.getClassDoc((ClassSymbol)element);
                case ENUM_CONSTANT:
                case FIELD:
                    return env.getFieldDoc((VarSymbol)element);
                case METHOD:
                    if (((MethodSymbol)element).enclClass().getKind() == ElementKind.ANNOTATION_TYPE)
                        return env.getAnnotationTypeElementDoc((MethodSymbol)element);
                    return env.getMethodDoc((MethodSymbol)element);
                case CONSTRUCTOR:
                    return env.getConstructorDoc((MethodSymbol)element);
                case PACKAGE:
                    return env.getPackageDoc((PackageSymbol)element);
            }
        }
        return null;
    }
    
    /**Find a {@link Element} corresponding to a given {@link Doc}.
     */
    public Element elementFor(Doc doc) {
        return (doc instanceof JavadocEnv.ElementHolder) ? ((JavadocEnv.ElementHolder)doc).getElement() : null;
    }
    
    /**
     * Returns all members of a type, whether inherited or
     * declared directly.  For a class the result also includes its
     * constructors, but not local or anonymous classes.
     * 
     * @param type  the type being examined
     * @param acceptor to filter the members
     * @return all members in the type
     * @see Elements#getAllMembers
     */
    public Iterable<? extends Element> getMembers(TypeMirror type, ElementAcceptor acceptor) {
        ArrayList<Element> members = new ArrayList<Element>();
        if (type != null) {
            Elements elements = JavacElements.instance(ctx);
            Types types = JavacTypes.instance(ctx);
            switch (type.getKind()) {
                case DECLARED:
                case UNION:
                    TypeElement te = (TypeElement)((DeclaredType)type).asElement();
                    if (te == null) break;
                    for (Element member : elements.getAllMembers(te)) {
                        if (acceptor == null || acceptor.accept(member, type)) {
                            if (!isHidden(member, members, elements, types))
                                members.add(member);
                        }
                    }
                    if (te.getKind().isClass() || te.getKind().isInterface() && Source.instance(ctx).allowDefaultMethods()) {
                        VarSymbol thisPseudoMember = new VarSymbol(Flags.FINAL | Flags.HASINIT, Names.instance(ctx)._this, (ClassType)te.asType(), (ClassSymbol)te);
                        if (acceptor == null || acceptor.accept(thisPseudoMember, type))
                            members.add(thisPseudoMember);
                        if (te.getSuperclass().getKind() == TypeKind.DECLARED) {
                            VarSymbol superPseudoMember = new VarSymbol(Flags.FINAL | Flags.HASINIT, Names.instance(ctx)._super, (ClassType)te.getSuperclass(), (ClassSymbol)te);
                            if (acceptor == null || acceptor.accept(superPseudoMember, type))
                                members.add(superPseudoMember);
                        }
                    }
                case BOOLEAN:
                case BYTE:
                case CHAR:
                case DOUBLE:
                case FLOAT:
                case INT:
                case LONG:
                case SHORT:
                case VOID:
                    Type t = Symtab.instance(ctx).classType;
                    com.sun.tools.javac.util.List<Type> typeargs = Source.instance(ctx).allowGenerics() ?
                        com.sun.tools.javac.util.List.of((Type)type) :
                        com.sun.tools.javac.util.List.<Type>nil();
                    t = new ClassType(t.getEnclosingType(), typeargs, t.tsym);
                    Element classPseudoMember = new VarSymbol(Flags.STATIC | Flags.PUBLIC | Flags.FINAL, Names.instance(ctx)._class, t, ((Type)type).tsym);
                    if (acceptor == null || acceptor.accept(classPseudoMember, type))
                        members.add(classPseudoMember);
                    break;
                case ARRAY:
                    for (Element member : elements.getAllMembers((TypeElement)((Type)type).tsym)) {
                        if (acceptor == null || acceptor.accept(member, type))
                            members.add(member);
                    }
                    t = Symtab.instance(ctx).classType;
                    typeargs = Source.instance(ctx).allowGenerics() ?
                        com.sun.tools.javac.util.List.of((Type)type) :
                        com.sun.tools.javac.util.List.<Type>nil();
                    t = new ClassType(t.getEnclosingType(), typeargs, t.tsym);
                    classPseudoMember = new VarSymbol(Flags.STATIC | Flags.PUBLIC | Flags.FINAL, Names.instance(ctx)._class, t, ((Type)type).tsym);
                    if (acceptor == null || acceptor.accept(classPseudoMember, type))
                        members.add(classPseudoMember);
                    break;
            }
        }
        return members;
    }
    
    /**Return members declared in the given scope.
     */
    public Iterable<? extends Element> getLocalMembersAndVars(Scope scope, ElementAcceptor acceptor) {
        ArrayList<Element> members = new ArrayList<Element>();
        Elements elements = JavacElements.instance(ctx);
        Types types = JavacTypes.instance(ctx);
        TypeElement cls;
        while(scope != null) {
            if ((cls = scope.getEnclosingClass()) != null) {
                for (Element local : scope.getLocalElements()) {
                    if (acceptor == null || acceptor.accept(local, null)) {
                        if (!isHidden(local, members, elements, types)) {
                            members.add(local);
                        }
                    }
                }
                TypeMirror type = cls.asType();
                for (Element member : elements.getAllMembers(cls)) {
                    if (acceptor == null || acceptor.accept(member, type)) {
                        if (!isHidden(member, members, elements, types)) {
                            members.add(member);
                        }
                    }
                }
            } else {
                for (Element local : scope.getLocalElements()) {
                    if (!local.getKind().isClass() && !local.getKind().isInterface() &&
                        (acceptor == null || local.getEnclosingElement() != null && acceptor.accept(local, local.getEnclosingElement().asType()))) {
                        if (!isHidden(local, members, elements, types)) {
                            members.add(local);
                        }
                    }
                }
            }
            scope = scope.getEnclosingScope();
        }
        return members;
    }

    /**Return variables declared in the given scope.
     */
    public Iterable<? extends Element> getLocalVars(Scope scope, ElementAcceptor acceptor) {
        ArrayList<Element> members = new ArrayList<Element>();
        Elements elements = JavacElements.instance(ctx);
        Types types = JavacTypes.instance(ctx);
        while(scope != null && scope.getEnclosingClass() != null) {
            for (Element local : scope.getLocalElements()) {
                if (acceptor == null || acceptor.accept(local, null)) {
                    if (!isHidden(local, members, elements, types)) {
                        members.add(local);
                    }
                }
            }
            scope = scope.getEnclosingScope();
        }
        return members;
    }
    
    /**Return {@link TypeElement}s:
     * <ul>
     *    <li>which are imported</li>
     *    <li>which are in the same package as the current file</li>
     *    <li>which are in the java.lang package</li>
     * </ul>
     */
    public Iterable<? extends TypeElement> getGlobalTypes(ElementAcceptor acceptor) {
        ArrayList<TypeElement> members = new ArrayList<TypeElement>();
        Trees trees = JavacTrees.instance(ctx);
        Elements elements = JavacElements.instance(ctx);
        Types types = JavacTypes.instance(ctx);
        for (CompilationUnitTree unit : Collections.singletonList(info.getCompilationUnit())) {
            TreePath path = new TreePath(unit);
            Scope scope = trees.getScope(path);
            while (scope != null && scope instanceof JavacScope && !((JavacScope)scope).isStarImportScope()) {
                for (Element local : scope.getLocalElements()) {
                    if (local.getKind().isClass() || local.getKind().isInterface()) {
                        if (!isHidden(local, members, elements, types)) {
                            if (acceptor == null || acceptor.accept(local, null))
                                members.add((TypeElement)local);
                        }
                    }
                }
                scope = scope.getEnclosingScope();
            }
            Element element = trees.getElement(path);
            if (element != null && element.getKind() == ElementKind.PACKAGE) {
                for (Element member : element.getEnclosedElements()) {
                    if (!isHidden(member, members, elements, types)) {
                        if (acceptor == null || acceptor.accept(member, null))
                            members.add((TypeElement) member);
                    }
                }
            }
            while (scope != null) {
                for (Element local : scope.getLocalElements()) {
                    if (local.getKind().isClass() || local.getKind().isInterface()) {
                        if (!isHidden(local, members, elements, types)) {
                            if (acceptor == null || acceptor.accept(local, null))
                                members.add((TypeElement)local);
                        }
                    }
                }
                scope = scope.getEnclosingScope();
            }
        }
        return members;
    }

    /**Filter {@link Element}s
     */
    public static interface ElementAcceptor {
        /**Is the given element accepted.
         * 
         * @param e element to test
         * @param type the type for which to check if the member is accepted
         * @return true if and only if given element should be accepted
         */
        boolean accept(Element e, TypeMirror type);
    }

    private boolean isHidden(Element member, List<? extends Element> members, Elements elements, Types types) {
        for (ListIterator<? extends Element> it = members.listIterator(); it.hasNext();) {
            Element hider = it.next();
            if (hider == member)
                return true;
            if (hider.getSimpleName() == member.getSimpleName()) {
                if (elements.hides(member, hider)) {
                    it.remove();
                } else {
                    TypeMirror memberType = member.asType();
                    TypeMirror hiderType = hider.asType();
                    if (memberType.getKind() == TypeKind.EXECUTABLE && hiderType.getKind() == TypeKind.EXECUTABLE) {
                        if (types.isSubsignature((ExecutableType)hiderType, (ExecutableType)memberType))
                            return true;
                    } else {
                        return false;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Returns true if the element is declared (directly or indirectly) local
     * to a method or variable initializer.  Also true for fields of inner 
     * classes which are in turn local to a method or variable initializer.
     */
    public boolean isLocal(Element element) {
        return delegate.isLocal(element);
    }
    
    /**
     * Returns true if a method specified by name and type is defined in a
     * class type.
     */
    public boolean alreadyDefinedIn(CharSequence name, ExecutableType method, TypeElement enclClass) {
        return delegate.alreadyDefinedIn(name, method, enclClass);
    }
    
    /**
     * Returns true if a type element has the specified element as a member.
     */
    public boolean isMemberOf(Element e, TypeElement type) {
        return delegate.isMemberOf(e, type);
    }                
    
    /**
     * Returns the parent method which the specified method overrides, or null
     * if the method does not override a parent class method.
     */
    public ExecutableElement getOverriddenMethod(ExecutableElement method) {
        return delegate.getOverriddenMethod(method);
    }        
    /**
     * Returns true if this element represents a method which 
     * implements a method in an interface the parent class implements.
     */
    public boolean implementsMethod(ExecutableElement element) {
        return delegate.implementsMethod(element);
    }
    
    /**Find all methods in given type and its supertypes, which are not implemented.
     * 
     * @param type to inspect
     * @return list of all unimplemented methods
     * 
     * @since 0.20
     */
    public List<? extends ExecutableElement> findUnimplementedMethods(TypeElement impl) {
        return findUnimplementedMethods(impl, impl);
    }


    /**
     * Checks whether 'e' contains error or is missing. If the passed element is null
     * it's assumed the element could not be resolved and this method returns true. Otherwise,
     * the element's type kind is checked against error constants and finally the erroneous
     * state of the element is checked. 
     * 
     * @param e Element to check or {@code null}
     * @return true, if the element is missing (is {@code null}) or contains errors.
     */
    public boolean isErroneous(@NullAllowed Element e) {
        if (e == null) {
            return true;
        }
        final TypeMirror type = e.asType();
        if (type == null) {
            return false;
        }
        if (type.getKind() == TypeKind.ERROR || type.getKind() == TypeKind.OTHER) {
            return true;
        }
        if (type instanceof Type) {
            if (((Type)type).isErroneous()) {
                return true;
            }
        }
        return false;
    }
    
    /** Check whether the given variable is effectively final or final.
     * 
     * @param e variable to check for effectively final status
     * @return true if the given variable is effectively final or final
     * @since 0.112
     */
    public boolean isEffectivelyFinal(VariableElement e) {
        return (((Symbol) e).flags() & (Flags.EFFECTIVELY_FINAL | Flags.FINAL)) != 0;
    }
    
    /**Looks up the given Java element.
     * 
     * The <code>elementDescription</code> format is as follows:
     * <dl>
     *   <dt>for type (class, enum, interface or annotation type)</dt>
     *     <dd><em>the FQN of the type</em></dd>
     *   <dt>for field or enum constant</dt>
     *     <dd><em>the FQN of the enclosing type</em><code>.</code><em>field name</em></dd>
     *   <dt>for method</dt>
     *     <dd><em>the FQN of the enclosing type</em><code>.</code><em>method name</em><code>(</code><em>comma separated parameter types</em><code>)</code><br>
     *         The parameter types may include type parameters, but these are ignored. The last parameter type can use ellipsis (...) to denote vararg method.</dd>
     *   <dt>for constructor</dt>
     *     <dd><em>the FQN of the enclosing type</em><code>.</code><em>simple name of enclosing type</em><code>(</code><em>comma separated parameter types</em><code>)</code><br>
     *         See method format for more details on parameter types.</dd>
     * </dl>
     * 
     * @param elementDescription the description of the element that should be checked for existence
     * @return the found element, or null if not available
     * @since 0.115
     */
    public @CheckForNull Element findElement(@NonNull String description) {
        if (description.contains("(")) {
            //method:
            String methodFullName = description.substring(0, description.indexOf('('));
            String className = methodFullName.substring(0, methodFullName.lastIndexOf('.'));
            TypeElement clazz = info.getElements().getTypeElement(className);
            
            if (clazz == null) return null;
            
            String methodSimpleName = methodFullName.substring(methodFullName.lastIndexOf('.') + 1);
            boolean constructor = clazz.getSimpleName().contentEquals(methodSimpleName);
            String parameters = description.substring(description.indexOf('(') + 1, description.lastIndexOf(')') + 1);
            
            int paramIndex = 0;
            int lastParamStart = 0;
            int angleDepth = 0;
            //XXX:
            List<TypeMirror> types = new ArrayList<TypeMirror>();
            
            while (paramIndex < parameters.length()) {
                switch (parameters.charAt(paramIndex)) {
                    case '<': angleDepth++; break;
                    case '>': angleDepth--; break; //TODO: check underflow
                    case ',':
                        if (angleDepth > 0) break;
                    case ')':
                        if (paramIndex > lastParamStart) {
                            String type = parameters.substring(lastParamStart, paramIndex).replace("...", "[]");
                            //TODO: handle varargs
                            types.add(info.getTypes().erasure(info.getTreeUtilities().parseType(type, info.getTopLevelElements().get(0)/*XXX*/)));
                            lastParamStart = paramIndex + 1;
                        }
                        break;
                }
                
                paramIndex++;
            }
            
            OUTER: for (ExecutableElement ee : constructor ? ElementFilter.constructorsIn(clazz.getEnclosedElements()) : ElementFilter.methodsIn(clazz.getEnclosedElements())) {
                if ((constructor || ee.getSimpleName().contentEquals(methodSimpleName)) && ee.getParameters().size() == types.size()) {
                    Iterator<? extends TypeMirror> real = ((ExecutableType) info.getTypes().erasure(ee.asType())).getParameterTypes().iterator();
                    Iterator<TypeMirror> expected = types.iterator();
                    
                    while (real.hasNext() && expected.hasNext()) {
                        if (!info.getTypes().isSameType(real.next(), expected.next())) {
                            continue OUTER;
                        }
                    }
                    
                    assert real.hasNext() == expected.hasNext();
                    
                    return ee;
                }
            }
        }
        
        //field or class:
        TypeElement el = info.getElements().getTypeElement(description);
        
        if (el != null) return el;
        
        int dot = description.lastIndexOf('.');
        
        if (dot != (-1)) {
            String simpleName = description.substring(dot + 1);
            
            el = info.getElements().getTypeElement(description.substring(0, dot));
            
            if (el != null) {
                for (VariableElement var : ElementFilter.fieldsIn(el.getEnclosedElements())) {
                    if (var.getSimpleName().contentEquals(simpleName)) {
                        return var;
                    }
                }
            }
        }
        
        return null;
    }
    
    // private implementation --------------------------------------------------


    private List<? extends ExecutableElement> findUnimplementedMethods(TypeElement impl, TypeElement element) {
        List<ExecutableElement> undef = new ArrayList<ExecutableElement>();
        Types types = JavacTypes.instance(ctx);
        com.sun.tools.javac.code.Types implTypes = com.sun.tools.javac.code.Types.instance(ctx);
        DeclaredType implType = (DeclaredType)impl.asType();
        if (element.getKind().isInterface() || element.getModifiers().contains(Modifier.ABSTRACT)) {
            for (Element e : element.getEnclosedElements()) {
                if (e.getKind() == ElementKind.METHOD && e.getModifiers().contains(Modifier.ABSTRACT)) {
                    ExecutableElement ee = (ExecutableElement)e;
                    Element eeImpl = getImplementationOf(ee, impl);
                    if ((eeImpl == null || (eeImpl == ee && impl != element)) && implTypes.asSuper((Type)implType, (Symbol)ee.getEnclosingElement()) != null)
                        undef.add(ee);
                }
            }
        }
        for (TypeMirror t : types.directSupertypes(element.asType())) {
            for (ExecutableElement ee : findUnimplementedMethods(impl, (TypeElement) ((DeclaredType) t).asElement())) {
                //check if "the same" method has already been added:
                boolean exists = false;
                ExecutableType eeType = (ExecutableType)types.asMemberOf(implType, ee);
                for (ExecutableElement existing : undef) {
                    if (existing.getSimpleName().contentEquals(ee.getSimpleName())) {
                        ExecutableType existingType = (ExecutableType)types.asMemberOf(implType, existing);
                        if (types.isSubsignature(existingType, eeType)) {
                            TypeMirror existingReturnType = existingType.getReturnType();
                            TypeMirror eeReturnType = eeType.getReturnType();
                            if (!types.isSubtype(existingReturnType, eeReturnType)) {
                                if (types.isSubtype(eeReturnType, existingReturnType)) {
                                    undef.remove(existing);
                                    undef.add(ee);
                                } else if (existingReturnType.getKind() == TypeKind.DECLARED && eeReturnType.getKind() == TypeKind.DECLARED) {
                                    Env<AttrContext> env = Enter.instance(ctx).getClassEnv((TypeSymbol)impl);
                                    DeclaredType subType = env != null ? findCommonSubtype((DeclaredType)existingReturnType, (DeclaredType)eeReturnType, env) : null;
                                    if (subType != null) {
                                        undef.remove(existing);
                                        MethodSymbol ms = ((MethodSymbol)existing).clone((Symbol)impl);
                                        Type mt = implTypes.createMethodTypeWithReturn((MethodType)ms.type, (Type)subType);
                                        ms.type = mt;
                                        undef.add(ms);
                                    }
                                }
                            }
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists) {
                    undef.add(ee);
                }
            }
        }
        return undef;
    }
    
    private DeclaredType findCommonSubtype(DeclaredType type1, DeclaredType type2, Env<AttrContext> env) {
        List<DeclaredType> subtypes1 = getSubtypes(type1, env);
        List<DeclaredType> subtypes2 = getSubtypes(type2, env);
        if (subtypes1 == null || subtypes2 == null) return null;
        Types types = info.getTypes();
        for (DeclaredType subtype1 : subtypes1) {
            for (DeclaredType subtype2 : subtypes2) {
                if (types.isSubtype(subtype1, subtype2))
                    return subtype1;
                if (types.isSubtype(subtype2, subtype1))
                    return subtype2;
            }
        }
        return null;
    }
    
    private List<DeclaredType> getSubtypes(DeclaredType baseType, Env<AttrContext> env) {
        LinkedList<DeclaredType> subtypes = new LinkedList<DeclaredType>();
        HashSet<TypeElement> elems = new HashSet<TypeElement>();
        LinkedList<DeclaredType> bases = new LinkedList<DeclaredType>();
        bases.add(baseType);
        ClassIndex index = info.getClasspathInfo().getClassIndex();
        Trees trees = info.getTrees();
        Types types = info.getTypes();
        Resolve resolve = Resolve.instance(ctx);
        while(!bases.isEmpty()) {
            DeclaredType head = bases.remove();
            TypeElement elem = (TypeElement)head.asElement();
            if (!elems.add(elem))
                continue;
            subtypes.add(head);
            List<? extends TypeMirror> tas = head.getTypeArguments();
            boolean isRaw = !tas.iterator().hasNext();
            Set<ElementHandle<TypeElement>> implementors = index.getElements(ElementHandle.create(elem), EnumSet.of(ClassIndex.SearchKind.IMPLEMENTORS), EnumSet.allOf(ClassIndex.SearchScope.class));
            if (implementors == null) return null; //cancelled
            subtypes:
            for (ElementHandle<TypeElement> eh : implementors) {
                TypeElement e = eh.resolve(info);
                if (e != null) {
                    if (resolve.isAccessible(env, (TypeSymbol)e)) {
                        if (isRaw) {
                            DeclaredType dt = types.getDeclaredType(e);
                            bases.add(dt);
                        } else {
                            HashMap<Element, TypeMirror> map = new HashMap<Element, TypeMirror>();
                            TypeMirror sup = e.getSuperclass();
                            if (sup.getKind() == TypeKind.DECLARED && ((DeclaredType)sup).asElement() == elem) {
                                DeclaredType dt = (DeclaredType)sup;
                                Iterator<? extends TypeMirror> ittas = tas.iterator();
                                Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                while(it.hasNext() && ittas.hasNext()) {
                                    TypeMirror basetm = ittas.next();
                                    TypeMirror stm = it.next();
                                    if (basetm != stm) {
                                        if (stm.getKind() == TypeKind.TYPEVAR) {
                                            map.put(((TypeVariable)stm).asElement(), basetm);
                                        } else {
                                            continue subtypes;
                                        }
                                    }
                                }
                                if (it.hasNext() != ittas.hasNext()) {
                                    continue subtypes;
                                }
                            } else {
                                for (TypeMirror tm : e.getInterfaces()) {
                                    if (((DeclaredType)tm).asElement() == elem) {
                                        DeclaredType dt = (DeclaredType)tm;
                                        Iterator<? extends TypeMirror> ittas = tas.iterator();
                                        Iterator<? extends TypeMirror> it = dt.getTypeArguments().iterator();
                                        while(it.hasNext() && ittas.hasNext()) {
                                            TypeMirror basetm = ittas.next();
                                            TypeMirror stm = it.next();
                                            if (basetm != stm) {
                                                if (stm.getKind() == TypeKind.TYPEVAR) {
                                                    map.put(((TypeVariable)stm).asElement(), basetm);
                                                } else {
                                                    continue subtypes;
                                                }
                                            }
                                        }
                                        if (it.hasNext() != ittas.hasNext()) {
                                            continue subtypes;
                                        }
                                        break;
                                    }
                                }
                            }
                            bases.add(getDeclaredType(e, map, types));
                        }
                    }
                }
            }
        }
        return subtypes;
    }

    private DeclaredType getDeclaredType(TypeElement e, HashMap<? extends Element, ? extends TypeMirror> map, Types types) {
        List<? extends TypeParameterElement> tpes = e.getTypeParameters();
        TypeMirror[] targs = new TypeMirror[tpes.size()];
        int i = 0;
        for (Iterator<? extends TypeParameterElement> it = tpes.iterator(); it.hasNext();) {
            TypeParameterElement tpe = it.next();
            TypeMirror t = map.get(tpe);
            targs[i++] = t != null ? t : tpe.asType();
        }
        Element encl = e.getEnclosingElement();
        if ((encl.getKind().isClass() || encl.getKind().isInterface()) && !((TypeElement)encl).getTypeParameters().isEmpty())
                return types.getDeclaredType(getDeclaredType((TypeElement)encl, map, types), e, targs);
        return types.getDeclaredType(e, targs);
    }
}
