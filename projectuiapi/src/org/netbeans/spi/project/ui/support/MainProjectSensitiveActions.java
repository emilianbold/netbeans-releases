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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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
