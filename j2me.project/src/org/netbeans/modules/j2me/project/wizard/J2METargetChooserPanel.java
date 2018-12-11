/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
 */
package org.netbeans.modules.j2me.project.wizard;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Utilities;
import static org.netbeans.modules.j2me.project.wizard.Bundle.*;
import org.netbeans.modules.j2me.project.wizard.NewJ2MEFileWizardIterator.Type;

/**
 */
public final class J2METargetChooserPanel implements WizardDescriptor.Panel<WizardDescriptor>, ChangeListener {

    public static final String MIDLET_NAME = "MidletName"; // NOI18N
    public static final String MIDLET_CLASSNAME = "MidletClassName"; // NOI18N

    private static final String FOLDER_TO_DELETE = "folderToDelete"; //NOI18N

    private final SpecificationVersion JDK_14 = new SpecificationVersion("1.4"); //NOI18N
    private final List<ChangeListener> listeners = new ArrayList<>();
    private J2METargetChooserPanelGUI gui;
    private final WizardDescriptor.Panel<WizardDescriptor> bottomPanel;
    private WizardDescriptor wizard;

    private final Project project;
    private final SourceGroup folders[];
    private final Type type;
    private final boolean isValidPackageRequired;

    @SuppressWarnings("LeakingThisInConstructor")
    public J2METargetChooserPanel(Project project, SourceGroup folders[], WizardDescriptor.Panel<WizardDescriptor> bottomPanel, Type type, boolean isValidPackageRequired) {
        this.project = project;
        this.folders = folders;
        this.bottomPanel = bottomPanel;
        this.type = type;
        if (bottomPanel != null) {
            bottomPanel.addChangeListener(this);
        }
        this.isValidPackageRequired = isValidPackageRequired;
    }

    @Override
    public Component getComponent() {
        if (gui == null) {
            gui = new J2METargetChooserPanelGUI(project, folders, bottomPanel == null ? null : bottomPanel.getComponent(), type);
            gui.addChangeListener(this);
        }
        return gui;
    }

    @Override
    public HelpCtx getHelp() {
        return bottomPanel != null
                ? bottomPanel.getHelp()
                : null;
    }

    @Messages("ERR_JavaTargetChooser_WrongPlatform=Wrong source level of the project. You will not be able to compile this file since it contains Java 5 features.")
    @Override
    public boolean isValid() {
        if (gui == null) {
            setErrorMessage(null);
            return false;
        }
        if (type == Type.PACKAGE) {
            if (gui.getTargetName() == null) {
                setErrorMessage("INFO_JavaTargetChooser_ProvidePackageName"); //NOI18N
                return false;
            }
            if (!isValidPackageName(gui.getTargetName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidPackage"); //NOI18N
                return false;
            }
            if (!isValidPackage(gui.getRootFolder(), gui.getTargetName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidFolder"); //NOI18N
                return false;
            }
        } else if (type == Type.PKG_INFO) {
            //Change in firing order caused that isValid is called before readSettings completed => no targetName available
            if (gui.getTargetName() == null) {
                setErrorMessage("INFO_JavaTargetChooser_ProvideClassName");
                return false;
            }
            assert "package-info".equals(gui.getTargetName());//NOI18N
            if (!isValidPackageName(gui.getPackageName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidPackage");
                return false;
            }
            if (!isValidPackage(gui.getRootFolder(), gui.getPackageName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidFolder"); //NOI18N
                return false;
            }
        } else {
            if (gui.getTargetName() == null) {
                setErrorMessage("INFO_JavaTargetChooser_ProvideClassName"); //NOI18N
                return false;
            } else if (!isValidTypeIdentifier(gui.getTargetName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidClass"); //NOI18N
                return false;
            } else if (!isValidPackageName(gui.getPackageName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidPackage"); //NOI18N
                return false;
            }
            if (!isValidPackage(gui.getRootFolder(), gui.getPackageName())) {
                setErrorMessage("ERR_JavaTargetChooser_InvalidFolder"); //NOI18N
                return false;
            }
        }

        // check if the file name can be created
        FileObject template = Templates.getTemplate(wizard);

        boolean returnValue = true;
        FileObject rootFolder = gui.getRootFolder();
        SpecificationVersion specVersion = null;
        if (type != Type.PACKAGE) {
            String sl = SourceLevelQuery.getSourceLevel(rootFolder);
            specVersion = sl != null ? new SpecificationVersion(sl) : null;
        }
        String errorMessage = canUseFileName(rootFolder, gui.getPackageFileName(), gui.getTargetName(), template.getExt());
        if (gui != null) {
            wizard.getNotificationLineSupport().setErrorMessage(errorMessage);
        }
        if (errorMessage != null) {
            returnValue = false;
        }

        if (type != Type.PACKAGE && returnValue && gui.getPackageName().length() == 0 && specVersion != null && JDK_14.compareTo(specVersion) <= 0) {
            if (isValidPackageRequired) {
                setInfoMessage("ERR_JavaTargetChooser_CantUseDefaultPackage"); //NOI18N
                return false;
            }
            //Only warning, display it only if everything else is OK.
            setErrorMessage("ERR_JavaTargetChooser_DefaultPackage"); //NOI18N
        }
        String categories = (String) template.getAttribute("templateCategory"); // NOI18N
        if (categories != null && Arrays.asList(categories.split(",")).contains(NewJ2MEFileWizardIterator.JDK_5)) {
            //Only warning, display it only if everything else id OK.
            if (specVersion != null && specVersion.compareTo(JDK_14) <= 0) {
                wizard.getNotificationLineSupport().setErrorMessage(ERR_JavaTargetChooser_WrongPlatform());
            }
        }

        // this enables to display error messages from the bottom panel
        // Nevertheless, the previous error messages have bigger priorities
        if (returnValue && bottomPanel != null) {
            if (!bottomPanel.isValid()) {
                return false;
            }
        }

        return returnValue;
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
        Iterator it = listeners.iterator();
        while (it.hasNext()) {
            ((ChangeListener) it.next()).stateChanged(e);
        }
    }

    @Override
    public void readSettings(WizardDescriptor wizard) {
        this.wizard = wizard;
        if (gui != null) {
            // Try to preselect a folder
            FileObject preselectedFolder = Templates.getTargetFolder(wizard);
            // Init values
            gui.initValues(Templates.getTemplate(wizard), preselectedFolder);
        }

        if (bottomPanel != null) {
            bottomPanel.readSettings(wizard);
        }

        // XXX hack, TemplateWizard in final setTemplateImpl() forces new wizard's title
        // this name is used in NewFileWizard to modify the title
        if (gui != null) {
            Object substitute = gui.getClientProperty("NewFileWizard_Title"); // NOI18N
            if (substitute != null) {
                wizard.putProperty("NewFileWizard_Title", substitute); // NOI18N
            }
        }
    }

    @Override
    public void storeSettings(WizardDescriptor wizard) {
        Object value = wizard.getValue();
        if (WizardDescriptor.PREVIOUS_OPTION.equals(value) || WizardDescriptor.CANCEL_OPTION.equals(value)
                || WizardDescriptor.CLOSED_OPTION.equals(value)) {
            return;
        }
        if (isValid()) {
            if (bottomPanel != null) {
                bottomPanel.storeSettings(wizard);
            }
            Templates.setTargetFolder(wizard, getTargetFolderFromGUI(wizard));
            Templates.setTargetName(wizard, gui.getTargetName());
        }
        wizard.putProperty("NewFileWizard_Title", null); // NOI18N

        if (WizardDescriptor.FINISH_OPTION.equals(value)) {
            wizard.putProperty(FOLDER_TO_DELETE, null);
        }

        if (gui.isMidlet()) {
            wizard.putProperty(MIDLET_NAME, gui.getTargetName());
            StringBuilder sbClassname = new StringBuilder();
            if (gui.getPackageName() != null && !gui.getPackageName().isEmpty()) {
                sbClassname.append(gui.getPackageName()).append("."); //NOI18N
            }
            sbClassname.append(gui.getTargetName());
            wizard.putProperty(MIDLET_CLASSNAME, sbClassname.toString());
        }
    }

    @Override
    public void stateChanged(ChangeEvent e) {
        fireChange();
    }

    // Private methods ---------------------------------------------------------
    private void setErrorMessage(String key) {
        if (key == null) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setErrorMessage(NbBundle.getMessage(J2METargetChooserPanelGUI.class, key));
        }
    }

    private void setInfoMessage(String key) {
        if (key == null) {
            wizard.getNotificationLineSupport().clearMessages();
        } else {
            wizard.getNotificationLineSupport().setInformationMessage(NbBundle.getMessage(J2METargetChooserPanelGUI.class, key));
        }
    }

    private FileObject getTargetFolderFromGUI(WizardDescriptor wd) {
        assert gui != null;
        FileObject rootFolder = gui.getRootFolder();
        if (!rootFolder.isValid()) {
            return null;
        }
        FileObject folder = null;
        if (type != Type.PACKAGE) {
            String packageFileName = gui.getPackageFileName();
            folder = rootFolder.getFileObject(packageFileName);
            if (folder == null) {
                try {
                    folder = rootFolder;
                    StringTokenizer tk = new StringTokenizer(packageFileName, "/"); //NOI18N
                    String name = null;
                    while (tk.hasMoreTokens()) {
                        name = tk.nextToken();
                        FileObject fo = folder.getFileObject(name, "");   //NOI18N
                        if (fo == null) {
                            break;
                        }
                        folder = fo;
                    }
                    folder = folder.createFolder(name);
                    FileObject toDelete = (FileObject) wd.getProperty(FOLDER_TO_DELETE);
                    if (toDelete == null) {
                        wd.putProperty(FOLDER_TO_DELETE, folder);
                    } else if (!toDelete.equals(folder)) {
                        toDelete.delete();
                        wd.putProperty(FOLDER_TO_DELETE, folder);
                    }
                    while (tk.hasMoreTokens()) {
                        name = tk.nextToken();
                        folder = folder.createFolder(name);
                    }

                } catch (IOException e) {
                    Exceptions.printStackTrace(e);
                    folder = null;
                }
            }
        } else {
            folder = rootFolder;
        }
        return folder;
    }

    // Nice copy of useful methods (Taken from JavaModule)
    static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, ".");
        while (tukac.hasMoreTokens()) {
            String token = tukac.nextToken();
            if ("".equals(token)) {
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
        if (root == null) {
            return false;
        }
        if (path == null) {
            return false;
        }
        final StringTokenizer tk = new StringTokenizer(path, ".");   //NOI18N
        while (tk.hasMoreTokens()) {
            root = root.getFileObject(tk.nextToken());
            if (root == null) {
                return true;
            } else if (root.isData()) {
                return false;
            }
        }
        return true;
    }

    @NonNull
    static String[] getPackageAndSimpleName(@NonNull final String name) {
        final int lastDot = name.lastIndexOf('.');  //NOI18N
        if (lastDot > 0) {
            return new String[]{
                name.substring(0, lastDot),
                lastDot == name.length() - 1
                ? "" : //NOI18N
                name.substring(lastDot + 1)
            };
        } else {
            return new String[]{
                "", //NOI18N
                name
            };
        }
    }

    private static boolean isValidTypeIdentifier(String ident) {
        return ident != null && !"".equals(ident) && Utilities.isJavaIdentifier(ident);  //NOI18N
    }

    // helper methods copied from project/ui/ProjectUtilities
    /**
     * Checks if the given file name can be created in the target folder.
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

        String relFileName = folderName + "/" + newObjectName; // NOI18N

        // test whether the selected folder on selected filesystem already exists
        if (targetFolder == null) {
            return NbBundle.getMessage(J2METargetChooserPanel.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }

        // target package should be writable
        File targetPackage = folderName != null ? new File(FileUtil.toFile(targetFolder), folderName) : FileUtil.toFile(targetFolder);
        if (targetPackage != null) {
            if (targetPackage.exists() && !targetPackage.canWrite()) {
                return NbBundle.getMessage(J2METargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
            }
        } else if (!targetFolder.canWrite()) {
            return NbBundle.getMessage(J2METargetChooserPanel.class, "MSG_fs_is_readonly"); // NOI18N
        }

        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage(J2METargetChooserPanel.class, "MSG_file_already_exist", newObjectNameToDisplay); // NOI18N
        }

        // all ok
        return null;
    }

    private static boolean existFileName(FileObject targetFolder, String relFileName) {
        File fileForTargetFolder = FileUtil.toFile(targetFolder);
        return fileForTargetFolder.exists()
                ? new File(fileForTargetFolder, relFileName).exists()
                : targetFolder.getFileObject(relFileName) != null;
    }
}
