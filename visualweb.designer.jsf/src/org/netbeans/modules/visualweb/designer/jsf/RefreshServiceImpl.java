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

package org.netbeans.modules.visualweb.designer.jsf;

import org.netbeans.modules.visualweb.project.jsf.api.JsfProjectUtils;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;

import org.netbeans.api.project.Project;

import org.netbeans.modules.visualweb.project.jsf.services.RefreshService;
import org.netbeans.modules.visualweb.insync.models.FacesModelSet;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;


// XXX Copied from designer/RefreshServiceProvider.
/**
 * Actual implementation of web/project's org.netbeans.modules.visualweb.jsf.project.services.RefreshService.
 * Provides designer refresh service. This is not part of the general DesignerService
 * API because web/project is in a different cluster which cannot access the rave
 * modules - the dependency goes the other way.
 *
 * @author Tor Norbye (the original code)
 * @author Peter Zavadsky (the new changes after the move)
 */
public class RefreshServiceImpl extends RefreshService {
    /** Creates a new instance of RefreshServiceProvider */
    public RefreshServiceImpl() {
    }

    public void refresh(Project project) {
        // Purge cached theme info from design container
        FacesModelSet fms = FacesModelSet.getInstance(project);

        if (fms != null) {
            FacesContext context = fms.getFacesContainer().getFacesContext();
            ExternalContext appContext = context.getExternalContext();

            if (JsfProjectUtils.JAVA_EE_5.equals(JsfProjectUtils.getJ2eePlatformVersion(project))) {
                // XXX Woodstock
                appContext.getApplicationMap().remove(com.sun.webui.theme.ThemeManager.THEME_MANAGER);

                //appContext.getInitParameterMap().put(ThemeFactory.DEFAULT_THEME, "defaulttheme");
                appContext.getSessionMap().remove(com.sun.webui.theme.Theme.THEME_ATTR);
            } else {
                appContext.getApplicationMap().remove(com.sun.rave.web.ui.theme.ThemeManager.THEME_MANAGER);

                //appContext.getInitParameterMap().put(ThemeFactory.DEFAULT_THEME, "defaulttheme");
                appContext.getSessionMap().remove(com.sun.rave.web.ui.theme.Theme.THEME_ATTR);
            }
        }

//        WebForm.refreshAll(project, false);
        refreshProject(project, false);
    }

    /** Refresh all forms in the project */
    static void refreshProject(Project project, boolean deep) {
        FileObject fobj = JsfProjectUtils.getDocumentRoot(project);
        refreshFolder(fobj, deep);
    }

    /** Refresh the given DataObject, if it's a webform */
    private static void refreshDataObject(DataObject dobj, boolean deep) {
//        if (hasWebFormForDataObject(dobj)) {
////            WebForm webform = WebForm.getWebFormForDataObject(WebForm.findHtmlDomProvider(dobj), dobj);
////            webform.getActions().refresh(deep);
////            WebForm webform = WebForm.findWebForm(dobj);
//            // XXX Really get, not find only? Revise.
//            WebForm webform = getWebFormForDataObject(dobj);
//            if (webform != null) {
////                webform.refresh(deep);
//                webform.refreshModel(deep);
//            }
//        }
        JsfForm jsfForm = JsfForm.findJsfForm(dobj);
        if (jsfForm != null) {
            jsfForm.refreshModel(deep);
        }
    }

    private static void refreshFolder(FileObject folder, boolean deep) {
        FileObject[] children = folder.getChildren();

        for (int i = 0; i < children.length; i++) {
            FileObject fo = children[i];

            if (fo.isFolder()) {
                refreshFolder(fo, deep);
            } else {
                try {
                    DataObject dobj = DataObject.find(fo);
//                    refresh(dobj, deep);
                    refreshDataObject(dobj, deep);
                } catch (DataObjectNotFoundException ex) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, ex);
                }
            }
        }
    }
    

}
