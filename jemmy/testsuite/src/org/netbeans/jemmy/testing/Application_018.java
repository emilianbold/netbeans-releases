package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_018 extends TestFrame {

    public Application_018() {
	super("Application_018");

	getContentPane().setLayout(new BorderLayout());

	JPanel pane = new JPanel();
	pane.setLayout(new GridLayout(5, 5));

	getContentPane().add(new JScrollPane(pane), BorderLayout.CENTER);

	for(int i = 0; i < 5; i++) {
	    for(int j = 0; j < 5; j++) {
		pane.add(new JButton(Integer.toString(i) + Integer.toString(j)));
	    }
	}

	setSize(100, 100);
    }

    public static void main(String[] argv) {
	(new Application_018()).show();
    }
}
