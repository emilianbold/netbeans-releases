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
import org.openide.loaders.TemplateWizard;
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
    
    SimpleTargetChooserPanel( Project project, SourceGroup[] folders ) {
        this.folders = folders;
        this.project = project;
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new SimpleTargetChooserPanelGUI( project, folders );
            gui.addChangeListener(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        // XXX
        return null;
    }

    public boolean isValid() {
        return gui != null && gui.getTargetName() != null && gui.getTargetFolder() != null;
    }

    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener)it.next()).stateChanged(e);
        }
    }

    public void readSettings( Object settings ) {
        
        TemplateWizard templateWizard = (TemplateWizard)settings;
        
        if ( gui != null ) {
            
            Project project = Templates.getProject( templateWizard );
            
            // Try to preselect a folder
            // XXX The test should be rewritten if external project dirs are supported
            
            FileObject preselectedTarget = Templates.getTargetFolder( templateWizard );
            String targetFolder = null;
            if ( preselectedTarget != null && FileUtil.isParentOf( project.getProjectDirectory(), preselectedTarget ) ) {
                targetFolder = FileUtil.getRelativePath( project.getProjectDirectory(), preselectedTarget );
            }
                        
            // Init values
            gui.initValues( project, Templates.getTemplate( templateWizard ), targetFolder );
        }
    }

    public void storeSettings(Object settings) { 
        if( isValid() ) {
            // XXX Better test for canWrite
            String folderName = gui.getTargetFolder();
            File f = new File( folderName );
            try {
                if ( !f.exists() ) {
                    // XXX add deletion of the file in uninitalize ow the wizard
                    String relativeFolder = gui.getRelativeTargetFolder();
                    FileObject prjDir = project.getProjectDirectory();
                    FileUtil.createFolder( prjDir, relativeFolder );
                }                
                FileObject folder = FileUtil.toFileObject(f);            
                Templates.setTargetFolder( (WizardDescriptor)settings, folder );
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
