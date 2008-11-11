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
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.visualweb.project.jsf.services.RefreshService.class)
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
////            WebForm webform = WebForm.getWebFormForDataObject(WebForm.findDomProvider(dobj), dobj);
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
