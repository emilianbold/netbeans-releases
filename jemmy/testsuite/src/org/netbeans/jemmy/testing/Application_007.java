package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class Application_007 extends TestFrame {

    public Application_007() {
	super("Application_007");
	
	getContentPane().setLayout(new GridLayout(3, 1));
	Object[] data = new Object[0];
	getContentPane().add(new JTree(data));
	getContentPane().add(new JTable());
	getContentPane().add(new JList());

	setSize(300, 300);
    }

    public static void main(String[] argv) {
	(new Application_007()).show();
    }

}
