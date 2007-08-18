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
package org.netbeans.modules.web.jsf.navigation;

import java.io.IOException;
import java.util.List;
import org.netbeans.modules.web.jsf.api.facesmodel.FacesConfig;
import org.netbeans.modules.web.jsf.api.facesmodel.JSFConfigModel;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationCase;
import org.netbeans.modules.web.jsf.api.facesmodel.NavigationRule;
import org.openide.util.Exceptions;

/**
 *
 * @author joelle
 */
public class FacesModelUtility {
    
    public FacesModelUtility() {
    }
    public static String getToViewIdFiltered( NavigationCase navCase ) {
        final String viewId = navCase.getToViewId();
        return getViewIdFiltiered( viewId );
    }
    
    public static String getFromViewIdFiltered( NavigationRule navRule ){
        final String viewId = navRule.getFromViewId();
        return getViewIdFiltiered( viewId );
    }
    
    public static String getViewIdFiltiered( String paramViewId ){
        String viewId = paramViewId;
        if( viewId != null && viewId.length() > 0 && viewId.charAt(0) == '/') {
            viewId = viewId.replaceFirst("/", "");
        }
        return viewId;
    }
    
    public static void setToViewId(NavigationCase navCase, String filteredName ){
        if( filteredName != null && filteredName.length() > 0 ) {
            final String unfilteredName = "/".concat(filteredName);
            navCase.setToViewId(unfilteredName);
        } else {
            navCase.setToViewId(filteredName);
        }
        
    }
    
    public static void setFromViewId( NavigationRule navRule, String filteredName ){
        
        if( filteredName != null && filteredName.length() > 0 ) {
            final String unfilteredName = "/".concat(filteredName);
            navRule.setFromViewId(unfilteredName);
        } else {
            navRule.setFromViewId(filteredName);
        }
    }
    
    /**
     * Renames a page in the faces configuration file.
     * @param oldDisplayName
     * @param newDisplayName
     */
    public static void renamePageInModel(JSFConfigModel configModel, String oldDisplayName, String newDisplayName ) {
        configModel.startTransaction();
        final FacesConfig facesConfig = configModel.getRootComponent();
        final List<NavigationRule> navRules = facesConfig.getNavigationRules();
        for( NavigationRule navRule : navRules ){
            final String fromViewId = getFromViewIdFiltered(navRule);
            if ( fromViewId != null && fromViewId.equals(oldDisplayName) ){
                //                navRule.setFromViewId(newDisplayName);
                setFromViewId(navRule, newDisplayName);
            }
            final List<NavigationCase> navCases = navRule.getNavigationCases();
            for( NavigationCase navCase : navCases ) {
                //                String toViewId = navCase.getToViewId();
                final String toViewId = getToViewIdFiltered(navCase);
                if ( toViewId != null && toViewId.equals(oldDisplayName) ) {
                    //                    navCase.setToViewId(newDisplayName);
                    setToViewId(navCase, newDisplayName);
                }
            }
        }
        
        configModel.endTransaction();
        try {
            configModel.sync();
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }
    
}
