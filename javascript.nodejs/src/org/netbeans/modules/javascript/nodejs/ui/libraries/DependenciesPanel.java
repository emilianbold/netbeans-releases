/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2014 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2014 Sun Microsystems, Inc.
 */

package org.netbeans.modules.javascript.nodejs.ui.libraries;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JTable;
import static javax.swing.SwingConstants.CENTER;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumnModel;
import org.netbeans.api.project.Project;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel for customization of one set (regular/development/optional) of npm dependencies
 *
 * @author Jan Stola
 */
public class DependenciesPanel extends javax.swing.JPanel {
    /** Request processor for this class. */
    private static final RequestProcessor RP = new RequestProcessor(DependenciesPanel.class.getName(), 3);
    /** Selected dependencies. */
    private final List<Library.Version> dependencies = new ArrayList<>();
    /** Model for a table of selected dependencies. */
    private final DependencyTableModel tableModel;
    /** Maps the name of the library to its npm meta-data. */
    private final Map<String,Library> dependencyInfo = new HashMap<>();
    /** Map of the installed libraries. */
    private Map<String,Library.Version> installedLibraries;
    /** Determines whether the installed libraries were set. */
    private boolean installedLibrariesSet;
    /** Owning project. */
    private Project project;
    /** Panel for searching npm libraries. */
    private SearchPanel searchPanel;

    /**
     * Creates a new {@code DependenciesPanel}.
     */
    public DependenciesPanel() {
        tableModel = new DependencyTableModel();
        initComponents();
        TableCellRenderer versionColumnRenderer = new VersionColumnRenderer();
        TableColumnModel tableColumnModel = table.getColumnModel();
        tableColumnModel.getColumn(1).setCellRenderer(versionColumnRenderer);
        tableColumnModel.getColumn(2).setCellRenderer(versionColumnRenderer);
        tableColumnModel.getColumn(3).setCellRenderer(versionColumnRenderer);
    }

    /**
     * Sets the owning project.
     * 
     * @param project owning project.
     */
    void setProject(Project project) {
        this.project = project;
    }

    /**
     * Sets the existing dependencies.
     * 
     * @param dependencies existing dependencies.
     */
    void setDependencies(List<Library.Version> dependencies) {
        this.dependencies.addAll(dependencies);
        sortDependencies();
        loadDependencyInfo(dependencies);
    }

    /**
     * Sorts the list of dependencies.
     */
    private void sortDependencies() {
        Collections.sort(dependencies, new LibraryVersionComparator());
    }

    /**
     * Loads information about given libraries/dependencies/packages.
     * 
     * @param dependencies dependencies to load information about.
     */
    private void loadDependencyInfo(final List<Library.Version> dependencies) {
        if (RP.isRequestProcessorThread()) {
            LibraryProvider provider = LibraryProvider.forProject(project);
            for (Library.Version dependency : dependencies) {
                String libraryName = dependency.getLibrary().getName();
                Library library = provider.libraryDetails(libraryName, false);
                updateDependencyInfo(libraryName, library);
            }
        } else {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    loadDependencyInfo(dependencies);
                }
            });
        }
    }

    /**
     * Updates the view according to the newly loaded library/dependency information.
     * 
     * @param libraryName name of the library.
     * @param library information about the library.
     */
    private void updateDependencyInfo(final String libraryName, final Library library) {
        if (EventQueue.isDispatchThread()) {
            dependencyInfo.put(libraryName, library);
            tableModel.fireTableDataChanged();
        } else {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    updateDependencyInfo(libraryName, library);
                }
            });
        }
    }

    /**
     * Sets the map of the installed libraries.
     * 
     * @param installedLibraries installed libraries (maps name
     * of the library/package to the library).
     */
    void setInstalledLibraries(Map<String,Library.Version> installedLibraries) {
        this.installedLibraries = installedLibraries;
        this.installedLibrariesSet = true;
        tableModel.fireTableDataChanged();
    }

    /**
     * Returns the panel for searching npm libraries.
     * 
     * @return panel for searching npm libraries.
     */
    private SearchPanel getSearchPanel() {
        if (searchPanel == null) {
            searchPanel = new SearchPanel(LibraryProvider.forProject(project));
        }
        return searchPanel;
    }

    /**
     * Shows the search panel.
     */
    @NbBundle.Messages({"DependenciesPanel.searchDialog.title=Add npm package"})
    private void showSearchPanel() {
        SearchPanel panel = getSearchPanel();
        panel.activate();
        DialogDescriptor descriptor = new DialogDescriptor(
                panel,
                Bundle.DependenciesPanel_searchDialog_title(),
                true,
                new Object[] {
                    panel.getAddButton(),
                    panel.getCancelButton()
                },
                panel.getAddButton(),
                DialogDescriptor.DEFAULT_ALIGN,
                HelpCtx.DEFAULT_HELP,
                null
        );
        Dialog dialog = DialogDisplayer.getDefault().createDialog(descriptor);
        dialog.setVisible(true);
        panel.deactivate();
        if (descriptor.getValue() == panel.getAddButton()) {
            Library.Version selectedVersion = panel.getSelectedVersion();
            if (selectedVersion != null) {
// PENDING
//                addLibrary(selectedVersion);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        scrollPane = new javax.swing.JScrollPane();
        table = new javax.swing.JTable();
        addButton = new javax.swing.JButton();

        table.setModel(tableModel);
        scrollPane.setViewportView(table);

        org.openide.awt.Mnemonics.setLocalizedText(addButton, org.openide.util.NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.addButton.text")); // NOI18N
        addButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(scrollPane)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(addButton)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(addButton)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(scrollPane))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void addButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addButtonActionPerformed
        showSearchPanel();
    }//GEN-LAST:event_addButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JScrollPane scrollPane;
    private javax.swing.JTable table;
    // End of variables declaration//GEN-END:variables

    /**
     * Comparator of {@code Library.Version}s.
     */
    static class LibraryVersionComparator implements Comparator<Library.Version> {
        @Override
        public int compare(Library.Version o1, Library.Version o2) {
            String name1 = o1.getLibrary().getName();
            String name2 = o2.getLibrary().getName();
            return name1.compareTo(name2);
        }        
    }

    /**
     * Renderer of the version columns.
     */
    static class VersionColumnRenderer extends DefaultTableCellRenderer {
        final static Object UP_TO_DATE = new Object();
        final static Object CHECKING = new Object();
        final static Object UNKNOWN = new Object();
        final static Object NO_VERSION = new Object();

        @Override
        @NbBundle.Messages({
            "DependenciesPanel.version.unknown=Version information not available",
            "DependenciesPanel.version.checking=Checking ...",
            "DependenciesPanel.version.uptodate=Up to date",
            "DependenciesPanel.version.noversion=No version installed"
        })
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            String icon = null;
            String toolTip = null;
            if (value == UNKNOWN) {
                icon = "org/netbeans/modules/javascript/nodejs/ui/resources/unknown.png"; // NOI18N
                toolTip = Bundle.DependenciesPanel_version_unknown();
            } else if (value == CHECKING) {
                icon = "org/netbeans/modules/javascript/nodejs/ui/resources/checking.png"; // NOI18N
                toolTip = Bundle.DependenciesPanel_version_checking();
            } else if (value == UP_TO_DATE) {
                icon = "org/netbeans/modules/javascript/nodejs/ui/resources/uptodate.gif"; // NOI18N
                toolTip = Bundle.DependenciesPanel_version_uptodate();
            } else if (value == NO_VERSION) {
                icon = "org/netbeans/modules/javascript/nodejs/ui/resources/no-version.png"; // NOI18N
                toolTip = Bundle.DependenciesPanel_version_noversion();
            }
            super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            setHorizontalAlignment(CENTER);
            setToolTipText(toolTip);
            if (icon == null) {
                setIcon(null);
            } else {
                setText(null);
                setIcon(ImageUtilities.loadImageIcon(icon, false));
            }
            return this;
        }
        
    }

    /**
     * Model for the dependencies table.
     */
    class DependencyTableModel extends AbstractTableModel {

        @Override
        public int getRowCount() {
            return dependencies.size();
        }

        @Override
        public int getColumnCount() {
            return 4;
        }

        @Override
        @NbBundle.Messages({
            "DependenciesPanel.table.libraryColumn=Library",
            "DependenciesPanel.table.requiredVersionColumn=Required Version",
            "DependenciesPanel.table.installedVersionColumn=Installed Version",
            "DependenciesPanel.table.latestVersionColumn=Latest Version",
        })
        public String getColumnName(int column) {
            String columnName;
            switch (column) {
                case 0: columnName = Bundle.DependenciesPanel_table_libraryColumn(); break;
                case 1: columnName = Bundle.DependenciesPanel_table_requiredVersionColumn(); break;
                case 2: columnName = Bundle.DependenciesPanel_table_installedVersionColumn(); break;
                case 3: columnName = Bundle.DependenciesPanel_table_latestVersionColumn(); break;
                default: throw new IllegalArgumentException();
            }
            return columnName;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Library.Version libraryVersion = dependencies.get(rowIndex);
            Object value;
            switch (columnIndex) {
                case 0: value = libraryVersion.getLibrary().getName(); break;
                case 1: value = libraryVersion.getName(); break;
                case 2:
                    if (installedLibrariesSet) {
                        String libraryName = libraryVersion.getLibrary().getName();
                        Library.Version installedVersion = installedLibraries.get(libraryName);
                        if (installedVersion == null) {
                            value = VersionColumnRenderer.NO_VERSION;
                        } else {
                            value = installedVersion.getName();
                        }
                    } else {
                        value = VersionColumnRenderer.CHECKING;
                    }
                    break;
                case 3:
                    String libraryName = libraryVersion.getLibrary().getName();
                    Library library = dependencyInfo.get(libraryName);
                    if (library == null) {
                        value = dependencyInfo.containsKey(libraryName)
                                ? VersionColumnRenderer.UNKNOWN
                                : VersionColumnRenderer.CHECKING;
                    } else {
                        String latestVersion = library.getLatestVersion().getName();
                        value = latestVersion;
                        // PENDING VersionColumnRenderer.UP_TO_DATE
                    }
                    break;
                default: throw new IllegalArgumentException();
            }
            return value;
        }

    }

}
