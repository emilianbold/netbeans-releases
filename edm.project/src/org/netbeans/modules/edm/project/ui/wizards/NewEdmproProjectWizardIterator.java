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
package org.netbeans.modules.edm.project.ui.wizards;

import java.io.File;
import java.io.IOException;

import org.openide.util.NbBundle;

import org.netbeans.modules.compapp.projects.base.ui.wizards.*;
import org.netbeans.modules.edm.project.EdmproProjectGenerator;

public class NewEdmproProjectWizardIterator extends NewIcanproProjectWizardIterator {

    private static final long serialVersionUID = 1L;

    @Override
    protected String getDefaultName() {
        return NbBundle.getMessage(NewEdmproProjectWizardIterator.class, "LBL_NPW1_DefaultProjectName"); //NOI18N        
    }

    @Override
    protected void createProject(File dirF, String name, String j2eeLevel) throws IOException {
        EdmproProjectGenerator.createProject(dirF, name, j2eeLevel);
    }
}
