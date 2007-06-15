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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.compapp.projects.jbi.api;

import org.netbeans.api.project.Project;

/**
 * Service Provider Interface for internal JBI projects.
 * Port types are generic.
 * 
 * @author jsandusky
 */
public interface InternalProjectTypePlugin {

    /**
     * Display name for this JBI project type.
     * @return display name
     */
    String getDisplayName();
    
    /**
     * Resource path to the icon that corresponds to this JBI project type.
     * @return icon
     */
    String getIconFileBase();
    
    /**
     * Category name for the JBI project type.
     * JBI project types that correspond to the same category name will be
     * grouped together (i.e. for example, on a palette).
     * @return category name
     */
    String getCategoryName();
    
    /**
     * Obtains the WizardIterator that can show a new project
     * wizard to the user (if necessary), and then create the project.
     * @return the wizard iterator
     */
    InternalProjectTypePluginWizardIterator getWizardIterator();
    
    /**
     * Opens whatever editor that corresponds to the JBI project type.
     * The user may need to rebuild the composite application project in order
     * for any external edits to be applied.
     * @param project the project object to open an editor for
     */
    void openEditor(Project project);

}
