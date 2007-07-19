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

package org.netbeans.modules.ruby.rubyproject.templates;

import org.netbeans.api.project.Project;
//import org.netbeans.modules.ruby.rubyproject.templates.RubyTargetChooserPanel;
//import org.netbeans.modules.ruby.rubyproject.templates.NewJavaFileWizardIterator;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;

/**
 * Default implementations of Java-specific template UI.
 * Based on org.netbeans.spi.gsfpath.project.support.ui.templates.*.
 *
 * @author Jesse Glick
 * @author Tor Norbye
 */
public class RubyTemplates {

    private RubyTemplates() {}
    
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
     * @since org.netbeans.modules.ruby.rubyproject.templates/1 1.3 
     */
    public static WizardDescriptor.Panel createPackageChooser(Project project, SourceGroup[] folders, 
        WizardDescriptor.Panel bottomPanel, boolean validPackageRequired) throws IllegalArgumentException {
        if (folders.length == 0) {
            throw new IllegalArgumentException("No folders selected"); // NOI18N
        }
        return new RubyTargetChooserPanel(project, folders, bottomPanel, NewRubyFileWizardIterator.TYPE_FILE, validPackageRequired);
    } 
    
    /** Creates new WizardIterator containing standard Package chooser
     * @return WizardIterator consisting of one panel containing package chooser
     */
    public static WizardDescriptor.InstantiatingIterator createRubyTemplateIterator () {
        return new NewRubyFileWizardIterator ();
    }
    
    /** Creates new WizardIterator containing standard Package chooser
     * @return WizardIterator consisting of one panel containing package chooser
     */
    public static WizardDescriptor.InstantiatingIterator createRubyClassTemplateIterator() {
        return NewRubyFileWizardIterator.classWizard();
    }

    public static WizardDescriptor.InstantiatingIterator createRubyModuleTemplateIterator() {
        return NewRubyFileWizardIterator.moduleWizard();
    }
    
    public static WizardDescriptor.InstantiatingIterator createRubyTestTemplateIterator() {
        return NewRubyFileWizardIterator.testWizard();
    }
}
