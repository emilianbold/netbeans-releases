package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;

public class Application_032 extends TestFrame {
    JButton buttons;
    Application_032_Buttons bttf;
    JButton menus;
    Application_032_Menus mns;
    JButton lists;
    Application_032_Lists lsts;
    JButton texts;
    Application_032_Texts tstx;
    public Application_032() {
	super("Application_032");

	getContentPane().setLayout(new FlowLayout());

	bttf = new Application_032_Buttons();	
	buttons = new JButton("Buttons");
	buttons.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    bttf.show();
		}
	    });
	getContentPane().add(buttons);

	mns = new Application_032_Menus();	
	menus = new JButton("Menus");
	menus.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    mns.show();
		}
	    });
	getContentPane().add(menus);

	lsts = new Application_032_Lists();	
	lists = new JButton("Lists");
	lists.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    lsts.show();
		}
	    });
	getContentPane().add(lists);

	tstx = new Application_032_Texts();	
	texts = new JButton("Texts");
	texts.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    tstx.show();
		}
	    });
	getContentPane().add(texts);

	setSize(100, 200);
	show();
    }

    public static void main(String[] agrv) {
	new Application_032();
    }
    
    class Application_032_Buttons extends TestFrame {
	public Application_032_Buttons() {
	    super("Application_032 Buttons");
	    try {
		Class[] classes = {
		    Class.forName("javax.swing.JButton"), 
		    Class.forName("javax.swing.JCheckBox"), 
		    Class.forName("javax.swing.JRadioButton"), 
		    Class.forName("javax.swing.JToggleButton"),
		    Class.forName("javax.swing.JMenuItem")};
		Class[] param_classes = {
		    Class.forName("java.lang.String")};
		
		getContentPane().setLayout(new FlowLayout());
		for(int i = 0; i < classes.length; i++) {
		    String[] params = {classes[i].getName()};
		    getContentPane().
			add((Component)classes[i].
			    getConstructor(param_classes).
			    newInstance(params));
		}
		setSize(300, 300);
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	}
    }	

    class Application_032_Menus extends TestFrame {
	public Application_032_Menus() {
	    super("Application_032 Menus");

	    JMenuItem menuItem = new JMenuItem("JMenuItem");
	    JMenu menu = new JMenu("JMenu");
	    menu.add(menuItem);
	    JMenuBar menuBar = new JMenuBar();
	    menuBar.add(menu);
	    setJMenuBar(menuBar);
	    setSize(300, 300);
	}
    }	

    class Application_032_Lists extends TestFrame {
	public Application_032_Lists() {
	    super("Application_032 Lists");

	    getContentPane().setLayout(new BorderLayout(5, 5));
	    
	    JPanel listPane = new JPanel();
	    listPane.setLayout(new GridLayout(3, 1));

	    DefaultMutableTreeNode node000 = new DefaultMutableTreeNode();
	    node000.setUserObject("node000");

	    DefaultMutableTreeNode node00 = new DefaultMutableTreeNode();
	    node00.setUserObject("node00");
	    node00.insert(node000, 0);

	    DefaultMutableTreeNode node0 = new DefaultMutableTreeNode();
	    node0.setUserObject("node0");
	    node0.insert(node00, 0);

	    JTree trr = new JTree(node0);
	    trr.setSelectionRow(1);
	    listPane.add(trr);

	    String[] tableColumns = new String[4];
	    String[][] tableItems = new String[4][4];
	    for(int i = 0; i < tableColumns.length; i++) {
		tableColumns[i] = Integer.toString(i);
		for(int j = 0; j < tableItems[i].length; j++) {
		    tableItems[j][i] = "table_" + Integer.toString(i) + Integer.toString(j);
		}
	    }
	    
	    JTable tbl = new JTable(tableItems, tableColumns);
	    tbl.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
	    tbl.getSelectionModel().setSelectionInterval(1, 1);
	    listPane.add(tbl);

	    String[] listItems = {"list_0", "list_1", "list_2", "list_3"};
	    JList lst = new JList(listItems);
	    lst.setSelectedIndex(1);
	    listPane.add(lst);

	    String[] combo_contents = {"combo_0", "combo_1", "combo_2", "combo_3"};
	    DefaultComboBoxModel comboModel = new DefaultComboBoxModel(combo_contents);

	    getContentPane().add(new JComboBox(comboModel), BorderLayout.NORTH);
	    getContentPane().add(listPane, BorderLayout.CENTER);

	    setSize(300, 300);
	}
    }

    class Application_032_Texts extends TestFrame {
	public Application_032_Texts() {
	    super("Application_032 Buttons");
	    try {
		Class[] classes = {
		    Class.forName("javax.swing.JTextField"), 
		    Class.forName("javax.swing.JTextArea"), 
		    Class.forName("javax.swing.JLabel")};
		Class[] param_classes = {
		    Class.forName("java.lang.String")};
		
		getContentPane().setLayout(new FlowLayout());
		for(int i = 0; i < classes.length; i++) {
		    String[] params = {classes[i].getName()};
		    getContentPane().
			add((Component)classes[i].
			    getConstructor(param_classes).
			    newInstance(params));
		}
		getContentPane().add(new JEditorPane("text", 
						     "javax.swing.JEditorPane"));
		setSize(300, 300);
	    } catch(Exception e) {
		e.printStackTrace();
	    }
	}
    }	
}
