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

import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

import java.io.File;

/**
 * Factory to be implemented bu the ui implementation
 * @author Petr Hrebejk
 */
public interface ProjectChooserFactory {

    public static final String WIZARD_KEY_PROJECT = "project"; // NOI18N

    public static final String WIZARD_KEY_TARGET_FOLDER = "targetFolder"; // NOI18N
    
    public static final String WIZARD_KEY_TARGET_NAME = "targetName"; // NOI18N
    
    public static final String WIZARD_KEY_TEMPLATE = "targetTemplate"; // NOI18N
    
    public File getProjectsFolder ();

    public void setProjectsFolder (File file);

    public JFileChooser createProjectChooser(); 
    
    public WizardDescriptor.Panel createSimpleTargetChooser( Project project, SourceGroup[] folders, WizardDescriptor.Panel bottomPanel );
            
}
