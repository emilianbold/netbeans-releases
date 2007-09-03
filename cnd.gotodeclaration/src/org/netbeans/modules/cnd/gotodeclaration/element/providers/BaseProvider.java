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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementProvider;
import org.netbeans.modules.cnd.gotodeclaration.util.NameMatcherFactory;
import org.netbeans.modules.cnd.gotodeclaration.util.NameMatcher;
import org.netbeans.spi.jumpto.type.SearchType;

/**
 * A common base class for several providers
 * @author Vladimir Kvashin
 */
public abstract class BaseProvider implements ElementProvider {

    private boolean isCancelled = false;
    protected static final boolean PROCESS_LIBRARIES = true; // Boolean.getBoolean("cnd.type.provider.libraries");
    protected static final boolean TRACE = Boolean.getBoolean("cnd.goto.fv.trace");
    
    public void cancel() {
	isCancelled = true;
    }
    
    protected final boolean isCancelled() {
	return isCancelled;
    }
    
    public boolean isSuitable() {
	return ! CsmModelAccessor.getModel().projects().isEmpty();
    }
    
    protected abstract void processProject(CsmProject project, List<ElementDescriptor> result, NameMatcher comparator);
    
    public Collection<? extends ElementDescriptor> getElements(Project project, String text, SearchType type) {

	if( TRACE ) System.err.printf("%s.getElements(%s, %s, %s)\n", getBriefClassName(), project, text, type);

	@SuppressWarnings("unchecked")
	List<ElementDescriptor> result = Collections.EMPTY_LIST;
	
	NameMatcher comparator = NameMatcherFactory.createNameMatcher(text, type);
	if( comparator == null ) {
	    return result;
	}
	
	Set<CsmProject> processedProjects = new HashSet<CsmProject>();
        result = new ArrayList<ElementDescriptor>(1000);
        CsmProject csmProject = CsmModelAccessor.getModel().getProject(project);
	if( csmProject != null ) {
	    // we should check the processed project here:
	    // otherwise when some of the required projects are open,we'll have duplicates
	    if( ! processedProjects.contains(csmProject) ) {
                processedProjects.add(csmProject);
                processProject(csmProject, result, comparator);
                if( PROCESS_LIBRARIES ) {
                    for( CsmProject lib : csmProject.getLibraries() ) {
                        if( isCancelled() ) {
                            break;
                        }
                        if( ! processedProjects.contains(lib) ) {
                            processedProjects.add(lib);
                            if( lib.isArtificial() ) {
                                processProject(lib, result, comparator);
                            }
                        }
                    }
                }
	    }
	}
	return result;
    }
    
    
    public void cleanup() {
    }

    private String getBriefClassName() {
	String name = getClass().getName();
	int pos = name.lastIndexOf('.');
	return (pos >= 0) ? name.substring(pos + 1) : name;
    }
    
    
}
