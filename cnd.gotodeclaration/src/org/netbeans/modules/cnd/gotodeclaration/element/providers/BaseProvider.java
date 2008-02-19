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
package org.netbeans.modules.cnd.gotodeclaration.element.providers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.model.CsmModelAccessor;
import org.netbeans.modules.cnd.api.model.CsmProject;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementDescriptor;
import org.netbeans.modules.cnd.gotodeclaration.element.spi.ElementProvider;
import org.netbeans.modules.cnd.gotodeclaration.util.NameMatcher;
import org.netbeans.modules.cnd.gotodeclaration.util.NameMatcherFactory;
import org.netbeans.spi.jumpto.type.SearchType;

/**
 * A common base class for several providers
 * @author Vladimir Kvashin
 */
public abstract class BaseProvider implements ElementProvider {

    protected interface ResultSet {
        public void add(ElementDescriptor descriptor);
        public Collection<? extends ElementDescriptor> getResult();
    }
    
    protected abstract static class ProviderDelegate {

            protected class ResultSetImpl implements ResultSet {

                private Map<ElementDescriptor, Boolean> data = new HashMap<ElementDescriptor, Boolean>();

                public void add(ElementDescriptor descriptor) {
                    // There might be several instances that correspond to the same declaration (IZ #116478);
                    // in this case instance that belongs to non-artificial project is stronger
                    if( ! data.containsKey(descriptor) || ! isArtificial(currentProject) ) {
                        data.put(descriptor, Boolean.TRUE);
                    }
                }

                public Collection<? extends ElementDescriptor> getResult() {
                    return new ArrayList<ElementDescriptor>(data.keySet());
                }

                private boolean isArtificial(CsmProject project) {
                    return project != null && project.isArtificial();
                }

            }

            private boolean isCancelled = false;

            protected static final boolean PROCESS_LIBRARIES = true; // Boolean.getBoolean("cnd.type.provider.libraries");
            protected static final boolean TRACE = Boolean.getBoolean("cnd.goto.fv.trace");

            /** To not process same libararies twice */
            private Set<CsmProject> processedProjects = new HashSet<CsmProject>();

            /** Project that is currently being processed. */
            private CsmProject currentProject;

            public void cancel() {
                isCancelled = true;
            }

            protected final boolean isCancelled() {
                return isCancelled;
            }

            protected abstract void processProject(CsmProject project, ResultSet result, NameMatcher comparator);

            public Collection<? extends ElementDescriptor> getElements(Project project, String text, SearchType type, boolean first) {

                if( TRACE ) System.err.printf("%s.getElements(%s, %s, %s)\n", getBriefClassName(), project, text, type);

                NameMatcher comparator = NameMatcherFactory.createNameMatcher(text, type);
                if( comparator == null ) {
                    return Collections.emptyList();
                }

                if( first ) {
                    processedProjects.clear();
                }
                ResultSet result = new ResultSetImpl();
                CsmProject csmProject = CsmModelAccessor.getModel().getProject(project);
                if( csmProject != null ) {
                    // we should check the processed project here:
                    // otherwise when some of the required projects are open,we'll have duplicates
                    if( ! processedProjects.contains(csmProject) ) {
                        processedProjects.add(csmProject);
                        currentProject = csmProject;
                        processProject(csmProject, result, comparator);
                        currentProject = null;
                        if( PROCESS_LIBRARIES ) {
                            for( CsmProject lib : csmProject.getLibraries() ) {
                                if( isCancelled() ) {
                                    break;
                                }
                                if( lib.isArtificial() ) {
                                    if( ! processedProjects.contains(lib) ) {
                                        processedProjects.add(lib);
                                        currentProject = lib;
                                        processProject(lib, result, comparator);
                                        currentProject = null;
                                    }
                                }
                            }
                        }
                    }
                }
                return result.getResult();
            }

            public void cleanup() {
                processedProjects.clear();
                currentProject = null;
            }

            private String getBriefClassName() {
                String name = getClass().getName();
                int pos = name.lastIndexOf('.');
                return (pos >= 0) ? name.substring(pos + 1) : name;
            }

        }
    
    private ProviderDelegate delegate;
    
    protected abstract ProviderDelegate createDelegate();
    
    public void cancel() {
        ProviderDelegate oldDelegate = null;
        synchronized (this) {
            if( delegate != null ) {
                oldDelegate = delegate;
                delegate = null;
            }
        }
        if( oldDelegate != null ) {
            oldDelegate.cancel();
        }
    }
    
    public boolean isSuitable() {
	return ! CsmModelAccessor.getModel().projects().isEmpty();
    }
    
    
    public Collection<? extends ElementDescriptor> getElements(Project project, String text, SearchType type, boolean first) {
        cancel();
	delegate = createDelegate();
        return delegate.getElements(project, text, type, first);
    }
    
    
    public void cleanup() {
        ProviderDelegate oldDelegate = null;
        synchronized (this) {
            if( delegate != null ) {
                oldDelegate = delegate;
                delegate = null;
            }
        }
        if( oldDelegate != null ) {
            oldDelegate.cancel();
            oldDelegate.cleanup();
        }
        
    }
}
