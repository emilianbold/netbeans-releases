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
package org.netbeans.modules.web.clientproject.ui.wizard;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.AbstractListModel;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.ListCellRenderer;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellEditor;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.clientproject.api.MissingLibResourceException;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.modules.web.clientproject.libraries.JavaScriptLibraryTypeProvider;
import org.netbeans.modules.web.common.api.Pair;
import org.netbeans.modules.web.common.api.Version;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.ChangeSupport;
import org.openide.util.NbBundle;

public class JavaScriptLibrarySelection extends JPanel {

    private static final long serialVersionUID = -468734354571212312L;

    static final Logger LOGGER = Logger.getLogger(JavaScriptLibrarySelection.class.getName());

    private static final Pattern LIBRARIES_FOLDER_PATTERN = Pattern.compile("^[\\w/-]+$", Pattern.CASE_INSENSITIVE); // NOI18N

    private final ChangeSupport changeSupport = new ChangeSupport(this);

    // selected items are accessed outside of EDT thread
    final List<Pair<String, LibraryVersion>> selectedLibraries = Collections.synchronizedList(new LinkedList<Pair<String, LibraryVersion>>());
    // @GuardedBy("EDT")
    final LibrariesTableModel librariesTableModel = new LibrariesTableModel();
    // @GuardedBy("EDT")
    final LibrariesListModel librariesListModel = new LibrariesListModel(selectedLibraries);

    // folder path is accessed outside of EDT thread
    private volatile String librariesFolder = null;


    public JavaScriptLibrarySelection() {
        assert EventQueue.isDispatchThread();

        initComponents();

        initInfo();
        initLibraries();
        initLibrariesFolder();
    }

    @NbBundle.Messages("JavaScriptLibrarySelection.info=<html>Choose a library version and shuttle it to the Selected list to add it to your project. "
            + "Libraries added by your template are already selected.")
    private void initInfo() {
        infoLabel.setText(Bundle.JavaScriptLibrarySelection_info());
    }

    private void initLibraries() {
        initLibrariesTable();
        initLibrariesList();
        initLibrariesButtons();
    }

    private void initLibrariesTable() {
        assert EventQueue.isDispatchThread();
        librariesTable.setModel(librariesTableModel);
        // tooltip
        librariesTable.addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                assert EventQueue.isDispatchThread();
                Point point = e.getPoint();
                int row = librariesTable.rowAtPoint(point);
                librariesTable.setToolTipText(librariesTableModel.getItems().get(row).getDescription());
            }
        });
    }

    private void initLibrariesList() {
        selectedLibrariesList.setModel(librariesListModel);
    }

    private void initLibrariesButtons() {
        // listen on table and list
        ListSelectionListener selectionListener = new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                enableLibraryButtons();
            }
        };
        librariesTable.getSelectionModel().addListSelectionListener(selectionListener);
        selectedLibrariesList.getSelectionModel().addListSelectionListener(selectionListener);
        // action listeners
        selectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectAllLibraries();
            }
        });
        selectSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                selectSelectedLibraries();
            }
        });
        deselectSelectedButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deselectSelectedLibraries();
            }
        });
        deselectAllButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deselectAllLibraries();
            }
        });
        // set correct state
        enableLibraryButtons();
    }

    private void initLibrariesFolder() {
        librariesFolder = librariesFolderTextField.getText();
        librariesFolderTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processChange();
            }
            private void processChange() {
                librariesFolder = librariesFolderTextField.getText();
                fireChangeEvent();
            }
        });
    }

    @NbBundle.Messages("JavaScriptLibrarySelection.name=JavaScript Libraries to install into project")
    @Override
    public String getName() {
        return Bundle.JavaScriptLibrarySelection_name();
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    final void fireChangeEvent() {
        changeSupport.fireChange();
    }

    public String getErrorMessage() {
        return validateLibrariesFolder();
    }

    public String getWarningMessage() {
        return null;
    }

    void enableLibraryButtons() {
        // select
        selectAllButton.setEnabled(librariesTableModel.getRowCount() > 0);
        selectSelectedButton.setEnabled(librariesTable.getSelectedRows().length > 0);
        // deselect
        // XXX do not allow to remove files from the selected site template
        deselectSelectedButton.setEnabled(selectedLibrariesList.getSelectedIndices().length > 0);
        // XXX do not count files from the selected site template
        deselectAllButton.setEnabled(selectedLibraries.size() > 0);
    }

    void selectAllLibraries() {
        assert EventQueue.isDispatchThread();
        //for ()
    }

    void selectSelectedLibraries() {
        assert EventQueue.isDispatchThread();
        //for ()
    }

    private void selectLibrary(Library library) {
    }

    void deselectAllLibraries() {
    }

    void deselectSelectedLibraries() {
    }

    private void deselectLibrary(Library library) {
    }

    @NbBundle.Messages({
        "JavaScriptLibrarySelection.error.librariesFolder.invalid=Libraries folder can contain only alphanumeric characters, \"_\", \"-\" and \"/\"."
    })
    private String validateLibrariesFolder() {
        if (!LIBRARIES_FOLDER_PATTERN.matcher(librariesFolder).matches()) {
            return Bundle.JavaScriptLibrarySelection_error_librariesFolder_invalid();
        }
        return null;
    }

    @NbBundle.Messages({
        "JavaScriptLibrarySelection.error.copying=Some of the library files could not be retrieved.",
        "# {0} - library name",
        "JavaScriptLibrarySelection.msg.downloading=Downloading {0}"
    })
    void apply(FileObject projectDir, ProgressHandle handle) throws IOException {
        assert !EventQueue.isDispatchThread();
        FileObject librariesRoot = null;
        boolean someFilesAreMissing = false;
        for (Pair<String, LibraryVersion> selectedLibrary : selectedLibraries) {
            if (librariesRoot == null) {
                librariesRoot = FileUtil.createFolder(projectDir, librariesFolder);
            }
            LibraryVersion libraryVersion = selectedLibrary.getB();
            if (libraryVersion == null) {
                // happens for js files from selected site template
                continue;
            }
            Library library = libraryVersion.getLibrary();
            handle.progress(Bundle.JavaScriptLibrarySelection_msg_downloading(library.getProperties().get(JavaScriptLibraryTypeProvider.PROPERTY_REAL_DISPLAY_NAME)));
            try {
                WebClientLibraryManager.addLibraries(new Library[]{library}, librariesRoot, libraryVersion.getType());
            } catch (MissingLibResourceException e) {
                someFilesAreMissing = true;
            }
        }
        if (someFilesAreMissing) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.JavaScriptLibrarySelection_error_copying(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    void updateDefaults(Collection<String> defaultLibs) {
        assert EventQueue.isDispatchThread();
        // XXX remove default libraries from list and add there defaultLibs (in the beginning of the list)
        //selectedLibraries.addAll(defaultLibs);
        // XXX fire list change
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        infoLabel = new javax.swing.JLabel();
        librariesLabel = new javax.swing.JLabel();
        librariesFilterTextField = new javax.swing.JTextField();
        librariesScrollPane = new javax.swing.JScrollPane();
        librariesTable = new LibrariesTable();
        selectAllButton = new javax.swing.JButton();
        selectSelectedButton = new javax.swing.JButton();
        deselectSelectedButton = new javax.swing.JButton();
        deselectAllButton = new javax.swing.JButton();
        selectedLabel = new javax.swing.JLabel();
        selectedLibrariesScrollPane = new javax.swing.JScrollPane();
        selectedLibrariesList = new javax.swing.JList();
        librariesFolderLabel = new javax.swing.JLabel();
        librariesFolderTextField = new javax.swing.JTextField();

        infoLabel.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(infoLabel, "INFO"); // NOI18N

        librariesLabel.setLabelFor(librariesTable);
        org.openide.awt.Mnemonics.setLocalizedText(librariesLabel, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.librariesLabel.text")); // NOI18N

        librariesScrollPane.setViewportView(librariesTable);

        org.openide.awt.Mnemonics.setLocalizedText(selectAllButton, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.selectAllButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectSelectedButton, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.selectSelectedButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deselectSelectedButton, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.deselectSelectedButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deselectAllButton, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.deselectAllButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectedLabel, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.selectedLabel.text")); // NOI18N

        selectedLibrariesScrollPane.setViewportView(selectedLibrariesList);

        librariesFolderLabel.setLabelFor(librariesFolderTextField);
        org.openide.awt.Mnemonics.setLocalizedText(librariesFolderLabel, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.librariesFolderLabel.text")); // NOI18N

        librariesFolderTextField.setText("js/libs"); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(librariesFolderLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(librariesFolderTextField))
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(infoLabel)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(librariesLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(librariesFilterTextField))
                            .addComponent(librariesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(selectAllButton)
                            .addComponent(selectSelectedButton)
                            .addComponent(deselectSelectedButton)
                            .addComponent(deselectAllButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectedLabel)
                    .addComponent(selectedLibrariesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {deselectAllButton, deselectSelectedButton, selectAllButton, selectSelectedButton});

        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(infoLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(librariesLabel)
                    .addComponent(selectedLabel)
                    .addComponent(librariesFilterTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectedLibrariesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(librariesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(selectAllButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectSelectedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deselectSelectedButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deselectAllButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(librariesFolderLabel)
                    .addComponent(librariesFolderTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
        );
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deselectAllButton;
    private javax.swing.JButton deselectSelectedButton;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JTextField librariesFilterTextField;
    private javax.swing.JLabel librariesFolderLabel;
    private javax.swing.JTextField librariesFolderTextField;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JScrollPane librariesScrollPane;
    private javax.swing.JTable librariesTable;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JButton selectSelectedButton;
    private javax.swing.JLabel selectedLabel;
    private javax.swing.JList selectedLibrariesList;
    private javax.swing.JScrollPane selectedLibrariesScrollPane;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class LibrariesTable extends JTable {

        private static final long serialVersionUID = 1578314546784244L;


        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (column != 1) {
                return super.getCellEditor(row, column);
            }
            LibrariesTableModel model = (LibrariesTableModel) getModel();
            JComboBox versionsComboBox = new JComboBox(model.getItems().get(row).getVersions());
            versionsComboBox.setRenderer(new VersionsRenderer(versionsComboBox.getRenderer()));
            return new DefaultCellEditor(versionsComboBox);
        }

    }

    private static final class VersionsRenderer implements ListCellRenderer {

        private final ListCellRenderer defaultRenderer;


        public VersionsRenderer(ListCellRenderer defaultRenderer) {
            this.defaultRenderer = defaultRenderer;
        }

        @NbBundle.Messages({
            "# {0} - library version",
            "# {1} - library type",
            "VersionsRenderer.label={0} {1}",
            "VersionsRenderer.type.minified=minified",
            "VersionsRenderer.type.documented=documented"
        })
        @Override
        public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            return defaultRenderer.getListCellRendererComponent(list, getLabel((LibraryVersion) value), index, isSelected, cellHasFocus);
        }

        public static String getLabel(LibraryVersion libraryVersion) {
            String version = libraryVersion.getLibraryVersion();
            String rawType = libraryVersion.getType();
            String type;
            if (WebClientLibraryManager.VOL_DOCUMENTED.equals(rawType)) {
                type = Bundle.VersionsRenderer_type_documented();
            } else if (WebClientLibraryManager.VOL_MINIFIED.equals(rawType)) {
                type = Bundle.VersionsRenderer_type_minified();
            } else if (WebClientLibraryManager.VOL_REGULAR.equals(rawType)) {
                type = ""; // NOI18N
            } else {
                assert false : "Unknown library type: " + libraryVersion;
                // fallback
                type = ""; // NOI18N
            }
            return Bundle.VersionsRenderer_label(version, type).trim();
        }

    }

    private static final class LibrariesTableModel extends AbstractTableModel {

        private static final long serialVersionUID = 8732134781780336L;

        // @GuardedBy("EDT")
        private final List<ModelItem> items = new ArrayList<ModelItem>();


        public LibrariesTableModel() {
            assert EventQueue.isDispatchThread();
            Map<String, List<Library>> map = new HashMap<String, List<Library>>();
            for (Library lib : LibraryManager.getDefault().getLibraries()) {
                if (WebClientLibraryManager.TYPE.equals(lib.getType())) {
                    String name = lib.getProperties().get(
                            WebClientLibraryManager.PROPERTY_REAL_NAME);
                    List<Library> libs = map.get(name);
                    if (libs == null) {
                        libs = new ArrayList<Library>();
                        map.put(name, libs);
                    }
                    libs.add(lib);
                }
            }
            for (String libName : map.keySet()) {
                items.add(new ModelItem(map.get(libName)));
            }
            // sort libraries according their name:
            Collections.sort(items, new Comparator<ModelItem>() {
                @Override
                public int compare(ModelItem o1, ModelItem o2) {
                    return o1.getSimpleDisplayName().toLowerCase().compareTo(
                            o2.getSimpleDisplayName().toLowerCase());
                }
            });
        }

        @Override
        public int getRowCount() {
            assert EventQueue.isDispatchThread();
            return items.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @NbBundle.Messages({
            "JavaScriptLibrarySelection.column.library=Library",
            "JavaScriptLibrarySelection.column.version=Version"
        })
        @Override
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return Bundle.JavaScriptLibrarySelection_column_library();
            }
            if (columnIndex == 1) {
                return Bundle.JavaScriptLibrarySelection_column_version();
            }
            assert false : "Unknown column index: " + columnIndex;
            return null;
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            return String.class;
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            assert EventQueue.isDispatchThread();
            ModelItem modelItem = items.get(rowIndex);
            if (columnIndex == 0) {
                return modelItem.getSimpleDisplayName();
            }
            if (columnIndex == 1) {
                return VersionsRenderer.getLabel(modelItem.getSelectedVersion());
            }
            assert false : "Unknown column index: " + columnIndex;
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            assert EventQueue.isDispatchThread();
            ModelItem modelItem = items.get(rowIndex);
            if (columnIndex == 1) {
                modelItem.setSelectedVersion((LibraryVersion) aValue);
                return;
            }
            assert false : "Unknown column index: " + columnIndex;
        }

        List<ModelItem> getItems() {
            assert EventQueue.isDispatchThread();
            return items;
        }

    }

    private static final class LibrariesListModel extends AbstractListModel {

        private static final long serialVersionUID = -57683546574861110L;

        private final List<Pair<String, LibraryVersion>> libraries;


        public LibrariesListModel(List<Pair<String, LibraryVersion>> libraries) {
            this.libraries = libraries;
        }

        @Override
        public int getSize() {
            return libraries.size();
        }

        @Override
        public Pair<String, LibraryVersion> getElementAt(int index) {
            return libraries.get(index);
        }

    }

    private static final class ModelItem {

        // sort libraries from latest to oldest; if the same version of library is comming
        // from different CDNs then put higher in the list one which has documentation or
        // regular version of JS files
        private static final Comparator<Library> LIBRARY_COMPARATOR = new Comparator<Library>() {
            @Override
            public int compare(Library o1, Library o2) {
                Version ver1 = Version.fromDottedNotationWithFallback(o1.getProperties().get(WebClientLibraryManager.PROPERTY_VERSION));
                Version ver2 = Version.fromDottedNotationWithFallback(o2.getProperties().get(WebClientLibraryManager.PROPERTY_VERSION));
                if (ver1.equals(ver2)) {
                    if (!o1.getContent(WebClientLibraryManager.VOL_DOCUMENTED).isEmpty()) {
                        return -1;
                    }
                    if (!o2.getContent(WebClientLibraryManager.VOL_DOCUMENTED).isEmpty()) {
                        return 1;
                    }
                    if (!o1.getContent(WebClientLibraryManager.VOL_REGULAR).isEmpty()) {
                        return -1;
                    }
                    if (!o2.getContent(WebClientLibraryManager.VOL_REGULAR).isEmpty()) {
                        return 1;
                    }
                    return 0;
                }
                return ver1.isBelowOrEqual(ver2) ? 1 : -1;
            }
        };

        // @GuardedBy("EDT")
        private final Set<LibraryVersion> versions;

        // @GuardedBy("EDT")
        private LibraryVersion selectedVersion;


        public ModelItem(List<Library> libraries) {
            assert EventQueue.isDispatchThread();
            Collections.sort(libraries, LIBRARY_COMPARATOR);
            versions = createVersions(libraries);
            selectedVersion = versions.iterator().next();
        }

        public String getSimpleDisplayName() {
            return getLibrary().getProperties().get(JavaScriptLibraryTypeProvider.PROPERTY_REAL_DISPLAY_NAME);
        }

        public String getDescription() {
            return getLibrary().getDescription();
        }

        public LibraryVersion[] getVersions() {
            assert EventQueue.isDispatchThread();
            return versions.toArray(new LibraryVersion[versions.size()]);
        }

        public LibraryVersion getSelectedVersion() {
            assert EventQueue.isDispatchThread();
            return selectedVersion;
        }

        public void setSelectedVersion(LibraryVersion selectedVersion) {
            assert EventQueue.isDispatchThread();
            assert selectedVersion != null;
            this.selectedVersion = selectedVersion;
        }

        private Set<LibraryVersion> createVersions(List<Library> libraries) {
            Set<LibraryVersion> libraryVersions = new LinkedHashSet<LibraryVersion>();
            for (Library library : libraries) {
                LibraryVersion libraryVersion = null;
                if (!library.getContent(WebClientLibraryManager.VOL_DOCUMENTED).isEmpty()) {
                    libraryVersion = new LibraryVersion(library, WebClientLibraryManager.VOL_DOCUMENTED);
                    libraryVersions.add(libraryVersion);
                }
                if (!library.getContent(WebClientLibraryManager.VOL_REGULAR).isEmpty()) {
                    libraryVersion = new LibraryVersion(library, WebClientLibraryManager.VOL_REGULAR);
                    libraryVersions.add(libraryVersion);
                }
                if (!library.getContent(WebClientLibraryManager.VOL_MINIFIED).isEmpty()) {
                    libraryVersion = new LibraryVersion(library, WebClientLibraryManager.VOL_MINIFIED);
                    libraryVersions.add(libraryVersion);
                }
                if (libraryVersion == null) {
                    assert false : "Unknown library version: " + library.getName();
                }
            }
            return libraryVersions;
        }

        private Library getLibrary() {
            assert EventQueue.isDispatchThread();
            return versions.iterator().next().getLibrary();
        }

    }

    private static final class LibraryVersion {

        private final Library library;
        private final String type;


        public LibraryVersion(Library library, String type) {
            assert library != null;
            assert type != null && !type.isEmpty();
            this.library = library;
            this.type = type;
        }

        public Library getLibrary() {
            return library;
        }

        public String getType() {
            return type;
        }

        public String getLibraryVersion() {
            return library.getProperties().get(WebClientLibraryManager.PROPERTY_VERSION);
        }

        @Override
        public int hashCode() {
            int hash = 5;
            hash = 41 * hash + getLibraryVersion().hashCode();
            hash = 41 * hash + (this.type != null ? this.type.hashCode() : 0);
            return hash;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null) {
                return false;
            }
            if (getClass() != obj.getClass()) {
                return false;
            }
            final LibraryVersion other = (LibraryVersion) obj;
            if (this.library != other.library && (this.library == null || !this.getLibraryVersion().equals(other.getLibraryVersion()))) {
                return false;
            }
            if ((this.type == null) ? (other.type != null) : !this.type.equals(other.type)) {
                return false;
            }
            return true;
        }

        @Override
        public String toString() {
            return "LibraryVersion{" + "library=" + library.getName() + ", type=" + type + '}'; // NOI18N
        }

    }

}
