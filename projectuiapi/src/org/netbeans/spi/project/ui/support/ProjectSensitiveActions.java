/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.ui.support;

import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.modules.project.uiapi.Utilities;
import org.openide.util.Lookup;

/**
 * Factory for creating project sensitive actions.
 * @author Jesse Glick, Petr Hrebejk
 */
public class ProjectSensitiveActions {
    
    private ProjectSensitiveActions() {}
        
    /** Creates action sensitive to set of currently selected projects. When
     * peroformed the action will call given command on the ActionProvider of
     * given project(s). The action will only be enbled when the exactly one 
     * project is selected and the command is endbled in the project's ActionProbider.
     * @param command The command which should be invoked when the action is
     *        performed.
     * @param namePattern Pattern which should be used for determining the Action's 
     *        name. It takes two parameters {0} - number of selected projects
     *        {1} - name of the first project.  
     * @param icon Icon of the action
     */    
    public static Action projectCommandAction( String command, String namePattern, Icon icon ) {
        return Utilities.getActionsFactory().projectCommandAction( command, namePattern, icon );
    }
    
    
    /** Creates action sensitive to set of currently selected projects. When
     * peroformed the action will call the {@link ProjectActionPerformer#perform} 
     * method on the ProjectActionPerformer supplied
     * as parameter. The action will only be enbled when the exactly one 
     * project is selected and passed in {@link ProjectActionPerformer#enable}
     * method returns true. 
     * @param performer ProjectActionPerformer which has to be called back.
     * @param namePattern Pattern which should be used for determining the Action's 
     *        name. It takes two parameters {0} - number of selected projects
     *        {1} - name of the first project.  
     * @param icon Icon of the action
     */    
    public static Action projectSensitiveAction( ProjectActionPerformer performer, String namePattern, Icon icon ) {
        return Utilities.getActionsFactory().projectSensitiveAction( performer, namePattern, icon );
    }
    
    
}
