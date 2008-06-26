package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_040 extends TestFrame {

    JLabel menuLabel;

    public Application_040() {
	super("Application_040");

	getContentPane().setLayout(new FlowLayout());

	JMenuItem menuItem = new JMenuItem("menuItem");
	menuLabel = new JLabel("Menu has not been pushed yet");
	menuItem.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent event) {
		    menuLabel.setText("menu item has been pushed");
		}
	    });

        JMenu submenu;
        JMenuItem prevmenu = menuItem;
        for(int i = 0; i < 20; i++) {
            submenu = new JMenu("submenu" + i);
            submenu.add(prevmenu);
            prevmenu = submenu;
        }

	JMenuBar menuBar = new JMenuBar();
	menuBar.add(prevmenu);

	setJMenuBar(menuBar);

	getContentPane().add(menuLabel);

	setSize(200, 200);
    }

    public static void main(String[] argv) {
	(new Application_040()).show();
    }

}
