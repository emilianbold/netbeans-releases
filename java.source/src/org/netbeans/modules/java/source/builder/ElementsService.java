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

package org.netbeans.modules.java.source.builder;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.util.Name;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.*;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.util.Context;
import javax.lang.model.element.*;
import javax.lang.model.type.*;

import static javax.lang.model.element.ElementKind.*;

/**
 * Utility methods for working with Element instances.
 */
public class ElementsService {
    private com.sun.tools.javac.code.Types jctypes;
    private Name.Table names;
    private Types types;
    
    private static final Context.Key<ElementsService> KEY =
	    new Context.Key<ElementsService>();

    public static ElementsService instance(Context context) {
	ElementsService instance = context.get(KEY);
	if (instance == null)
	    instance = new ElementsService(context);
	return instance;
    }

    protected ElementsService(Context context) {
        context.put(KEY, this);
        jctypes = com.sun.tools.javac.code.Types.instance(context);
        names = Name.Table.instance(context);
        types = TypesService.instance(context);
    }

    /**
     * Returns the TypeElement which encloses the specified element.
     */
    public TypeElement enclosingTypeElement(Element element) {
        if (element instanceof PackageElement)
            throw new IllegalArgumentException("package elements cannot be enclosed");
        Element e = element;
	while (e != null && 
               e.getKind() != CLASS &&
               e.getKind() != INTERFACE &&
               e.getKind() != ANNOTATION_TYPE &&
               e.getKind() != ENUM) {
	    e = e.getEnclosingElement();
	}
	return (TypeElement)e;
    }

    /** 
     * The outermost TypeElement which indirectly encloses this element.
     */
    public TypeElement outermostTypeElement(Element element) {
	Element e = element;
	Element prev = null;
	while (e.getKind() != PACKAGE) {
	    prev = e;
	    e = e.getEnclosingElement();
	}
	return (TypeElement)prev;
    }

    /** 
     * The package element which indirectly encloses this element..
     */
    public PackageElement packageElement(Element element) {
	Element e = element;
	while (e.getKind() != PACKAGE) {
	    e = e.getEnclosingElement();
	}
	return (PackageElement)e;
    }
    
    /**
     * Returns true if this element represents a method which overrides a
     * method in one of its superclasses.
     */
    public boolean overridesMethod(ExecutableElement element) {
        MethodSymbol m = (MethodSymbol)element;
        if ((m.flags() & Flags.STATIC) == 0) {
            ClassSymbol owner = (ClassSymbol) m.owner;
            for (Type sup = jctypes.supertype(m.owner.type);
                 sup.tag == TypeTags.CLASS;
                 sup = jctypes.supertype(sup)) {
                for (Scope.Entry e = sup.tsym.members().lookup(m.name);
                     e.scope != null; e = e.next()) {
                    if (m.overrides(e.sym, owner, jctypes, true)) 
                        return true;
                }
            }
        }
	return false;
    }
    
    /**
     * Returns true if this element represents a method which 
     * implements a method in an interface the parent class implements.
     */
    public boolean implementsMethod(ExecutableElement element) {
        MethodSymbol m = (MethodSymbol)element;
	TypeSymbol owner = (TypeSymbol) m.owner;
	for (Type type : jctypes.interfaces(m.owner.type)) {
	    for (Scope.Entry e = type.tsym.members().lookup(m.name);
		 e.scope != null; e = e.next()) {
		if (m.overrides(e.sym, owner, jctypes, true)) 
		    return true;
	    }
	}
	return false;
    }

    public boolean alreadyDefinedIn(CharSequence name, ExecutableType method, TypeElement enclClass) {
        Type.MethodType meth = ((Type)method).asMethodType();
        ClassSymbol clazz = (ClassSymbol)enclClass;
        Scope scope = clazz.members();
        Name n = names.fromString(name.toString());
        scanSymbol:
        for(Scope.Entry e = scope.lookup(n); e.scope==scope; e = e.next())
            if(e.sym.type instanceof ExecutableType && 
               types.isSubsignature(meth, (ExecutableType)e.sym.type))
                return true;
        return false;
     }
    
    public boolean isMemberOf(Element e, TypeElement type) {
        return ((Symbol)e).isMemberOf((TypeSymbol)type, jctypes);
    }
    
    public boolean isDeprecated(Element element) {
        Symbol sym = (Symbol)element;
	if ((sym.flags() & Flags.DEPRECATED) != 0 && 
	    (sym.owner.flags() & Flags.DEPRECATED) == 0)
	    return true;
	 
	// Check if this method overrides a deprecated method. 
	TypeSymbol owner = sym.enclClass();
	for (Type sup = jctypes.supertype(owner.type);
	     sup.tag == TypeTags.CLASS;
	     sup = jctypes.supertype(sup)) {
	    for (Scope.Entry e = sup.tsym.members().lookup(sym.name);
		 e.scope != null; e = e.next()) {
		if (sym.overrides(e.sym, owner, jctypes, true) &&
			(e.sym.flags() & Flags.DEPRECATED) != 0)
		    return true;
	    }
	}
	return false;
    }
    
    public boolean isLocal(Element element) {
        return ((Symbol)element).isLocal();
    }
    
    public CharSequence getFullName(Element element) {
        Symbol sym = (Symbol)element;
        return element instanceof Symbol.ClassSymbol ? 
            ((Symbol.ClassSymbol)element).fullname :
            Symbol.TypeSymbol.formFullName(sym.name, sym.owner);
    }

    public Element getImplementationOf(ExecutableElement method, TypeElement origin) {
        return ((MethodSymbol)method).implementation((TypeSymbol)origin, jctypes, true);
    }

    public boolean isSynthetic(Element e) {
        return (((Symbol) e).flags() & Flags.SYNTHETIC) != 0 || (((Symbol) e).flags() & Flags.GENERATEDCONSTR) != 0;
    }
    
    public ExecutableElement getOverriddenMethod(ExecutableElement method) {
        MethodSymbol m = (MethodSymbol)method;
	ClassSymbol origin = (ClassSymbol)m.owner;
	for (Type t = jctypes.supertype(origin.type); t.tag == TypeTags.CLASS; t = jctypes.supertype(t)) {
	    TypeSymbol c = t.tsym;
	    Scope.Entry e = c.members().lookup(m.name);
	    while (e.scope != null) {
		if (m.overrides(e.sym, origin, jctypes, false))
		    return (MethodSymbol)e.sym;
		e = e.next();
	    }
	}
        return null;
    }
}
