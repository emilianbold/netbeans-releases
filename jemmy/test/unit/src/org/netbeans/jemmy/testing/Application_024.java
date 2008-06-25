package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

public class Application_024 extends TestFrame {

    JTable tbl;

    public Application_024() {
	super("Application_024");

	JTabbedPane tp = new JTabbedPane();

	//////////////////////////////////////////////////////////////////////
	//table
	//////////////////////////////////////////////////////////////////////
	String[] tableColumns = new String[50];
	String[][] tableItems = new String[50][50];
	for(int i = 0; i < tableColumns.length; i++) {
	    tableColumns[i] = Integer.toString(i);
	    for(int j = 0; j < tableItems[i].length; j++) {
		tableItems[j][i] = Integer.toString(i) + Integer.toString(j);
	    }
	}
        tableItems[0][1] = null;
        tableItems[1][0] = null;
        tableItems[3][2] = null;
	tbl = new JTable(tableItems, tableColumns);
	tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	tp.add("Table Page", new JScrollPane(tbl));

	//////////////////////////////////////////////////////////////////////
	//tree
	//////////////////////////////////////////////////////////////////////
	DefaultMutableTreeNode root = new DefaultMutableTreeNode("-1");
	DefaultMutableTreeNode node = null;
        DefaultTreeModel model = new DefaultTreeModel(root);
        JTree tr = new JTree(root);
        tr.setModel(model);
	for(int i = 0; i < 50; i++) {
	    node = new DefaultMutableTreeNode(Integer.toString(i));
            model.insertNodeInto(node, root, i);
	    for(int j = 0; j < 50; j++) {
                model.insertNodeInto(new DefaultMutableTreeNode(Integer.toString(i) + Integer.toString(j)), node, j);
            }
        }
        tr.expandRow(0);
	tr.setEditable(true);
	tp.add("Tree Page", new JScrollPane(tr));

	//////////////////////////////////////////////////////////////////////
	//list
	//////////////////////////////////////////////////////////////////////
	String[] listItems = new String[50];
	for(int i = 0; i < listItems.length; i++) {
	    listItems[i] = Integer.toString(i);
	}
	tp.add("List Page", new JScrollPane(new JList(listItems)));

	//////////////////////////////////////////////////////////////////////
	//text
	//////////////////////////////////////////////////////////////////////
	String text = "";
	for(int i = 0; i < 50; i++) {
	    for(int j = 0; j < 50; j++) {
		text = text + Integer.toString(i) + Integer.toString(j);
	    }
	    text = text + "\n";
	}
	text = text.substring(0, text.length() - 1);
	tp.add("Text Page", new JScrollPane(new JTextArea(text)));

	getContentPane().add(tp);

	setSize(400, 400);
    }

    public static void main(String[] argv) {
        Application_024 app = new Application_024();
        app.show();
        System.out.println(app.tbl.getValueAt(1, 0));
        System.out.println(app.tbl.getValueAt(1, 0));
    }
}
