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
 * Factory for creating actions sensitive to the project selected
 * as the main project in the UI.
 * @author Petr Hrebejk
 */
public class MainProjectSensitiveActions {
    
    private MainProjectSensitiveActions() {}
        
    /**
     * Creates an action sensitive to the project currently selected as main in the UI.
     * The action will invoke the given command on the main project. The action
     * may be disabled when no project is marked as main, or it may prompt the user
     * to select a main project, etc.
     * @param command the command which should be invoked when the action is
     *        performed
     * @param name display name of the action
     * @param icon icon of the action; may be null, in which case the action will
     *        not have an icon
     * @return an action sensitive to the main project
     */    
    public static Action mainProjectCommandAction( String command, String name, Icon icon  ) {
        return Utilities.getActionsFactory().mainProjectCommandAction( command, name, icon );
    }
        
    /**
     * Creates an action sensitive to the project currently selected as main in the UI.
     * When the action is invoked the supplied {@link ProjectActionPerformer#perform} 
     * will be called. The {@link ProjectActionPerformer#enable} method will
     * be consulted when the main project changes to determine whether the 
     * action should or should not be enabled. If no main project is selected the 
     * project parameter in the callback will be null.
     * @param performer callback class for enabling and performing the action    
     * @param name display name of the action
     * @param icon icon of the action; may be null, in which case the action will
     *        not have an icon
     * @return an action sensitive to the main project
     */
    public static Action mainProjectSensitiveAction( ProjectActionPerformer performer, String name, Icon icon ) {
        return Utilities.getActionsFactory().mainProjectSensitiveAction( performer, name, icon );
    }
    
}
