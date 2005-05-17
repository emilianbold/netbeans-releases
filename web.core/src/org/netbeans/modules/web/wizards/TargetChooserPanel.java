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

package org.netbeans.modules.web.wizards;

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
import org.netbeans.modules.web.api.webmodule.WebModule;
import org.openide.util.NbBundle;

/**
 *
 * @author  Petr Hrebejk, mkuchtiak
 */
final class TargetChooserPanel implements WizardDescriptor.Panel {

    private final List/*<ChangeListener>*/ listeners = new ArrayList();
    private TargetChooserPanelGUI gui;

    private Project project;
    private SourceGroup[] folders;
    private FileType fileType;
    private TemplateWizard templateWizard;
    private String j2eeVersion;
    
    TargetChooserPanel(Project project, SourceGroup[] folders, FileType fileType) {
        this.folders = folders;
        this.project = project;
        this.fileType=fileType;
        
        if (FileType.TAG.equals(fileType)) {
            j2eeVersion = WebModule.J2EE_14_LEVEL;
            if (folders!=null && folders.length>0) {
                WebModule wm = WebModule.getWebModule(folders[0].getRootFolder());
                if (wm!=null) j2eeVersion=wm.getJ2eePlatformVersion();
            }
        }
    }
    
    TemplateWizard getTemplateWizard() {
        return templateWizard;
    } 

    public Component getComponent() {
        if (gui == null) {
            gui = new TargetChooserPanelGUI(this, project, folders, fileType );
        }
        return gui;
    }

    public HelpCtx getHelp() {
        return null;
        //return new HelpCtx( this.getClass().getName() +"."+fileType.toString()); //NOI18N
    }

    public boolean isValid() {
        // cannot create tag files in j2ee1.3
        if (FileType.TAG.equals(fileType) && WebModule.J2EE_13_LEVEL.equals(j2eeVersion)) {
            templateWizard.putProperty ("WizardPanel_errorMessage", // NOI18N
                NbBundle.getMessage(TargetChooserPanel.class, "MSG_13notSupported"));
            return false;
        }
        
        boolean ok = ( gui != null && gui.getTargetName() != null);
        
        if (!ok) {
            templateWizard.putProperty ("WizardPanel_errorMessage", null); // NOI18N
            return false;
        }
        
        //  check if the TLD info is correct
        if (FileType.TAG.equals(fileType) && gui.isTldCheckBoxSelected()) {
            String mes=null;
            FileObject tldFo = gui.getTldFileObject();
            String tagName = gui.getTagName();
            if (tldFo==null) {
                mes = NbBundle.getMessage(TargetChooserPanel.class,"MSG_noTldSelectedForTagFile");
            } else if (!gui.isValidTagName(tagName)) {
                mes = NbBundle.getMessage(TargetChooserPanel.class,"TXT_wrongTagName",tagName);
            } else if (gui.tagNameExists(tagName)) {
                mes = NbBundle.getMessage(TargetChooserPanel.class,"TXT_tagNameExists",tagName);
            }
            if (mes!=null) {
                templateWizard.putProperty ("WizardPanel_errorMessage", mes); // NOI18N
                return false;
            }
        }
        
        //  check if the TLD info is correct
        if (FileType.TAGLIBRARY.equals(fileType)) {
            // XX precisely we should check for 'tokens composed of characters, 
            // digits, ".", ":", "-", and the characters defined by Unicode, 
            // such as "combining" or "extender"' to be sure that TLD will validate
            String tldName = gui.getTargetName();
            if (tldName.indexOf(' ') >= 0 ||
                    tldName.indexOf(',') >= 0) {
                templateWizard.putProperty ("WizardPanel_errorMessage", NbBundle.getMessage(TargetChooserPanel.class,"TXT_wrongTagLibName",tldName)); // NOI18N
                return false;
            }
        }
        
        // check if the file name can be created
        String targetName=gui.getTargetName();
        java.io.File file = gui.getTargetFile();
        FileObject template = Templates.getTemplate( templateWizard );
        String ext = template.getExt ();
        if (FileType.JSP.equals(fileType) || FileType.TAG.equals(fileType)) {
            if (isSegment()) ext+="f"; //NOI18N
            else if (isXml()) ext+="x"; //NOI18N
        }
        
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
            
            if (FileType.JSP.equals(fileType))
                templateWizard.putProperty ("NewFileWizard_Title", // NOI18N
                    NbBundle.getMessage(TargetChooserPanel.class, "TITLE_JspFile"));
            else if (FileType.TAG.equals(fileType))
                templateWizard.putProperty ("NewFileWizard_Title", // NOI18N
                    NbBundle.getMessage(TargetChooserPanel.class, "TITLE_TagFile"));
            else if (FileType.TAGLIBRARY.equals(fileType))
                templateWizard.putProperty ("NewFileWizard_Title", // NOI18N
                    NbBundle.getMessage(TargetChooserPanel.class, "TITLE_TLD"));
            else if (FileType.HTML.equals(fileType))
                templateWizard.putProperty ("NewFileWizard_Title", // NOI18N
                    NbBundle.getMessage(TargetChooserPanel.class, "TITLE_HTML"));
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
            // XXX Better test for canWrite
            String folderName = gui.getTargetFolder();
            File f = new File( folderName );
            try {
                if ( !f.exists() ) {
                    // XXX add deletion of the file in uninitalize ow the wizard
                    WebModule wm = gui.getWebModule();
                    String relativeFolder = gui.getRelativeTargetFolder();
                    FileObject prjDir = (wm==null?gui.getLocationRoot():wm.getDocumentBase());
                    FileUtil.createFolder( prjDir, relativeFolder );
                }
                FileObject folder = FileUtil.toFileObject( f );            
                Templates.setTargetFolder( (WizardDescriptor)settings, folder );
                Templates.setTargetName( (WizardDescriptor)settings, gui.getTargetName() );
            }
            catch( java.io.IOException e ) {
                org.openide.ErrorManager.getDefault().notify( org.openide.ErrorManager.INFORMATIONAL, e );
            }
        }
        ((WizardDescriptor)settings).putProperty ("NewFileWizard_Title", null); // NOI18N
    }
    
    boolean isXml() {
        return gui.isXml();
    }
    
    boolean isSegment() {
        return gui.isSegment();
    }
    
    String getUri() {
        return gui.getUri();
    }
    
    String getPrefix() {
        return gui.getPrefix();
    }
    
    boolean isTldCheckBoxSelected() {
        return gui.isTldCheckBoxSelected();
    }
    
    String getTagName() {
        return gui.getTagName();
    }
    
    FileObject getTldFileObject() {
        return gui.getTldFileObject();
    }
}
