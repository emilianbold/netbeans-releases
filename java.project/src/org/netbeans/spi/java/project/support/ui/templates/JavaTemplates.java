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
     * @param project the project which the template will be created in
     * @param folders a list of possible Java package roots to create the new file in (must be nonempty)
     * @return a wizard panel prompting the user to choose a name and package
     * @throws IllegalArgumentException if folders is empty
     */
    public static WizardDescriptor.Panel createPackageChooser(Project project, SourceGroup[] folders) throws IllegalArgumentException {
        return createPackageChooser(project, folders, null);
    }
    
    /**
     * Create a Java-oriented target chooser suitable for templates which are Java
     * sources or otherwise intended to reside in a Java package.
     * The user is prompted to choose a package location for the new file and a (base) name.
     * Resulting panel can be decorated with additional panel. Which will
     * be placed below the standard package chooser.
     * @param project the project which the template will be created in
     * @param folders a list of possible Java package roots to create the new file in (must be nonempty)
     * @param bottomPanel panel which should be placed underneth the default chooser
     * @return a wizard panel prompting the user to choose a name and package
     * @throws IllegalArgumentException if folders is empty
     */
    public static WizardDescriptor.Panel createPackageChooser(Project project, SourceGroup[] folders, WizardDescriptor.Panel bottomPanel) throws IllegalArgumentException {
        return createPackageChooser(project, folders, bottomPanel, false);
    }
    
    /**
     * Create a Java-oriented target chooser suitable for templates which are Java
     * sources or otherwise intended to reside in a Java package.
     * The user is prompted to choose a package location for the new file and a (base) name;
     * this method allows to specify whether a valid (non-empty) package is required.
     * Resulting panel can be decorated with additional panel. Which will
     * be placed below the standard package chooser.
     * @param project the project which the template will be created in
     * @param folders a list of possible Java package roots to create the new file in (must be nonempty)
     * @param bottomPanel panel which should be placed underneth the default chooser
     * @param validPackageRequired indicates whether a only a valid (non-empty) package is accepted
     * @return a wizard panel prompting the user to choose a name and package
     * @throws IllegalArgumentException if folders is empty
     */
    public static WizardDescriptor.Panel createPackageChooser(Project project, SourceGroup[] folders, 
        WizardDescriptor.Panel bottomPanel, boolean validPackageRequired) throws IllegalArgumentException {
        if (folders.length == 0) {
            throw new IllegalArgumentException("No folders selected"); // NOI18N
        }
        return new JavaTargetChooserPanel(project, folders, bottomPanel, false, validPackageRequired);
    } 
    
    /** Creates new WizardIterator containing standard Package chooser
     * @return WizardIterator consisting of one panel containing package chooser
     */
    public static WizardDescriptor.InstantiatingIterator createJavaTemplateIterator () {
        return new NewJavaFileWizardIterator ();
    }
    
}
