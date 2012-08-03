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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import javax.swing.DefaultCellEditor;
import javax.swing.JComboBox;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
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
import org.netbeans.modules.web.common.api.Version;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public class JavaScriptLibrarySelection extends javax.swing.JPanel {

    private static final Logger LOGGER = Logger.getLogger(JavaScriptLibrarySelection.class.getName());

    private JavaScriptLibrarySelectionPanel wp;
    private LibrariesModel model;

    /**
     * Creates new form JavaScriptLibrarySelection
     */
    public JavaScriptLibrarySelection(JavaScriptLibrarySelectionPanel wp) {
        this.wp = wp;
        initComponents();
        model = new LibrariesModel();
        librariesTable.setModel(model);
        librariesTable.setRowSelectionAllowed(true);
        //librariesTable.setTableHeader(null);
        //librariesTable.setShowHorizontalLines(false);
        //librariesTable.setShowVerticalLines(false);
        librariesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateDescription();
            }
        });
    }

    @NbBundle.Messages("JavaScriptLibrarySelection.info=<html>Choose a library version and shuttle it to the Selected list to add it to your project. "
            + "Libraries added by your template are already selected.")
    private void init() {
        infoLabel.setText(Bundle.JavaScriptLibrarySelection_info());
    }

    @Override
    public void addNotify() {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                updateDescription();
            }
        });
        super.addNotify();
    }

    @Override
    public void removeNotify() {
        // if descriptionTextPane is too long and user goes to previous
        // and next page the wizard panel might resize too much; as worarkound
        // the descriptionTextPane will be emptied here
        //descriptionTextPane.setText(""); // NOI18N
        super.removeNotify();
    }

    private void updateDescription() {
        int i = librariesTable.getSelectedRow();
        if (i == -1) {
            return;
        }
        ModelItem mi = model.l.get(i);
        if (mi.getDescription() != null) {
//            descriptionTextPane.setText(mi.getDescription());
        } else {
//            descriptionTextPane.setText("");
        }
    }

    @NbBundle.Messages("JavaScriptLibrarySelection.name=JavaScript Libraries to install into project")
    @Override
    public String getName() {
        return Bundle.JavaScriptLibrarySelection_name();
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
        librariesTable = new MyTable();
        selectAllButton = new javax.swing.JButton();
        selectOneButton = new javax.swing.JButton();
        deselectOneButton = new javax.swing.JButton();
        deselectAllButton = new javax.swing.JButton();
        selectedLabel = new javax.swing.JLabel();
        selectedScrollPane = new javax.swing.JScrollPane();
        selectedList = new javax.swing.JList();
        librariesFolderLabel = new javax.swing.JLabel();
        librariesFolderTextField = new javax.swing.JTextField();

        infoLabel.setLabelFor(this);
        org.openide.awt.Mnemonics.setLocalizedText(infoLabel, "INFO"); // NOI18N

        librariesLabel.setLabelFor(librariesTable);
        org.openide.awt.Mnemonics.setLocalizedText(librariesLabel, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.librariesLabel.text")); // NOI18N

        librariesScrollPane.setViewportView(librariesTable);

        org.openide.awt.Mnemonics.setLocalizedText(selectAllButton, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.selectAllButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectOneButton, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.selectOneButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deselectOneButton, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.deselectOneButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(deselectAllButton, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.deselectAllButton.text")); // NOI18N

        org.openide.awt.Mnemonics.setLocalizedText(selectedLabel, org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.selectedLabel.text")); // NOI18N

        selectedScrollPane.setViewportView(selectedList);

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
                            .addComponent(selectOneButton)
                            .addComponent(deselectOneButton)
                            .addComponent(deselectAllButton))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(selectedLabel)
                    .addComponent(selectedScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)))
        );

        layout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {deselectAllButton, deselectOneButton, selectAllButton, selectOneButton});

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
                    .addComponent(selectedScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addComponent(librariesScrollPane, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(selectAllButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(selectOneButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deselectOneButton)
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
    private javax.swing.JButton deselectOneButton;
    private javax.swing.JLabel infoLabel;
    private javax.swing.JTextField librariesFilterTextField;
    private javax.swing.JLabel librariesFolderLabel;
    private javax.swing.JTextField librariesFolderTextField;
    private javax.swing.JLabel librariesLabel;
    private javax.swing.JScrollPane librariesScrollPane;
    private javax.swing.JTable librariesTable;
    private javax.swing.JButton selectAllButton;
    private javax.swing.JButton selectOneButton;
    private javax.swing.JLabel selectedLabel;
    private javax.swing.JList selectedList;
    private javax.swing.JScrollPane selectedScrollPane;
    // End of variables declaration//GEN-END:variables

    @NbBundle.Messages({
        "JavaScriptLibrarySelection.error.copying=Some of the library files could not be retrieved.",
        "# {0} - library name",
        "JavaScriptLibrarySelection.msg.downloading=Downloading {0}"
    })
    void apply(FileObject p, ProgressHandle handle) throws IOException {
        FileObject librariesRoot = FileUtil.createFolder(p, librariesFolderTextField.getText());
        boolean someFilesAreMissing = false;
        for (ModelItem mi : ((LibrariesModel) librariesTable.getModel()).l) {
            if (!mi.selected) {
                continue;
            }
            Library l = mi.getChosenLibrary();
            handle.progress(Bundle.JavaScriptLibrarySelection_msg_downloading(l.getProperties().get(JavaScriptLibraryTypeProvider.PROPERTY_REAL_DISPLAY_NAME)));
            try {
                WebClientLibraryManager.addLibraries(new Library[]{l}, librariesRoot,
                        mi.getChosenLibraryVolume());
            } catch (MissingLibResourceException e) {
                someFilesAreMissing = true;
            }
        }
        if (someFilesAreMissing) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.JavaScriptLibrarySelection_error_copying(), NotifyDescriptor.ERROR_MESSAGE));
        }
    }

    void updateDefaults(Collection<String> defaultLibs) {
        model.setSelected(defaultLibs);
        model.fireTableDataChanged();
    }

    private static class LibrariesModel extends AbstractTableModel {

        private List<ModelItem> l = new ArrayList<ModelItem>();

        public LibrariesModel() {
            Map<String,List<Library>> map = new HashMap<String, List<Library>>();
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
                l.add(new ModelItem(map.get(libName)));
            }
            // sort libraries according their name:
            Collections.sort(l, new Comparator<ModelItem>() {
                @Override
                public int compare(ModelItem o1, ModelItem o2) {
                    return o1.getSimpleDisplayName().toLowerCase().compareTo(
                            o2.getSimpleDisplayName().toLowerCase());
                }
            });
        }

        void setSelected(Collection<String> preSelected) {
            for (ModelItem mi : l) {
                if (preSelected.contains(mi.getLibrary().getName())) {
                    mi.selected = true;
                }
            }

        }

        @Override
        public int getRowCount() {
            return l.size();
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
            ModelItem m = l.get(rowIndex);
            if (columnIndex == 0) {
                return m.getSimpleDisplayName();
            }
            if (columnIndex == 1) {
                return m.selectedVersion;
            }
            assert false : "Unknown column index: " + columnIndex;
            return null;
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            ModelItem m = l.get(rowIndex);
            if (columnIndex == 1) {
                m.selectedVersion = (String) aValue;
                return;
            }
            assert false : "Unknown column index: " + columnIndex;
        }

    }

    private static class ModelItem {
        private boolean selected;
        private String selectedVersion;
        // this list represents single library in several different versions:
        private List<Library> libraries;

        private static final String VER_DOCUMENTED = " documented"; // NOI18N
        private static final String VER_MINIFIED = " minified"; // NOI18N

        public ModelItem(List<Library> libraries) {
            // sort libraries from latest to oldest; if the same version of library is comming
            // from different CDNs then put higher in the list one which has documentation or
            // regular version of JS files
            Collections.sort(libraries, new Comparator<Library>() {
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
                    } else {
                        return ver1.isBelowOrEqual(ver2) ? 1 : -1;
                    }
                }
            });
            this.libraries = libraries;
            this.selected = false;
            this.selectedVersion = getLibrary().getProperties().get(WebClientLibraryManager.PROPERTY_VERSION);
            if (!getLibrary().getContent(WebClientLibraryManager.VOL_DOCUMENTED).isEmpty()) {
                this.selectedVersion += VER_DOCUMENTED;
            } else if (!getLibrary().getContent(WebClientLibraryManager.VOL_REGULAR).isEmpty()) {
            } else if (!getLibrary().getContent(WebClientLibraryManager.VOL_MINIFIED).isEmpty()) {
                this.selectedVersion += VER_MINIFIED;
            }
        }

        public String getSimpleDisplayName() {
            return getLibrary().getProperties().get(JavaScriptLibraryTypeProvider.PROPERTY_REAL_DISPLAY_NAME);
        }

        public String getDescription() {
            return getLibrary().getDescription();
        }

        private Library getLibrary() {
            return libraries.get(0);
        }

        public Library getChosenLibrary() {
            String selVersion = selectedVersion;
            if (selVersion.endsWith(VER_DOCUMENTED)) {
                selVersion = selVersion.substring(0, selVersion.length()-VER_DOCUMENTED.length());
            } else if (selVersion.endsWith(VER_MINIFIED)) {
                selVersion = selVersion.substring(0, selVersion.length()-VER_MINIFIED.length());
            }
            for (Library l : libraries) {
                if (selVersion.equals(l.getProperties().get(WebClientLibraryManager.PROPERTY_VERSION))) {
                    return l;
                }
            }
            assert false;
            return null;
        }

        private String getChosenLibraryVolume() {
            if (selectedVersion.endsWith(VER_DOCUMENTED)) {
                return WebClientLibraryManager.VOL_DOCUMENTED;
            } else if (selectedVersion.endsWith(VER_MINIFIED)) {
                return WebClientLibraryManager.VOL_MINIFIED;
            } else {
                return WebClientLibraryManager.VOL_REGULAR;
            }
        }

        public String[] getVersions() {
            List<String> vers = new ArrayList<String>();
            for (Library l : libraries) {
                String version = l.getProperties().get(WebClientLibraryManager.PROPERTY_VERSION);
                if (!l.getContent(WebClientLibraryManager.VOL_DOCUMENTED).isEmpty()) {
                    if (!vers.contains(version + VER_DOCUMENTED)) {
                        vers.add(version + VER_DOCUMENTED);
                    }
                }
                if (!l.getContent(WebClientLibraryManager.VOL_REGULAR).isEmpty()) {
                    if (!vers.contains(version)) {
                        vers.add(version);
                    }
                }
                if (!l.getContent(WebClientLibraryManager.VOL_MINIFIED).isEmpty()) {
                    if (!vers.contains(version + VER_MINIFIED)) {
                        vers.add(version + VER_MINIFIED);
                    }
                }
            }
            return vers.toArray(new String[vers.size()]);
        }

    }

    private static class MyTable extends JTable {

        @Override
        public TableCellEditor getCellEditor(int row, int column) {
            if (column != 1) {
                return super.getCellEditor(row, column);
            }
            LibrariesModel model = (LibrariesModel)getModel();
            JComboBox jc = new JComboBox(model.l.get(row).getVersions());
            return new DefaultCellEditor(jc);
        }

    }
}
