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

package org.netbeans.modules.javascript.libraries.ui;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.libraries.Library;
import org.netbeans.api.project.libraries.LibraryChooser;
import org.netbeans.api.project.libraries.LibraryManager;
import org.netbeans.modules.javascript.libraries.util.JSLibraryData;
import org.netbeans.modules.javascript.libraries.util.JSLibraryProjectUtils;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;

/**
 *
 * @author Quy Nguyen <quynguyen@netbeans.org>
 */
final class AddLibraryTableModel extends AbstractTableModel implements PropertyChangeListener {
    private static final int COLUMN_COUNT = 4;
    
    private final List<LibraryRowModel> tableRows;
    private final LibraryManager manager;
    private final LibraryChooser.Filter filter;
    private final Project project;
    private final Object lock = new Object();
    
    public AddLibraryTableModel(Project project, LibraryChooser.Filter filter) {
        this.project = project;
        this.manager = JSLibraryProjectUtils.getLibraryManager(project);
        this.tableRows = new ArrayList<LibraryRowModel>();
        this.filter = filter;
        init();
    }
    
    private void init() {
        synchronized(tableRows) {
            List<Library> libs = getFilteredLibraries();
            for (Library library : libs) {
                tableRows.add(new LibraryRowModel(library));
                
            }
        }
        manager.addPropertyChangeListener(WeakListeners.propertyChange(this, manager));
    }
    
    private List<Library> getFilteredLibraries() {
        List<Library> filteredLibs = new ArrayList<Library>();
        Library[] allLibraries = manager.getLibraries();
        
        for (Library library : allLibraries) {
            if (filter.accept(library)) {
                filteredLibs.add(library);
            }
        }
        
        return filteredLibs;
    }
    
    public int getRowCount() {
        return tableRows.size();
    }

    public int getColumnCount() {
        return COLUMN_COUNT;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        LibraryRowModel row = tableRows.get(rowIndex);
        
        switch (columnIndex) {
            case 0:
                return Boolean.valueOf(row.isSelected());
            case 1:
                return row.getLibraryDisplayName();
            case 2:
                return row.getDestination();
            default:
                return row;
        }
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        if (columnIndex == 0) {
            return true;
        } else {
            boolean selected = tableRows.get(rowIndex).isSelected();
            return selected && (columnIndex == 2 || columnIndex == 3);
        }
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Boolean.class;
            default:
                return Object.class;
        }
    }
    
    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        LibraryRowModel row = tableRows.get(rowIndex);
        if (columnIndex == 0) {
            assert aValue instanceof Boolean;
            row.setSelected(((Boolean)aValue).booleanValue());
        } else {
            row.setDestination((String)aValue);
        }
    }

    @Override
    public String getColumnName(int column) {
        ResourceBundle bundle = NbBundle.getBundle(AddLibraryTableModel.class);
        switch (column) {
            case 0:
                return bundle.getString("AddLibraryPanel_AddLabel");
            case 1:
                return bundle.getString("AddLibraryPanel_NameLabel");
            case 2:
                return bundle.getString("AddLibraryPanel_LocationLabel");
            default:
                return "";
        }
    }
    
    public void propertyChange(PropertyChangeEvent evt) {
        if (evt.getPropertyName().equals(LibraryManager.PROP_LIBRARIES)) {
            synchronized(lock) {
                List<LibraryRowModel> oldRows = new ArrayList<LibraryRowModel>();
                oldRows.addAll(tableRows);
                tableRows.clear();
                
                List<Library> allLibraries = getFilteredLibraries();
                for (Library library : allLibraries) {
                    boolean found = false;
                    for (LibraryRowModel rowModel : oldRows) {
                        if (rowModel.getLibrary().equals(library)) {
                            found = true;
                            tableRows.add(rowModel);
                        }

                        if (!found) {
                            tableRows.add(new LibraryRowModel(library));
                        }
                    }                    
                }
            }
            
            fireTableDataChanged();
        }
    }
    
    public List<JSLibraryData> getSelectedLibraries() {
        ArrayList<JSLibraryData> selectedLibraries = new ArrayList<JSLibraryData>();
        
        for (LibraryRowModel model : tableRows) {
            if (model.isSelected()) {
                selectedLibraries.add(new JSLibraryData(model.getLibrary().getName(), model.getDestination()));
            }
        }
        
        return selectedLibraries;
    }
    
    public LibraryRowModel getLibraryModel(int row) {
        return tableRows.get(row);
    }
    
    final class LibraryRowModel {
        private final Library library;
        private boolean selected;
        private String destination;
        private boolean init;
        private boolean hasError;
        
        public LibraryRowModel(Library library) {
            this.library = library;
            this.destination = "";
            this.selected = false;
            this.init = false;
            this.hasError = false;
        }

        public String getDestination() {
            return destination;
        }

        public Library getLibrary() {
            return library;
        }

        public String getLibraryDisplayName() {
            String baseDisplayName = library.getDisplayName();
            if (hasError) {
                baseDisplayName = "<html><font color=\"#A40000\"><b>" + baseDisplayName + "</b></font></html>";
            }
            
            return baseDisplayName;
        }
        
        public boolean isSelected() {
            return selected;
        }

        public void setError(boolean error) {
            if (error != hasError) {
                fireCellUpdate(1);
                this.hasError = error;
            }
        }
        
        private int getRowInTable() {
            synchronized(lock) {
                for (int i = 0; i < tableRows.size(); i++) {
                    if (tableRows.get(i) == this) {
                        return i;
                    }
                }
            }
            
            return -1;
        }
        
        public void setDestination(String destination) {
            init = true;
            this.destination = destination;
            fireCellUpdate(2);
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
            if (selected == true && !init) {
                destination = JSLibraryProjectUtils.getDefaultRelativeLibraryPath(project, library);
            }
            fireCellUpdate(0);
            fireCellUpdate(2);
            fireCellUpdate(3);
        }
        
        private void fireCellUpdate(final int column) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    int row = getRowInTable();
                    if (row != -1) {
                        fireTableCellUpdated(row, column);
                    }
                }
            });
        }
    }
       
}
