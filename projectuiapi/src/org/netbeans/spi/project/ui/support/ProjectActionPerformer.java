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

import org.netbeans.api.project.Project;

/** Callback interface for project/main project sensitive actions.
 * @author Petr Hrebejk
 */
public interface ProjectActionPerformer {
    
    /** Called when context of the action changes and the action should
     * be enabled/disabled within the new context (according to the newly
     * selected project.
     * @param project The currently selected project.
     * @return true or false to enable/disable the action
     */
    public boolean enable( Project project ); 
        
    /** Called when the user invokes the action
     * @param project The project this action was invoked for
     */
    public void perform( Project project ); 
    
}
