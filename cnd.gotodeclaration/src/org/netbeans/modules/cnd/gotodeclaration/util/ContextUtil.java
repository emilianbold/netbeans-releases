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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.util;

import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.api.model.util.CsmKindUtilities;

/**
 * Utility class with methods that returns context information
 * @author Vladimir Kvashin
 */
public class ContextUtil {

    private ContextUtil() {
    }
    
    public static String getContextName(CsmScopeElement element) {
	CsmScope scope = element.getScope();
	if( CsmKindUtilities.isClass(scope) ) {
	    CsmClass cls = ((CsmClass) scope);
	    CsmNamespace ns = getClassNamespace(cls);
	    return ns.getQualifiedName();
	}
	else if( CsmKindUtilities.isNamespace(scope) ) {
	    return ((CsmNamespace) scope).getQualifiedName();
	}
	else if( CsmKindUtilities.isFile(scope) ) {
	    return ((CsmFile) scope).getName();
	}
	return "";
    }
    
    public static CsmNamespace getClassNamespace(CsmClass cls) {
	CsmScope scope = cls.getScope();
	while( scope != null && CsmKindUtilities.isClass(scope) ) {
	    CsmClass outer = (CsmClass)scope;
	    scope = outer.getScope();
	}
	return CsmKindUtilities.isNamespace(scope) ? (CsmNamespace) scope : null;
    }    
    
    public static String getClassFullName(CsmClass cls) {
	StringBuilder sb = new StringBuilder(cls.getName());
	CsmScope scope = cls.getScope();
	while( scope != null && CsmKindUtilities.isClass(scope) ) {
	    CsmClass outer = (CsmClass)scope;
	    sb.insert(0, "::");
	    sb.insert(0, (outer).getName());
	    scope = outer.getScope();
	}
	    
        return sb.toString(); 
    }
    

}
