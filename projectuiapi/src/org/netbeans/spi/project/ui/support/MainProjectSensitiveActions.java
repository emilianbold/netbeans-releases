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
import org.netbeans.modules.project.uiapi.ActionsFactory;
import org.netbeans.modules.project.uiapi.PhysicalViewFactory;
import org.netbeans.modules.project.uiapi.Utilities;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.openide.util.Lookup;

/** Factory for creating actions sensitive to project selected as Main in the 
 * UI.
 * 
 * @author Petr Hrebejk
 */
public class MainProjectSensitiveActions {
    
    private MainProjectSensitiveActions() {}
        
    /** Creates action sensitive to project currently selected as main in the
     * UI. The action will invoke given command on the main project. The action
     * will be disabled when no project is marked as main     
     * @param command The command which should be invoked when the action is
     *        performed.    
     * @param name Display name of the action.
     * @param icon Icon of the action may be null in which case the Action will 
     *        not have an icon.
     */    
    public static Action mainProjectCommandAction( String command, String name, Icon icon  ) {
        return Utilities.getActionsFactory().mainProjectCommandAction( command, name, icon );
    }
        
    /** Creates action sensitive to project currently selected as main in the
     * UI. When the action is invoked the supplied {@link ProjectActionPerformer#perform} 
     * will be called. The {@link ProjectActionPerformer#enable} method will
     * be consulted when the main project changes to determine whether the 
     * action should or should not be enabled. If no main project is selected the 
     * project parameters in the callback will be null.
     * @param performer Callback class for enabling and performing the action    
     * @param name Display name of the action.
     * @param icon Icon of the action may be null in which case the Action will 
     *        not have an icon.
     */
    public static Action mainProjectSensitiveAction( ProjectActionPerformer performer, String name, Icon icon ) {
        return Utilities.getActionsFactory().mainProjectSensitiveAction( performer, name, icon );
    }
        
    
}
