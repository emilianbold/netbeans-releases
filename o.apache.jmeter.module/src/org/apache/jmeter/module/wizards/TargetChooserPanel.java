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

package org.apache.jmeter.module.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Hrebejk, mkuchtiak
 */
final class TargetChooserPanel implements WizardDescriptor.Panel {

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private TargetChooserPanelGUI gui;

    private Project project;
    private TemplateWizard templateWizard;
    
    //TODO how to add [,] to the regular expression?
    private static final Pattern INVALID_FILENAME_CHARACTERS = Pattern.compile("[`~!@#$%^&*()=+\\|{};:'\",<>/?]"); // NOI18N

    TargetChooserPanel(Project project) {
        this.project = project;
    }
    
    TemplateWizard getTemplateWizard() {
        return templateWizard;
    } 

    public Component getComponent() {
        if (gui == null) {
            gui = new TargetChooserPanelGUI(this, project);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        return null;
        //return new HelpCtx( this.getClass().getName() +"."+fileType.toString()); //NOI18N
    }

    public boolean isValid() {        
        boolean ok = ( gui != null && gui.getTargetName() != null);
        
        if (!ok) {
            templateWizard.putProperty ("WizardPanel_errorMessage", null); // NOI18N
            return false;
        }
        
        String filename = gui.getTargetName();
        if (INVALID_FILENAME_CHARACTERS.matcher(filename).find()) {
            templateWizard.putProperty ("WizardPanel_errorMessage", NbBundle.getMessage(TargetChooserPanel.class, "MSG_invalid_filename", filename)); // NOI18N
            return false;
        }

        // check if the file name can be created
        String targetName=gui.getTargetName();
        java.io.File file = gui.getTargetFile();
        FileObject template = Templates.getTemplate( templateWizard );
        String ext = template.getExt ();
        
        String errorMessage = Utilities.canUseFileName (file, gui.getRelativeTargetFolder(), targetName, ext);
        if (errorMessage!=null)
            templateWizard.putProperty ("WizardPanel_errorMessage", errorMessage); // NOI18N
        else
            templateWizard.putProperty("WizardPanel_errorMessage", gui.getErrorMessage()); //NOI18N
        
        boolean valid = gui.isPanelValid() && errorMessage == null;

        if (valid && targetName.indexOf(".")>=0) {
            // warning when file name contains dots
            templateWizard.putProperty("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(TargetChooserPanel.class, "MSG_dotsInName",targetName+"."+ext));
        }
        return valid;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    protected void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings( Object settings ) {
        
        templateWizard = (TemplateWizard)settings;
        
        if ( gui != null ) {
            
            Project project = Templates.getProject( templateWizard );
            
            // Try to preselect a folder
            // XXX The test should be rewritten if external project dirs are supported
            
            FileObject preselectedTarget = Templates.getTargetFolder( templateWizard );
        
            // Init values
            gui.initValues( project, Templates.getTemplate( templateWizard ), preselectedTarget );
            
            templateWizard.putProperty ("NewFileWizard_Title", // NOI18N
              NbBundle.getMessage(TargetChooserPanel.class, "TITLE_JMXFile")); // NOI18N
        }
    }

    public void storeSettings(Object settings) {
        if ( WizardDescriptor.PREVIOUS_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        if ( WizardDescriptor.CANCEL_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        if( isValid() ) {
            File f = new File(gui.getCreatedFilePath());
            File ff = new File(f.getParentFile().getPath());
            if ( !ff.exists() ) {
                try {
                    FileUtil.createFolder(ff);
                } catch (IOException exc) {
                    Logger.getLogger("global").log(Level.INFO, null, exc);
                }
            }
            FileObject folder = FileUtil.toFileObject(ff);                

            Templates.setTargetFolder( (WizardDescriptor)settings, folder );
            Templates.setTargetName( (WizardDescriptor)settings, gui.getTargetName() );
        }
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }
}
