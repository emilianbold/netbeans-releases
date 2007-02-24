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

package org.netbeans.modules.uml.project.ui.customizer.uiapi;

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
            
    // Actions sensitive to project selection
    
    public Action projectCommandAction( String command, String namePattern, Icon icon );
    
    public Action projectSensitiveAction( ProjectActionPerformer performer, String name, Icon icon );
    
    // Actions selection to main project selection
    
    public Action mainProjectCommandAction( String command, String name, Icon icon  );
        
    public Action mainProjectSensitiveAction( ProjectActionPerformer performer, String name, Icon icon );
    
    // Actions sensitive to file
    
    public Action fileCommandAction( String command, String name, Icon icon );
    
}
