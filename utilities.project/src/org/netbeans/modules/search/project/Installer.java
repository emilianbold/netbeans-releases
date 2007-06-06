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

import org.netbeans.modules.search.SearchScope;
import org.netbeans.modules.search.SearchScopeRegistry;
import org.openide.ErrorManager;
import org.openide.modules.ModuleInstall;

/**
 * Module installer that registers search scopes for project-wide searching
 * to the Utilities module.
 *
 * @author  Marian Petras
 */
public class Installer extends ModuleInstall {
    
    private static final String PROP_OPEN_PROJECTS = "scopeOpenProjects";//NOI18N
    private static final String PROP_MAIN_PROJECT  = "scopeMainProject";//NOI18N

    @Override
    public void restored() {
        SearchScope searchScope;
        
        putProperty(PROP_OPEN_PROJECTS,
                    searchScope = new SearchScopeOpenProjects(),
                    false);
        SearchScopeRegistry.getDefault().registerSearchScope(searchScope);
        
        putProperty(PROP_MAIN_PROJECT,
                    searchScope = new SearchScopeMainProject(),
                    false);
        SearchScopeRegistry.getDefault().registerSearchScope(searchScope);
        
    }
    
    @Override
    public void uninstalled() {
        Object value;
        
        value = getProperty(PROP_OPEN_PROJECTS);
        if (value instanceof SearchScope) {
            try {
                SearchScopeRegistry.getDefault().unregisterSearchScope(
                        (SearchScope) value);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }

        value = getProperty(PROP_MAIN_PROJECT);
        if (value instanceof SearchScope) {
            try {
                SearchScopeRegistry.getDefault().unregisterSearchScope(
                        (SearchScope) value);
            } catch (Exception ex) {
                ErrorManager.getDefault().notify(ex);
            }
        }
    }

}
