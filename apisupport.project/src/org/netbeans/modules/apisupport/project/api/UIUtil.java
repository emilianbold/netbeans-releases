/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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

package org.netbeans.modules.apisupport.project.api;

import javax.swing.DefaultListModel;
import javax.swing.ListModel;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.net.MalformedURLException;
import java.text.Collator;
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
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileView;
import javax.swing.plaf.UIResource;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.templates.TemplateRegistration;
import org.netbeans.modules.apisupport.project.layers.LayerUtils;
import org.netbeans.modules.apisupport.project.layers.SynchronousStatus;
import org.netbeans.modules.apisupport.project.ui.wizard.common.CreatedModifiedFiles;
import org.netbeans.spi.java.project.support.ui.PackageView;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystem.Status;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbCollections;
import org.openide.util.Utilities;
import static org.netbeans.modules.apisupport.project.api.Bundle.*;

/**
 * @author Martin Krauskopf
 */
@Messages({
    "please_wait=Please wait...",
    "LBL_Empty=<empty>"
})
public final class UIUtil {
    
    private UIUtil() {}

    /**
     * Category to use for NBM-related templates.
     * @see TemplateRegistration#category
     * @see RecommendedTemplates
     */
    public static final String TEMPLATE_CATEGORY = "nbm-specific";
    
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
    @Messages("MSG_WrongIconSize=Incorrect icon size: {0}x{1} but expected {2}x{3}.")
    public static String getIconDimensionWarning(File icon, int expectedWidth, int expectedHeight) {
        Dimension real = new Dimension(UIUtil.getIconDimension(icon));
        if (real.height == expectedHeight && real.width == expectedWidth) {
            return "";
        }
        return MSG_WrongIconSize(real.width, real.height, expectedWidth, expectedHeight);
    }

    /**
     * @param expectedWidth expected width
     * @param expectedHeight expected height
     * @return warning 
     */
    @Messages("MSG_NoIconSelected=No Icon ({0}x{1}) selected.")
    public static String getNoIconSelectedWarning(int expectedWidth, int expectedHeight) {
        return MSG_NoIconSelected(expectedWidth, expectedHeight);
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
    @Messages("MSG_Missing_Source_Root=Missing source root in project")
    public static JComboBox createPackageComboBox(SourceGroup srcRoot) {
        JComboBox packagesComboBox;
        if (srcRoot != null) {
            packagesComboBox = new JComboBox(PackageView.createListView(srcRoot));
            packagesComboBox.setRenderer(PackageView.listRenderer());
        } else {
            packagesComboBox = new JComboBox();
            packagesComboBox.addItem(MSG_Missing_Source_Root());
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
     * Returns {@link ComboBoxModel} containing {@link LayerItemPresenter}s
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
                Status status = fo.getFileSystem().getStatus();
                if (status instanceof SynchronousStatus) {
                    name = ((SynchronousStatus) status).annotateNameSynch(fo.getNameExt(), Collections.singleton(fo));
                } else {
                    name = status.annotateName(fo.getNameExt(), Collections.singleton(fo));
                }
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
    
    private static class IconFileChooser extends JFileChooser {
        private final JTextField iconInfo = new JTextField();        
        @Messages("TITLE_IconDialog=Select Icon")
        private  IconFileChooser() {
            JPanel accessoryPanel = getAccesoryPanel(iconInfo);
            setDialogTitle(TITLE_IconDialog());//NOI18N
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
        
        @Messages("LBL_IconInfo=Selected icon [size]:")
        private static JPanel getAccesoryPanel(final JTextField iconInfo) {
            iconInfo.setColumns(15);
            iconInfo.setEditable(false);
            
            JPanel accessoryPanel = new JPanel();
            JPanel inner = new JPanel();
            JLabel iconInfoLabel = new JLabel();
            accessoryPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 6, 0));
            
            inner.setLayout(new GridLayout(2, 1, 0, 6));
            
            iconInfoLabel.setLabelFor(iconInfo);
            Mnemonics.setLocalizedText(iconInfoLabel, LBL_IconInfo());
            inner.add(iconInfoLabel);
            
            inner.add(iconInfo);
            
            accessoryPanel.add(inner);
            return accessoryPanel;
        }
    }

    /** Generally usable in conjuction with {@link #createComboWaitModel}. */
    public static final String WAIT_VALUE =
            please_wait();

    /** Generally usable in conjuction with {@link #createComboEmptyModel}. */
    public static final String EMPTY_VALUE =
            LBL_Empty();

    /**
     * Use this model in situation when you need to populate combo in the
     * background. The only item in this model is {@link #WAIT_VALUE}.
     */
    public static ComboBoxModel createComboWaitModel() {
        return new DefaultComboBoxModel(new Object[] { WAIT_VALUE });
    }

    /** The only item in this model is {@link #EMPTY_VALUE}. */
    public static ComboBoxModel createComboEmptyModel() {
        return new DefaultComboBoxModel(new Object[] { EMPTY_VALUE });
    }

    public interface WaitingModel extends ListModel {
        boolean isWaiting();
    }

    /**
     * Conveninent method which delegates to {@link #hasOnlyValue} passing a
     * given model and {@link #WAIT_VALUE} as a value.
     * Also checks {@link WaitingModel}.
     */
    public static boolean isWaitModel(final ListModel model) {
        if (model instanceof WaitingModel) {
            return ((WaitingModel) model).isWaiting();
        }
        return hasOnlyValue(model, WAIT_VALUE);
    }

    /**
     * Returns true if the given model is not <code>null</code> and contains
     * only the given value.
     */
    public static boolean hasOnlyValue(final ListModel model, final Object value) {
        return model != null && model.getSize() == 1 && model.getElementAt(0) == value;
    }

    /**
     * Use this model in situation when you need to populate list in the
     * background. The only item in this model is {@link #WAIT_VALUE}.
     *
     * @see #isWaitModel
     */
    public static ListModel createListWaitModel() {
        DefaultListModel listWaitModel = new DefaultListModel();
        listWaitModel.addElement(WAIT_VALUE);
        return listWaitModel;
    }

}
