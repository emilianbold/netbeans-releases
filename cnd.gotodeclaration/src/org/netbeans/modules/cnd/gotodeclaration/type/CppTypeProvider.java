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
package org.netbeans.modules.cnd.gotodeclaration.type;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.gotodeclaration.util.ComparatorFactory;

import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.util.NbBundle;

/**
 * Implementation of provider for "Jump to Type" for C/C++
 * @author Vladimir Kvashin
 */
public class CppTypeProvider implements TypeProvider {

    private boolean isCancelled = false;
    private static final boolean PROCESS_LIBRARIES = true; // Boolean.getBoolean("cnd.type.provider.libraries");
    private static final boolean TRACE = Boolean.getBoolean("cnd.type.provider.trace");
    
    public CppTypeProvider() {
	if( TRACE ) System.err.printf("CppTypeProvider.ctor\n");
    }
    
    public String name() {
        return "C/C++"; // NOI18N
    }

    public String getDisplayName() {
        return NbBundle.getMessage(CppTypeProvider.class, "TYPE_PROVIDER_DISPLAY_NAME"); // NOI18N
    }

    public List<? extends TypeDescriptor> getTypeNames(Project project, String text, SearchType type) {
	
	if( TRACE ) System.err.printf("CppTypeProvider.getTypeNames(%s, %s, %s)\n", project, text, type);
	
	@SuppressWarnings("unchecked")
	List<TypeDescriptor> result = Collections.EMPTY_LIST;
	
	ComparatorFactory.NameComparator comparator = ComparatorFactory.createNameComparator(text, type);
	if( comparator == null ) {
	    return result;
	}
	
	if( project == null ) {
            Collection<CsmProject> csmProjects = CsmModelAccessor.getModel().projects();
	    if( ! csmProjects.isEmpty() ) {
		result = new ArrayList<TypeDescriptor>(1000);
		for( CsmProject csmProject : csmProjects ) {
		    processProject(csmProject, result, comparator);
		}
	    }
	}
	else {
	    result = new ArrayList<TypeDescriptor>(1000);
            CsmProject csmProject = CsmModelAccessor.getModel().getProject(project);
	    processProject(csmProject, result, comparator);
	}
	
	
	return result;
    }

    public void cancel() {
	if( TRACE ) System.err.printf("CppTypeProvider.cancel\n");
        isCancelled = true;
    }

    public void cleanup() {
	if( TRACE ) System.err.printf("CppTypeProvider.cleanup\n");
    }
    
    private static TypeDescriptor createTypeDescriptor(CsmClassifier classifier) {
	CppTypeDescriptor descriptor = new CppTypeDescriptor(classifier);
	return TRACE ? new TracingTypeDescriptor(descriptor) : descriptor;
    }

    private void processProject(CsmProject project, List<TypeDescriptor> result, ComparatorFactory.NameComparator comparator) {
	if( TRACE ) System.err.printf("processProject %s\n", project.getName());
        processNamespace(project.getGlobalNamespace(), result, comparator);
	if( PROCESS_LIBRARIES ) {
            for( CsmProject lib : project.getLibraries() ) {
                if( isCancelled ) {
                    return;
                }
                if( lib.isArtificial() ) {
                    processProject(lib, result, comparator);
                }
            }
	}
    }
    
    private void processNamespace(CsmNamespace nsp, List<TypeDescriptor> result, ComparatorFactory.NameComparator comparator) {
        if( TRACE ) System.err.printf("processNamespace %s\n", nsp.getQualifiedName());
	for( CsmDeclaration declaration : nsp.getDeclarations() ) {
            if( isCancelled ) {
		return;
	    }
	    processDeclaration(declaration, result, comparator);
	}
	for( CsmNamespace child : nsp.getNestedNamespaces() ) {
            if( isCancelled ) {
		return;
	    }
	    processNamespace(child, result, comparator);
	}
    }

    private void processDeclaration(CsmDeclaration decl, List<TypeDescriptor> result, ComparatorFactory.NameComparator comparator) {
        switch (decl.getKind()) {
            case CLASS:
            case UNION:
            case STRUCT:
                CsmClass cls = (CsmClass) decl;
		if( comparator.match(decl.getName()) ) {
                    result.add(createTypeDescriptor(cls));
		}
                if( ! isCancelled ) {
                    for( CsmMember member : cls.getMembers() ) {
                        if( ! isCancelled ) {
                            processDeclaration(member, result, comparator);
                            return;
                        }
                    }
                }
                break;
            case ENUM:
            case TYPEDEF:
		if( comparator.match(decl.getName()) ) {
                    result.add(createTypeDescriptor((CsmClassifier) decl));
		}
                break;
            case BUILT_IN:
            case ENUMERATOR:
            case MACRO:
            case VARIABLE:
            case VARIABLE_DEFINITION:
            case FUNCTION:
            case FUNCTION_DEFINITION:
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
