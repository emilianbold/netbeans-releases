package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class Application_019 extends TestFrame {

    JPopupMenu popup;
    JList list;
    JTree tree;
    
    public Application_019() {
	super("Application_019");
	
	getContentPane().setLayout(new BorderLayout());
	JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
					  new JScrollPane(new JTextArea("Top side")),
					  new JScrollPane(new JTextArea("Bottom side")));
	split.setOneTouchExpandable(true);
	getContentPane().add(split, BorderLayout.CENTER);

	setSize(200, 400);
    }

    public static void main(String[] argv) {
	(new Application_019()).show();
    }

}
