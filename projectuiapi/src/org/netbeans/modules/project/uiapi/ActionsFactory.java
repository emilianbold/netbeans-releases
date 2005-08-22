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

package org.netbeans.modules.project.uiapi;

import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.spi.project.ui.support.ProjectActionPerformer;

/**
 * Factory to be implemented bu the ui implementation
 * @author Petr Hrebejk
 */
public interface ActionsFactory {
    
    // Actions releated directly to project UI
    
    public Action setAsMainProjectAction(); 
    
    public Action customizeProjectAction();
    
    public Action openSubprojectsAction(); 
    
    public Action closeProjectAction();
    
    public Action newFileAction();
    
    public Action deleteProjectAction();
    
    public Action copyProjectAction();
    
    public Action moveProjectAction();
    
    public Action newProjectAction();
            
    // Actions sensitive to project selection
    
    public Action projectCommandAction( String command, String namePattern, Icon icon );
    
    public Action projectSensitiveAction( ProjectActionPerformer performer, String name, Icon icon );
    
    // Actions selection to main project selection
    
    public Action mainProjectCommandAction( String command, String name, Icon icon  );
        
    public Action mainProjectSensitiveAction( ProjectActionPerformer performer, String name, Icon icon );
    
    // Actions sensitive to file
    
    public Action fileCommandAction( String command, String name, Icon icon );

    public Action renameProjectAction();
    
}
