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

package org.netbeans.modules.java.project;

import java.awt.Component;
import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.SourceGroup;
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
final class JavaTargetChooserPanel implements WizardDescriptor.Panel, ChangeListener {

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private JavaTargetChooserPanelGUI gui;

    // private Project project;
    
    JavaTargetChooserPanel() {
        
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new JavaTargetChooserPanelGUI();
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
            String folderName = gui.getTargetFolder();
            FileObject folder = FileUtil.fromFile( new File( folderName ) )[0];            
            Templates.setTargetFolder( (WizardDescriptor)settings, folder );
            Templates.setTargetName( (WizardDescriptor)settings, gui.getTargetName() );
        }
    }

    public void stateChanged(ChangeEvent e) {        
        fireChange();
    }

}
