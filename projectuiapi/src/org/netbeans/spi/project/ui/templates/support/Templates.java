/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.spi.project.ui.templates.support;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.netbeans.modules.project.uiapi.Utilities;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;

/**
 * Default implementations of template UI. 
 * @author Jesse Glick et al.
 */
public class Templates {
    
    private Templates() {}
    
    /** Method to communicate current choice of project to a custom WizardIteartor
     * associated with particular template
     */
    public static Project getProject( WizardDescriptor wizardDescriptor ) {
        return (Project) wizardDescriptor.getProperty( ProjectChooserFactory.WIZARD_KEY_PROJECT );
    }
    
    /** Method to communicate current choice of template to a custom 
     * {@link WizardDescriptor.InstantiatingIterator} associated with particular template.
     */
    public static FileObject getTemplate( WizardDescriptor wizardDescriptor ) {
        assert wizardDescriptor != null : "wizardDescriptor cannot be null.";
        if ( wizardDescriptor instanceof TemplateWizard ) {
            DataObject template = ((TemplateWizard)wizardDescriptor).getTemplate();
            if (template != null) {
                return template.getPrimaryFile();            
            }
        }
        return (FileObject) wizardDescriptor.getProperty( ProjectChooserFactory.WIZARD_KEY_TEMPLATE );
    }
    
    /** Method to communicate current choice of target folder to a custom 
     * {@link WizardDescriptor.InstantiatingIterator} associated with particular template.
     */
    public static FileObject getTargetFolder( WizardDescriptor wizardDescriptor ) {
        
        if ( wizardDescriptor instanceof TemplateWizard ) {
            try {
                return ((TemplateWizard)wizardDescriptor).getTargetFolder().getPrimaryFile();
            }
            catch ( IOException e ) {
                return null;
            }
        }
        else {
            return (FileObject) wizardDescriptor.getProperty( ProjectChooserFactory.WIZARD_KEY_TARGET_FOLDER );
        }
    }
    
    /** Sets the target folder for given WizardDescriptor to be used from
     * custom target choosers
     */    
    public static void setTargetFolder( WizardDescriptor wizardDescriptor, FileObject folder ) {
        
        if ( wizardDescriptor instanceof TemplateWizard ) {            
            DataFolder dataFolder = DataFolder.findFolder( folder );            
            ((TemplateWizard)wizardDescriptor).setTargetFolder( dataFolder );
        }
        else {
            wizardDescriptor.putProperty( ProjectChooserFactory.WIZARD_KEY_TARGET_FOLDER, folder );
        }
    }

    /** Method to communicate current choice of target name to a custom 
     * {@link WizardDescriptor.InstantiatingIterator} associated with particular template.
     */
    public static String getTargetName( WizardDescriptor wizardDescriptor ) {
        if ( wizardDescriptor instanceof TemplateWizard ) {
            return ((TemplateWizard)wizardDescriptor).getTargetName();
        }
        else {
            return (String) wizardDescriptor.getProperty( ProjectChooserFactory.WIZARD_KEY_TARGET_NAME );
        }
    }
    
    /** Sets the target name for given WizardDescriptor to be used from
     * custom target choosers
     */
    public static void setTargetName( WizardDescriptor wizardDescriptor, String targetName ) {
        if ( wizardDescriptor instanceof TemplateWizard ) {                        
            ((TemplateWizard)wizardDescriptor).setTargetName( targetName );
        }
        else {
            wizardDescriptor.putProperty( ProjectChooserFactory.WIZARD_KEY_TARGET_NAME, targetName );
        }
    }
            
    /**
     * Create a basic target chooser suitable for many kinds of templates.
     * The user is prompted to choose a location for the new file and a (base) name.
     * Instantiation is handled by {@link org.openide.loaders.DataObject#createFromTemplate}.
     * @param project The project to work on.
     * @param folders a list of possible roots to create the new file in
     * @return a wizard panel(s) prompting the user to choose a name and location
     */
    public static WizardDescriptor.Panel createSimpleTargetChooser( Project project, SourceGroup[] folders ) {        
        return createSimpleTargetChooser( project, folders, null );
    }
    
    /**
     * Create a basic target chooser suitable for many kinds of templates.
     * The user is prompted to choose a location for the new file and a (base) name.
     * Instantiation is handled by {@link org.openide.loaders.DataObject#createFromTemplate}.
     * Resulting panel can be decorated with additional panel placed below the standard target 
     * chooser.
     * @param project The project to work on.
     * @param folders a list of possible roots to create the new file in
     * @param bottomPanel panel which should be placed underneth the default chooser
     * @return a wizard panel(s) prompting the user to choose a name and location
     */
    public static WizardDescriptor.Panel createSimpleTargetChooser( Project project, SourceGroup[] folders, WizardDescriptor.Panel bottomPanel ) {        
        return Utilities.getProjectChooserFactory().createSimpleTargetChooser( project, folders, bottomPanel );
    }
        
}
