package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_035 extends TestFrame {

    public Application_035() {
	super("Application_035");

	getContentPane().setLayout(new BorderLayout());

	Panel pane = new Panel();
	pane.setLayout(new GridLayout(5, 5));
	ScrollPane scroll = new ScrollPane();
	scroll.add(pane);
	getContentPane().add(scroll, BorderLayout.CENTER);

	for(int i = 0; i < 5; i++) {
	    for(int j = 0; j < 5; j++) {
		pane.add(new Button(Integer.toString(i) + Integer.toString(j)));
	    }
	}

	setSize(100, 100);
    }

    public static void main(String[] argv) {
	(new Application_035()).show();
    }
}
