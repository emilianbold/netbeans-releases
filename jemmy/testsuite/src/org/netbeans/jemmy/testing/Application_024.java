package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

public class Application_024 extends TestFrame {

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
	JTable tbl = new JTable(tableItems, tableColumns);
	tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

	tp.add("Table Page", new JScrollPane(tbl));

	//////////////////////////////////////////////////////////////////////
	//tree
	//////////////////////////////////////////////////////////////////////
	DefaultMutableTreeNode[][] subnodes = new DefaultMutableTreeNode[50][50];
	DefaultMutableTreeNode[] nodes = new DefaultMutableTreeNode[50];
	DefaultMutableTreeNode root = new DefaultMutableTreeNode();
	root.setUserObject("-1");
	for(int i = 0; i < nodes.length; i++) {
	    nodes[i] = new DefaultMutableTreeNode();
	    nodes[i].setUserObject(Integer.toString(i));
	    for(int j = 0; j < subnodes[i].length; j++) {
		subnodes[i][j] = new DefaultMutableTreeNode();
		subnodes[i][j].setUserObject(Integer.toString(i) + Integer.toString(j));
		nodes[i].insert(subnodes[i][j], j);
	    }
	    root.insert(nodes[i], i);
	}
	JTree tr = new JTree(root);
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
	for(int i = 0; i < listItems.length; i++) {
	    for(int j = 0; j < subnodes[i].length; j++) {
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
	(new Application_024()).show();
    }
}
