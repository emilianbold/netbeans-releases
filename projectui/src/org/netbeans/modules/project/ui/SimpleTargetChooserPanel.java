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

package org.netbeans.modules.project.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Hrebejk
 */
final class SimpleTargetChooserPanel implements WizardDescriptor.Panel, ChangeListener {

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private SimpleTargetChooserPanelGUI gui;

    private Project project;
    private SourceGroup[] folders;
    private WizardDescriptor.Panel bottomPanel;
    private WizardDescriptor wizard;
    private boolean isFolder;
    
    SimpleTargetChooserPanel( Project project, SourceGroup[] folders, WizardDescriptor.Panel bottomPanel, boolean isFolder ) {
        this.folders = folders;
        this.project = project;
        this.bottomPanel = bottomPanel;
        if ( bottomPanel != null ) {
            bottomPanel.addChangeListener( this );
        }
        this.isFolder = isFolder;
        this.gui = null;
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new SimpleTargetChooserPanelGUI( project, folders, bottomPanel == null ? null : bottomPanel.getComponent(), isFolder );
            gui.addChangeListener(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        if ( bottomPanel != null ) {
            HelpCtx bottomHelp = bottomPanel.getHelp();
            if ( bottomHelp != null ) {
                return bottomHelp;
            }
        }
        
        //XXX
        return null;
        
    }

    public boolean isValid() {
        boolean ok = ( gui != null && gui.getTargetName() != null &&
               ( bottomPanel == null || bottomPanel.isValid() ) );
        
        if (!ok) {
            return false;
        }
        
        // check if the file name can be created
        FileObject template = Templates.getTemplate( wizard );

        String errorMessage = ProjectUtilities.canUseFileName (gui.getTargetGroup().getRootFolder(), gui.getTargetFolder(), gui.getTargetName(), template.getExt ());
        wizard.putProperty ("WizardPanel_errorMessage", errorMessage); // NOI18N

        return errorMessage == null;
    }

    public synchronized void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public synchronized void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        List templist;
        synchronized (this) {
            templist = new ArrayList (listeners);
        }
        Iterator it = templist.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings( Object settings ) {
                
        wizard = (WizardDescriptor)settings;
                
        if ( gui == null ) {
            getComponent();
        }
        
        // Try to preselect a folder            
        FileObject preselectedTarget = Templates.getTargetFolder( wizard );
        // Init values
        gui.initValues( Templates.getTemplate( wizard ), preselectedTarget );
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        Object substitute = gui.getClientProperty ("NewFileWizard_Title"); // NOI18N
        if (substitute != null) {
            wizard.putProperty ("NewFileWizard_Title", substitute); // NOI18N
        }
        
        wizard.putProperty ("WizardPanel_contentData", new String[] { // NOI18N
            NbBundle.getBundle (SimpleTargetChooserPanel.class).getString ("LBL_TemplatesPanel_Name"), // NOI18N
            NbBundle.getBundle (SimpleTargetChooserPanel.class).getString ("LBL_SimpleTargetChooserPanel_Name")}); // NOI18N
            
        if ( bottomPanel != null ) {
            bottomPanel.readSettings( settings );
        }
    }
    
    public void storeSettings(Object settings) { 
        if ( WizardDescriptor.PREVIOUS_OPTION.equals( ((WizardDescriptor)settings).getValue() ) ) {
            return;
        }
        if( isValid() ) {
            if ( bottomPanel != null ) {
                bottomPanel.storeSettings( settings );
            }
            
            Templates.setTargetFolder( (WizardDescriptor)settings, getTargetFolderFromGUI () );
            Templates.setTargetName( (WizardDescriptor)settings, gui.getTargetName() );
        }
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }

    public void stateChanged(ChangeEvent e) {        
        fireChange();
    }
    
    private FileObject getTargetFolderFromGUI () {
        FileObject rootFolder = gui.getTargetGroup().getRootFolder();
        String folderName = gui.getTargetFolder();

        FileObject targetFolder;
        if ( folderName == null ) {
            targetFolder = rootFolder;
        }
        else {            
            targetFolder = rootFolder.getFileObject( folderName );
        }

        if ( targetFolder == null ) {
            // XXX add deletion of the file in uninitalize ow the wizard
            try {
                targetFolder = FileUtil.createFolder( rootFolder, folderName );
            } catch (IOException ioe) {
                // XXX
                // Can't create the folder
            }
        }
        
        return targetFolder;
    }
}
