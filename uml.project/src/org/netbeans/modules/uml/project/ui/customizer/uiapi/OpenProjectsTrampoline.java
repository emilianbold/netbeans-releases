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

import java.beans.PropertyChangeListener;
import org.netbeans.api.project.Project;

/**
 * List of projects open in the GUI.
 * @author Petr Hrebejk
 */
public interface OpenProjectsTrampoline {

    public Project[] getOpenProjectsAPI();

    public void openAPI (Project[] projects, boolean openRequiredProjects);

    public void closeAPI (Project[] projects);

    public void addPropertyChangeListenerAPI( PropertyChangeListener listener );

    public void removePropertyChangeListenerAPI( PropertyChangeListener listener );
    
}
