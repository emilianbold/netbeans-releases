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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.api.java.source;

import com.sun.javadoc.Doc;
import com.sun.source.tree.Scope;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
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
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javadoc.DocEnv;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.ExecutableType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;

import org.netbeans.modules.java.builder.ElementsService;
import org.netbeans.modules.java.source.JavadocEnv;

/**
 *
 * @author Jan Lahoda, Dusan Balek
 */
public final class ElementUtilities {
    
    private CompilationInfo info;
    private org.netbeans.jackpot.model.ElementUtilities delegate;
    
    /** Creates a new instance of ElementUtilities */
    ElementUtilities(CompilationInfo info) {
        this.info = info;
        this.delegate = ElementsService.instance(info.getJavacTask().getContext());
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
    public boolean isSyntetic(Element element) {
        return delegate.isSynthetic(element);
    }
    
    /**
     * Returns a binary name of a type.
     * @param element for which the binary name should be returned
     * @return the binary name, see Java Language Specification 13.1
     * @throws IllegalArgumentException when the element is not a javac element
     */
    public String getBinaryName (TypeElement element) throws IllegalArgumentException {
        if (element instanceof Symbol.TypeSymbol) {
            return ((Symbol.TypeSymbol)element).flatName().toString();
        }
        else {
            throw new IllegalArgumentException ();
        } 
    }
    
    public Doc javaDocFor(Element element) {
        if (element != null) {
            DocEnv env = DocEnv.instance(info.getJavacTask().getContext());
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
                    return env.getMethodDoc((MethodSymbol)element);
                case CONSTRUCTOR:
                    return env.getConstructorDoc((MethodSymbol)element);
                case PACKAGE:
                    return env.getPackageDoc((PackageSymbol)element);
            }
        }
        return null;
    }
    
    public Element elementFor(Doc doc) {
        return (doc instanceof JavadocEnv.ElementHolder) ? ((JavadocEnv.ElementHolder)doc).getElement() : null;
    }
    
    public Iterable<? extends Element> getMembers(TypeMirror type, ElementAcceptor acceptor) {
        ArrayList<Element> members = new ArrayList<Element>();
        Elements elements = info.getElements();
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
                Context ctx = info.getJavacTask().getContext();
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
        return members;
    }
    
    public Iterable<? extends Element> getLocalMembersAndVars(Scope scope, ElementAcceptor acceptor) {
        ArrayList<Element> members = new ArrayList<Element>();
        HashMap<String, ArrayList<Element>> hiders = new HashMap<String, ArrayList<Element>>();
        Elements elements = info.getElements();
        Types types = info.getTypes();
        TypeElement cls;
        while(scope != null && (cls = scope.getEnclosingClass()) != null) {
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

    public Iterable<? extends Element> getLocalVars(Scope scope, ElementAcceptor acceptor) {
        ArrayList<Element> members = new ArrayList<Element>();
        HashMap<String, ArrayList<Element>> hiders = new HashMap<String, ArrayList<Element>>();
        Elements elements = info.getElements();
        Types types = info.getTypes();
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
    
    public Iterable<? extends TypeElement> getGlobalTypes(ElementAcceptor acceptor) {
        HashSet<TypeElement> members = new HashSet<TypeElement>();
        TreePath path = new TreePath(info.getCompilationUnit());
        Trees trees = info.getTrees();
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
        return members;
    }

    public static interface ElementAcceptor {
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
}
