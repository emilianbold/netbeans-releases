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
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.search.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;
import org.openidex.search.FileObjectFilter;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchInfoFactory;
import org.netbeans.modules.search.AbstractSearchScope;

/**
 * Base class for implementations of search scopes depending on the set
 * of open projects.
 *
 * @author  Marian Petras
 */
abstract class AbstractProjectSearchScope extends AbstractSearchScope
                                          implements PropertyChangeListener {
    
    private final String interestingProperty;
    private PropertyChangeListener openProjectsWeakListener;

    protected AbstractProjectSearchScope(String interestingProperty) {
        super();
        this.interestingProperty = interestingProperty;
    }
    
    protected void startListening() {
        OpenProjects openProjects = OpenProjects.getDefault();
        openProjectsWeakListener = WeakListeners.propertyChange(this, openProjects);
        openProjects.addPropertyChangeListener(openProjectsWeakListener);
    }
    
    protected void stopListening() {
        OpenProjects.getDefault().removePropertyChangeListener(openProjectsWeakListener);
        openProjectsWeakListener = null;
    }
    
    public final void propertyChange(PropertyChangeEvent e) {
        if (interestingProperty.equals(e.getPropertyName())) {
            updateIsApplicable();
        }
    }
    
    protected SearchInfo createSingleProjectSearchInfo(Project project) {
        SearchInfo prjSearchInfo = project.getLookup().lookup(SearchInfo.class);
        if (prjSearchInfo != null) {
            return prjSearchInfo;
        }
        
        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        
        if (sourceGroups.length == 0) {
            return createEmptySearchInfo();
        }
        
        FileObjectFilter[] filters
                = new FileObjectFilter[] {SearchInfoFactory.VISIBILITY_FILTER,
                                          SearchInfoFactory.SHARABILITY_FILTER};
        if (sourceGroups.length == 1) {
            return SearchInfoFactory.createSearchInfo(
                                            sourceGroups[0].getRootFolder(),
                                            true,
                                            filters);
        } else {
            FileObject[] rootFolders = new FileObject[sourceGroups.length];
            for (int i = 0; i < sourceGroups.length; i++) {
                rootFolders[i] = sourceGroups[i].getRootFolder();
            }
            return SearchInfoFactory.createSearchInfo(
                                            rootFolders,
                                            true,
                                            filters);
        }
    }

}
