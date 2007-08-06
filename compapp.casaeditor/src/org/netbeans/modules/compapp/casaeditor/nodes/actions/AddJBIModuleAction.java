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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.compapp.casaeditor.nodes.actions;

import java.awt.event.ActionEvent;
import java.lang.ref.WeakReference;
import javax.swing.AbstractAction;
import org.netbeans.api.project.Project;
import org.netbeans.modules.compapp.projects.jbi.ui.actions.AddProjectAction;
import org.openide.util.NbBundle;

/**
 * Action to add a new JBI module.
 *
 * @author jqian
 */
public class AddJBIModuleAction extends AbstractAction {

    private WeakReference mProjectReference;

    public AddJBIModuleAction(Project jbiProject) {
        super(NbBundle.getMessage(AddJBIModuleAction.class, "LBL_AddProjectAction_Name"), null);
        mProjectReference = new WeakReference<Project>(jbiProject);
    }

    public void actionPerformed(ActionEvent e) {
        Project jbiProject = (Project) mProjectReference.get();
        if (jbiProject != null) {
            new AddProjectAction().perform(jbiProject);
        }
    }
}