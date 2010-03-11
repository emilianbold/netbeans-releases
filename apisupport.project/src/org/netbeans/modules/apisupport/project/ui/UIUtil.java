/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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

package org.netbeans.modules.apisupport.project.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.text.Collator;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.SortedSet;
import java.util.Stack;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.UIResource;
import javax.swing.text.JTextComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.apisupport.project.CreatedModifiedFiles;
import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.spi.NbModuleProvider;
import org.netbeans.modules.apisupport.project.Util;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.suite.SuiteProject;
import org.netbeans.modules.apisupport.project.ui.customizer.SuiteUtils;
import org.netbeans.modules.apisupport.project.ui.wizard.NewNbModuleWizardIterator;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.support.ProjectChooser;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.WizardDescriptor;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;

/**
 * UI related utility methods for the module.
 *
 * @author Martin Krauskopf
 */
public final class UIUtil {
    
    private static final String ICON_KEY_UIMANAGER = "Tree.closedIcon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER = "Tree.openIcon"; // NOI18N
    private static final String ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.icon"; // NOI18N
    private static final String OPENED_ICON_KEY_UIMANAGER_NB = "Nb.Explorer.Folder.openedIcon"; // NOI18N
    private static final String ICON_PATH = "org/netbeans/modules/apisupport/project/resources/defaultFolder.gif"; // NOI18N
    private static final String OPENED_ICON_PATH = "org/netbeans/modules/apisupport/project/resources/defaultFolderOpen.gif"; // NOI18N
    
    private UIUtil() {}
    
    public static String keyToLogicalString(KeyStroke keyStroke) {
        String keyDesc = Utilities.keyToString(keyStroke);
        int dash = keyDesc.indexOf('-');
        return dash == -1 ? keyDesc :
            keyDesc.substring(0, dash).replace('C', 'D').replace('A', 'O') + keyDesc.substring(dash);
    }
    
    public static String keyStrokeToString(KeyStroke keyStroke) {
        int modifiers = keyStroke.getModifiers();
        StringBuffer sb = new StringBuffer();
        if ((modifiers & InputEvent.CTRL_DOWN_MASK) > 0) {
            sb.append("Ctrl+"); // NOI18N
        }
        if ((modifiers & InputEvent.ALT_DOWN_MASK) > 0) {
            sb.append("Alt+"); // NOI18N
        }
        if ((modifiers & InputEvent.SHIFT_DOWN_MASK) > 0) {
            sb.append("Shift+"); // NOI18N
        }
        if ((modifiers & InputEvent.META_DOWN_MASK) > 0) {
            sb.append("Meta+"); // NOI18N
        }
        if (keyStroke.getKeyCode() != KeyEvent.VK_SHIFT &&
                keyStroke.getKeyCode() != KeyEvent.VK_CONTROL &&
                keyStroke.getKeyCode() != KeyEvent.VK_META &&
                keyStroke.getKeyCode() != KeyEvent.VK_ALT &&
                keyStroke.getKeyCode() != KeyEvent.VK_ALT_GRAPH) {
            sb.append(Utilities.keyToString(
                    KeyStroke.getKeyStroke(keyStroke.getKeyCode(), 0)));
        }
        return sb.toString();
    }
    
    public static KeyStroke stringToKeyStroke(String keyStroke) {
        int modifiers = 0;
        if (keyStroke.startsWith("Ctrl+")) { // NOI18N
            modifiers |= InputEvent.CTRL_DOWN_MASK;
            keyStroke = keyStroke.substring(5);
        }
        if (keyStroke.startsWith("Alt+")) { // NOI18N
            modifiers |= InputEvent.ALT_DOWN_MASK;
            keyStroke = keyStroke.substring(4);
        }
        if (keyStroke.startsWith("Shift+")) { // NOI18N
            modifiers |= InputEvent.SHIFT_DOWN_MASK;
            keyStroke = keyStroke.substring(6);
        }
        if (keyStroke.startsWith("Meta+")) { // NOI18N
            modifiers |= InputEvent.META_DOWN_MASK;
            keyStroke = keyStroke.substring(5);
        }
        KeyStroke ks = Utilities.stringToKey(keyStroke);
        if (ks == null) {
            return null;
        }
        KeyStroke result = KeyStroke.getKeyStroke(ks.getKeyCode(), modifiers);
        return result;
    }
    
    /**
     * Returns multi keystroke for given text representation of shortcuts
     * (like Alt+A B). Returns null if text is not parsable, and empty array
     * for empty string.
     */
    public static KeyStroke[] stringToKeyStrokes(String keyStrokes) {
        String delim = " "; // NOI18N
        if (keyStrokes.length() == 0) {
            return new KeyStroke [0];
        }
        StringTokenizer st = new StringTokenizer(keyStrokes, delim);
        List<KeyStroke> result = new ArrayList<KeyStroke>();
        while (st.hasMoreTokens()) {
            String ks = st.nextToken().trim();
            KeyStroke keyStroke = stringToKeyStroke(ks);
            if (keyStroke == null) { // text is not parsable
                return null;
            }
            result.add(keyStroke);
        }
        return result.toArray(new KeyStroke[result.size()]);
    }
    
    public static String keyStrokesToString(final KeyStroke[] keyStrokes) {
        StringBuffer sb = new StringBuffer(UIUtil.keyStrokeToString(keyStrokes [0]));
        int i, k = keyStrokes.length;
        for (i = 1; i < k; i++) {
            sb.append(' ').append(UIUtil.keyStrokeToString(keyStrokes [i]));
        }
        String newShortcut = sb.toString();
        return newShortcut;
    }
    
    public static String keyStrokesToLogicalString(final KeyStroke[] keyStrokes) {
        StringBuffer sb = new StringBuffer(UIUtil.keyToLogicalString(keyStrokes [0]));
        int i, k = keyStrokes.length;
        for (i = 1; i < k; i++) {
            sb.append(' ').append(UIUtil.keyToLogicalString((keyStrokes [i])));
        }
        String newShortcut = sb.toString();
        return newShortcut;
    }
    
    /**
     * Calls in turn {@link ProjectChooser#setProjectsFolder} if the
     * <code>folder</code> is not <code>null</code> and is a directory.
     */
    public static void setProjectChooserDir(File folder) {
        if (folder == null || !folder.isDirectory()) {
            return;
        }
        ProjectChooser.setProjectsFolder(folder);
    }
    
    /**
     * Calls {@link #setProjectChooserDir} with the <code>fileOrFolder</code>'s
     * parent if it isn't <code>null</code>. Otherwise fallbacks to
     * <code>fileOrFolder</code> itself if it is a directory.
     */
    public static void setProjectChooserDirParent(File fileOrFolder) {
        if (fileOrFolder == null) {
            return;
        }
        File parent = fileOrFolder.getParentFile();
        setProjectChooserDir(parent != null ? parent :
            (fileOrFolder.isDirectory() ? fileOrFolder : null));
    }
    
    /**
     * Set the <code>text</code> for the <code>textComp</code> and set its
     * caret position to the end of the text.
     */
    public static void setText(JTextComponent textComp, String text) {
        textComp.setText(text);
        textComp.setCaretPosition(text == null ? 0 : text.length());
    }
    
    /**
     * Convenient class for listening on document changes. Use it if you do not
     * care what exact change really happened. {@link #removeUpdate} and {@link
     * #changedUpdate} just delegate to {@link #insertUpdate}. So everything
     * what is needed in order to be notified about document changes is to
     * override {@link #insertUpdate} method.
     */
    public abstract static class DocumentAdapter implements DocumentListener {
        public void removeUpdate(DocumentEvent e) { insertUpdate(null); }
        public void changedUpdate(DocumentEvent e) { insertUpdate(null); }
    }
    
    private static Reference<JFileChooser> iconChooser;
    
    /**
     * @param icon file representing icon
     * @param expectedWidth expected width
     * @param expectedHeight expected height
     * @return warning or empty <code>String</code>
     */
    public static String getIconDimensionWarning(File icon, int expectedWidth, int expectedHeight) {
        Dimension real = new Dimension(UIUtil.getIconDimension(icon));
        if (real.height == expectedHeight && real.width == expectedWidth) {
            return "";
        }
        return NbBundle.getMessage(UIUtil.class, "MSG_WrongIconSize",new Object[]  {
            real.width,
            real.height,
            expectedWidth,
            expectedHeight
        });
    }

    /**
     * @param expectedWidth expected width
     * @param expectedHeight expected height
     * @return warning 
     */
    public static String getNoIconSelectedWarning(int expectedWidth, int expectedHeight) {
        return NbBundle.getMessage(UIUtil.class, "MSG_NoIconSelected", expectedWidth, expectedHeight);
    }
    
    /**
     * @param icon file representing icon
     * @param expectedWidth expected width
     * @param expectedHeight expected height
     * @return true if icon corresponds to expected dimension
     */
    public static boolean isValidIcon(final File icon, int expectedWidth, int expectedHeight) {
        Dimension iconDimension = UIUtil.getIconDimension(icon);
        return (expectedWidth == iconDimension.getWidth() &&
                expectedHeight == iconDimension.getHeight());
    }
    
    /**
     * @param icon file representing icon
     * @return width and height of icon encapsulated into {@link java.awt.Dimension}
     */
    public static Dimension getIconDimension(final File icon) {
        try {
            ImageIcon imc = new ImageIcon(icon.toURI().toURL());
            return new Dimension(imc.getIconWidth(), imc.getIconHeight());
        } catch (MalformedURLException ex) {
            ErrorManager.getDefault().notify(ex);
        }
        return new Dimension(-1, -1);
    }
    
    /**
     * Returns an instance of {@link javax.swing.JFileChooser} permitting
     * selection only a regular <em>icon</em>.
     */
    public static JFileChooser getIconFileChooser() {        
        if (iconChooser != null) {
            JFileChooser choose = iconChooser.get();
            if (choose != null) {
                return choose;
            }
        }
        final JFileChooser chooser = new IconFileChooser();        
        iconChooser = new WeakReference<JFileChooser>(chooser);
        return chooser;
    }

        
    /**
     * tries to set the selected file according to currently existing data.
     * Will se it only if the String represents a file path that exists.
     */
    public static JFileChooser getIconFileChooser(String oldValue) {
        JFileChooser chooser = getIconFileChooser();
        String iconText = oldValue.trim();
        if ( iconText.length() > 0) {
            File fil = new File(iconText);
            if (fil.exists()) {
                chooser.setSelectedFile(fil);
            }
        }
        return chooser;
    }
    
    /**
     * Create combobox containing packages from the given {@link SourceGroup}.
     *
     * When null srcRoot is passed, combo box is disabled and shows a warning message (#143392).
     */
    public static JComboBox createPackageComboBox(SourceGroup srcRoot) {
        JComboBox packagesComboBox;
        if (srcRoot != null) {
            packagesComboBox = new JComboBox(PackageView.createListView(srcRoot));
            packagesComboBox.setRenderer(PackageView.listRenderer());
        } else {
            packagesComboBox = new JComboBox();
            packagesComboBox.addItem(NbBundle.getMessage(UIUtil.class, "MSG_Missing_Source_Root"));
            packagesComboBox.setEnabled(false);
        }
        return packagesComboBox;
    }
    
    /**
     * Returns true for valid package name.
     */
    public static boolean isValidPackageName(String str) {
        if (str.length() > 0 && str.charAt(0) == '.') {
            return false;
        }
        StringTokenizer tukac = new StringTokenizer(str, "."); // NOI18N
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
    
    /**
     * Returns a string suitable for text areas respresenting content of {@link
     * CreatedModifiedFiles} <em>paths</em>.
     *
     * @param relPaths should be either
     *        {@link CreatedModifiedFiles#getCreatedPaths()} or
     *        {@link CreatedModifiedFiles#getModifiedPaths()}.
     */
    public static String generateTextAreaContent(String[] relPaths) {
        StringBuffer sb = new StringBuffer();
        if (relPaths.length > 0) {
            for (int i = 0; i < relPaths.length; i++) {
                if (i > 0) {
                    sb.append('\n');
                }
                sb.append(relPaths[i]);
            }
        }
        return sb.toString();
    }
    
    /**
     * Calls in turn {@link #createLayerPresenterComboModel(Project, String,
     * Map)} with {@link Collections#EMPTY_MAP} as a third parameter.
     */
    public static ComboBoxModel createLayerPresenterComboModel(
            final Project project, final String sfsRoot) {
        return createLayerPresenterComboModel(project, sfsRoot, Collections.<String,Object>emptyMap());
    }
    
    /**
     * Returns {@link ComboBoxModel} containing {@link #LayerItemPresenter}s
     * wrapping all folders under the given <code>sfsRoot</code>.
     *
     * @param excludeAttrs {@link Map} of pairs String - Object used to filter
     *                     out folders which have one or more attribute(key)
     *                     with a corresponding value.
     */
    public static ComboBoxModel createLayerPresenterComboModel(
            final Project project, final String sfsRoot, final Map<String,Object> excludeAttrs) {
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        try {
            FileSystem sfs = LayerUtils.getEffectiveSystemFilesystem(project);
            FileObject root = sfs.getRoot().getFileObject(sfsRoot);
            if (root != null) {
                SortedSet<LayerItemPresenter> presenters = new TreeSet<LayerItemPresenter>();
                for (FileObject subFolder : getFolders(root, excludeAttrs)) {
                    presenters.add(new LayerItemPresenter(subFolder, root));
                }
                for (LayerItemPresenter presenter : presenters) {
                    model.addElement(presenter);
                }
            }
        } catch (IOException exc) {
            Util.err.notify(exc);
        }
        return model;
    }
    
    public static class LayerItemPresenter implements Comparable<LayerItemPresenter> {
        
        private String displayName;
        private final FileObject item;
        private final FileObject root;
        private final boolean contentType;
        private static Logger LOGGER = Logger.getLogger(LayerItemPresenter.class.getName());
        
        public LayerItemPresenter(final FileObject item,
                final FileObject root,
                final boolean contentType) {
            this.item = item;
            this.root = root;
            this.contentType = contentType;
        }
        
        public LayerItemPresenter(final FileObject item, final FileObject root) {
            this(item, root, false);
        }
        
        public FileObject getFileObject() {
            return item;
        }
        
        public String getFullPath() {
            return item.getPath();
        }
        
        public String getDisplayName() {
            if (displayName == null) {
                displayName = computeDisplayName();
                LOGGER.log(Level.FINE, "Computed display name '" + displayName + "'");
            }
            return displayName;
        }
        
        public @Override String toString() {
            return getDisplayName();
        }
        
        public int compareTo(LayerItemPresenter o) {
            int res = Collator.getInstance().compare(getDisplayName(), o.getDisplayName());
            if (res != 0) {
                return res;
            } else {
                return getFullPath().compareTo(o.getFullPath());
            }
        }
        
        private static String getFileObjectName(FileObject fo) {
            String name = null;
            try {
                name = fo.getFileSystem().getStatus().annotateName(
                        fo.getNameExt(), Collections.singleton(fo));
                LOGGER.log(Level.FINER, "getFileObjectName for '" + fo.getPath() + "': " + name);
            } catch (FileStateInvalidException ex) {
                name = fo.getName();
            }
            return name;
        }
        
        private String computeDisplayName() {
            FileObject displayItem = contentType ? item.getParent() : item;
            String displaySeparator = contentType ? "/" : " | "; // NOI18N
            Stack<String> s = new Stack<String>();
            s.push(getFileObjectName(displayItem));
            FileObject parent = displayItem.getParent();
            while (!root.getPath().equals(parent.getPath())) {
                s.push(getFileObjectName(parent));
                parent = parent.getParent();
            }
            StringBuffer sb = new StringBuffer();
            sb.append(s.pop());
            while (!s.empty()) {
                sb.append(displaySeparator).append(s.pop());
            }
            return sb.toString();
        }
        
    }
    
    /**
     * Returns path relative to the root of the SFS. May return
     * <code>null</code> for empty String or user's custom non-string items.
     * Also see {@link Util#isValidSFSPath(String)}.
     */
    public static String getSFSPath(final JComboBox lpCombo, final String supposedRoot) {
        Object editorItem = lpCombo.getEditor().getItem();
        String path = null;
        if (editorItem instanceof LayerItemPresenter) {
            path = ((LayerItemPresenter) editorItem).getFullPath();
        } else if (editorItem instanceof String) {
            String editorItemS = ((String) editorItem).trim();
            if (editorItemS.length() > 0) {
                path = searchLIPCategoryCombo(lpCombo, editorItemS);
                if (path == null) {
                    // entered by user - absolute and relative are supported...
                    path = editorItemS.startsWith(supposedRoot) ? editorItemS :
                        supposedRoot + '/' + editorItemS;
                }
            }
        }
        return path;
    }
    
    public static NbModuleProject chooseSuiteComponent(Component parent, SuiteProject suite) {
        NbModuleProject suiteComponent = null;
        Project project = chooseProject(parent);
        if (project != null) {
            NbModuleProvider nmtp = project.getLookup().lookup(NbModuleProvider.class);
            if (nmtp == null || !(project instanceof NbModuleProject)) { // not netbeans module
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(UIUtil.class, "MSG_TryingToAddNonNBModule",
                        ProjectUtils.getInformation(project).getDisplayName())));
            } else if (SuiteUtils.getSubProjects(suite).contains((NbModuleProject) project)) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(UIUtil.class, "MSG_SuiteAlreadyContainsProject",
                        ProjectUtils.getInformation(suite).getDisplayName(),
                        ProjectUtils.getInformation(project).getDisplayName())));
            } else if (nmtp.getModuleType() == NbModuleProvider.SUITE_COMPONENT) {
                Object[] params = new Object[] {
                    ProjectUtils.getInformation(project).getDisplayName(),
                    getSuiteProjectName(project),
                    getSuiteProjectDirectory(project),
                    ProjectUtils.getInformation(suite).getDisplayName(),
                };
                NotifyDescriptor.Confirmation confirmation = new NotifyDescriptor.Confirmation(
                        NbBundle.getMessage(UIUtil.class, "MSG_MoveFromSuiteToSuite", params),
                        NotifyDescriptor.OK_CANCEL_OPTION);
                DialogDisplayer.getDefault().notify(confirmation);
                if (confirmation.getValue() == NotifyDescriptor.OK_OPTION) {
                    suiteComponent = (NbModuleProject) project;
                }
            } else if (nmtp.getModuleType() == NbModuleProvider.STANDALONE) {
                suiteComponent = (NbModuleProject) project;
            } else if (nmtp.getModuleType() == NbModuleProvider.NETBEANS_ORG) {
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        NbBundle.getMessage(UIUtil.class, "MSG_TryingToAddNBORGModule",
                        ProjectUtils.getInformation(project).getDisplayName())));
            }
        }
        return suiteComponent;
    }
    
    /**
     * Appropriately renders {@link Project}s. For others instances delegates
     * to {@link DefaultListCellRenderer}.
     */
    public static ListCellRenderer createProjectRenderer() {
        return new ProjectRenderer();
    }
    
    private static class ProjectRenderer extends JLabel implements ListCellRenderer, UIResource {
        
        public ProjectRenderer () {
            setOpaque(true);
        }
        
        public Component getListCellRendererComponent(
                JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            // #93658: GTK needs name to render cell renderer "natively"
            setName("ComboBox.listRenderer"); // NOI18N

            String text = null;
            if (!(value instanceof Project)) {
                text = value.toString();
            } else {
                ProjectInformation pi = ProjectUtils.getInformation((Project) value);
                text = pi.getDisplayName();
                setIcon(pi.getIcon());
            }
            setText(text);
            
            if ( isSelected ) {
                setBackground(list.getSelectionBackground());
                setForeground(list.getSelectionForeground());             
            }
            else {
                setBackground(list.getBackground());
                setForeground(list.getForeground());
            }
            
            return this;
        }
        
        // #93658: GTK needs name to render cell renderer "natively"
        public @Override String getName() {
            String name = super.getName();
            return name == null ? "ComboBox.renderer" : name;  // NOI18N
        }
        
    }
    
    /**
     * Returns default folder icon as {@link java.awt.Image}. Never returns
     * <code>null</code>.
     *
     * @param opened wheter closed or opened icon should be returned.
     */
    public static Image getTreeFolderIcon(boolean opened) {
        Image base = (Image) UIManager.get(opened ? OPENED_ICON_KEY_UIMANAGER_NB : ICON_KEY_UIMANAGER_NB); // #70263;
        if (base == null) {
            Icon baseIcon = UIManager.getIcon(opened ? OPENED_ICON_KEY_UIMANAGER : ICON_KEY_UIMANAGER); // #70263
            if (baseIcon != null) {
                base = ImageUtilities.icon2Image(baseIcon);
            } else { // fallback to our owns
                base = ImageUtilities.loadImage(opened ? OPENED_ICON_PATH : ICON_PATH, true);
            }
        }
        assert base != null;
        return base;
    }
    
    public static NbModuleProject runLibraryWrapperWizard(final Project suiteProvider) {
        NewNbModuleWizardIterator iterator = NewNbModuleWizardIterator.createLibraryModuleIterator(suiteProvider);
        return UIUtil.runProjectWizard(iterator, "CTL_NewLibraryWrapperProject"); // NOI18N
    }
    
    public static NbModuleProject runProjectWizard(
            final NewNbModuleWizardIterator iterator, final String titleBundleKey) {
        WizardDescriptor wd = new WizardDescriptor(iterator);
        wd.setTitleFormat(new MessageFormat("{0}")); // NOI18N
        wd.setTitle(NbBundle.getMessage(UIUtil.class, titleBundleKey));
        Dialog dialog = DialogDisplayer.getDefault().createDialog(wd);
        dialog.setVisible(true);
        dialog.toFront();
        NbModuleProject project = null;
        boolean cancelled = wd.getValue() != WizardDescriptor.FINISH_OPTION;
        if (!cancelled) {
            FileObject folder = iterator.getCreateProjectFolder();
            try {
                project = (NbModuleProject) ProjectManager.getDefault().findProject(folder);
                OpenProjects.getDefault().open(new Project[] { project }, false);
                if (wd.getProperty("setAsMain") == Boolean.TRUE) { // NOI18N
                    OpenProjects.getDefault().setMainProject(project);
                }
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
            }
        }
        return project;
    }
    
    /**
     * Searches LayerItemPresenter combobox by the item's display name.
     */
    private static String searchLIPCategoryCombo(final JComboBox lpCombo, final String displayName) {
        String path = null;
        for (int i = 0; i < lpCombo.getItemCount(); i++) {
            Object item = lpCombo.getItemAt(i);
            if (!(item instanceof LayerItemPresenter)) {
                continue;
            }
            LayerItemPresenter presenter = (LayerItemPresenter) lpCombo.getItemAt(i);
            if (displayName.equals(presenter.getDisplayName())) {
                path = presenter.getFullPath();
                break;
            }
        }
        return path;
    }
    
    public static Project chooseProject(Component parent) {
        JFileChooser chooser = ProjectChooser.projectChooser();
        int option = chooser.showOpenDialog(parent);
        Project project = null;
        if (option == JFileChooser.APPROVE_OPTION) {
            File projectDir = chooser.getSelectedFile();
            UIUtil.setProjectChooserDirParent(projectDir);
            try {
                project = ProjectManager.getDefault().findProject(
                        FileUtil.toFileObject(projectDir));
            } catch (IOException e) {
                ErrorManager.getDefault().notify(ErrorManager.WARNING, e);
            }
        }
        return project;
    }
    
    private static File getSuiteDirectory(Project suiteComp) {
        File suiteDir = SuiteUtils.getSuiteDirectory(suiteComp);
        assert suiteDir != null : "Invalid suite provider for: "
                + suiteComp.getProjectDirectory();
        return suiteDir;
    }
    
    private static String getSuiteProjectDirectory(Project suiteComp) {
        return getSuiteDirectory(suiteComp).getAbsolutePath();
    }
    
    private static String getSuiteProjectName(Project suiteComp) {
        FileObject suiteDir = FileUtil.toFileObject(getSuiteDirectory(suiteComp));
        if (suiteDir == null) {
            // #94915
            return "???"; // NOI18N
        }
        return Util.getDisplayName(suiteDir);
    }
    
    private static Collection<FileObject> getFolders(final FileObject root, final Map<String,Object> excludeAttrs) {
        Collection<FileObject> folders = new HashSet<FileObject>();
        SUBFOLDERS: for (FileObject subFolder : NbCollections.iterable(root.getFolders(false))) {
            for (Map.Entry<String,Object> entry : excludeAttrs.entrySet()) {
                if (entry.getValue().equals(subFolder.getAttribute(entry.getKey()))) {
                    continue SUBFOLDERS;
                }
            }
            folders.add(subFolder);
            folders.addAll(getFolders(subFolder, excludeAttrs));
        }
        return folders;
    }
    
    private static final class IconFilter extends FileFilter {
        public boolean accept(File pathname) {
            return pathname.isDirectory() ||
                    pathname.getName().toLowerCase(Locale.ENGLISH).endsWith("gif") || // NOI18N
                    pathname.getName().toLowerCase(Locale.ENGLISH).endsWith("png"); // NOI18N
        }
        public String getDescription() {
            return "*.gif, *.png"; // NOI18N
        }
    }
    
    /**
     * Show an OK/cancel-type dialog with customized button texts.
     * Only a separate method because it is otherwise cumbersome to replace
     * the OK button with a button that is set as the default.
     * @param title the dialog title
     * @param message the body of the message (usually HTML text)
     * @param acceptButton a label for the default accept button; should not use mnemonics
     * @param cancelButton a label for the cancel button (or null for default); should not use mnemonics
     * @param messageType {@link NotifyDescriptor#WARNING_MESSAGE} or similar
     * @return true if user accepted the dialog
     */
    public static boolean showAcceptCancelDialog(String title, String message, String acceptButton, String cancelButton, int messageType) {
        return showAcceptCancelDialog(title, message, acceptButton, null , 
                cancelButton, messageType) ;
    }
    
    /**
     * Show an OK/cancel-type dialog with customized button texts.
     * Only a separate method because it is otherwise cumbersome to replace
     * the OK button with a button that is set as the default.
     * @param title the dialog title
     * @param message the body of the message (usually HTML text)
     * @param acceptButton a label for the default accept button; should not use mnemonics
     * @param accDescrAcceptButton a accessible description for acceptButton 
     * @param cancelButton a label for the cancel button (or null for default); should not use mnemonics
     * @param messageType {@link NotifyDescriptor#WARNING_MESSAGE} or similar
     * @return true if user accepted the dialog
     */
    public static boolean showAcceptCancelDialog(String title, String message, 
            String acceptButton, String accDescrAcceptButton , 
            String cancelButton, int messageType) 
    {
        DialogDescriptor d = new DialogDescriptor(message, title);
        d.setModal(true);
        JButton accept = new JButton(acceptButton);
        accept.setDefaultCapable(true);
        if ( accDescrAcceptButton != null ){
            accept.getAccessibleContext().
            setAccessibleDescription( accDescrAcceptButton);
        }
        d.setOptions(new Object[] {
            accept,
            cancelButton != null ? new JButton(cancelButton) : NotifyDescriptor.CANCEL_OPTION,
        });
        d.setMessageType(messageType);
        return DialogDisplayer.getDefault().notify(d).equals(accept);
    }
    
    private static class IconFileChooser extends JFileChooser {
        private final JTextField iconInfo = new JTextField();        
        private  IconFileChooser() {
            JPanel accessoryPanel = getAccesoryPanel(iconInfo);
            setDialogTitle(NbBundle.getMessage(UIUtil.class, "TITLE_IconDialog"));//NOI18N
            setAccessory(accessoryPanel);
            setAcceptAllFileFilterUsed(false);
            setFileSelectionMode(JFileChooser.FILES_ONLY);
            setMultiSelectionEnabled(false);
            addChoosableFileFilter(new IconFilter());
            setFileView(new FileView() {
                public @Override Icon getIcon(File f) {
                    // Show icons right in the chooser, to make it easier to find
                    // the right one.
                    if (f.getName().endsWith(".gif") || f.getName().endsWith(".png")) { // NOI18N
                        Icon icon = new ImageIcon(f.getAbsolutePath());
                        if (icon.getIconWidth() == 16 && icon.getIconHeight() == 16) {
                            return icon;
                        }
                    }
                    return null;
                }
                public @Override String getName(File f) {
                    File f2 = getSelectedFile();
                    if (f2 != null && (f2.getName().endsWith(".gif") || f2.getName().endsWith(".png"))) { // NOI18N
                        Icon icon = new ImageIcon(f2.getAbsolutePath());
                        StringBuffer sb = new StringBuffer();
                        sb.append(f2.getName()).append(" [");//NOI18N
                        sb.append(icon.getIconWidth()).append('x').append(icon.getIconHeight());
                        sb.append(']');
                        setApproveButtonToolTipText(sb.toString());
                        iconInfo.setText(sb.toString());
                    } else {
                        iconInfo.setText("");
                    }
                    return super.getName(f);
                }
                
            });            
        }
        
        private static JPanel getAccesoryPanel(final JTextField iconInfo) {
            iconInfo.setColumns(15);
            iconInfo.setEditable(false);
            
            JPanel accessoryPanel = new JPanel();
            JPanel inner = new JPanel();
            JLabel iconInfoLabel = new JLabel();
            accessoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
            
            inner.setLayout(new GridLayout(2, 1, 0, 6));
            
            iconInfoLabel.setLabelFor(iconInfo);
            Mnemonics.setLocalizedText(iconInfoLabel, NbBundle.getMessage(UIUtil.class, "LBL_IconInfo"));
            inner.add(iconInfoLabel);
            
            inner.add(iconInfo);
            
            accessoryPanel.add(inner);
            return accessoryPanel;
        }
    }
}
