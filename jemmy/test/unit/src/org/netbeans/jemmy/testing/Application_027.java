package org.netbeans.jemmy.testing;

import java.awt.*;
import java.util.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;

public class Application_027 extends TestFrame {
    
    String[] tableColumns;

    public Application_027() {
	super("Application_027");

	JTabbedPane tp = new JTabbedPane();

	//////////////////////////////////////////////////////////////////////
	//table
	//////////////////////////////////////////////////////////////////////
	tableColumns = new String[5];
	String[][] tableItems = new String[5][5];
	for(int i = 0; i < tableColumns.length; i++) {
	    tableColumns[i] = Integer.toString(i);
	    for(int j = 0; j < tableItems[i].length; j++) {
		tableItems[j][i] = Integer.toString(i) + Integer.toString(j);
	    }
	}
	JTable tbl = new JTable(tableItems, tableColumns);
	tbl.setCellSelectionEnabled(true);
	tbl.setRowHeight(tbl.getRowHeight() * 2);

	TableCellRenderer renderer = new TableCellRenderer() {
		public Component getTableCellRendererComponent(JTable table, 
							       Object value, 
							       boolean isSelected, 
							       boolean cellHasFocus,
							       int row,
							       int column) {
		    JPanel res = new JPanel();
		    if(isSelected) {
			res.setBorder(new BevelBorder(BevelBorder.LOWERED));
			res.add(new JLabel("!" + value.toString() + "!"));
		    } else {
			res.add(new JLabel(value.toString()));
		    }
		    return(res);
		}
	    };

	TableCellEditor editor = new TableDualComboEditor();

	for(int i = 0; i < tableColumns.length; i++) {
	    tbl.getColumnModel().getColumn(i).setCellRenderer(renderer);
	    tbl.getColumnModel().getColumn(i).setCellEditor(editor);
	}

	tp.add("Table Page", new JScrollPane(tbl));

	//////////////////////////////////////////////////////////////////////
	//tree
	//////////////////////////////////////////////////////////////////////
	DefaultMutableTreeNode[][] subnodes = new DefaultMutableTreeNode[5][5];
	DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[5];
	DefaultMutableTreeNode root = new DefaultMutableTreeNode();
	root.setUserObject("00");
	for(int i = 0; i < nodes.length; i++) {
	    nodes[i] = new DefaultMutableTreeNode();
	    nodes[i].setUserObject(Integer.toString(i) + "0");
	    for(int j = 0; j < subnodes[i].length; j++) {
		subnodes[i][j] = new DefaultMutableTreeNode();
		subnodes[i][j].setUserObject(Integer.toString(i) + Integer.toString(j));
		nodes[i].insert(subnodes[i][j], j);
	    }
	    root.insert(nodes[i], i);
	}
	JTree tr = new JTree(root);

	TreeCellRenderer treeRenderer = new TreeCellRenderer() {
		public Component getTreeCellRendererComponent(JTree tree, 
							      Object value, 
							      boolean isSelected, 
							      boolean isExpanded, 
							      boolean isLeaf, 
							      int row,
							      boolean cellHasFocus) {
		    JPanel res = new JPanel();
		    if(isSelected) {
			res.setBorder(new BevelBorder(BevelBorder.LOWERED));
			res.add(new JLabel("!" + value.toString() + "!"));
		    } else {
			res.add(new JLabel(value.toString()));
		    }
		    return(res);
		}
	    };

	tr.setCellRenderer(treeRenderer);
	tr.setCellEditor(new TreeDualComboEditor());

	tr.setEditable(true);
	tp.add("Tree Page", new JScrollPane(tr));

	//////////////////////////////////////////////////////////////////////
	//list
	//////////////////////////////////////////////////////////////////////
	String[] listItems = new String[5];
	for(int i = 0; i < listItems.length; i++) {
	    listItems[i] = Integer.toString(i);
	}
	JList list = new JList(listItems);
	list.setCellRenderer(new ListCellRenderer() {
		public Component getListCellRendererComponent(JList list, 
							      Object value, 
							      int index, 
							      boolean isSelected, 
							      boolean cellHasFocus) {
		    JPanel res = new JPanel();
		    if(isSelected) {
			res.setBorder(new BevelBorder(BevelBorder.LOWERED));
			res.add(new JLabel("!" + value.toString() + "!"));
		    } else {
			res.add(new JLabel(value.toString()));
		    }
		    return(res);
		}
	    });

	tp.add("List Page", new JScrollPane(list));

	getContentPane().add(tp);

	setSize(600, 400);
    }

    public static void main(String[] argv) {
	(new Application_027()).show();
    }


    class TableDualComboEditor implements TableCellEditor {
	JComboBox fCombo, sCombo;
	JPanel res;
	int row, column;
	JTable tbl;
	Vector lists;
	public TableDualComboEditor() {
	    res = new JPanel();
	    fCombo = new JComboBox(tableColumns);
	    sCombo = new JComboBox(tableColumns);
	    res.add(fCombo);
	    res.add(sCombo);
	    lists = new Vector();
	}
	public Component getTableCellEditorComponent(JTable table, 
						     Object value, 
						     boolean isSelected, 
						     int row, 
						     int column) {
	    this.row = row;
	    this.column = column;
	    this.tbl = table;
	    fCombo.setSelectedIndex(new Integer(value.toString().substring(0, 1)).intValue());
	    sCombo.setSelectedIndex(new Integer(value.toString().substring(1)).intValue());
	    return(res);
	}
	public void addCellEditorListener(CellEditorListener l) {
	    lists.add(l);
	};
	public void cancelCellEditing() {
	    res.setVisible(false);
	    for(int i = 0; i < lists.size(); i++) {
		((CellEditorListener)lists.get(i)).editingCanceled(new ChangeEvent(tbl));
	    }
	}
	public Object getCellEditorValue() {
	    return(Integer.toString(fCombo.getSelectedIndex()) +
		   Integer.toString(sCombo.getSelectedIndex()));
	}
	public boolean isCellEditable(EventObject anEvent) {
	    return(anEvent instanceof MouseEvent &&
		   ((MouseEvent)anEvent).getClickCount() > 1);
	}
	public void removeCellEditorListener(CellEditorListener l) {
	    lists.remove(l);
	} 
	public boolean shouldSelectCell(EventObject anEvent) {
	    return(true);
	}
	public boolean stopCellEditing() {
	    tbl.getModel().setValueAt(getCellEditorValue(),
				      row,
				      column);
	    for(int i = 0; i < lists.size(); i++) {
		((CellEditorListener)lists.get(i)).editingStopped(new ChangeEvent(tbl));
	    }
	    return(true);
	}
    }

    class TreeDualComboEditor implements TreeCellEditor {
	JComboBox fCombo, sCombo;
	JPanel res;
	TreePath path;
	JTree tree;
	Vector lists;
	public TreeDualComboEditor() {
	    res = new JPanel();
	    fCombo = new JComboBox(tableColumns);
	    fCombo.setPopupVisible(false);
	    sCombo = new JComboBox(tableColumns);
	    sCombo.setPopupVisible(false);
	    res.add(fCombo);
	    res.add(sCombo);
	    lists = new Vector();
	}
	public Component getTreeCellEditorComponent(JTree tree, 
						    Object value, 
						    boolean isSelected, 
						    boolean isExpanded, 
						    boolean isLeaf, 
						    int row) {
	    this.path = tree.getPathForRow(row);
	    this.tree = tree;
	    fCombo.setSelectedIndex(new Integer(value.toString().substring(0, 1)).intValue());
	    sCombo.setSelectedIndex(new Integer(value.toString().substring(1)).intValue());
	    res.setVisible(true);
	    return(res);
	}
	public void addCellEditorListener(CellEditorListener l) {
	    lists.add(l);
	};
	public void cancelCellEditing() {
	    res.setVisible(false);
	    ((DefaultMutableTreeNode)path.getLastPathComponent()).
		setUserObject(getCellEditorValue());
	    for(int i = 0; i < lists.size(); i++) {
		((CellEditorListener)lists.get(i)).editingCanceled(new ChangeEvent(tree));
	    }
	}
	public Object getCellEditorValue() {
	    return(Integer.toString(fCombo.getSelectedIndex()) +
		   Integer.toString(sCombo.getSelectedIndex()));
	}
	public boolean isCellEditable(EventObject anEvent) {
	    return(anEvent instanceof MouseEvent &&
		   ((MouseEvent)anEvent).getClickCount() > 1);
	}
	public void removeCellEditorListener(CellEditorListener l) {
	    lists.remove(l);
	} 
	public boolean shouldSelectCell(EventObject anEvent) {
	    return(true);
	}
	public boolean stopCellEditing() {
	    ((DefaultMutableTreeNode)path.getLastPathComponent()).
		setUserObject(getCellEditorValue());
	    for(int i = 0; i < lists.size(); i++) {
		((CellEditorListener)lists.get(i)).editingStopped(new ChangeEvent(tree));
	    }
	    return(true);
	}
    }

}
