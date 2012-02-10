/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 */
public class ConfigureFXMLPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    private final List<ChangeListener> listeners = new ArrayList<ChangeListener>();
    private ConfigureFXMLPanelVisual view;
    private WizardDescriptor wizard;

    private Project project;
    private SourceGroup folders[];

    public ConfigureFXMLPanel(Project project, SourceGroup[] folders) {
        this.project = project;
        this.folders = folders;
        initView(project, folders);
    }

    private void initView(Project project, SourceGroup[] folders) {
        view = new ConfigureFXMLPanelVisual(project, folders);
        view.addChangeListener(this);
    }
    
    String name() {
        return view.name();
    }

    @Override
    public Component getComponent() {
        return view;
    }

    @Override
    public HelpCtx getHelp() {
        return new HelpCtx(ConfigureFXMLPanel.class);
    }

    @Override
    public void readSettings(WizardDescriptor settings) {
        this.wizard = settings;
        // Try to preselect a folder
        FileObject preselectedFolder = Templates.getTargetFolder(wizard);
        // Init values
        view.initValues(Templates.getTemplate(wizard), preselectedFolder);

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        Object substitute = view.getClientProperty("NewFileWizard_Title"); // NOI18N
        if (substitute != null) {
            wizard.putProperty("NewFileWizard_Title", substitute); // NOI18N
        }
    }

    @Override
    public void storeSettings(WizardDescriptor settings) {
        Object value = wizard.getValue();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(value) ||
                WizardDescriptor.CANCEL_OPTION.equals(value) ||
                WizardDescriptor.CLOSED_OPTION.equals(value)) {
            return;
        }
        if (isValid()) {
            Templates.setTargetFolder(wizard, getTargetFolderFromView());
            Templates.setTargetName(wizard, view.getFXMLName());
            wizard.putProperty(FXMLTemplateWizardIterator.JAVA_CONTROLLER_CREATE, view.shouldCreateController());
            wizard.putProperty(FXMLTemplateWizardIterator.JAVA_CONTROLLER_NAME_PROPERTY, 
                    view.shouldCreateController() ? view.getNewControllerName() : view.getExistingControllerName());
            wizard.putProperty(FXMLTemplateWizardIterator.CSS_NAME_PROPERTY, view.getCSSName());
        }
        wizard.putProperty("NewFileWizard_Title", null); // NOI18N
    }

    @Override
    public boolean isValid() {
        if (view.getFXMLName() == null) {
            setInfoMessage("WARN_ConfigureFXMLPanel_Provide_FXML_Name"); // NOI18N
            return false;
        } else if (view.isControllerEnabled() && !view.isControllerValid()) {
            setErrorMessage("WARN_ConfigureFXMLPanel_Provide_Java_Name"); // NOI18N
            return false;
        } else if (view.isCSSEnabled() && view.getCSSName() == null) {
            setErrorMessage("WARN_ConfigureFXMLPanel_Provide_CSS_Name"); // NOI18N
            return false;
        } else if (!isValidPackageName(view.getPackageName())) {
            setErrorMessage("WARN_ConfigureFXMLPanel_Provide_Package_Name"); // NOI18N
            return false;
        }
        
        if (!isValidPackage(view.getLocationFolder(), view.getPackageName())) {
            setErrorMessage("WARN_ConfigureFXMLPanel_Package_Invalid"); // NOI18N
            return false;
        }
        
        // check if the files can be created
        FileObject template = Templates.getTemplate(wizard);

        FileObject rootFolder = view.getLocationFolder();
        String errorMessage = canUseFileName(rootFolder, view.getPackageFileName(), view.getFXMLName(), template.getExt());
        wizard.getNotificationLineSupport().setErrorMessage(errorMessage);
        if (errorMessage != null) {
            return false;
        }

        errorMessage = canUseFileName(rootFolder, view.getPackageFileName(), view.getNewControllerName(), template.getExt());
        wizard.getNotificationLineSupport().setErrorMessage(errorMessage);
        if (errorMessage != null) {
            return false;
        }

        errorMessage = canUseFileName(rootFolder, view.getPackageFileName(), view.getCSSName(), template.getExt());
        wizard.getNotificationLineSupport().setErrorMessage(errorMessage);
        if (errorMessage != null) {
            return false;
        }

        return true;
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    @Override
    public void addChangeListener(ChangeListener l) {
        listeners.add(l);
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
        listeners.remove(l);
    }

    private void fireChange() {
        ChangeEvent e = new ChangeEvent(this);
        for (ChangeListener l : listeners) {
            l.stateChanged(e);
        }
    }

    // Private methods ---------------------------------------------------------
    
    private void setErrorMessage(String key) {
        if (key == null) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setErrorMessage(NbBundle.getMessage(ConfigureFXMLPanel.class, key));
        }
    }

    private void setInfoMessage(String key) {
        if (key == null) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setInformationMessage(NbBundle.getMessage(ConfigureFXMLPanel.class, key));
        }
    }

    private FileObject getTargetFolderFromView() {
        FileObject rootFolder = view.getLocationFolder();
        String packageFileName = view.getPackageFileName();
        FileObject folder = rootFolder.getFileObject(packageFileName);
        if (folder == null) {
            try {
                folder = rootFolder;
                StringTokenizer tk = new StringTokenizer(packageFileName, "/"); // NOI18N
                String name = null;
                while (tk.hasMoreTokens()) {
                    name = tk.nextToken();
                    FileObject fo = folder.getFileObject(name, ""); // NOI18N
                    if (fo == null) {
                        break;
                    }
                    folder = fo;
                }
                folder = folder.createFolder(name);
                while (tk.hasMoreTokens()) {
                    name = tk.nextToken();
                    folder = folder.createFolder(name);
                }
            } catch (IOException e) {
                Exceptions.printStackTrace(e);
            }
        }
        return folder;
    }
    
    static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') { // NOI18N
            return false;
        }
        StringTokenizer st = new StringTokenizer(str, "."); // NOI18N
        while (st.hasMoreTokens()) {
            String token = st.nextToken();
            if (token.isEmpty()) {
                return false;
            }
            if (!Utilities.isJavaIdentifier(token)) {
                return false;
            }
        }
        return true;
    }

    private static boolean isValidPackage(FileObject root, final String path) {
        //May be null when nothing selected in the GUI.
        if (root == null || path == null) {
            return false;
        }

        final StringTokenizer st = new StringTokenizer(path, "."); // NOI18N
        while (st.hasMoreTokens()) {
            root = root.getFileObject(st.nextToken());
            if (root == null) {
                return true;
            } else if (root.isData()) {
                return false;
            }
        }
        return true;
    }

    // helper methods copied and refactored from JavaTargetChooserPanel
    /** Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */    
    public static String canUseFileName(FileObject targetFolder, String folderName, String newObjectName, String extension) {
        String newObjectNameToDisplay = newObjectName;
        if (newObjectName != null) {
            newObjectName = newObjectName.replace('.', '/'); // NOI18N
        }
        if (extension != null && extension.length() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(newObjectName);
            sb.append('.'); // NOI18N
            sb.append(extension);
            newObjectName = sb.toString();
        }

        if (extension != null && extension.length() > 0) {
            StringBuilder sb = new StringBuilder();
            sb.append(newObjectNameToDisplay);
            sb.append('.'); // NOI18N
            sb.append(extension);
            newObjectNameToDisplay = sb.toString();
        }

        String relFileName = folderName + '/' + newObjectName; // NOI18N

        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return NbBundle.getMessage(ConfigureFXMLPanel.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }

        // target package should be writable
        File targetPackage = folderName != null ? new File(FileUtil.toFile(targetFolder), folderName) : FileUtil.toFile(targetFolder);
        if (targetPackage != null) {
            if (targetPackage.exists() && !targetPackage.canWrite()) {
                return NbBundle.getMessage(ConfigureFXMLPanel.class, "MSG_fs_is_readonly"); // NOI18N
            }
        } else if (!targetFolder.canWrite()) {
            return NbBundle.getMessage(ConfigureFXMLPanel.class, "MSG_fs_is_readonly"); // NOI18N
        }

        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage(ConfigureFXMLPanel.class, "MSG_file_already_exist", newObjectNameToDisplay); // NOI18N
        }

        // all ok
        return null;
    }

    private static boolean existFileName(FileObject targetFolder, String relFileName) {
        File fold = FileUtil.toFile(targetFolder);
        return fold.exists() ?
                new File(fold, relFileName).exists() :
                targetFolder.getFileObject(relFileName) != null;
    }
}
