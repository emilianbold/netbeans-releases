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

import org.netbeans.api.project.Project;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.api.project.ui.OpenProjects;
import org.openide.util.NbBundle;
import org.openidex.search.SearchInfo;
import org.openidex.search.SearchInfoFactory;

/**
 * Defines search scope across all open projects.
 *
 * @author  Marian Petras
 */
final class SearchScopeOpenProjects extends AbstractProjectSearchScope {
    
    SearchScopeOpenProjects() {
        super(OpenProjects.PROPERTY_OPEN_PROJECTS);
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(),
                                   "SearchScopeNameOpenProjects");      //NOI18N
    }
    
    protected boolean checkIsApplicable() {
        return OpenProjects.getDefault().getOpenProjects().length > 0;
    }

    public SearchInfo getSearchInfo() {
        Project[] openProjects = OpenProjects.getDefault().getOpenProjects();
        if (openProjects.length == 0) {
            /*
             * We cannot prevent this situation. The action may be invoked
             * between moment the last project had been removed and the removal
             * notice was distributed to the open projects listener (and this
             * action disabled). This may happen if the the last project
             * is being removed in another thread than this action was
             * invoked from.
             */
            return createEmptySearchInfo();
        }
        
        if (openProjects.length == 1) {
            return createSingleProjectSearchInfo(openProjects[0]);
        }
        
        SearchInfo[] prjSearchInfos = new SearchInfo[openProjects.length];
        for (int i = 0; i < prjSearchInfos.length; i++) {
            prjSearchInfos[i] = createSingleProjectSearchInfo(openProjects[i]);
        }
        return SearchInfoFactory.createCompoundSearchInfo(prjSearchInfos);
    }

}
