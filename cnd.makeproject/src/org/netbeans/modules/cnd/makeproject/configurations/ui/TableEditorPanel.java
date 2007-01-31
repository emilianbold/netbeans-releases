/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.makeproject.configurations.ui;

import java.awt.Component;
import java.awt.Image;
import java.util.ResourceBundle;
import java.util.Vector;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.MakeArtifact;
import org.netbeans.modules.cnd.makeproject.api.remote.FilePathAdaptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem;
import org.netbeans.modules.cnd.makeproject.ui.utils.ListEditorPanel;
import org.netbeans.modules.cnd.api.utils.IpeUtils;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

public class TableEditorPanel extends ListEditorPanel {
    private static Image brokenProjectBadge = Utilities.loadImage( "org/netbeans/modules/cnd/makeproject/ui/resources/brokenProjectBadge.gif" ); // NOI18N

    private String baseDir;
    private JTable targetList;
    private MyTableCellRenderer myTableCellRenderer = new MyTableCellRenderer();

    public char getDownButtonMnemonics() {
        return getString("DOWN_OPTION_BUTTON_MN").charAt(0);
    }
    
    /*
    public TableEditorPanel(Object[] objects) {
	this(objects, null, null);
    }
    */

    public TableEditorPanel(Object[] objects, JButton[] extraButtons, String baseDir) {
	super(objects, extraButtons);
	this.baseDir = baseDir;
    }

    // Overrides ListEditorPanel
    public int getSelectedIndex() {
	int index = getTargetList().getSelectedRow();
	if (index >= 0 && index < listData.size())
	    return index;
	else
	    return 0;
    }

    protected void setSelectedIndex(int i) {
	getTargetList().getSelectionModel().setSelectionInterval(i, i);
    }

    protected void setData(Vector data) {
	getTargetList().setModel(new MyTableModel());
	// Set column sizes
	getTargetList().getColumnModel().getColumn(1).setPreferredWidth(100);
	getTargetList().getColumnModel().getColumn(1).setMaxWidth(200);
	//getTargetList().getColumnModel().getColumn(1).setResizable(true);
	getTargetList().getColumnModel().getColumn(2).setPreferredWidth(35);
	getTargetList().getColumnModel().getColumn(2).setMaxWidth(35);
	getTargetList().getColumnModel().getColumn(2).setResizable(false);
	//
	getTargetList().getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	getTargetList().getSelectionModel().addListSelectionListener(new TargetSelectionListener());
	// Left align table header
	((DefaultTableCellRenderer)getTargetList().getTableHeader().getDefaultRenderer()).setHorizontalAlignment(SwingConstants.LEFT);
    }

    private class TargetSelectionListener implements ListSelectionListener {
        public void valueChanged(ListSelectionEvent e) {
            if (e.getValueIsAdjusting())
                return;
            checkSelection();
        }
    }

    protected void ensureIndexIsVisible(int selectedIndex) {
	// FIXUP...
	//targetList.ensureIndexIsVisible(selectedIndex);
	//java.awt.Rectangle rect = targetList.getCellRect(selectedIndex, 0, true);
	//targetList.scrollRectToVisible(rect);
    }

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
	    //setTableHeader(null); // Hides table headers
	    if (getRowHeight() < 19)
		setRowHeight(19);
            getAccessibleContext().setAccessibleDescription(""); // NOI18N
            getAccessibleContext().setAccessibleName(""); // NOI18N
	}

	public boolean getShowHorizontalLines() {
	    return false;
	}

	public boolean getShowVerticalLines() {
	    return false;
	}

	public TableCellRenderer getCellRenderer(int row, int column) {
	    return myTableCellRenderer;
	}

	public TableCellEditor getCellEditor(int row, int col) {
	    //TableColumn col = getTargetList().getColumnModel().getColumn(1);
	    if (col == 0) {
		return super.getCellEditor(row, col);
	    }
	    else if (col == 1) {
		LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)listData.elementAt(row);
		Project project = projectItem.getProject(baseDir);
		if (project == null) {
		    return super.getCellEditor(row, col);
		}
		else {
		    MakeArtifact[] artifacts = MakeArtifact.getMakeArtifacts(project);
		    JComboBox comboBox = new JComboBox();
		    for (int i = 0; i < artifacts.length; i++)
			comboBox.addItem(new MakeArtifactWrapper(artifacts[i]));
		    return new DefaultCellEditor(comboBox);
		}
	    }
	    else {
		// col 2
		LibraryItem libraryItem = (LibraryItem)listData.elementAt(row);
		if (libraryItem instanceof LibraryItem.ProjectItem) {
		    LibraryItem.ProjectItem projectItem = (LibraryItem.ProjectItem)listData.elementAt(row);
		    JCheckBox checkBox = new JCheckBox();
		    checkBox.setSelected(((LibraryItem.ProjectItem)libraryItem).getMakeArtifact().getBuild());
		    return new DefaultCellEditor(checkBox);
		}
		else {
		    return super.getCellEditor(row, col);
		}
	    }
	}
    }

    class MakeArtifactWrapper {
	private MakeArtifact makeArtifact;
	public MakeArtifactWrapper(MakeArtifact makeArtifact) {
	    this.makeArtifact = makeArtifact;
	}

	public MakeArtifact getMakeArtifact() {
	    return makeArtifact;
	}

	public String toString() {
	    return getMakeArtifact().getConfigurationName();
	}
    }

    class MyTableCellRenderer extends DefaultTableCellRenderer {
	public Component getTableCellRendererComponent(JTable table, Object color, boolean isSelected, boolean hasFocus, int row, int col) {
	    JLabel label = (JLabel)super.getTableCellRendererComponent(table, color, isSelected, hasFocus, row, col);
	    Object element = listData.elementAt(row);
	    if (!(element instanceof LibraryItem)) {
		// FIXUP ERROR!
		label.setIcon(new ImageIcon(Utilities.loadImage("org/netbeans/modules/cnd/resources/blank.gif"))); // NOI18N
		label.setToolTipText("unknown"); // NOI18N
		return label;
	    }
	    LibraryItem libraryItem = (LibraryItem)element;
	    if (col == 0) {
		Image iconImage = Utilities.loadImage(libraryItem.getIconName());
		label.setToolTipText(libraryItem.getToolTip());
		if (libraryItem instanceof LibraryItem.ProjectItem && ((LibraryItem.ProjectItem)libraryItem).getProject(baseDir) == null) {
		    iconImage = Utilities.mergeImages(iconImage, brokenProjectBadge, 8, 0);
		    label.setToolTipText(getString("BROKEN") + label.getToolTipText());
		}
		label.setIcon(new ImageIcon(iconImage));
	    }
	    else if (col == 1) {
		label.setText(""); // NOI18N
		label.setIcon(null);
		label.setToolTipText(null);
		if (libraryItem instanceof LibraryItem.ProjectItem) {
		    label.setText(((LibraryItem.ProjectItem)libraryItem).getMakeArtifact().getConfigurationName());
		    label.setToolTipText(getString("CLICK_TO_CHANGE"));
		    if (((LibraryItem.ProjectItem)libraryItem).getProject(baseDir) == null) {
			label.setToolTipText(""); // NOI18N
		    }
		}
	    }
	    else {
		// col 2
		if (libraryItem instanceof LibraryItem.ProjectItem) {
		    JCheckBox checkBox = new JCheckBox();
		    checkBox.setSelected(((LibraryItem.ProjectItem)libraryItem).getMakeArtifact().getBuild());
		    checkBox.setBackground(label.getBackground());
		    return checkBox;
		}
		else {
		    label.setText(""); // NOI18N
		    label.setIcon(null);
		    label.setToolTipText(null);
		}
	    }
	    return label;
	}
    }
           

    class MyTableModel extends DefaultTableModel {
	private String[] columnNames = {"", getString("CONFIGURATION"), getString("BUILD")}; // NOI18N

	public String getColumnName(int col) {
	    return columnNames[col];
	}

        public int getColumnCount() {
            return 3;
        }

        public int getRowCount() {
            return listData.size();
        }

        public Object getValueAt(int row, int col) {
            return listData.elementAt(row);
        }

        public boolean isCellEditable(int row, int col) {
	    Object element = listData.elementAt(row);
	    LibraryItem libraryItem = (LibraryItem)element;
            if (col == 0) {
                return libraryItem.canEdit();
            }
	    else if (col == 1) {
		if (libraryItem instanceof LibraryItem.ProjectItem) {
		    if (((LibraryItem.ProjectItem)libraryItem).getProject(baseDir) != null)
			return true;
		    else
			return false;
		}
		else
		    return false;
            }
	    else {
		// col 2
		if (libraryItem instanceof LibraryItem.ProjectItem) 
		    return true;
		else
		    return false;
	    }
        }

        public void setValueAt(Object value, int row, int col) {
	    LibraryItem libraryItem = (LibraryItem)listData.elementAt(row);
            if (col == 0) {
                libraryItem.setValue((String)value);
		fireTableCellUpdated(row, col);
            }
	    else if (col == 1) {
		// FIXUP: should do a deep clone of the list
		MakeArtifact oldMakeArtifact = ((LibraryItem.ProjectItem)libraryItem).getMakeArtifact();
		boolean abs = IpeUtils.isPathAbsolute(oldMakeArtifact.getProjectLocation());
		listData.removeElementAt(row);
		MakeArtifact makeArtifact = ((MakeArtifactWrapper)value).getMakeArtifact();
                String projectLocation = makeArtifact.getProjectLocation();
                String workingDirectory = makeArtifact.getWorkingDirectory();
		if (!abs) {
		    // retain abs/rel paths...
                    projectLocation = IpeUtils.toRelativePath(baseDir, projectLocation);
                    workingDirectory = IpeUtils.toRelativePath(baseDir, workingDirectory);
		}
                makeArtifact.setProjectLocation(FilePathAdaptor.normalize(projectLocation));
                makeArtifact.setWorkingDirectory(FilePathAdaptor.normalize(workingDirectory));
		listData.add(row, new LibraryItem.ProjectItem(makeArtifact));
		// FIXUP
		fireTableCellUpdated(row, 0);
		fireTableCellUpdated(row, 1);
		fireTableCellUpdated(row, 2);
            }
	    else {
		// FIXUP: should do a deep clone of the list
		// col 2
		if (libraryItem instanceof LibraryItem.ProjectItem) {
		    MakeArtifact newMakeArtifact = (MakeArtifact)((LibraryItem.ProjectItem)libraryItem).getMakeArtifact().clone();
		    newMakeArtifact.setBuild(!newMakeArtifact.getBuild());
		    listData.removeElementAt(row);
		    listData.add(row, new LibraryItem.ProjectItem(newMakeArtifact));
		}
		fireTableCellUpdated(row, 0);
		fireTableCellUpdated(row, 1);
		fireTableCellUpdated(row, 2);
	    }
        }
    }
    
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(TableEditorPanel.class);
	}
	return bundle.getString(s);
    }
}
