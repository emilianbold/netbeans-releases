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

import org.netbeans.api.project.Project;

/**
 * Callback interface for project- and main project-sensitive actions.
 * @author Petr Hrebejk
 */
public interface ProjectActionPerformer {

    /**
     * Called when the context of the action changes and the action should
     * be enabled or disabled within the new context, according to the newly
     * selected project.
     * @param project the currently selected project, or null if no project is selected
     * @return true to enable the action, false to disable it
     */
    boolean enable(Project project);
        
    /**
     * Called when the user invokes the action.
     * @param project the project this action was invoked for (XXX can this be null or not?)
     */
    void perform(Project project);
    
}
