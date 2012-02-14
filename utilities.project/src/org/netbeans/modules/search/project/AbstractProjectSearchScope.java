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

package org.netbeans.modules.search.project;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.search.SearchInfoDefinitionFactory;
import org.netbeans.api.search.provider.SearchInfo;
import org.netbeans.api.search.provider.SearchInfoUtils;
import org.netbeans.spi.search.SearchFilterDefinition;
import org.netbeans.spi.search.SearchInfoDefinition;
import org.netbeans.spi.search.SearchScopeDefinition;
import org.openide.filesystems.FileObject;
import org.openide.util.WeakListeners;

/**
 * Base class for implementations of search scopes depending on the set
 * of open projects.
 *
 * @author  Marian Petras
 */
abstract class AbstractProjectSearchScope extends SearchScopeDefinition
                                          implements PropertyChangeListener {
    
    private final String interestingProperty;
    private PropertyChangeListener openProjectsWeakListener;

    protected AbstractProjectSearchScope(String interestingProperty) {
        super();
        this.interestingProperty = interestingProperty;
        OpenProjects openProjects = OpenProjects.getDefault();
        openProjectsWeakListener = WeakListeners.propertyChange(this,
                openProjects);
        openProjects.addPropertyChangeListener(openProjectsWeakListener);
    }

    @Override
    public void clean() {
        OpenProjects.getDefault().removePropertyChangeListener(
                openProjectsWeakListener);
        openProjectsWeakListener = null;
    }

    @Override
    public final void propertyChange(PropertyChangeEvent e) {
        if (interestingProperty.equals(e.getPropertyName())) {
            notifyListeners();
        }
    }
    
    protected SearchInfo createSingleProjectSearchInfo(Project project) {

        SearchInfoDefinition prjSearchInfo =
                project.getLookup().lookup(SearchInfoDefinition.class);
        if (prjSearchInfo != null) {
            SearchInfoUtils.createForDefinition(prjSearchInfo);
        }

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] sourceGroups = sources.getSourceGroups(
                Sources.TYPE_GENERIC);
        
        if (sourceGroups.length == 0) {        
        }

        SearchFilterDefinition[] filters
                = new SearchFilterDefinition[] {SearchInfoDefinitionFactory.VISIBILITY_FILTER,
                                                SearchInfoDefinitionFactory.SHARABILITY_FILTER};
        if (sourceGroups.length == 1) {
            return SearchInfoUtils.createSearchInfoForRoot(
                    sourceGroups[0].getRootFolder(), filters);
        } else {
            FileObject[] rootFolders = new FileObject[sourceGroups.length];
            for (int i = 0; i < sourceGroups.length; i++) {
                rootFolders[i] = sourceGroups[i].getRootFolder();
            }
            return SearchInfoUtils.createSearchInfoForRoots(
                                            rootFolders,
                                            filters);
        }
    }
}
