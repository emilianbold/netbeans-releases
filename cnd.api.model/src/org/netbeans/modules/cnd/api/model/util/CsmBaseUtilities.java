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

package org.netbeans.modules.cnd.api.model.util;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassForwardDeclaration;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmFunctionDefinition;
import org.netbeans.modules.cnd.api.model.CsmInstantiation;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmMethod;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmNamespaceDefinition;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetableDeclaration;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmValidable;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.CsmVariableDefinition;
import org.netbeans.modules.cnd.api.model.services.CsmClassifierResolver;

/**
 *
 * @author Vladimir Voskresensky
 */
public class CsmBaseUtilities {

    /** Creates a new instance of CsmBaseUtilities */
    private CsmBaseUtilities() {
    }

    public static boolean isValid(CsmObject obj) {
        if (CsmKindUtilities.isValidable(obj)) {
            return ((CsmValidable)obj).isValid();
        } else {
            return obj != null;
        }
    }
    
    public static boolean isGlobalNamespace(CsmScope scope) {
        if (CsmKindUtilities.isNamespace(scope)) {
            return ((CsmNamespace)scope).isGlobal();
        }
        return false;
    }
    
    public static boolean isInlineFunction(CsmFunction fun) {
        if (fun.isInline()) {
            return true;
        }
        CsmScope outScope = fun.getScope();
        if (outScope == null || isGlobalNamespace(outScope)) {
            return false;
        } else {
            CsmFunction decl = CsmBaseUtilities.getFunctionDeclaration(fun);
            if (decl == null || !CsmKindUtilities.isMethod(fun)) {
                return false;
            } else {
                return outScope.equals(((CsmMethod)decl).getContainingClass());
            }
        }
    }
    
    public static boolean isStaticContext(CsmFunction fun) {
        assert (fun != null) : "must be not null";
        // static context is in global functions and static methods
        if (CsmKindUtilities.isGlobalFunction(fun)) {
            return true;
        } else {
            CsmFunction funDecl = getFunctionDeclaration(fun);
            if (CsmKindUtilities.isClassMember(funDecl)) {
                return ((CsmMember)funDecl).isStatic();
            }
        }
        return false;
    }
    
    private static boolean TRACE_XREF_REPOSITORY = Boolean.getBoolean("cnd.modelimpl.trace.xref.repository");
    
    public static CsmFunction getOperator(CsmClassifier cls, CsmFunction.OperatorKind opKind) {
        if (!CsmKindUtilities.isClass(cls)) {
            return null;
        }
        for (CsmMember member : ((CsmClass)cls).getMembers()) {
            if (CsmKindUtilities.isOperator(member)) {
                if (((CsmFunction)member).getOperatorKind() == opKind) {
                    return (CsmFunction)member;
                }
            }
        }        
        return null;
    }
    
    /**
     * 
     * @param target
     * @return new CsmObject[] { declaration, definion }
     */
    public static CsmObject[] getDefinitionDeclaration(CsmObject target, boolean unboxInstantiation) {
        CsmObject decl;
        CsmObject def; 
        if (unboxInstantiation && CsmKindUtilities.isTemplateInstantiation(target)) {
            target = ((CsmInstantiation)target).getTemplateDeclaration();
        }
        if (CsmKindUtilities.isVariableDefinition(target)) {
            decl = ((CsmVariableDefinition)target).getDeclaration();
            if (decl == null) {
                decl = target;
                if (TRACE_XREF_REPOSITORY) {
                    System.err.println("not found declaration for variable definition " + target);
                }
            }
            def = target;
        } else if (CsmKindUtilities.isVariableDeclaration(target)) {
            decl = target;
            def = ((CsmVariable)target).getDefinition();
        } else if (CsmKindUtilities.isFunctionDefinition(target)) {
            decl = ((CsmFunctionDefinition)target).getDeclaration();
            if (decl == null) {
                decl = target;
                if (TRACE_XREF_REPOSITORY) {
                    System.err.println("not found declaration for function definition " + target);
                }
            }
            def = target;
        } else if (CsmKindUtilities.isFunctionDeclaration(target)) {
            decl = target;
            def = ((CsmFunction)target).getDefinition();
        } else if (CsmKindUtilities.isClassForwardDeclaration(target)) {
            CsmClassForwardDeclaration fd = (CsmClassForwardDeclaration) target;
            if (fd.getCsmClass() != null){
                decl = target;
                def = fd.getCsmClass();
            } else {
                decl = target;
                def = null;
            }
        } else if (CsmKindUtilities.isClass(target)) {
            CsmClass cls = (CsmClass)target;
            CsmClassifier c = cls.getContainingFile().getProject().findClassifier(cls.getQualifiedName());
            if (cls.equals(c)) {
                decl = target;
                def = null;
            } else if (c != null){
                decl = c;
                def = cls;
            } else {
                decl = target;
                def = null;
            }
        } else {
            decl = target;
            def = null;
        }
        assert decl != null;
        return new CsmObject[] { decl, def };
    }
    
    public static CsmClass getFunctionClass(CsmFunction fun) {
        assert (fun != null) : "must be not null";
        CsmClass clazz = null;
        CsmFunction funDecl = getFunctionDeclaration(fun);
        if (CsmKindUtilities.isClassMember(funDecl)) {
            clazz = ((CsmMember)funDecl).getContainingClass();
        }
        return clazz;
    }   
        
    public static CsmClass getObjectClass(CsmObject obj) {
        CsmClass objClass = null;
        if (CsmKindUtilities.isFunction(obj)) {
            objClass = CsmBaseUtilities.getFunctionClass((CsmFunction)obj);
        } else if (CsmKindUtilities.isClass(obj)) {
            objClass = (CsmClass)obj;
        } else if (CsmKindUtilities.isEnumerator(obj)) {
            objClass = getObjectClass(((CsmEnumerator)obj).getEnumeration());
        } else if (CsmKindUtilities.isScopeElement(obj)) {
            CsmScope scope = ((CsmScopeElement)obj).getScope();
            if (CsmKindUtilities.isClass(scope)) {
                objClass = (CsmClass)scope;
            }
        }
        return objClass;
    }
    
    public static CsmNamespace getObjectNamespace(CsmObject obj) {
        CsmNamespace objNs = null;
        if (CsmKindUtilities.isNamespace(obj)) {
            objNs = (CsmNamespace)obj;
        } else if (CsmKindUtilities.isFunction(obj)) {
            objNs = CsmBaseUtilities.getFunctionNamespace((CsmFunction)obj);
        } else if (CsmKindUtilities.isClass(obj)) {
            objNs = CsmBaseUtilities.getClassNamespace((CsmClassifier)obj);
        } else if (CsmKindUtilities.isEnumerator(obj)) {
            objNs = getObjectNamespace(((CsmEnumerator)obj).getEnumeration());
        } else if (CsmKindUtilities.isScopeElement(obj)) {
            CsmScope scope = ((CsmScopeElement)obj).getScope();
            if (CsmKindUtilities.isNamespace(scope)) {
                objNs = (CsmNamespace)scope;
            }
        }
        return objNs;
    }
    
    public static CsmNamespace getFunctionNamespace(CsmFunction fun) {
        if (CsmKindUtilities.isFunctionDefinition(fun)) {
            CsmFunction decl = ((CsmFunctionDefinition) fun).getDeclaration();
            fun = decl != null ? decl : fun;
        }
        if (fun != null) {
            CsmScope scope = fun.getScope();
            if (CsmKindUtilities.isNamespaceDefinition(scope)) {
                CsmNamespace ns = ((CsmNamespaceDefinition) scope).getNamespace();
                return ns;
            } else if (CsmKindUtilities.isNamespace(scope)) {
                CsmNamespace ns = (CsmNamespace) scope;
                return ns;
            } else if (CsmKindUtilities.isClass(scope)) {
                return getClassNamespace((CsmClass) scope);
            }
        }
        return null;
    }
    
    public static CsmNamespace getClassNamespace(CsmClassifier cls) {
        CsmScope scope = cls.getScope();
        while (scope != null) {
            if (CsmKindUtilities.isNamespace(scope)) {
                return (CsmNamespace) scope;
            }
            if (CsmKindUtilities.isScopeElement(scope)) {
                scope = ((CsmScopeElement) scope).getScope();
            } else {
                break;
            }
        }
        return null;
    } 
    
    public static CsmFunction getFunctionDeclaration(CsmFunction fun) {
        assert (fun != null) : "must be not null";
        CsmFunction funDecl = fun;
        if (CsmKindUtilities.isFunctionDefinition(funDecl)) {
            funDecl = ((CsmFunctionDefinition)funDecl).getDeclaration();
        }
        return funDecl;
    }    
    
    public static boolean isFileLocalFunction(CsmFunction fun) {
        CsmFunction decl = getFunctionDeclaration(fun);
        if (decl != null && CsmKindUtilities.isFile(decl.getScope())) {
            return true;
        }
        return false;
    }
    
    public static boolean isDeclarationFromUnnamedNamespace(CsmObject obj) {
        if (CsmKindUtilities.isScopeElement(obj)) {
            CsmScope scope = ((CsmScopeElement)obj).getScope();
            if (CsmKindUtilities.isNamespaceDefinition(scope)) {
                return ((CsmNamespaceDefinition)scope).getName().length() == 0;
            } else if (CsmKindUtilities.isNamespace(scope)) {
                CsmNamespace ns = (CsmNamespace)scope;
                return !ns.isGlobal() && ns.getName().length() == 0;
            }
        }
        return false;
    }
    
    public static CsmClass getContextClass(CsmOffsetableDeclaration contextDeclaration) {
        if (contextDeclaration == null) {
            return null;
        }   
        CsmClass clazz = null;
        if (CsmKindUtilities.isClass(contextDeclaration)) {
            clazz = (CsmClass)contextDeclaration;
        } else if (CsmKindUtilities.isClassMember(contextDeclaration)) {
            clazz = ((CsmMember)contextDeclaration).getContainingClass();            
        } else if (CsmKindUtilities.isFunction(contextDeclaration)) {
            clazz = getFunctionClass((CsmFunction)contextDeclaration);
        }
        return clazz;
    }
    
    public static CsmFunction getContextFunction(CsmOffsetableDeclaration contextDeclaration) {
        if (contextDeclaration == null) {
            return null;
        }
        CsmFunction fun = null;
        if (CsmKindUtilities.isFunction(contextDeclaration)) {
            fun = (CsmFunction)contextDeclaration;
        }
        return fun;
    }      
    
    public static CsmClassifier getOriginalClassifier(CsmClassifier orig, CsmFile contextFile) {
        return CsmClassifierResolver.getDefault().getOriginalClassifier(orig, contextFile);
    }

    public static CsmClassifier getClassifier(CsmType type, CsmFile contextFile, boolean resolveTypeChain) {
        return CsmClassifierResolver.getDefault().getTypeClassifier(type, contextFile, 0, resolveTypeChain);
    }
}
