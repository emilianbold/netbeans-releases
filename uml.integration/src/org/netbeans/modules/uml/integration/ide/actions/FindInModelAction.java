/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.uml.integration.ide.actions;

import org.netbeans.modules.uml.integration.finddialog.FindControllerDialog;
import org.netbeans.modules.uml.project.ProjectUtil;
import org.netbeans.api.project.Project;
import org.openide.util.NbBundle;

public final class FindInModelAction extends AbstractUMLToolbarAction
{
    public boolean shouldEnable()
    {
        Project[] projects = ProjectUtil.getOpenUMLProjects();
        return projects.length > 0 ;
    }
    
    public String getName()
    {
        return NbBundle.getMessage(FindInModelAction.class, "CTL_FindInModelAction"); // NOI18N
    }
    
    // Commented out this method to fix issue #111506
//    protected String iconResource()
//    {
//        return ImageUtil.IMAGE_FOLDER + "find-in-model.png"; // NOI18N
//    }
    
    public void performAction()
    {
        Thread r = new Thread()
        {
            public void run()
            {
                FindControllerDialog fc = new FindControllerDialog();
                fc.showFindDialog();
            }
        };
        r.start();
    }
}
