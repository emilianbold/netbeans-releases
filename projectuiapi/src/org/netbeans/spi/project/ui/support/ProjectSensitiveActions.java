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
import org.netbeans.modules.project.uiapi.Utilities;

/**
 * Factory for creating project-sensitive actions.
 * @author Petr Hrebejk
 */
public class ProjectSensitiveActions {
    
    private ProjectSensitiveActions() {}
        
    /**
     * Creates an action sensitive to the set of currently selected projects.
     * When performed the action will call the given command on the {@link ActionProvider} of
     * the selected project(s). The action will only be enabled when the exactly one
     * project is selected and the command is enabled in the project's action provider.<BR>
     * Shorcuts for actions are shared on per command. (I.e. Actions with equals commands
     * will have eqal shorcuts.)
     * @param command the command which should be invoked when the action is
     *        performed
     * @param namePattern a pattern which should be used for determining the action's
     *        name (label). It takes two parameters a la {@link MessageFormat}: <code>{0}</code> - number of selected projects;
     *        <code>{1}</code> - name of the first project.
     * @param icon icon of the action (or null)
     * @return an action sensitive to the current project
     */    
    public static Action projectCommandAction( String command, String namePattern, Icon icon ) {
        return Utilities.getActionsFactory().projectCommandAction( command, namePattern, icon );
    }
    
    /**
     * Creates an action sensitive to the set of currently selected projects.
     * When performed the action will call {@link ProjectActionPerformer#perform}
     * on the action performer supplied
     * as a parameter. The action will only be enabled when the exactly one 
     * project is selected and {@link ProjectActionPerformer#enable}
     * returns true.
     * @param performer an action performer
     * @param namePattern pattern which should be used for determining the action's
     *        name (label). It takes two parameters a la {@link MessageFormat}: <code>{0}</code> - number of selected projects;
     *        <code>{1}</code> - name of the first project.
     * @param icon icon of the action (XXX or null?)
     * @return an action sensitive to the current project
     */    
    public static Action projectSensitiveAction( ProjectActionPerformer performer, String namePattern, Icon icon ) {
        return Utilities.getActionsFactory().projectSensitiveAction( performer, namePattern, icon );
    }
    
}
