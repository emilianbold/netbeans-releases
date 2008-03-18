/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2008 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.php.project.ui.wizards;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.MutableComboBoxModel;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Helper class with static methods
 * @author Tomas Mysik
 */
public final class Utils {

    private Utils() {
    }

    public static String browseLocationAction(final Component parent, String path) {
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(NbBundle.getMessage(ConfigureProjectPanel.class, "LBL_SelectProjectLocation"));
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (path != null && path.length() > 0) {
            File f = new File(path);
            if (f.exists()) {
                chooser.setSelectedFile(f);
            }
        }
        if (JFileChooser.APPROVE_OPTION == chooser.showOpenDialog(parent)) {
            return FileUtil.normalizeFile(chooser.getSelectedFile()).getAbsolutePath();
        }
        return null;
    }

    public static void browseLocalServerAction(final Component parent, final JComboBox localServerComboBox,
            final MutableComboBoxModel localServerComboBoxModel, String newSubfolderName) {
        LocalServer ls = (LocalServer) localServerComboBox.getSelectedItem();
        String newLocation = browseLocationAction(parent, ls.getDocumentRoot());
        if (newLocation == null) {
            return;
        }

        String projectLocation = new File(newLocation, newSubfolderName).getAbsolutePath();
        for (int i = 0; i < localServerComboBoxModel.getSize(); i++) {
            LocalServer element = (LocalServer) localServerComboBoxModel.getElementAt(i);
            if (projectLocation.equals(element.getSrcRoot())) {
                localServerComboBox.setSelectedIndex(i);
                return;
            }
        }
        LocalServer localServer = new LocalServer(newLocation, projectLocation);
        localServerComboBoxModel.addElement(localServer);
        localServerComboBox.setSelectedItem(localServer);
        sortComboBoxModel(localServerComboBoxModel);
    }

    public static void locateLocalServerAction() {
        // XXX
        String message = "Not implemented yet."; // NOI18N
        NotifyDescriptor descriptor = new NotifyDescriptor(
                message,
                message,
                NotifyDescriptor.OK_CANCEL_OPTION,
                NotifyDescriptor.INFORMATION_MESSAGE,
                new Object[] {NotifyDescriptor.OK_OPTION},
                NotifyDescriptor.OK_OPTION);
        DialogDisplayer.getDefault().notify(descriptor);
    }

    public static List getAllItems(final JComboBox comboBox) {
        return new AbstractList() {
            public Object get(int i) {
                return comboBox.getItemAt(i);
            }

            public int size() {
                return comboBox.getItemCount();
            }
        };
    }

    /**
     * Sort {@link MutableComboBoxModel} according to the natural ordering of its items
     * and preserves selected item if any.
     * @param comboBoxModel {@link MutableComboBoxModel} to sort.
     */
    public static void sortComboBoxModel(MutableComboBoxModel comboBoxModel) {
        int size = comboBoxModel.getSize();
        if (size < 2) {
            return;
        }
        Object selected = comboBoxModel.getSelectedItem();
        Object[] items = new Object[size];
        for (int i = size - 1; i >= 0; i--) {
            items[i] = comboBoxModel.getElementAt(i);
            comboBoxModel.removeElementAt(i);
        }
        assert comboBoxModel.getSize() == 0;
        Arrays.sort(items);
        for (int i = 0; i < size; i++) {
            comboBoxModel.addElement(items[i]);
        }
        if (selected != null) {
            comboBoxModel.setSelectedItem(selected);
        }
    }

    public static File getCanonicalFile(File file) {
        try {
            return file.getCanonicalFile();
        } catch (IOException e) {
            // ignored
        }
        return null;
    }

    /**
     * Check whether the provided String is valid file name.
     * @param fileName file name.
     * @return <code>true</true> if the provided String is valid file name.
     */
    public static boolean isValidFileName(String fileName) {
        return fileName != null && fileName.length() > 0
                && fileName.indexOf('/')  == -1 // NOI18N
                && fileName.indexOf('\\') == -1 // NOI18N
                && fileName.indexOf(':') == -1; // NOI18N
    }

    /**
     * Validate the path and get the error message or <code>null</code> if it's all right.
     * @param projectPath the path to validate
     * @param type the type for error messages, currently "Project", "Sources" and "Folder".
     *             Add other to Bundle.properties file if more types are needed.
     * @return localized error message in case of error, <code>null</code> otherwise.
     */
    public static String validateProjectDirectory(String projectPath, String type) {
        assert projectPath != null;
        assert type != null;

        // not allow to create project on unix root folder, see #82339
        File cfl = Utils.getCanonicalFile(new File(projectPath));
        if (Utilities.isUnix() && cfl != null && cfl.getParentFile().getParent() == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_" + type + "InRootNotSupported");
        }

        final File destFolder = new File(projectPath).getAbsoluteFile();
        if (Utils.getCanonicalFile(destFolder) == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_Illegal" + type + "Location");
        }

        File projLoc = FileUtil.normalizeFile(destFolder);
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_" + type + "FolderReadOnly");
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_Illegal" + type + "Location");
        }

        File[] kids = destFolder.listFiles();
        if (destFolder.exists() && kids != null && kids.length > 0) {
            // Folder exists and is not empty
            return NbBundle.getMessage(ConfigureProjectPanel.class, "MSG_" + type + "FolderExists");
        }
        return null;
    }
}
