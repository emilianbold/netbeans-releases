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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.*;
import java.util.logging.Level;
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
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.web.clientproject.ClientSideProject;
import org.netbeans.modules.web.clientproject.api.MissingLibResourceException;
import org.netbeans.modules.web.clientproject.api.WebClientLibraryManager;
import org.netbeans.modules.web.clientproject.libraries.JavaScriptLibraryTypeProvider;
import org.netbeans.modules.web.common.api.Version;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

@NbBundle.Messages({"JavaScriptLibrarySelection_Label=JavaScript Libraries to install into project",
    "ERR_SomeErrorDuringCopying=Some of the library files could not be retrieved.",
    "MSG_DownloadingLibraries=Downloading {0}",
    "JSColumn1=Name",
    "JSColumn2=Version"
})
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
        librariesTable.getColumnModel().getColumn(0).setMaxWidth(30);
        librariesTable.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                updateDescription();
            }
        });
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
        descriptionTextPane.setText(""); // NOI18N
        super.removeNotify();
    }
    
    private void updateDescription() {
        int i = librariesTable.getSelectedRow();
        if (i == -1) {
            return;
        }
        ModelItem mi = model.l.get(i);
        if (mi.getDescription() != null) {
            descriptionTextPane.setText(mi.getDescription());
        } else {
            descriptionTextPane.setText("");
        }
    }
    
    @Override
    public String getName() {
        return Bundle.JavaScriptLibrarySelection_Label();
    }
    
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        librariesFolder = new javax.swing.JTextField();
        jScrollPane2 = new javax.swing.JScrollPane();
        librariesTable = new MyTable();
        descriptionLabel = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        descriptionTextPane = new javax.swing.JTextPane();

        jLabel1.setText(org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.jLabel1.text")); // NOI18N

        jLabel2.setText(org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.jLabel2.text")); // NOI18N

        librariesFolder.setText(org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.librariesFolder.text")); // NOI18N

        jScrollPane2.setViewportView(librariesTable);

        descriptionLabel.setText(org.openide.util.NbBundle.getMessage(JavaScriptLibrarySelection.class, "JavaScriptLibrarySelection.descriptionLabel.text")); // NOI18N
        descriptionLabel.setVerticalAlignment(javax.swing.SwingConstants.TOP);

        descriptionTextPane.setEditable(false);
        jScrollPane1.setViewportView(descriptionTextPane);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 518, Short.MAX_VALUE)
            .addComponent(jScrollPane1)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(librariesFolder))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jLabel1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 173, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(descriptionLabel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 62, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(librariesFolder, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JTextPane descriptionTextPane;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextField librariesFolder;
    private javax.swing.JTable librariesTable;
    // End of variables declaration//GEN-END:variables

    void apply(FileObject p, ProgressHandle handle) throws IOException {
        FileObject librariesRoot = FileUtil.createFolder(p, librariesFolder.getText());
        boolean someFilesAreMissing = false;
        for (ModelItem mi : ((LibrariesModel)librariesTable.getModel()).l) {
            if (!mi.selected) {
                continue;
            }
            Library l = mi.getChosenLibrary();
            handle.progress(Bundle.MSG_DownloadingLibraries(
                    l.getProperties().get(JavaScriptLibraryTypeProvider.PROPERTY_REAL_DISPLAY_NAME)));
            try {
                WebClientLibraryManager.addLibraries(new Library[]{l}, librariesRoot, 
                        mi.getChosenLibraryVolume());
            }
            catch(MissingLibResourceException e ) {
                someFilesAreMissing = true;
            }
        }
        if (someFilesAreMissing) {
            DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(Bundle.ERR_SomeErrorDuringCopying()));
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
            return 3;
        }
        
        public String getColumnName(int columnIndex) {
            if (columnIndex == 0) {
                return "";
            } else if (columnIndex == 1) {
                return Bundle.JSColumn1();
            } else {
                return Bundle.JSColumn2();
            }
        }

        public Class<?> getColumnClass(int columnIndex) {
            if (columnIndex == 0) {
                return Boolean.class;
            } else {
                return String.class;
            }
        }
        
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 0 || columnIndex == 2;
        }
        
        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            ModelItem m = l.get(rowIndex);
            if (columnIndex == 0) {
                return Boolean.valueOf(m.selected);
            } else if (columnIndex == 1) {
                return m.getSimpleDisplayName();
            } else {
                return m.selectedVersion;
            }
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
            ModelItem m = l.get(rowIndex);
            if (columnIndex == 0) {
                m.selected = ((Boolean)aValue).booleanValue();
            } else if (columnIndex == 2) {
                m.selectedVersion = (String)aValue;
            } else {
                assert false : columnIndex;
            }
        }
        
    }

    private static class ModelItem {
        private boolean selected;
        private String selectedVersion;
        // this list represents single library in several different versions:
        private List<Library> libraries;

        private static final String VER_DOCUMENTED = " [documented]"; // NOI18N
        private static final String VER_MINIFIED = " [minified]"; // NOI18N
        
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
            if (column != 2) {
                return super.getCellEditor(row, column);
            }
            LibrariesModel model = (LibrariesModel)getModel();
            JComboBox jc = new JComboBox(model.l.get(row).getVersions());
            return new DefaultCellEditor(jc);
        }
    
    }
}
