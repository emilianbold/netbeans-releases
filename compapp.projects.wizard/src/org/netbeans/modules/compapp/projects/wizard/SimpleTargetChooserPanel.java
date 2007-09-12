/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

package org.netbeans.modules.compapp.projects.wizard;

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
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
 *
 * @author  Tientien Li
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

    /** Checks if the given file name can be created in the target folder.
         *
         * @param targetFolder target folder (e.g. source group)
         * @param folderName name of the folder relative to target folder
         * @param newObjectName name of created file
         * @param extension extension of created file
         * @return localized error message or null if all right
         */
    private String canUseFileName(FileObject targetFolder, String folderName, String newObjectName, String extension) {
        if (extension != null && extension.length() > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append(newObjectName);
            sb.append('.'); // NOI18N
            sb.append(extension);
            newObjectName = sb.toString();
        }

        String relFileName = folderName == null ? newObjectName : folderName + "/" + newObjectName; // NOI18N

        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return NbBundle.getMessage(SimpleTargetChooserPanel.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }

        // target filesystem should be writable
        if (!targetFolder.canWrite()) {
            return NbBundle.getMessage(SimpleTargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
        }
        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage(SimpleTargetChooserPanel.class, "MSG_file_already_exist", newObjectName); // NOI18N
        }

        // all ok
        return null;
    }

    private static boolean existFileName(FileObject targetFolder, String relFileName) {
        boolean result = false;
        File fileForTargetFolder = FileUtil.toFile(targetFolder);
        if (fileForTargetFolder.exists()) {
            result = new File (fileForTargetFolder, relFileName).exists();
        } else {
            result = targetFolder.getFileObject (relFileName) != null;
        }

        return result;
    }

    public boolean isValid() {
        boolean ok = ( gui != null && gui.getTargetName() != null &&
               ( bottomPanel == null || bottomPanel.isValid() ) );

        if (!ok) {
            return false;
        }

        // check if the file name can be created
        FileObject template = Templates.getTemplate( wizard );

        // String errorMessage = ProjectUtilities.canUseFileName (gui.getTargetGroup().getRootFolder(), gui.getTargetFolder(), gui.getTargetName(), template.getExt ());
        String errorMessage = canUseFileName (gui.getTargetGroup().getRootFolder(), gui.getTargetFolder(), gui.getTargetName(), template.getExt ());
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

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new generic's title
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
            // XXX add deletion of the file in uninitalize ow the generic
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
