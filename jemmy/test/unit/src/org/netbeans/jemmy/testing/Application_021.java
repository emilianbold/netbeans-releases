package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class Application_021 extends TestFrame {

    
    public Application_021() {
	super("Application_021");
	
	JEditorPane editor = new JEditorPane("text", "");

	JTextArea area = new JTextArea();

	JTabbedPane tp = new JTabbedPane();
	tp.add("JEditorPane", new JScrollPane(editor));
	tp.add("JTextArea", new JScrollPane(area));

	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(tp, BorderLayout.CENTER);

	setSize(200, 400);
    }

    public static void main(String[] argv) {
	(new Application_021()).show();
    }

}
