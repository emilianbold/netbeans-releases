/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source;

import com.sun.javadoc.Doc;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.Scope;
import com.sun.source.util.JavacTask;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.sun.tools.javac.api.JavacTaskImpl;
import com.sun.tools.javac.api.JavacTrees;
import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.code.Source;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.PackageSymbol;
import com.sun.tools.javac.code.Symbol.VarSymbol;
import com.sun.tools.javac.code.Symtab;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.Type.ClassType;
import com.sun.tools.javac.model.JavacElements;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javadoc.DocEnv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import org.netbeans.modules.java.source.builder.ASTService;

import org.netbeans.modules.java.source.builder.ElementsService;
import org.netbeans.modules.java.source.JavadocEnv;
import org.netbeans.modules.java.source.engine.RootTree;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public final class ElementUtilities {
    
    private Context ctx;
    private ElementsService delegate;
    
    /** Creates a new instance of ElementUtilities */
    ElementUtilities(CompilationInfo info) {
        this(info.getJavacTask());
    }
    
    public ElementUtilities(JavacTask task) {
        this.ctx = ((JavacTaskImpl)task).getContext();
        this.delegate = ElementsService.instance(ctx);
    }
    
    /**
     * Returns the type element within which this member or constructor
     * is declared. Does not accept pakages
     * If this is the declaration of a top-level type (a non-nested class
     * or interface), returns null.
     *
     * @return the type declaration within which this member or constructor
     * is declared, or null if there is none
     * @throws IllegalArgumentException if the provided element is a package element
     */
    public TypeElement enclosingTypeElement( Element element ) throws IllegalArgumentException {
	
	if( element.getKind() == ElementKind.PACKAGE ) {
	    throw new IllegalArgumentException();
	}
	
        if (element.getEnclosingElement().getKind() == ElementKind.PACKAGE) {
            //element is a top level class, returning null according to the contract:
            return null;
        }
        
	while( !(element.getEnclosingElement().getKind().isClass() || 
	       element.getEnclosingElement().getKind().isInterface()) ) {
	    element = element.getEnclosingElement();
	}
	
	return (TypeElement)element.getEnclosingElement(); // Wrong
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
    
    /**Returns true if the given element is syntetic.
     * 
     *  @param element to check
     *  @return true if and only if the given element is syntetic, false otherwise
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
                    if (element.getEnclosingElement().getKind() == ElementKind.ANNOTATION_TYPE)
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
            switch (type.getKind()) {
                case DECLARED:
                    for (Element member : elements.getAllMembers((TypeElement)((DeclaredType)type).asElement())) {
                        if (acceptor == null || acceptor.accept(member, type))
                            members.add(member);
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
                    Element classPseudoMember = new VarSymbol(Flags.STATIC | Flags.PUBLIC | Flags.FINAL, Name.Table.instance(ctx)._class, t, ((Type)type).tsym);
                    if (acceptor == null || acceptor.accept(classPseudoMember, type))
                        members.add(classPseudoMember);
                    break;
                case ARRAY:
                    for (Element member : elements.getAllMembers((TypeElement)((Type)type).tsym)) {
                        if (acceptor == null || acceptor.accept(member, type))
                            members.add(member);
                    }
                    break;
            }
        }
        return members;
    }
    
    /**Return members declared in the given scope.
     */
    public Iterable<? extends Element> getLocalMembersAndVars(Scope scope, ElementAcceptor acceptor) {
        ArrayList<Element> members = new ArrayList<Element>();
        HashMap<String, ArrayList<Element>> hiders = new HashMap<String, ArrayList<Element>>();
        Elements elements = JavacElements.instance(ctx);
        Types types = JavacTypes.instance(ctx);
        TypeElement cls;
        while(scope != null) {
            if ((cls = scope.getEnclosingClass()) != null) {
                for (Element local : scope.getLocalElements())
                    if (acceptor == null || acceptor.accept(local, null)) {
                        String name = local.getSimpleName().toString();
                        ArrayList<Element> h = hiders.get(name);
                        if (!isHidden(local, h, elements, types)) {
                            members.add(local);
                            if (h == null) {
                                h = new ArrayList<Element>();
                                hiders.put(name, h);
                            }
                            h.add(local);
                        }
                    }
                TypeMirror type = cls.asType();
                for (Element member : elements.getAllMembers(cls)) {
                    if (acceptor == null || acceptor.accept(member, type)) {
                        String name = member.getSimpleName().toString();
                        ArrayList<Element> h = hiders.get(name);
                        if (!isHidden(member, h, elements, types)) {
                            members.add(member);
                            if (h == null) {
                                h = new ArrayList<Element>();
                                hiders.put(name, h);
                            }
                            h.add(member);
                        }
                    }
                }
            }
            scope = scope.getEnclosingScope();
        }
        while(scope != null) {
            for (Element local : scope.getLocalElements()) {
                if (!local.getKind().isClass() && !local.getKind().isInterface() &&
                    (acceptor == null || acceptor.accept(local, local.getEnclosingElement().asType()))) {
                    String name = local.getSimpleName().toString();
                    ArrayList<Element> h = hiders.get(name);
                    if (!isHidden(local, h, elements, types)) {
                        members.add(local);
                        if (h == null) {
                            h = new ArrayList<Element>();
                            hiders.put(name, h);
                        }
                        h.add(local);
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
        HashMap<String, ArrayList<Element>> hiders = new HashMap<String, ArrayList<Element>>();
        Elements elements = JavacElements.instance(ctx);
        Types types = JavacTypes.instance(ctx);
        while(scope != null && scope.getEnclosingClass() != null) {
            for (Element local : scope.getLocalElements())
                if (acceptor == null || acceptor.accept(local, null)) {
                    String name = local.getSimpleName().toString();
                    ArrayList<Element> h = hiders.get(name);
                    if (!isHidden(local, h, elements, types)) {
                        members.add(local);
                        if (h == null) {
                            h = new ArrayList<Element>();
                            hiders.put(name, h);
                        }
                        h.add(local);
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
        HashSet<TypeElement> members = new HashSet<TypeElement>();
        Trees trees = JavacTrees.instance(ctx);
        RootTree root = (RootTree)ASTService.instance(ctx).getRoot();
        for (CompilationUnitTree unit : root.getCompilationUnits()) {
            TreePath path = new TreePath(unit);
            Element element = trees.getElement(path);
            if (element != null && element.getKind() == ElementKind.PACKAGE) {
                for (Element member : element.getEnclosedElements()) {
                    if (acceptor == null || acceptor.accept(member, null))
                        members.add((TypeElement)member);
                }
            }
            Scope scope = trees.getScope(path);
            while (scope != null) {
                for (Element local : scope.getLocalElements())
                    if ((local.getKind().isClass() || local.getKind().isInterface()) &&
                        (acceptor == null || acceptor.accept(local, null)))
                        members.add((TypeElement)local);
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
    
    private boolean isHidden(Element member, Iterable<Element> hiders, Elements elements, Types types) {
        if (hiders != null) {
            for (Element hider : hiders) {
                if (hider == member || (hider.getClass() == member.getClass() && //TODO: getClass() should not be used here
                    hider.getSimpleName() == member.getSimpleName() &&
                    ((hider.getKind() != ElementKind.METHOD && hider.getKind() != ElementKind.CONSTRUCTOR)
                    || types.isSubsignature((ExecutableType)hider.asType(), (ExecutableType)member.asType()))))
		    return true;
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

    // private implementation --------------------------------------------------


    private List<? extends ExecutableElement> findUnimplementedMethods(TypeElement impl, TypeElement element) {
        List<ExecutableElement> undef = new ArrayList<ExecutableElement>();
        if (element.getModifiers().contains(Modifier.ABSTRACT)) {
            for (Element e : element.getEnclosedElements()) {
                if (e.getKind() == ElementKind.METHOD && e.getModifiers().contains(Modifier.ABSTRACT)) {
                    ExecutableElement ee = (ExecutableElement)e;
                    Element eeImpl = getImplementationOf(ee, impl);
                    if (eeImpl == null || (eeImpl == ee && impl != element))
                        undef.add(ee);
                }
            }
        }
        Types types = JavacTypes.instance(ctx);
        DeclaredType implType = (DeclaredType)impl.asType();
        for (TypeMirror t : types.directSupertypes(element.asType())) {
            for (ExecutableElement ee : findUnimplementedMethods(impl, (TypeElement)((DeclaredType)t).asElement())) {
                //check if "the same" method has already been added:
                boolean exists = false;
                TypeMirror eeType = types.asMemberOf(implType, ee);
                for (ExecutableElement existing : undef) {
                    if (existing.getSimpleName().contentEquals(ee.getSimpleName())) {
                        TypeMirror existingType = types.asMemberOf(implType, existing);
                        if (types.isSameType(eeType, existingType)) {
                            exists = true;
                            break;
                        }
                    }
                }
                if (!exists)
                    undef.add(ee);
            }
        }
        return undef;
    }

}
