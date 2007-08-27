/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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

package org.netbeans.modules.cnd.api.model.util;

import org.netbeans.modules.cnd.api.model.CsmClass;
import org.netbeans.modules.cnd.api.model.CsmClassifier;
import org.netbeans.modules.cnd.api.model.CsmConstructor;
import org.netbeans.modules.cnd.api.model.CsmDeclaration;
import org.netbeans.modules.cnd.api.model.CsmEnumerator;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.api.model.CsmFriend;
import org.netbeans.modules.cnd.api.model.CsmFriendClass;
import org.netbeans.modules.cnd.api.model.CsmFriendFunction;
import org.netbeans.modules.cnd.api.model.CsmFunction;
import org.netbeans.modules.cnd.api.model.CsmIdentifiable;
import org.netbeans.modules.cnd.api.model.CsmInclude;
import org.netbeans.modules.cnd.api.model.CsmInheritance;
import org.netbeans.modules.cnd.api.model.CsmMacro;
import org.netbeans.modules.cnd.api.model.CsmMember;
import org.netbeans.modules.cnd.api.model.CsmNamedElement;
import org.netbeans.modules.cnd.api.model.CsmNamespace;
import org.netbeans.modules.cnd.api.model.CsmObject;
import org.netbeans.modules.cnd.api.model.CsmOffsetable;
import org.netbeans.modules.cnd.api.model.CsmParameter;
import org.netbeans.modules.cnd.api.model.CsmQualifiedNamedElement;
import org.netbeans.modules.cnd.api.model.CsmScope;
import org.netbeans.modules.cnd.api.model.CsmScopeElement;
import org.netbeans.modules.cnd.api.model.CsmType;
import org.netbeans.modules.cnd.api.model.CsmUsingDeclaration;
import org.netbeans.modules.cnd.api.model.CsmUsingDirective;
import org.netbeans.modules.cnd.api.model.CsmVariable;
import org.netbeans.modules.cnd.api.model.deep.CsmDeclarationStatement;
import org.netbeans.modules.cnd.api.model.deep.CsmExpression;
import org.netbeans.modules.cnd.api.model.deep.CsmStatement;


/**
 * Utulity functions to prevent using of "instanceof" on CsmObjects for 
 * determining type/kind of Csm element
 *
 * @author Vladimir Kvashin
 * @author Vladimir Voskresensky
 */
public class CsmKindUtilities {
    
    private CsmKindUtilities() {
        
    }

    public static boolean isCsmObject(Object obj) {
        if (obj instanceof CsmObject) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isQualified(CsmObject obj) {
        if (obj instanceof CsmQualifiedNamedElement) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isDeclaration(CsmObject obj) {
        if (obj instanceof CsmDeclaration) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isBuiltIn(CsmObject obj) {
        if (isDeclaration(obj)) {
            return ((CsmDeclaration)obj).getKind() == CsmDeclaration.Kind.BUILT_IN;
        } else {
            return false;
        }
    }
    
    public static boolean isType(CsmObject obj) {
        if (obj instanceof CsmType) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isTypedef(CsmObject obj) {
        if (isDeclaration(obj)) {
            return ((CsmDeclaration)obj).getKind() == CsmDeclaration.Kind.TYPEDEF;
        } else {
            return false;
        }
    }
    
    public static boolean isStatement(CsmObject obj) {
        if (obj instanceof CsmStatement) {
            return true;
        } else {
            return false;
        }          
    }

    public static boolean isDeclarationStatement(CsmObject obj) {
        if (obj instanceof CsmDeclarationStatement) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isCompoundStatement(CsmObject obj) {
        if (isStatement(obj)) {
            return ((CsmStatement)obj).getKind() == CsmStatement.Kind.COMPOUND;
        } else {
            return false;
        }          
    }
    
    public static boolean isOffsetable(Object obj) {
        if (obj instanceof CsmOffsetable) {
            return true;
        } else {
            return false;
        }        
    }
      
    public static boolean isNamedElement(CsmObject obj) {
        if (obj instanceof CsmNamedElement) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isNamedElement(Object obj) {
        if (obj instanceof CsmNamedElement) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isEnum(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.ENUM; 
        } else {
            return false;
        }
    }
    
    public static boolean isEnumerator(CsmObject obj) {
        if (obj instanceof CsmEnumerator) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isClassifier(CsmObject obj) {
        if (obj instanceof CsmClassifier) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isClass(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return 
                kind == CsmDeclaration.Kind.CLASS 
                || kind == CsmDeclaration.Kind.STRUCT 
                || kind == CsmDeclaration.Kind.UNION;
        } else {
            return false;
        }
    }
    
    public static boolean isClassForwardDeclaration(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.CLASS_FORWARD_DECLARATION;
        } else {
            return false;
        }
    }
    
    public static boolean isScope(CsmObject obj) {
        if (obj instanceof CsmScope) {
            return true;
        } else {
            return false;
        }
    }
    
    public static boolean isScopeElement(CsmObject obj) {
        if (obj instanceof CsmScopeElement) {
            return true;
        } else {
            return false;
        }
    }
    
    /*
     * checks if object is function declaration or function definition
     * it's safe to cast to CsmFunction
     */
    public static boolean isFunction(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.FUNCTION ||
                    kind == CsmDeclaration.Kind.FUNCTION_DEFINITION;
        } else {
            return false;
        }
    }   

    /*
     * checks if object is operatir
     */
    public static boolean isOperator(CsmObject obj) {
        // Fix me.
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            if (kind == CsmDeclaration.Kind.FUNCTION ||
                kind == CsmDeclaration.Kind.FUNCTION_DEFINITION) {
                String name = ((CsmDeclaration)obj).getName();
                return name != null && name.startsWith("operator "); // NOI18N
            }
        }
        return false;
    }   

    /*
     * checks if object is function declaration
     * it's safe to cast to CsmFunction which is not CsmFunctionDefinition
     */
    public static boolean isFunctionDeclaration(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.FUNCTION;
        } else {
            return false;
        }
    }   
    
    /*
     * checks if object is function definition
     * it's safe to cast to CsmFunction or CsmFunctionDefinition
     */
    public static boolean isFunctionDefinition(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.FUNCTION_DEFINITION;
        } else {
            return false;
        }
    } 
    
    public static boolean isFile(CsmObject obj) {
        if (obj instanceof CsmFile) {
            return true;
        } else {
            return false;
        }
    }  
    
    public static boolean isInheritance(CsmObject obj) {
        if (obj instanceof CsmInheritance) {
            return true;
        } else {
            return false;
        }
    } 

    public static boolean isNamespace(CsmObject obj) {
        if (obj instanceof CsmNamespace) {
            return true;
        } else {
            return false;
        }
    } 
    
    public static boolean isNamespaceDefinition(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.NAMESPACE_DEFINITION;
        } else {
            return false;
        }
    }   
    
    public static boolean isClassMember(CsmObject obj) {
        if (obj instanceof CsmMember) {
	    if (isClass(obj) ) {
		return isClass(((CsmClass) obj).getScope());
	    } else {
		return true;
	    }   
        } else {
            return false;
        }
    }    
    
    public static boolean isVariable(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.VARIABLE ||
                   kind == CsmDeclaration.Kind.VARIABLE_DEFINITION ;
        } else {
            return false;
        }        
    }

    public static boolean isVariableDeclaration(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.VARIABLE ;
        } else {
            return false;
        }        
    }

    public static boolean isVariableDefinition(CsmObject obj) {
        if (isDeclaration(obj)) {
            CsmDeclaration.Kind kind = ((CsmDeclaration)obj).getKind();
            return kind == CsmDeclaration.Kind.VARIABLE_DEFINITION ;
        } else {
            return false;
        }        
    }
    
    /* 
     * Local variable is CsmVariable object declared through
     * CsmDeclaration as part of CsmDeclarationStatement's declarators list
     * or it's CsmParameter
     * for file local variables and global variables return false
     */
    public static boolean isLocalVariable(CsmObject obj) {
        if (isVariable(obj)) {
            // can't be class member
            if (isClassMember(obj)) {
                return false;
            }
            // check scope
            CsmScope scope = ((CsmVariable)obj).getScope();
            return !isFile(scope) && !isNamespace(scope);
        } else {
            return false;
        }
    }  
    
    public static boolean isFileLocalVariable(CsmObject obj) {
        if (isVariable(obj)) {
            return isFile(((CsmVariable)obj).getScope());
        } else {
            return false;
        }
    }      
    
    public static boolean isGlobalVariable(CsmObject obj) {
        if (isVariable(obj)) {
            // global variable has scope - namespace
            return isNamespace(((CsmVariable)obj).getScope());
        } else {
            return false;
        }
    }   
    
    public static boolean isParamVariable(CsmObject obj) {
        if (isVariable(obj)) {
            assert (!(obj instanceof CsmParameter) || !isClassMember(obj)) : "parameter is not class member";
            return obj instanceof CsmParameter;
        } else {
            return false;
        }
    }  
    
    public static boolean isField(CsmObject obj) {
        if (isVariable(obj)) {
            return isClassMember(obj);
        } else {
            return false;
        }
    }  
    
    public static boolean isGlobalFunction(CsmObject obj) {
        if (isFunction(obj)) {
            return !isClassMember(CsmBaseUtilities.getFunctionDeclaration((CsmFunction)obj));
        } else {
            return false;
        }
    }       
    
    /**
     * checks if passed object is method definition or method declaration
     * after this check it is safe to cast only to CsmFunction (not CsmMethod)
     * @see isMethodDeclaration
     */
    public static boolean isMethod(CsmObject obj) {
        if (isFunction(obj)) {
            return isClassMember(CsmBaseUtilities.getFunctionDeclaration((CsmFunction)obj));
        } else {
            return false;
        }
    }
    
    /**
     * checks if passed object is method declaration;
     * after this check it is safe to cast to CsmMethod
     */
    public static boolean isMethodDeclaration(CsmObject obj) {
        if (isFunction(obj)) {
            return isClassMember(obj);
        } else {
            return false;
        }
    }     

    /**
     * checks if passed object is method definition;
     * after this check it is safe to cast to CsmFunctionDefinition (not CsmMethod)
     * @see isMethodDeclaration
     */
    public static boolean isMethodDefinition(CsmObject obj) {
        if (isFunctionDefinition(obj)) {
            return isClassMember(CsmBaseUtilities.getFunctionDeclaration((CsmFunction)obj));
        } else {
            return false;
        }
    }     

    /**
     * checks if passed object is constructor definition or declaration;
     * after this check it is safe to cast to CsmFunction
     */    
    public static boolean isConstructor(CsmObject obj) {
        if (isMethod(obj)) {
            return CsmBaseUtilities.getFunctionDeclaration((CsmFunction)obj) instanceof CsmConstructor;
        } else {
            return false;
        }
    }   
    
    public static boolean isDestructor(CsmObject obj) {
        if (isMethod(obj)) {
            CsmFunction funDecl = CsmBaseUtilities.getFunctionDeclaration((CsmFunction)obj);
            // check constructor by ~ at the begining of the name
            if (funDecl != null && funDecl.getName().length() > 0) {
                return funDecl.getName().charAt(0) == '~'; //NOI18N
            }
        }
        return false;
    }     
    
    public static boolean isExpression(CsmObject obj) {
        return obj instanceof CsmExpression;
    }
    
    public static boolean isMacro(CsmObject obj) {
        return obj instanceof CsmMacro;
    }
    
    public static boolean isInclude(CsmObject obj) {
        return obj instanceof CsmInclude;
    }

    public static boolean isUsing(CsmObject obj) {
        return (obj instanceof CsmUsingDeclaration) ||
               (obj instanceof CsmUsingDirective);
    }

    public static boolean isUsingDirective(CsmObject obj) {
        return obj instanceof CsmUsingDirective;
    }

    public static boolean isUsingDeclaration(CsmObject obj) {
        return obj instanceof CsmUsingDeclaration;
    }

    public static boolean isFriend(CsmObject obj) {
        return obj instanceof CsmFriend;
    }

    public static boolean isFriendClass(CsmObject obj) {
        return obj instanceof CsmFriendClass;
    }

    public static boolean isFriendMethod(CsmObject obj) {
        return obj instanceof CsmFriendFunction;
    }

    public static boolean isExternVariable(CsmDeclaration decl) {
        if (isVariable(decl)) {
            return ((CsmVariable)decl).isExtern();
        }
        return false;
    }
    
    public static boolean isIdentifiable(Object obj) {
        if (obj instanceof CsmIdentifiable) {
            return true;
        } else {
            return false;
        }
    }
}
