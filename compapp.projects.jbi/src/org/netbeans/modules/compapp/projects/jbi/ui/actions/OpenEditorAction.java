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

package org.netbeans.modules.compapp.projects.jbi.ui.actions;

import org.netbeans.spi.project.ui.support.ProjectActionPerformer;
import org.netbeans.api.project.Project;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.cookies.EditCookie;

import org.netbeans.modules.compapp.projects.jbi.CasaHelper;
import org.netbeans.modules.compapp.projects.jbi.JbiProject;

/**
 * Open the casa editor.
 *
 * To change this template use File | Settings | File Templates.
 */
public class OpenEditorAction implements ProjectActionPerformer {    

    /**
     * Creates a new instance of ProjectLevelAddAction
     */
    public OpenEditorAction() {
    }

    /**
     * DOCUMENT ME!
     *
     * @param p DOCUMENT ME!
     *
     * @return DOCUMENT ME!
     */
    public boolean enable(Project p) {
        return true;
    }

    /**
     * DOCUMENT ME!
     *
     * @param p DOCUMENT ME!
     */
    public void perform(Project p) {
        try {
            FileObject fobj = CasaHelper.getCasaFileObject((JbiProject)p, true); // create on demand
            DataObject dobj = DataObject.find(fobj);
            EditCookie ec = dobj.getCookie(EditCookie.class);
            if (ec != null) {
                ec.edit();
            }
        } catch (Exception ex) {
            // failed to open casa...
            ex.printStackTrace();
        }
    }
}
