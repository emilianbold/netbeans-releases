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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.cnd.gotodeclaration.type;

import java.util.*;
import org.netbeans.modules.cnd.api.model.*;

import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.gotodeclaration.util.NameMatcherFactory;

import org.netbeans.spi.jumpto.type.SearchType;
import org.netbeans.spi.jumpto.type.TypeDescriptor;
import org.netbeans.spi.jumpto.type.TypeProvider;
import org.openide.util.NbBundle;

/**
 * Implementation of provider for "Jump to Type" for C/C++
 * @author Vladimir Kvashin
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.jumpto.type.TypeProvider.class)
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

//    public List<? extends TypeDescriptor> getTypeNames(Project project, String text, SearchType type) {
    public void computeTypeNames(Context context, Result res) {
        isCancelled = false;
        Project project = context.getProject();
        String text = context.getText();
        SearchType type = context.getSearchType();
	
	if( TRACE ) System.err.printf("CppTypeProvider.getTypeNames(%s, %s, %s)\n", project, text, type);
	
	
	
	CsmSelect.CsmFilter filter = NameMatcherFactory.createNameFilter(text, type);
	if( filter == null ) {
	    return;
	}
        
	if( project == null ) {
            Collection<CsmProject> csmProjects = CsmModelAccessor.getModel().projects();
	    if( ! csmProjects.isEmpty() ) {
                Set<TypeDescriptor> result = new HashSet<TypeDescriptor>();
		for( CsmProject csmProject : csmProjects ) {
		    processProject(csmProject, result, filter);
		}
                if( PROCESS_LIBRARIES ) {
                    for( CsmProject csmProject : csmProjects ) {
                        if( isCancelled ) {
                            break;
                        }
                        Set<CsmProject> processedLibs = new HashSet<CsmProject>();
                        processProjectLibs(csmProject, result, filter, processedLibs);
                    }
                }
                res.addResult(new ArrayList<TypeDescriptor>(result));
	    }
	}
	else {
	    Set<TypeDescriptor> result = new HashSet<TypeDescriptor>();
            CsmProject csmProject = CsmModelAccessor.getModel().getProject(project);
	    processProject(csmProject, result, filter);
            if( PROCESS_LIBRARIES ) {
                processProjectLibs(csmProject, result, filter, new HashSet<CsmProject>());
            }
            res.addResult(new ArrayList<TypeDescriptor>(result));
	}
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
    
    private void processProjectLibs(CsmProject project, Set<TypeDescriptor> result, 
            CsmSelect.CsmFilter filter, Set<CsmProject> processedLibs) {
        for( CsmProject lib : project.getLibraries() ) {
            if( isCancelled ) {
                return;
            }
            if( lib.isArtificial() ) {
                if( ! processedLibs.contains(lib) ) {
                    processedLibs.add(lib);
                    processProject(lib, result, filter);
                }
            }
        }
    }

    private void processProject(CsmProject project, Set<TypeDescriptor> result, CsmSelect.CsmFilter filter) {
	if( TRACE ) System.err.printf("processProject %s\n", project.getName());
        processNamespace(project.getGlobalNamespace(), result, filter);
    }
    
    private void processNamespace(CsmNamespace nsp, Set<TypeDescriptor> result, CsmSelect.CsmFilter filter) {
        if( TRACE ) System.err.printf("processNamespace %s\n", nsp.getQualifiedName());
        for( Iterator<CsmOffsetableDeclaration> iter  = CsmSelect.getDefault().getDeclarations(nsp, filter); iter.hasNext(); ) {
            if( isCancelled ) {
		return;
	    }
            CsmDeclaration declaration = iter.next();
	    processDeclaration(declaration, result);
	}
	for( CsmNamespace child : nsp.getNestedNamespaces() ) {
            if( isCancelled ) {
		return;
	    }
	    processNamespace(child, result, filter);
	}
    }

    private void processDeclaration(CsmDeclaration decl, Set<TypeDescriptor> result) {
        switch (decl.getKind()) {
            case CLASS:
            case UNION:
            case STRUCT:
                CsmClass cls = (CsmClass) decl;
                result.add(createTypeDescriptor(cls));
                if( ! isCancelled ) {
                    for( CsmMember member : cls.getMembers() ) {
                        if( ! isCancelled ) {
                            processDeclaration(member, result);
                        }
                    }
                }
                break;
            case ENUM:
            case TYPEDEF:
                result.add(createTypeDescriptor((CsmClassifier) decl));
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
