/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.java.source.builder;

import com.sun.tools.javac.code.Flags;
import com.sun.tools.javac.util.Name;
import com.sun.tools.javac.util.Names;
import javax.lang.model.util.Types;
import org.netbeans.api.java.source.*;
import com.sun.tools.javac.code.Scope;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Symbol.ClassSymbol;
import com.sun.tools.javac.code.Symbol.MethodSymbol;
import com.sun.tools.javac.code.Symbol.TypeSymbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.code.TypeTags;
import com.sun.tools.javac.model.JavacTypes;
import com.sun.tools.javac.util.Context;
import javax.lang.model.element.*;
import javax.lang.model.type.*;

import static javax.lang.model.element.ElementKind.*;

/**
 * Utility methods for working with Element instances.
 */
public class ElementsService {
    private com.sun.tools.javac.code.Types jctypes;
    private Names names;
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
        names = Names.instance(context);
        types = JavacTypes.instance(context);
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
	return prev instanceof TypeElement ? (TypeElement) prev : null;
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
