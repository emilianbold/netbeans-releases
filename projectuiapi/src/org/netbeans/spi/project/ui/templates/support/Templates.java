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

package org.netbeans.spi.project.ui.templates.support;

import java.io.IOException;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.project.uiapi.ProjectChooserFactory;
import org.netbeans.modules.project.uiapi.Utilities;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
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
    
    /**
     * Find the project selected for a custom template wizard iterator.
     * <p class="nonnormative">
     * If the user selects File | New File, this will be the project chosen in the first panel.
     * If the user selects New from {@link org.netbeans.spi.project.ui.support.CommonProjectActions#newFileAction}, this will
     * be the project on which the context menu was invoked.
     * </p>
     * @param wizardDescriptor the wizard as passed to {@link WizardDescriptor.InstantiatingIterator#initialize}
     *                         or {@link TemplateWizard.Iterator#initialize}
     * @return the project into which the user has requested this iterator create a file (or null if not set)
     */
    public static Project getProject( WizardDescriptor wizardDescriptor ) {
        return (Project) wizardDescriptor.getProperty( ProjectChooserFactory.WIZARD_KEY_PROJECT );
    }
    
    /**
     * Find the template with which a custom template wizard iterator is associated.
     * <p class="nonnormative">
     * If the user selects File | New File, this will be the template chosen in the first panel.
     * If the user selects New from {@link org.netbeans.spi.project.ui.support.CommonProjectActions#newFileAction}, this will
     * be the template selected from the context submenu.
     * </p>
     * @param wizardDescriptor the wizard as passed to {@link WizardDescriptor.InstantiatingIterator#initialize}
     *                         or {@link TemplateWizard.Iterator#initialize}
     * @return the corresponding template marker file (or null if not set)
     */
    public static FileObject getTemplate( WizardDescriptor wizardDescriptor ) {
        if (wizardDescriptor == null) {
            throw new IllegalArgumentException("Cannot pass a null wizardDescriptor"); // NOI18N
        }
        if ( wizardDescriptor instanceof TemplateWizard ) {
            DataObject template = ((TemplateWizard)wizardDescriptor).getTemplate();
            if (template != null) {
                return template.getPrimaryFile();            
            }
        }
        return (FileObject) wizardDescriptor.getProperty( ProjectChooserFactory.WIZARD_KEY_TEMPLATE );
    }
    
    /**
     * Find the target folder selected for a custom template wizard iterator.
     * <p class="nonnormative">
     * If the user selects File | New File
     * this may not be set, unless you have called {@link #setTargetFolder}
     * in an earlier panel (such as that created by {@link #createSimpleTargetChooser(Project,SourceGroup[])}).
     * It may however have a preselected folder, e.g. if the user invoked New from
     * the context menu of a folder.
     * </p>
     * @param wizardDescriptor the wizard as passed to {@link WizardDescriptor.InstantiatingIterator#initialize}
     *                         or {@link TemplateWizard.Iterator#initialize}
     * @return the folder into which the user has requested this iterator create a file (or null if not set)
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

    /**
     * Find the existing sources folder selected for a custom template wizard iterator.
     * <p class="nonnormative">
     * This may not be set, unless you have CommonProjectActions.newProjectAction
     * with CommonProjectActions.EXISTING_SOURCES_FOLDER value.
     * <p>
     *
     * @param wizardDescriptor the wizard as passed to {@link WizardDescriptor.InstantiatingIterator#initialize}
     *                         or {@link TemplateWizard.Iterator#initialize}
     * @return the existing sources folder from which the user has requested this iterator to create a project
     *
     * @since 1.3 (17th May 2005)
     */
    public static FileObject getExistingSourcesFolder( WizardDescriptor wizardDescriptor ) {         
        return (FileObject) wizardDescriptor.getProperty( CommonProjectActions.EXISTING_SOURCES_FOLDER );
    }    
    /**
     * Stores a target folder so that it can be remembered later using {@link #getTargetFolder}.
     * @param wizardDescriptor a template wizard
     * @param folder a target folder to remember
     */    
    public static void setTargetFolder( WizardDescriptor wizardDescriptor, FileObject folder ) {
        
        if ( wizardDescriptor instanceof TemplateWizard ) {
            if (folder == null) {
                //#103971
                ((TemplateWizard)wizardDescriptor).setTargetFolder( null );
            } else {
                DataFolder dataFolder = DataFolder.findFolder( folder );            
                ((TemplateWizard)wizardDescriptor).setTargetFolder( dataFolder );
            }
        }
        else {
            wizardDescriptor.putProperty( ProjectChooserFactory.WIZARD_KEY_TARGET_FOLDER, folder );
        }
    }

    /** Method to communicate current choice of target name to a custom 
     * {@link WizardDescriptor.InstantiatingIterator} associated with particular template.
     * <p>XXX why is this public? only used from NewFileIterator in projectui?
     * @param wizardDescriptor a file wizard
     * @return the selected target name (could be null?)
     * @see TemplateWizard#getTargetName
     * @see ProjectChooserFactory#WIZARD_KEY_TARGET_NAME
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
     * <p>XXX why is this public? only used from SimpleTargetChooserPanel in projectui?
     * @param wizardDescriptor a file wizard
     * @param targetName a desired target name
     * @see TemplateWizard#setTargetName
     * @see ProjectChooserFactory#WIZARD_KEY_TARGET_NAME
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
     * Instantiation is handled by {@link DataObject#createFromTemplate}.
     * @param project The project to work on.
     * @param folders a list of possible roots to create the new file in
     * @return a wizard panel(s) prompting the user to choose a name and location
     */
    public static WizardDescriptor.Panel<WizardDescriptor> createSimpleTargetChooser( Project project, SourceGroup[] folders ) {        
        return createSimpleTargetChooser( project, folders, null );
    }
    
    /**
     * Create a basic target chooser suitable for many kinds of templates.
     * The user is prompted to choose a location for the new file and a (base) name.
     * Instantiation is handled by {@link DataObject#createFromTemplate}.
     * Resulting panel can be decorated with additional panel placed below the standard target 
     * chooser.
     * @param project The project to work on.
     * @param folders a list of possible roots to create the new file in
     * @param bottomPanel panel which should be placed underneth the default chooser
     * @return a wizard panel(s) prompting the user to choose a name and location
     */
    public static WizardDescriptor.Panel<WizardDescriptor> createSimpleTargetChooser(Project project, SourceGroup[] folders, WizardDescriptor.Panel<WizardDescriptor> bottomPanel) {
        return Utilities.getProjectChooserFactory().createSimpleTargetChooser( project, folders, bottomPanel );
    }
        
}
