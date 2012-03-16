/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserve *
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.javafx2.project;

import java.io.File;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.*;
import javax.swing.UIManager;
import javax.swing.event.ChangeListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileSystemView;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Wizard to create a new FXML file and optionally Java Controller and CSS file.
 *
 * @author Anton Chechel <anton.chechel@oracle.com>
 */
// TODO register via annotations instead of layer.xml
public class FXMLTemplateWizardIterator implements WizardDescriptor.InstantiatingIterator<WizardDescriptor> {
    
    static final String PROP_SRC_ROOTS = "srcRootFolder"; // NOI18N
    static final String PROP_ROOT_FOLDER = "rootFolder"; // NOI18N
    static final String PROP_JAVA_CONTROLLER_CREATE = "javaControllerCreate"; // NOI18N
    static final String PROP_JAVA_CONTROLLER_NAME_PROPERTY = "javaController"; // NOI18N
    static final String PROP_CSS_CREATE = "cssCreate"; // NOI18N
    static final String PROP_CSS_NAME_PROPERTY = "CSS"; // NOI18N

    static final String FXML_FILE_EXTENSION = ".fxml"; // NOI18N
    static final String JAVA_FILE_EXTENSION = ".java"; // NOI18N
    static final String CSS_FILE_EXTENSION = ".css"; // NOI18N
    
    private WizardDescriptor wizard;
    private transient int index;
    private transient WizardDescriptor.Panel[] panels;

    public static WizardDescriptor.InstantiatingIterator<WizardDescriptor> create() {
        return new FXMLTemplateWizardIterator();
    }

    private FXMLTemplateWizardIterator() {
    }

    @Override
    public String name() {
        switch (index) {
            default:
            case 0:
                return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "LBL_ConfigureFXMLPanel_Name"); // NOI18N
            case 1:
                return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "LBL_ConfigureFXMLPanel_Controller_Name"); // NOI18N
            case 2:
                return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "LBL_ConfigureFXMLPanel_CSS_Name"); // NOI18N
        }
    }

    @Override
    public void initialize(WizardDescriptor wizard) {
        this.wizard = wizard;

        Project project = Templates.getProject(wizard);
        if (project == null) {
            throw new IllegalStateException(
                    NbBundle.getMessage(FXMLTemplateWizardIterator.class,
                    "MSG_ConfigureFXMLPanel_Project_Null_Error")); // NOI18N
        }

        Sources sources = ProjectUtils.getSources(project);
        SourceGroup[] groups = sources.getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        if (groups == null) {
            throw new IllegalStateException(
                    NbBundle.getMessage(FXMLTemplateWizardIterator.class,
                    "MSG_ConfigureFXMLPanel_SGs_Error")); // NOI18N
        }
        if (groups.length == 0) {
            groups = sources.getSourceGroups(Sources.TYPE_GENERIC);
        }

        index = 0;
        panels = createPanels(project, groups);
    }

    private WizardDescriptor.Panel[] createPanels(Project project, SourceGroup[] groups) {
        return new WizardDescriptor.Panel[]{
                    new ConfigureFXMLPanelVisual.Panel(project, groups),
                    new ConfigureFXMLControllerPanelVisual.Panel(),
                    new ConfigureFXMLCSSPanelVisual.Panel()
                };
    }

    @Override
    public void uninitialize(WizardDescriptor wizard) {
    }

    @Override
    public Set instantiate() throws IOException, IllegalArgumentException {
        Set<FileObject> set = new HashSet<FileObject>(3);
        FileObject dir = Templates.getTargetFolder(wizard);
        DataFolder df = DataFolder.findFolder(dir);

        String targetName = Templates.getTargetName(wizard);
        boolean createController = (Boolean) wizard.getProperty(FXMLTemplateWizardIterator.PROP_JAVA_CONTROLLER_CREATE);
        String controller = (String) wizard.getProperty(FXMLTemplateWizardIterator.PROP_JAVA_CONTROLLER_NAME_PROPERTY);
        boolean createCSS = (Boolean) wizard.getProperty(FXMLTemplateWizardIterator.PROP_CSS_CREATE);
        String css = (String) wizard.getProperty(FXMLTemplateWizardIterator.PROP_CSS_NAME_PROPERTY);
        
        Map<String, String> params = new HashMap<String, String>();
        if (controller != null) {
            params.put("controller", controller); // NOI18N
        }
        if (css != null) {
            //remove file extension from name
            css = css.substring(0, css.length() - CSS_FILE_EXTENSION.length());
            // normalize path
            css = css.replace("\\", "/"); // NOI18N
        
            params.put("css", css); // NOI18N
        }

        FileObject xmlTemplate = FileUtil.getConfigFile("Templates/javafx/FXML.fxml"); // NOI18N
        DataObject dXMLTemplate = DataObject.find(xmlTemplate);
        DataObject dobj = dXMLTemplate.createFromTemplate(df, targetName, params);
        set.add(dobj.getPrimaryFile());

        if (createController && controller != null) {
            FileObject javaTemplate = FileUtil.getConfigFile("Templates/javafx/FXMLController.java"); // NOI18N
            DataObject dJavaTemplate = DataObject.find(javaTemplate);
            DataObject dobj2 = dJavaTemplate.createFromTemplate(df, controller);
            set.add(dobj2.getPrimaryFile());
        }

        if (createCSS && css != null) {
            FileObject cssTemplate = FileUtil.getConfigFile("Templates/javafx/FXML.css"); // NOI18N
            DataObject dCSSTemplate = DataObject.find(cssTemplate);
            DataObject dobj3 = dCSSTemplate.createFromTemplate(df, css);
            set.add(dobj3.getPrimaryFile());
        }

        return set;
    }

    @Override
    public WizardDescriptor.Panel<WizardDescriptor> current() {
        return panels[index];
    }

    @Override
    public boolean hasNext() {
        return index < panels.length - 1;
    }

    @Override
    public boolean hasPrevious() {
        return index > 0;
    }

    @Override
    public void nextPanel() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        index++;
    }

    @Override
    public void previousPanel() {
        if (!hasPrevious()) {
            throw new NoSuchElementException();
        }
        index--;
    }

    @Override
    public void addChangeListener(ChangeListener l) {
    }

    @Override
    public void removeChangeListener(ChangeListener l) {
    }

    // Utility methods ---------------------------------------------------------
    /**
     * Get a package combo model item for the package the user selected before
     * opening the wizard. May return null if it cannot find it; or a String
     * instance if there is a well-defined package but it is not listed among
     * the packages shown in the list model.
     */
    static Object getPreselectedPackage(SourceGroup group, FileObject folder) {
        if (folder == null) {
            return null;
        }

        FileObject root = group.getRootFolder();
        String relPath = FileUtil.getRelativePath(root, folder);
        if (relPath == null) {
            // Group Root folder is not a parent of the preselected folder
            // No package should be selected
            return null;
        } else {
            // Find the right item.            
            String name = relPath.replace('/', '.'); // NOI18N
            return name;
        }
    }

    static void setErrorMessage(String key, WizardDescriptor settings) {
        if (key == null) {
            settings.getNotificationLineSupport().clearMessages();
        } else {
            settings.getNotificationLineSupport().setErrorMessage(NbBundle.getMessage(FXMLTemplateWizardIterator.class, key));
        }
    }

    static void setInfoMessage(String key, WizardDescriptor settings) {
        if (key == null) {
            settings.getNotificationLineSupport().clearMessages();
        } else {
            settings.getNotificationLineSupport().setInformationMessage(NbBundle.getMessage(FXMLTemplateWizardIterator.class, key));
        }
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

    static boolean isValidPackage(FileObject root, final String path) {
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

    static String fileExist(String fileName) {
        if (!new File(fileName).exists()) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_file_doesnt_exist", fileName); // NOI18N
        }
        return null;
    }

    static String canUseFileName(File rootFolder, String fileName) {
        assert rootFolder != null;
        String relFileName = rootFolder.getPath() + File.separatorChar + fileName;

        // test whether the file already exists
        if (new File(relFileName).exists()) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_file_already_exist", relFileName); // NOI18N
        }

        // target folder should be writable
        if (!rootFolder.canWrite()) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_fs_is_readonly"); // NOI18N
        }

        // all ok
        return null;
    }

    // helper methods copied and refactored from JavaTargetChooserPanel
    /**
     * Checks if the given file name can be created in the target folder.
     *
     * @param targetFolder target folder (e.g. source group)
     * @param folderName name of the folder relative to target folder
     * @param newObjectName name of created file
     * @param extension extension of created file
     * @return localized error message or null if all right
     */
    static String canUseFileName(FileObject targetFolder, String folderName, String newObjectName, String extension) {
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
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_fs_or_folder_does_not_exist"); // NOI18N
        }

        // target package should be writable
        File targetPackage = folderName != null ? new File(FileUtil.toFile(targetFolder), folderName) : FileUtil.toFile(targetFolder);
        if (targetPackage != null) {
            if (targetPackage.exists() && !targetPackage.canWrite()) {
                return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_fs_is_readonly"); // NOI18N
            }
        } else if (!targetFolder.canWrite()) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_fs_is_readonly"); // NOI18N
        }

        if (existFileName(targetFolder, relFileName)) {
            return NbBundle.getMessage(FXMLTemplateWizardIterator.class, "MSG_file_already_exist", newObjectNameToDisplay); // NOI18N
        }

        // all ok
        return null;
    }

    private static boolean existFileName(FileObject targetFolder, String relFileName) {
        File fold = FileUtil.toFile(targetFolder);
        return fold.exists()
                ? new File(fold, relFileName).exists()
                : targetFolder.getFileObject(relFileName) != null;
    }

    // Utility classes ---------------------------------------------------------
    static class SrcFileSystemView extends FileSystemView {

        private static final String newFolderStringWin = UIManager.getString("FileChooser.win32.newFolder"); // NOI18N
        private static final String newFolderNextStringWin = UIManager.getString("FileChooser.win32.newFolder.subsequent"); // NOI18N
        private static final String newFolderString = UIManager.getString("FileChooser.other.newFolder"); // NOI18N
        private static final String newFolderNextString = UIManager.getString("FileChooser.other.newFolder.subsequent"); // NOI18N
        
        // Roots, bloody roooooots! :D
        private File[] roots;

        public SrcFileSystemView(File[] roots) {
            assert roots != null && roots.length > 0;
            this.roots = roots;
        }

        @Override
        public File[] getRoots() {
            return roots;
        }

        @Override
        public boolean isRoot(File file) {
            for (File root : roots) {
                if (root.equals(file)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public File getHomeDirectory() {
            return roots[0];
        }

        @Override
        public File createNewFolder(File containingDir) throws IOException {
            assert containingDir != null;
            boolean win = Utilities.isWindows();
            File newFolder = createFileObject(containingDir, win ? newFolderStringWin : newFolderString);
            int i = 2;
            while (newFolder.exists() && (i < 100)) {
                newFolder = createFileObject(containingDir, MessageFormat.format(
                        win ? newFolderNextStringWin : newFolderNextString, new Object[]{new Integer(i)}));
                i++;
            }

            if (newFolder.exists()) {
                throw new IOException(NbBundle.getMessage(
                        FXMLTemplateWizardIterator.class, "LBL_ConfigureFXMLPanel_Name", // NOI18N
                        newFolder.getAbsolutePath()));
            } else {
                newFolder.mkdirs();
            }

            return newFolder;
        }
    }
    
    static class FXMLTemplateFileFilter extends FileFilter {
        
        private enum Type {FXML, JAVA, CSS}
        
        private Type type;

        private FXMLTemplateFileFilter(Type type) {
            this.type = type;
        }
        
        public static FXMLTemplateFileFilter createFXMLFilter() {
            return new FXMLTemplateFileFilter(Type.FXML);
        }

        public static FXMLTemplateFileFilter createJavaFilter() {
            return new FXMLTemplateFileFilter(Type.JAVA);
        }

        public static FXMLTemplateFileFilter createCSSFilter() {
            return new FXMLTemplateFileFilter(Type.CSS);
        }

        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }

            String extension;
            switch (type) {
                default:
                case FXML: extension = FXML_FILE_EXTENSION;
                    break;
                case JAVA: extension = JAVA_FILE_EXTENSION;
                    break;
                case CSS: extension = CSS_FILE_EXTENSION;
                    break;
            }
            return ("." + FileUtil.getExtension(f.getName())).equals(extension); // NOI18N
        }

        @Override
        public String getDescription() {
            String key;
            switch (type) {
                default:
                case FXML: key = "LBL_ConfigureFXMLPanel_FileChooser_FXML_Description"; // NOI18N
                    break;
                case JAVA: key = "LBL_ConfigureFXMLPanel_FileChooser_Java_Description"; // NOI18N
                    break;
                case CSS: key = "LBL_ConfigureFXMLPanel_FileChooser_CSS_Description"; // NOI18N
                    break;
            }
            return NbBundle.getMessage(ConfigureFXMLControllerPanelVisual.class, key);
        }
    }

}
