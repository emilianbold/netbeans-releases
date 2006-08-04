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

package org.netbeans.modules.mobility.project.ui.wizard;
import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.project.Project;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.TemplateWizard;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author  Petr Hrebejk
 */
final class MIDPTargetChooserPanel implements WizardDescriptor.FinishablePanel, ChangeListener {
    
    public static final String MIDLET_NAME = "MidletName"; // NOI18N
    public static final String MIDLET_CLASSNAME = "MidletClassName"; // NOI18N
    public static final String MIDLET_ICON = "MidletIcon"; // NOI18N
    
    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private MIDPTargetChooserPanelGUI gui;
    private TemplateWizard templateWizard = null;
    
    public Component getComponent() {
        if (gui == null) {
            gui = new MIDPTargetChooserPanelGUI();
            gui.addChangeListener(this);
        }
        return gui;
    }
    
    public HelpCtx getHelp() {
        return new HelpCtx(MIDPTargetChooserPanel.class);
    }
    
    public boolean isValid() {
        String message = null;
        if (gui == null)
            message = "ERR_File_NoGUI"; // NOI18N
        else if (gui.getTargetName() == null  ||  "".equals(gui.getTargetName())) // NOI18N
            message = "ERR_File_NoTargetName"; // NOI18N
        else if (gui.getCreatedFile() != null  &&  new File(gui.getCreatedFile()).exists())
            message = "ERR_File_AlreadyExists"; // NOI18N
        else if (! Utilities.isJavaIdentifier(gui.getClassName()))
            message = "ERR_File_InvalidClassName"; // NOI18N
        else if (! isValidJavaFolderName(gui.getPackageFileName()))
            message = "ERR_File_InvalidPackageName"; // NOI18N
        if (templateWizard != null)
            templateWizard.putProperty("WizardPanel_errorMessage", message != null ? NbBundle.getMessage(MIDPTargetChooserPanel.class, message) : null); // NOI18N
        return message == null;
    }
    
    private static boolean isValidJavaFolderName(final String packageFileName) {
        if (packageFileName == null)
            return false;
        final StringTokenizer st = new StringTokenizer(packageFileName, "/"); // NOI18N
        while (st.hasMoreElements()) {
            final String s = (String) st.nextElement();
            if (! Utilities.isJavaIdentifier(s))
                return false;
        }
        return true;
    }
    
    public void addChangeListener(final ChangeListener l) {
        listeners.add(l);
    }
    
    public void removeChangeListener(final ChangeListener l) {
        listeners.remove(l);
    }
    
    private void fireChange() {
        final ChangeEvent e = new ChangeEvent(this);
        for ( ChangeListener lit : listeners) {
            lit.stateChanged(e);
        }
    }
    
    public void readSettings( final Object settings ) {
        
        templateWizard = (TemplateWizard)settings;
        
        if ( getComponent() != null ) {
            
            final Project project = Templates.getProject( templateWizard );
            
            // Try to preselect a folder
            // XXX The test should be rewritten if external project dirs are supported
            final FileObject preselectedTarget = Templates.getTargetFolder( templateWizard );
            
            // Init values
            gui.initValues( project, Templates.getTemplate( templateWizard ), preselectedTarget );
        }
    }
    
    public void storeSettings(final Object settings) {
        templateWizard = (TemplateWizard) settings;
        if( isValid() ) {
            
            final FileObject rootFolder = gui.getRootFolder();
            final String packageFileName = gui.getPackageFileName();
            FileObject folder = rootFolder.getFileObject(packageFileName);
            if (folder == null) {
                try {
                    folder = FileUtil.createFolder(rootFolder, packageFileName);
                } catch (IOException e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                    return;
                }
            }
            Templates.setTargetFolder(templateWizard, folder);
            Templates.setTargetName(templateWizard, gui.getTargetName());
            
            if (gui.isMIDletTemplate()) {
                templateWizard.putProperty(MIDLET_NAME, gui.getMIDletName());
                templateWizard.putProperty(MIDLET_ICON, gui.getMIDletIcon());
                
                final ClassPath cp = ClassPath.getClassPath(folder,ClassPath.SOURCE);
                String fullTarget = null;
                if (cp != null)
                    fullTarget = cp.getResourceName(folder, '.',false);
                if (fullTarget != null  &&  !"".equals(fullTarget)) // NOI18N
                    fullTarget += "." + gui.getClassName(); // NOI18N
                else
                    fullTarget = gui.getClassName();
                ((WizardDescriptor) settings).putProperty(MIDLET_CLASSNAME, fullTarget);
            }
        }
    }
    
    public void stateChanged(@SuppressWarnings("unused")
	final ChangeEvent e) {
        if (templateWizard != null)
            templateWizard.setValid(isValid());
        fireChange();
    }
    
    public boolean isFinishPanel() {
        return true;
    }
    
}
