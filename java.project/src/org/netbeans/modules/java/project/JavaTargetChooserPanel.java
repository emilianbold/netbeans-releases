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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Petr Hrebejk
 */
public final class JavaTargetChooserPanel implements WizardDescriptor.Panel, ChangeListener {

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private JavaTargetChooserPanelGUI gui;
    private WizardDescriptor.Panel bottomPanel;
    private WizardDescriptor wizard;

    private Project project;
    private SourceGroup folders[];
    private boolean isPackage;
    
    public JavaTargetChooserPanel( Project project, SourceGroup folders[], WizardDescriptor.Panel bottomPanel, boolean isPackage ) {
        this.project = project;
        this.folders = folders;
        this.bottomPanel = bottomPanel;
        this.isPackage = isPackage;
        if ( bottomPanel != null ) {
            bottomPanel.addChangeListener( this );
        }
    }

    public Component getComponent() {
        if (gui == null) {
            gui = new JavaTargetChooserPanelGUI( bottomPanel == null ? null : bottomPanel.getComponent(), isPackage );
            gui.addChangeListener(this);
        }
        return gui;
    }

    public HelpCtx getHelp() {
        // XXX
        return null;
    }

    public boolean isValid() {
        if( gui == null || gui.getTargetName() == null ||
            ( bottomPanel != null && !bottomPanel.isValid() ) ) {
           setErrorMessage( null );
           return false;        
        }
        
        if ( isPackage ) {
            if ( !isValidPackageName( gui.getTargetName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
                return false;
            }
        }
        else {
            if ( !isValidTypeIdentifier( gui.getTargetName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidClass" );
                return false;
            }
            else if ( !isValidPackageName( gui.getPackageName() ) ) {
                setErrorMessage( "ERR_JavaTargetChooser_InvalidPackage" );
                return false;
            }
        }
        
        setErrorMessage( null );
        return true;
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
        
        wizard = (WizardDescriptor)settings;
        
        if ( gui != null ) {
            
            Project project = Templates.getProject( wizard );
            
            // Try to preselect a folder
            FileObject preselectedFolder = Templates.getTargetFolder( wizard );            
            // Init values
            gui.initValues( project, folders, Templates.getTemplate( wizard ), preselectedFolder );
        }
        if ( bottomPanel != null ) {
            bottomPanel.readSettings( settings );
        }        
        
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        Object substitute = gui.getClientProperty ("NewFileWizard_Title"); // NOI18N
        if (substitute != null) {
            wizard.putProperty ("NewFileWizard_Title", substitute); // NOI18N
        }
    }

    public void storeSettings(Object settings) { 
        if( isValid() ) {
            if ( bottomPanel != null ) {
                bottomPanel.storeSettings( settings );
            }
            FileObject rootFolder = gui.getRootFolder();
            FileObject folder = null;
            if ( !isPackage ) {
                String packageFileName = gui.getPackageFileName();
                folder = rootFolder.getFileObject( packageFileName );            
                if ( folder == null ) {
                    try {
                        folder = FileUtil.createFolder( rootFolder, packageFileName );
                    }
                    catch( IOException e ) {
                        ErrorManager.getDefault().notify( ErrorManager.INFORMATIONAL, e );
                        return;
                    }
                }
            }
            else {
                folder = rootFolder;
            }
            Templates.setTargetFolder( (WizardDescriptor)settings, folder );
            Templates.setTargetName( (WizardDescriptor)settings, gui.getTargetName() );
        }
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }

    public void stateChanged(ChangeEvent e) {
        fireChange();
    }
    
    // Private methods ---------------------------------------------------------
    
    private void setErrorMessage( String key ) {
        if ( key == null ) {
            wizard.putProperty( "WizardPanel_errorMessage", "" ); // NOI18N
        }
        else {
            wizard.putProperty( "WizardPanel_errorMessage", NbBundle.getMessage( JavaTargetChooserPanelGUI.class, key) ); // NOI18N
        }
    }
    
    // Nice copy of useful methods (Taken from JavaModule)
    
    static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, ".");
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token))
                return false;
            if (!Utilities.isJavaIdentifier(token))
                return false;
        }
        return true;
    }
    
    static boolean isValidTypeIdentifier(String ident) {
        if (ident == null || "".equals(ident) || !Utilities.isJavaIdentifier( ident ) ) {
            return false;
        }
        else {
            return true;
        }
    }

}
