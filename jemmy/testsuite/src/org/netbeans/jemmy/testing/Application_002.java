package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_002 extends TestFrame {

    JLabel buttonLabel;
    JLabel menuLabel;

    public Application_002() {
	super("Application_002");

	getContentPane().setLayout(new FlowLayout());

	JButton button = new JButton("button");
	buttonLabel = new JLabel("Button has not been pushed yet");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    buttonLabel.setText("Button has been pushed");
		}
	    });
	
	getContentPane().add(button);
	getContentPane().add(buttonLabel);

	JTextField field = new JTextField("Text has not been typed yet");

	getContentPane().add(field);

	MyMenuItem menuItem = new MyMenuItem("menuItem");
	menuLabel = new JLabel("Menu has not been pushed yet");
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    menuLabel.setText("Menu \"menu/menuItem\" has been pushed");
		}
	    });

	MyMenu subsubmenu = new MyMenu("subsubmenu");
	subsubmenu.add(menuItem);
	MyMenu subsubmenu2 = new MyMenu("subsubmenu2");
        subsubmenu2.setEnabled(false);

	MyMenu submenu = new MyMenu("submenu");
	submenu.add(subsubmenu);
	submenu.add(subsubmenu2);

	MyMenu menu = new MyMenu("menu");
	menu.add(submenu);

	MyMenuItem menu0Item = new MyMenuItem("menu0Item");
	menuLabel = new JLabel("Menu has not been pushed yet");
	menu0Item.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    menuLabel.setText("Menu \"menu/menuItem\" has been pushed");
		}
	    });

	MyMenu menu0 = new MyMenu("menu0");
	menu0.add(menu0Item);

	MyMenuItem menu1Item = new MyMenuItem("menu1Item");
	menu1Item.addActionListener(new ActionListener() {
	    public void actionPerformed(ActionEvent event) {
		menuLabel.setText("Menu \"menu1Item\" has been pushed");
	    }
	});

	MyMenuBar menuBar = new MyMenuBar();
	menuBar.add(menu);
	menuBar.add(menu0);
	menuBar.add(menu1Item);

	setJMenuBar(menuBar);

	getContentPane().add(menuLabel);

	setSize(200, 200);
    }

    public static void main(String[] argv) {
	(new Application_002()).show();
    }

    private class MyMenuItem extends JMenuItem{
	public MyMenuItem(String text) {
	    super(text);
	}
    }

    private class MyMenu extends JMenu{
	public MyMenu(String text) {
	    super(text);
	}
    }

    private class MyMenuBar extends JMenuBar{
	public MyMenuBar() {
	    super();
	}
    }
}
