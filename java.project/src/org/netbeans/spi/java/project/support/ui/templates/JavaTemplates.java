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

package org.netbeans.spi.java.project.support.ui.templates;

import org.netbeans.api.project.Project;
import org.netbeans.modules.java.project.JavaTargetChooserPanel;
import org.netbeans.modules.java.project.NewJavaFileWizardIterator;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Default implementations of Java-specific template UI.
 * @author Jesse Glick
 */
public class JavaTemplates {
    
    private JavaTemplates() {}
    
    /**
     * Create a Java-oriented target chooser suitable for templates which are Java
     * sources or otherwise intended to reside in a Java package.
     * The user is prompted to choose a package location for the new file and a (base) name.
     * @param template the file to use as a template (see {@link org.openide.loaders.DataObject#createFromTemplate})
     * @param folders a list of possible Java package roots to create the new file in
     * @return a wizard panel(s) prompting the user to choose a name and package
     */
    public static WizardDescriptor.Panel createPackageChooser(Project project, SourceGroup[] folders) {       
        return new JavaTargetChooserPanel(project, folders);
    }
    
    public static WizardDescriptor.InstantiatingIterator createJavaTemplateIterator () {
        return new NewJavaFileWizardIterator ();
    }
    
}
