/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.cnd.editor.filecreation;

import java.io.File;
import java.io.IOException;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.ErrorManager;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 */
public abstract class CndPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {
    
    private final ChangeSupport changeSupport = new ChangeSupport(this);
    protected CndPanelGUI gui;
    protected final Object guiLock = new Object();

    protected final Project project;
    protected final SourceGroup[] folders;
    protected final WizardDescriptor.Panel<WizardDescriptor> bottomPanel;
    protected WizardDescriptor wizard;
    
    public CndPanel(Project project, SourceGroup[] folders, WizardDescriptor.Panel<WizardDescriptor> bottomPanel) {
        this.folders = folders;
        this.project = project;
        this.bottomPanel = bottomPanel;
        if ( bottomPanel != null ) {
            bottomPanel.addChangeListener( this );
        }
    }

    @Override
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

    @Override
    public void addChangeListener(ChangeListener l) {
        changeSupport.addChangeListener(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        changeSupport.removeChangeListener(l);
    }

    @Override
    public boolean isValid() {
        return gui != null && gui.getTargetName() != null &&
               ( bottomPanel == null || bottomPanel.isValid() ) ;
    }
    
    @Override
    public void readSettings(WizardDescriptor settings) {
                
        wizard = settings;
                
        if ( gui == null ) {
            getComponent();
        }
        
        // Try to preselect a folder            
        FileObject preselectedTarget = Templates.getTargetFolder(wizard);
        // Try to preserve the already entered target name
        String targetName = Templates.getTargetName(wizard);
        // Init values
        gui.initValues(Templates.getTemplate(wizard), preselectedTarget, targetName);
        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        Object substitute = gui.getClientProperty ("NewFileWizard_Title"); // NOI18N
        if (substitute != null) {
            wizard.putProperty ("NewFileWizard_Title", substitute); // NOI18N
        }
        
        wizard.putProperty (WizardDescriptor.PROP_CONTENT_DATA, new String[] { // NOI18N
            NbBundle.getMessage (NewCndFileChooserPanel.class, "LBL_TemplatesPanel_Name"), // NOI18N
            NbBundle.getMessage (NewCndFileChooserPanel.class, "LBL_SimpleTargetChooserPanel_Name")}); // NOI18N
            
        if ( bottomPanel != null ) {
            bottomPanel.readSettings( settings );
        }
    }
    
    @Override
    public void storeSettings(WizardDescriptor settings) {
        if (WizardDescriptor.PREVIOUS_OPTION.equals(settings.getValue())) {
            return;
        }
        if(!settings.getValue().equals(WizardDescriptor.CANCEL_OPTION) && isValid()) {
            if ( bottomPanel != null ) {
                bottomPanel.storeSettings( settings );
            }
            
            String name = gui.getTargetName ();
            if (name.indexOf ('/') > 0) { // NOI18N
                name = name.substring (name.lastIndexOf ('/') + 1);
            }
            
            FileObject fo = getTargetFolderFromGUI();
            try {
                Templates.setTargetFolder(settings, fo);
            } catch (IllegalArgumentException iae) {
                ErrorManager.getDefault().annotate(iae, ErrorManager.EXCEPTION, null, 
                        NbBundle.getMessage(NewCndFileChooserPanel.class, "MSG_Cannot_Create_Folder", 
                        gui.getTargetFolder()), null, null);
                throw iae;
            }
            Templates.setTargetName(settings, name);
            doStoreSettings(settings);
        }
        settings.putProperty("NewFileWizard_Title", null); // NOI18N
    }
    
    protected abstract void doStoreSettings(WizardDescriptor settings);

    @Override
    public void stateChanged(ChangeEvent e) {
        // Firing change event in EDT - IZ 218103
        if (SwingUtilities.isEventDispatchThread()) {
            changeSupport.fireChange();
        } else {
            SwingUtilities.invokeLater(new Runnable() {

                @Override
                public void run() {
                    changeSupport.fireChange();
                }
            });
        }
    }

    private FileObject getTargetFolderFromGUI () {
        FileObject rootFolder = gui.getTargetGroup().getRootFolder();
        String folderName = gui.getTargetFolder();
        String newObject = gui.getTargetName ();
        
        if (newObject.indexOf ('/') > 0) { // NOI18N
            String path = newObject.substring (0, newObject.lastIndexOf ('/')); // NOI18N
            folderName = folderName == null || "".equals (folderName) ? path : folderName + '/' + path; // NOI18N
        }

        FileObject targetFolder;
        if ( folderName == null || folderName.length() == 0) {
            targetFolder = rootFolder;
        }
        else {            
            targetFolder = rootFolder.getFileObject( folderName );
        }

        if ( targetFolder == null ) {
            // XXX add deletion of the file in uninitalize of the wizard
            try {
                targetFolder = FileUtil.createFolder( rootFolder, folderName );
            } catch (IOException ioe) {
                // XXX
                // Can't create the folder
            }
        }
        
        return targetFolder;
    }

    protected void setInfoMessage(String message) {
        wizard.putProperty(WizardDescriptor.PROP_INFO_MESSAGE, message);
    }

    protected void setErrorMessage(String message) {
        wizard.putProperty (WizardDescriptor.PROP_ERROR_MESSAGE, message);
    }

    /** Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */    
    public static String canUseFileName(FileObject targetFolder, String folderName, String newObjectName, boolean allowFileSeparator) {
        String relFileName = folderName + "/" + newObjectName; // NOI18N

        boolean allowSlash = false;
        boolean allowBackslash = false;
        int errorVariant = 0;
        
        if (allowFileSeparator) {
            char separatorChar = File.separatorChar;
            if (targetFolder != null) {
                separatorChar = FileSystemProvider.getFileSeparatorChar(targetFolder);
            }
            if (separatorChar == '\\') {
                errorVariant = 3;
                allowSlash = allowBackslash = true;
            } else {
                errorVariant = 1;
                allowSlash = true;
            }
        }
        
        if ((!allowSlash && newObjectName.indexOf('/') != -1) || (!allowBackslash && newObjectName.indexOf('\\') != -1)) {
            //if errorVariant == 3, the test above should never be true:
            assert errorVariant == 0 || errorVariant == 1 : "Invalid error variant: " + errorVariant;
            
            return NbBundle.getMessage(CndPanel.class, "MSG_not_valid_filename", newObjectName, Integer.valueOf(errorVariant));
        }
        
        if (!isValidName(newObjectName)) {
            return NbBundle.getMessage(CndPanel.class, "MSG_Invalid_File_Name");
        }

        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return NbBundle.getMessage (CndPanel.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }
        
        // target filesystem should be writable
        if (!targetFolder.canWrite ()) {
            return NbBundle.getMessage (CndPanel.class, "MSG_fs_is_readonly"); // NOI18N
        }
        
        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage (CndPanel.class, "MSG_file_already_exist", newObjectName); // NOI18N
        }
        
        // all ok
        return null;
    }

    /* package */ static boolean existFileName(FileObject targetFolder, String relFileName) {
        if (targetFolder.isValid()) {
            FileObject fo = targetFolder.getFileObject (relFileName);
            return fo != null && fo.isValid();
        }        
        return false;
    }
    
    // if user would request support of wider array of symbols we can allow it by improving escaping symbols during Makefiles generation 
    private static final String PROHIBITED = "\"$#\'*{}[]()"; // NOI18N
    
    /* package */ static boolean isValidName(String name) {
	int len = name.length();
        
	if (len == 0) {
	    return false;
	}
	for (int i = 0; i < len; i++) {
	    char c = name.charAt(i);
	    if (Character.isISOControl(c) || PROHIBITED.indexOf(c) > -1) { 
		return false;
	    }
	}
	return true;
    }
}
