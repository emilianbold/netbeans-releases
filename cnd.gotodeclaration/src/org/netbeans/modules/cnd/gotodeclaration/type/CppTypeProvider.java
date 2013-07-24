/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
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
import org.netbeans.modules.cnd.api.model.services.CsmClassifierResolver;
import org.netbeans.modules.cnd.api.model.services.CsmSelect;
import org.netbeans.modules.cnd.api.model.services.CsmVisibilityQuery;
import org.netbeans.spi.jumpto.support.NameMatcher;
import org.netbeans.spi.jumpto.support.NameMatcherFactory;

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

    private volatile boolean isCancelled = false;
    private static final boolean PROCESS_LIBRARIES = true; // Boolean.getBoolean("cnd.type.provider.libraries");
    private static final boolean TRACE = Boolean.getBoolean("cnd.type.provider.trace");
    
    public CppTypeProvider() {
	if( TRACE ) System.err.printf("CppTypeProvider.ctor\n");
    }
    
    @Override
    public String name() {
        return "C/C++"; // NOI18N
    }

    @Override
    public String getDisplayName() {
        return NbBundle.getMessage(CppTypeProvider.class, "TYPE_PROVIDER_DISPLAY_NAME"); // NOI18N
    }

    @Override
    public void computeTypeNames(Context context, Result res) {
        isCancelled = false;
        Project project = context.getProject();
        String text = context.getText();
        SearchType type = context.getSearchType();
	
	if( TRACE ) System.err.printf("CppTypeProvider.getTypeNames(%s, %s, %s)\n", project, text, type);
	
	CsmSelect.CsmFilter filter = CsmSelect.CLASSIFIER_KIND_FILTER;
	NameMatcher matcher = NameMatcherFactory.createNameMatcher(text, type);
	if( project == null ) {
            Collection<CsmProject> csmProjects = CsmModelAccessor.getModel().projects();
	    if( ! csmProjects.isEmpty() ) {
                Set<TypeDescriptor> result = new HashSet<TypeDescriptor>();
		for( CsmProject csmProject : csmProjects ) {
		    processProject(csmProject, result, filter, matcher);
		}
                if( PROCESS_LIBRARIES ) {
                    for( CsmProject csmProject : csmProjects ) {
                        if( isCancelled ) {
                            break;
                        }
                        Set<CsmProject> processedLibs = new HashSet<CsmProject>();
                        processProjectLibs(csmProject, result, filter, processedLibs, matcher);
                    }
                }
                res.addResult(new ArrayList<TypeDescriptor>(result));
	    }
	} else {
	    Set<TypeDescriptor> result = new HashSet<TypeDescriptor>();
            CsmProject csmProject = CsmModelAccessor.getModel().getProject(project);
	    processProject(csmProject, result, filter, matcher);
            if( PROCESS_LIBRARIES ) {
                processProjectLibs(csmProject, result, filter, new HashSet<CsmProject>(), matcher);
            }
            res.addResult(new ArrayList<TypeDescriptor>(result));
	}
    }

    @Override
    public void cancel() {
	if( TRACE ) System.err.printf("CppTypeProvider.cancel\n");
        isCancelled = true;
    }

    @Override
    public void cleanup() {
	if( TRACE ) System.err.printf("CppTypeProvider.cleanup\n");
    }
    
    private static TypeDescriptor createTypeDescriptor(CsmClassifier classifier) {
	CppTypeDescriptor descriptor = new CppTypeDescriptor(classifier);
	return TRACE ? new TracingTypeDescriptor(descriptor) : descriptor;
    }
    
    private void processProjectLibs(CsmProject project, Set<TypeDescriptor> result, 
            CsmSelect.CsmFilter filter, Set<CsmProject> processedLibs, NameMatcher matcher) {
        for( CsmProject lib : project.getLibraries() ) {
            if( isCancelled ) {
                return;
            }
            if( lib.isArtificial() ) {
                if( ! processedLibs.contains(lib) ) {
                    processedLibs.add(lib);
                    processProject(lib, result, filter, matcher);
                }
            }
        }
    }

    private void processProject(CsmProject project, Set<TypeDescriptor> result, CsmSelect.CsmFilter filter, NameMatcher matcher) {
	if( TRACE ) System.err.printf("processProject %s\n", project.getName());
        if (isCancelled) {
            return;
        }
        processNamespace(project.getGlobalNamespace(), result, filter, matcher);
    }
    
    private void processNamespace(CsmNamespace nsp, Set<TypeDescriptor> result, CsmSelect.CsmFilter filter, NameMatcher matcher) {
        if( TRACE ) System.err.printf("processNamespace %s\n", nsp.getQualifiedName());
        if (isCancelled) {
            return;
        }
        for( Iterator<CsmOffsetableDeclaration> iter  = CsmSelect.getDeclarations(nsp, filter); iter.hasNext(); ) {
            if( isCancelled ) {
		return;
	    }
            CsmDeclaration declaration = iter.next();
	    processDeclaration(declaration, result, matcher);
	}
	for( CsmNamespace child : nsp.getNestedNamespaces() ) {
            if( isCancelled ) {
		return;
	    }
	    processNamespace(child, result, filter, matcher);
	}
    }

    private void processDeclaration(CsmDeclaration decl, Set<TypeDescriptor> result, NameMatcher matcher) {
        switch (decl.getKind()) {
            case CLASS:
            case UNION:
            case STRUCT:
                if(!isCancelled) {
                    CsmClass cls = (CsmClass) decl;
                    if (!CsmClassifierResolver.getDefault().isForwardClass(cls)) {
                        if (matcher.accept(decl.getName().toString())) {
                            if(CsmVisibilityQuery.isVisible(cls)) {
                                result.add(createTypeDescriptor(cls));
                            }
                        }
                        for( CsmMember member : cls.getMembers() ) {
                            if( ! isCancelled ) {
                                if (matcher.accept(member.getName().toString())) {
                                    processDeclaration(member, result, matcher);
                                }
                            }
                        }
                    }
                }
                break;
            case ENUM:
            case TYPEDEF:
                if(!isCancelled) {
                    if (matcher.accept(decl.getName().toString())) {
                        if(CsmVisibilityQuery.isVisible(decl)) {
                            result.add(createTypeDescriptor((CsmClassifier) decl));
                        }
                    }
                }
                break;
        }
    }    
}
