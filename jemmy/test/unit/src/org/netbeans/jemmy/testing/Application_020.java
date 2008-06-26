package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class Application_020 extends TestFrame {

    
    public Application_020() {
	super("Application_020");
	
	JTextArea area = new JTextArea("");
	area.setLineWrap(true);

	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(new JScrollPane(area), BorderLayout.CENTER);

	setSize(200, 400);
    }

    public static void main(String[] argv) {
	(new Application_020()).show();
    }

}
