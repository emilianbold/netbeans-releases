/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import org.netbeans.modules.cnd.makeproject.ui.utils.ListEditorPanel;
import org.netbeans.modules.cnd.makeproject.packaging.InfoElement;
import org.openide.util.NbBundle;

public class PackagingHeaderPanel extends ListEditorPanel {

    private String baseDir;
    private JTable targetList;
    private MyTableCellRenderer myTableCellRenderer = new MyTableCellRenderer();
    private JButton addButton;
    private JTextArea docArea;

    public PackagingHeaderPanel(List<InfoElement> infoList, String baseDir) {
        super(infoList.toArray(), new JButton[]{new JButton()});
        this.baseDir = baseDir;
        this.addButton = extraButtons[0];

        addButton.setText("Add [Empty]");
        addButton.addActionListener(new AddButtonAction());

        getEditButton().setVisible(false);
        getDefaultButton().setVisible(false);
    }
    
    public void setDocArea(JTextArea docArea) {
        this.docArea = docArea;
    }

    class AddButtonAction implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            addObjectAction(new InfoElement("", "")); // FIXUP
        }
    }

    @Override
    public Object copyAction(Object o) {
        InfoElement elem = (InfoElement) o;
        return new InfoElement(new String(elem.getName()), new String(elem.getValue()));
    }

    @Override
    public String getCopyButtonText() {
        return "Duplicate";
    }

    @Override
    public String getListLabelText() {
        return "Files:";
    }

    // Overrides ListEditorPanel
    @Override
    public int getSelectedIndex() {
        int index = getTargetList().getSelectedRow();
        if (index >= 0 && index < listData.size()) {
            return index;
        } else {
            return 0;
        }
    }

    @Override
    protected void setSelectedIndex(int i) {
        getTargetList().getSelectionModel().setSelectionInterval(i, i);
    }

    @Override
    protected void setData(Vector data) {
        getTargetList().setModel(new MyTableModel());
        // Set column sizes
        getTargetList().getColumnModel().getColumn(0).setPreferredWidth(200);
        getTargetList().getColumnModel().getColumn(0).setMaxWidth(200);
//	//getTargetList().getColumnModel().getColumn(1).setResizable(true);
//	getTargetList().getColumnModel().getColumn(2).setPreferredWidth(40);
//	getTargetList().getColumnModel().getColumn(2).setMaxWidth(40);
//	getTargetList().getColumnModel().getColumn(2).setResizable(false);
        //
        getTargetList().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        getTargetList().getSelectionModel().addListSelectionListener(new TargetSelectionListener());
        // Left align table header
        ((DefaultTableCellRenderer) getTargetList().getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    private class TargetSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting()) {
                return;
            }
            checkSelection();
        }
    }

    @Override
    protected void ensureIndexIsVisible(int selectedIndex) {
        // FIXUP...
        //targetList.ensureIndexIsVisible(selectedIndex);
        //java.awt.Rectangle rect = targetList.getCellRect(selectedIndex, 0, true);
        //targetList.scrollRectToVisible(rect);
    }

    @Override
    protected Component getViewComponent() {
        return getTargetList();
    }

    private JTable getTargetList() {
        if (targetList == null) {
            targetList = new MyTable();
            setData(null);
        }
        return targetList;
    }

    class MyTable extends JTable {

        public MyTable() {
//	    //setTableHeader(null); // Hides table headers
//	    if (getRowHeight() < 19)
//		setRowHeight(19);
            getAccessibleContext().setAccessibleDescription(""); // NOI18N
            getAccessibleContext().setAccessibleName(""); // NOI18N
            
            getSelectionModel().addListSelectionListener(new MyListSelectionListener());
        }

        @Override
        public boolean getShowHorizontalLines() {
            return false;
        }

        @Override
        public boolean getShowVerticalLines() {
            return false;
        }

        @Override
        public TableCellRenderer getCellRenderer(int row, int column) {
            return myTableCellRenderer;
        }        //        @Override
//	public TableCellEditor getCellEditor(int row, int col) {
//	    //TableColumn col = getTargetList().getColumnModel().getColumn(1);
//	    if (col == 0) {
//		return super.getCellEditor(row, col);
//	    }
//	    else if (col == 1) {
//		LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)listData.elementAt(row);
//		Project project = projectItem.getProject(baseDir);
//		if (project == null) {
//		    return super.getCellEditor(row, col);
//		}
//		else {
//		    MakeArtifact[] artifacts = MakeArtifact.getMakeArtifacts(project);
//		    JComboBox comboBox = new JComboBox();
//		    for (int i = 0; i < artifacts.length; i++)
//			comboBox.addItem(new MakeArtifactWrapper(artifacts[i]));
//		    return new DefaultCellEditor(comboBox);
//		}
//	    }
//	    else {
//		// col 2
//		LibraryItem libraryItem = (LibraryItem)listData.elementAt(row);
//		if (libraryItem instanceof LibraryItem.ProjectItem) {
//		    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)listData.elementAt(row);
//		    JCheckBox checkBox = new JCheckBox();
//		    checkBox.setSelected(((LibraryItem.ProjectItem)libraryItem).getMakeArtifact().getBuild());
//		    return new DefaultCellEditor(checkBox);
//		}
//		else {
//		    return super.getCellEditor(row, col);
//		}
//	    }
//	}
    }
    
    public void refresh() {
        updateDoc();
    }
    
    class MyListSelectionListener implements ListSelectionListener {

        public void valueChanged(ListSelectionEvent arg0) {
            updateDoc();
        }
    }
    
    public void updateDoc() {
        if (docArea == null) {
            return;
        }
        docArea.setText("");

        int i = targetList.getSelectedRow();
        if (listData.size() == 0 || i < 0 || i >= listData.size()) {
            return;
        }
        
        InfoElement elem = (InfoElement)listData.get(i);
        if (elem.getName().equals("ARCH")) {
            docArea.setText(getString("PACKAGING_ARCH_DOC"));
        }
        else if (elem.getName().equals("CATEGORY")) {
            docArea.setText(getString("PACKAGING_CATEGORY_DOC"));
        }
        else if (elem.getName().equals("NAME")) {
            docArea.setText(getString("PACKAGING_NAME_DOC"));
        }
        else if (elem.getName().equals("PKG")) {
            docArea.setText(getString("PACKAGING_PKG_DOC"));
        }
        else if (elem.getName().equals("VERSION")) {
            docArea.setText(getString("PACKAGING_VERSION_DOC"));
        }
    }

    class MyTableCellRenderer extends DefaultTableCellRenderer {

        @Override
        public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int col) {
            JLabel label = (JLabel) super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, col);
            InfoElement elem = (InfoElement) listData.elementAt(row);
            if (col == 0) {
            } else if (col == 1) {
                if (!isSelected) {
                    label = new JLabel();
                }
//                label.setToolTipText(file.getAbsolutePath());
                if (!isSelected && elem.getValue().indexOf('<') >= 0) {
                    label.setForeground(Color.RED);
                }
                label.setText(elem.getValue());
            }
            return label;
        }
    }

    class MyTableModel extends DefaultTableModel {

        private String[] columnNames = {"Name", "Value"}; // FIXUP

        @Override
        public String getColumnName(int col) {
            return columnNames[col];
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public int getRowCount() {
            return listData.size();
        }

        @Override
        public Object getValueAt(int row, int col) {
//            return listData.elementAt(row);
            InfoElement elem = (InfoElement) listData.elementAt(row);
            if (col == 0) {
                return elem.getName();
            }
            if (col == 1) {
                return elem.getValue();
            }
            assert false;
            return null;
        }

        @Override
        public boolean isCellEditable(int row, int col) {
            return true;
        }

        @Override
        public void setValueAt(Object value, int row, int col) {
            InfoElement elem = (InfoElement) listData.elementAt(row);
            if (col == 0) {
                elem.setName((String) value);
            } else if (col == 1) {
                elem.setValue((String) value);
            } else {
                assert false;
            }
        }
    }
    /** Look up i18n strings here */
    private static ResourceBundle bundle;

    private static String getString(String s) {
        if (bundle == null) {
            bundle = NbBundle.getBundle(PackagingHeaderPanel.class);
        }
        return bundle.getString(s);
    }
}
