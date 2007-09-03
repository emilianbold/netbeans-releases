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
package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementProvider;
import org.netbeans.modules.cnd.gotodeclaration.util.ComparatorFactory;
import org.openide.util.NbBundle;

/**
 * ElementProvider for functions and variables
 * @author Vladimir Kvashin
 */
public class FuncVarElementProvider extends BaseProvider implements ElementProvider {

    
    public String name() {
	return "C/C++ Functions and Variables"; // NOI18N
    }

    public String getDisplayName() {
	return NbBundle.getMessage(FuncVarElementProvider.class, "FUNCVAR_PROVIDER_DISPLAY_NAME"); // NOI18N
    }

    protected void processProject(CsmProject project, List<ElementDescriptor> result, ComparatorFactory.NameComparator comparator) {
	if( TRACE ) System.err.printf("FuncVarElementProvider.processProject %s\n", project.getName());
        processNamespace(project.getGlobalNamespace(), result, comparator);
    }
    
    private void processNamespace(CsmNamespace nsp, List<ElementDescriptor> result, ComparatorFactory.NameComparator comparator) {
        if( TRACE ) System.err.printf("processNamespace %s\n", nsp.getQualifiedName());
	for( CsmDeclaration declaration : nsp.getDeclarations() ) {
            if( isCancelled() ) {
		return;
	    }
	    processDeclaration(declaration, result, comparator);
	}
	for( CsmNamespace child : nsp.getNestedNamespaces() ) {
            if( isCancelled() ) {
		return;
	    }
	    processNamespace(child, result, comparator);
	}
    }

    private void processDeclaration(CsmDeclaration decl, List<ElementDescriptor> result, ComparatorFactory.NameComparator comparator) {
        switch (decl.getKind()) {
            case FUNCTION_DEFINITION:
		if( comparator.match(decl.getName()) ) {
		    CsmFunctionDefinition fdef = (CsmFunctionDefinition) decl;
		    CsmFunction fdecl = fdef.getDeclaration();
		    if( fdecl == null || fdecl == fdef) {
                        result.add(new FunctionElementDescriptor(fdef));
		    }
		}
		break;
            case FUNCTION:
		if( comparator.match(decl.getName()) ) {
                    result.add(new FunctionElementDescriptor((CsmFunction) decl));
		}
		break;
            case VARIABLE:
		if( comparator.match(decl.getName()) ) {
                    result.add(new VariableElementDescriptor((CsmVariable) decl));
		}
		break;
            case CLASS:
            case UNION:
            case STRUCT:
            case ENUM:
            case TYPEDEF:
            case BUILT_IN:
            case ENUMERATOR:
            case MACRO:
            case VARIABLE_DEFINITION:
            case TEMPLATE_SPECIALIZATION:
            case ASM:
            case TEMPLATE_DECLARATION:
            case NAMESPACE_DEFINITION:
            case NAMESPACE_ALIAS:
            case USING_DIRECTIVE:
            case USING_DECLARATION:
            case CLASS_FORWARD_DECLARATION:
            case CLASS_FRIEND_DECLARATION:
                break;
        }
    }


}
