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

/**
 * Defines search scope across the main project.
 *
 * @author  Marian Petras
 */
final class SearchScopeMainProject extends AbstractProjectSearchScope {
    
    SearchScopeMainProject() {
        super(OpenProjects.PROPERTY_MAIN_PROJECT);
    }
    
    public String getDisplayName() {
        return NbBundle.getMessage(getClass(),
                                   "SearchScopeNameMainProject");       //NOI18N
    }

    protected boolean checkIsApplicable() {
        return OpenProjects.getDefault().getMainProject() != null;
    }

    public SearchInfo getSearchInfo() {
        Project mainProject = OpenProjects.getDefault().getMainProject();
        if (mainProject == null) {
            /*
             * We cannot prevent this situation. The action may be invoked
             * between moment the main project had been closed and the removal
             * notice was distributed to the main project listener (and this
             * action disabled). This may happen if the the main project
             * is being closed in another thread than this action was
             * invoked from.
             */
            return createEmptySearchInfo();
        }
        
        return createSingleProjectSearchInfo(mainProject);
    }
    
}
