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

package org.netbeans.modules.php.project.ui;

import java.awt.Component;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
import java.nio.charset.IllegalCharsetNameException;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.MutableComboBoxModel;
import javax.swing.plaf.UIResource;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Miscellaneous UI utils.
 * @author Tomas Mysik
 */
public final class Utils {

    public static final String URL_REGEXP = "^https?://[^/?# ]+(:\\d+)?/[^?# ]*(\\?[^#]*)?(#\\w*)?$";
    private static final Pattern URL_PATTERN = Pattern.compile(URL_REGEXP);

    private Utils() {
    }

    public static boolean isValidUrl(String url) {
        return URL_PATTERN.matcher(url).matches();
    }

    public static String browseLocationAction(final Component parent, String path, String title) {
        JFileChooser chooser = new JFileChooser();
        FileUtil.preventFileChooserSymlinkTraversal(chooser, null);
        chooser.setDialogTitle(title);
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
            final MutableComboBoxModel localServerComboBoxModel, String newSubfolderName, String title) {
        LocalServer ls = (LocalServer) localServerComboBox.getSelectedItem();
        String newLocation = browseLocationAction(parent, ls.getDocumentRoot(), title);
        if (newLocation == null) {
            return;
        }

        File file = null;
        if (newSubfolderName == null) {
            file = new File(newLocation);
        } else {
            file = new File(newLocation, newSubfolderName);
        }
        String projectLocation = file.getAbsolutePath();
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
        Object[] items = removeComboBoxItems(comboBoxModel);
        Arrays.sort(items);
        putComboBoxItems(comboBoxModel, items);
        if (selected != null) {
            comboBoxModel.setSelectedItem(selected);
        }
    }

    public static Object[] removeComboBoxItems(MutableComboBoxModel comboBoxModel) {
        int size = comboBoxModel.getSize();
        Object[] items = new Object[size];
        for (int i = size - 1; i >= 0; i--) {
            items[i] = comboBoxModel.getElementAt(i);
            comboBoxModel.removeElementAt(i);
        }
        assert comboBoxModel.getSize() == 0;
        return items;
    }

    public static void putComboBoxItems(MutableComboBoxModel comboBoxModel, Object[] items) {
        int size = items.length;
        for (int i = 0; i < size; i++) {
            comboBoxModel.addElement(items[i]);
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
        return fileName != null && fileName.trim().length() > 0
                && fileName.indexOf('/')  == -1 // NOI18N
                && fileName.indexOf('\\') == -1 // NOI18N
                && fileName.indexOf(':') == -1; // NOI18N
    }

    /**
     * Check whether the provided File has a valid file name. File is not
     * {@link FileUtil#normalizeFile(java.io.File) normalized}, caller should do it if needed.
     * @param file File to check.
     * @return <code>true</true> if the provided File has valid file name.
     * @see #isValidFileName(java.lang.String)
     */
    public static boolean isValidFileName(File file) {
        assert file != null;
        // #132520
        if (file.isAbsolute() && file.getParentFile() == null) {
            return true;
        }
        return isValidFileName(file.getName());
    }

    /**
     * Validate the path and get the error message or <code>null</code> if it's all right.
     * @param projectPath the path to validate
     * @param type the type for error messages, currently "Project", "Sources" and "Folder".
     *             Add other to Bundle.properties file if more types are needed.
     * @param allowNonEmpty <code>true</code> if the folder can exist and can be non empty.
     * @param allowInRoot  <code>true</code> if the folder can exist and can be a root directory "/"
     *                     (this parameter is taken into account only for *NIX OS).
     * @return localized error message in case of error, <code>null</code> otherwise.
     */
    public static String validateProjectDirectory(String projectPath, String type, boolean allowNonEmpty,
            boolean allowInRoot) {
        assert projectPath != null;
        assert type != null;

        File project = new File(projectPath);
        // #131753
        if (!project.isAbsolute()) {
            return NbBundle.getMessage(Utils.class, "MSG_" + type + "NotAbsolute");
        }

        // not allow to create project on unix root folder, see #82339
        if (!allowInRoot && Utilities.isUnix()) {
            File cfl = Utils.getCanonicalFile(project);
            if (cfl != null && (cfl.getParentFile() == null || cfl.getParentFile().getParent() == null)) {
                return NbBundle.getMessage(Utils.class, "MSG_" + type + "InRootNotSupported");
            }
        }

        final File destFolder = project.getAbsoluteFile();
        if (Utils.getCanonicalFile(destFolder) == null) {
            return NbBundle.getMessage(Utils.class, "MSG_Illegal" + type + "Location");
        }

        File projLoc = FileUtil.normalizeFile(destFolder);
        while (projLoc != null && !projLoc.exists()) {
            projLoc = projLoc.getParentFile();
        }
        if (projLoc == null || !projLoc.canWrite()) {
            return NbBundle.getMessage(Utils.class, "MSG_" + type + "FolderReadOnly");
        }

        if (FileUtil.toFileObject(projLoc) == null) {
            return NbBundle.getMessage(Utils.class, "MSG_Illegal" + type + "Location");
        }

        if (!allowNonEmpty) {
            File[] kids = destFolder.listFiles();
            if (destFolder.exists() && kids != null && kids.length > 0) {
                // Folder exists and is not empty
                return NbBundle.getMessage(Utils.class, "MSG_" + type + "FolderExists");
            }
        }
        return null;
    }

    /**
     * Validate that the project sources directory and directory for copying files are "independent". It means
     * that the sources isn't underneath the target directory and vice versa. Both paths have to be normalized.
     * @param sources project sources.
     * @param copyTarget directory for copying files.
     * @return <code>true</code> if the directories are "independent".
     */
    public static String validateSourcesAndCopyTarget(String sources, String copyTarget) {
        assert sources != null;
        assert copyTarget != null;
        // handle "/myDir" and "/myDirectory"
        if (!sources.endsWith(File.separator)) {
            sources = sources + File.separator;
        }
        if (!copyTarget.endsWith(File.separator)) {
            copyTarget = copyTarget + File.separator;
        }
        if (sources.startsWith(copyTarget)
                || copyTarget.startsWith(sources)) {
            return NbBundle.getMessage(Utils.class, "MSG_SourcesEqualCopyTarget");
        }
        return null;
    }

    public static class EncodingModel extends DefaultComboBoxModel {
        private static final long serialVersionUID = -3139920099217726436L;

        public EncodingModel(String originalEncoding) {
            Charset defEnc = null;
            for (Charset c : Charset.availableCharsets().values()) {
                if (c.name().equals(originalEncoding)) {
                    defEnc = c;
                }
                addElement(c);
            }
            if (defEnc == null && originalEncoding != null) {
                //Create artificial Charset to keep the original value
                //May happen when the project was set up on the platform
                //which supports more encodings
                try {
                    defEnc = new UnknownCharset(originalEncoding);
                    addElement(defEnc);
                } catch (IllegalCharsetNameException e) {
                    //The source.encoding property is completely broken
                    Logger.getLogger(EncodingModel.class.getName()).info("IllegalCharsetName: " + originalEncoding);
                }
            }
            if (defEnc == null) {
                defEnc = Charset.defaultCharset();
            }
            setSelectedItem(defEnc);
        }
    }

    public static class EncodingRenderer extends JLabel implements ListCellRenderer, UIResource {
        private static final long serialVersionUID = 3196531352192214602L;

        public EncodingRenderer() {
            setOpaque(true);
        }

        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
                boolean cellHasFocus) {
            assert value instanceof Charset;
            setName("ComboBox.listRenderer"); // NOI18N
            setText(((Charset) value).displayName());
            setIcon(null);
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            return this;
        }

        @Override
        public String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name; // NOI18N
        }
    }

    private static class UnknownCharset extends Charset {

        UnknownCharset(String name) {
            super(name, new String[0]);
        }

        public boolean contains(Charset c) {
            throw new UnsupportedOperationException();
        }

        public CharsetDecoder newDecoder() {
            throw new UnsupportedOperationException();
        }

        public CharsetEncoder newEncoder() {
            throw new UnsupportedOperationException();
        }
    }
}
