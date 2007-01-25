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

package org.netbeans.modules.sql.project.ui.wizards;

import org.netbeans.modules.compapp.projects.base.ui.wizards.NewIcanproProjectWizardIterator;
import org.netbeans.modules.sql.project.SQLproProjectGenerator;
import java.io.File;
import java.io.IOException;
import org.openide.util.NbBundle;



/**
 * Wizard to create a new Web project.
 * @author Jesse Glick
 */
public class NewSQLproProjectWizardIterator extends NewIcanproProjectWizardIterator{
    
    private static final long serialVersionUID = 1L;
    
    protected String getDefaultName() {
        return NbBundle.getMessage(NewSQLproProjectWizardIterator.class, "LBL_NPW1_DefaultProjectName"); //NOI18N        
    }

    protected void createProject(File dirF, String name, String j2eeLevel) throws IOException {
        SQLproProjectGenerator.createProject(dirF, name, j2eeLevel);
    }
}
