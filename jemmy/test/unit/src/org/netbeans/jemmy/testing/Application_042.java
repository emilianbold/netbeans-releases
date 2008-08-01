package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_042 extends TestDialog {

    JComboBox editable;
    DefaultComboBoxModel editableModel;

    public Application_042() {
	super("Application_042");

	getContentPane().setLayout(new BorderLayout());

	String[] editable_contents = {"editable_one", "editable_two", "editable_three", "editable_four"};
	editableModel = new DefaultComboBoxModel(editable_contents);
	editable = new JComboBox(editableModel);
	editable.setEditable(true);
	editable.getEditor().addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent e) {
		editableModel.addElement(editable.getEditor().getItem());
	    }
	});

	getContentPane().add(editable, BorderLayout.NORTH);
	
	String[] non_editable_contents = {"non_editable_one", "non_editable_two", "non_editable_three", "non_editable_four"};
	JComboBox non_editable = new JComboBox(non_editable_contents);
	non_editable.setEditable(false);

	getContentPane().add(non_editable, BorderLayout.SOUTH);

        JMenuItem item00 = new JMenuItem("item00");
        JMenuItem item01 = new JMenuItem("item01");
        JMenuItem item10 = new JMenuItem("item10");
        JMenuItem item11 = new JMenuItem("item11");

        JMenu submenu00 = new JMenu("submenu00");
        submenu00.add(item00);
        JMenu submenu01 = new JMenu("submenu01");
        submenu01.add(item01);
        JMenu submenu10 = new JMenu("submenu10");
        submenu10.add(item10);
        JMenu submenu11 = new JMenu("submenu11");
        submenu11.add(item11);

        JMenu menu0 = new JMenu("menu0");
        menu0.add(submenu00);
        menu0.add(submenu01);

        JMenu menu1 = new JMenu("menu1");
        menu1.add(submenu10);
        menu1.add(submenu11);

        JMenuBar bar = new JMenuBar();
        bar.add(menu0);
        bar.add(menu1);

        setJMenuBar(bar);

	setSize(200, 200);

        setModal(true);
    }

    public static void main(String[] argv) {
        try {
            new Thread(new Runnable() {
                    public void run() {
                        (new Application_042()).show();
                    }
                }).start();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }
}
