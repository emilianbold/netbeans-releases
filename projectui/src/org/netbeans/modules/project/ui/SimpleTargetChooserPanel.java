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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;

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
    
    SimpleTargetChooserPanel( Project project, SourceGroup[] folders, WizardDescriptor.Panel bottomPanel ) {
        this.folders = folders;
        this.project = project;
        this.bottomPanel = bottomPanel;
        if ( bottomPanel != null ) {
            bottomPanel.addChangeListener( this );
        }
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new SimpleTargetChooserPanelGUI( project, folders, bottomPanel == null ? null : bottomPanel.getComponent() );
            gui.addChangeListener(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        // XXX
        return null;
    }

    public boolean isValid() {
        return gui != null && gui.getTargetName() != null &&
               ( bottomPanel == null || bottomPanel.isValid() );
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
                
        WizardDescriptor wd = (WizardDescriptor)settings;
                
        if ( gui == null ) {
            getComponent();
        }
        
        Project project = Templates.getProject( wd );

        // Try to preselect a folder            
        FileObject preselectedTarget = Templates.getTargetFolder( wd );
        // Init values
        gui.initValues( project, Templates.getTemplate( wd ), preselectedTarget );
        
        
        if ( bottomPanel != null ) {
            bottomPanel.readSettings( settings );
        }
    }

    public void storeSettings(Object settings) { 
        if( isValid() ) {
            if ( bottomPanel != null ) {
                bottomPanel.storeSettings( settings );
            }
            // XXX Better test for canWrite
            
            FileObject rootFolder = gui.getTargetGroup().getRootFolder();
            String folderName = gui.getTargetFolder();
            
            FileObject targetFolder;
            if ( folderName == null ) {
                targetFolder = rootFolder;
            }
            else {            
                targetFolder = rootFolder.getFileObject( folderName );
            }
            
            try {
                if ( targetFolder == null ) {
                    // XXX add deletion of the file in uninitalize ow the wizard
                    targetFolder = FileUtil.createFolder( rootFolder, folderName );
                }                
                Templates.setTargetFolder( (WizardDescriptor)settings, targetFolder );
                Templates.setTargetName( (WizardDescriptor)settings, gui.getTargetName() );
            }
            catch( java.io.IOException e ) {
                // XXX
                // Can't create the folder
            }
        }
    }

    public void stateChanged(ChangeEvent e) {        
        fireChange();
    }

}
