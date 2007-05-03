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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006s Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package projects.apitest;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.api.project.ui.OpenProjects;

/**
 *
 * @author jaromiruhrik
 */

public class ProjectOpenListener implements PropertyChangeListener {
    public boolean projectOpened = false;
    
    /** Listen for property which changes when project is hopefully opened. */
    public void propertyChange(PropertyChangeEvent evt) {
        if(OpenProjects.PROPERTY_OPEN_PROJECTS.equals(evt.getPropertyName())) {
            projectOpened = true;
        }
    }
}