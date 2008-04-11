package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_033 extends TestFrame {

    JLabel label;

    public Application_033() {
	super("Application_033");

	getContentPane().setLayout(new FlowLayout());

	label = new JLabel("has not been pushed yet");

	MyButton button = new MyButton("Button");
	button.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    label.setText("has been pushed");
		}
	    });
	
	setSize(200, 200);

	getContentPane().add(button);
	getContentPane().add(label);
    }

    public static void main(String[] argv) {
	(new Application_033()).show();
    }
}
