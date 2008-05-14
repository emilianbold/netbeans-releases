package org.netbeans.jemmy.testing;

import java.awt.*;
import java.util.*;

import java.awt.event.*;

import java.beans.PropertyVetoException;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.event.*;
import javax.swing.tree.*;
import javax.swing.table.*;


public class Application_028 extends TestFrame {
    
    JTextField target;

    public Application_028() {
	super("Right one");

	target = new JTextField("This is a component supposed to be made visible");

	JPanel pane = new JPanel();
	pane.add(new JLabel("Well, this does.          "));
	for(int i = 0; i < 100; i++) {
	    pane.add(new JLabel(Integer.toString(i)));
	}

	pane.add(target);

	JScrollPane scroll = new JScrollPane(pane);

	JTabbedPane tabbed = new JTabbedPane();

	tabbed.add("Wrong one", new JLabel("This tab does not contain right component"));
	tabbed.add("Right one", scroll);

	JInternalFrame wIFrame = new JInternalFrame("Wrong one", true, true, true, true);
	JInternalFrame iFrame = new JInternalFrame("Right one", true, true, true, true);

	iFrame.getContentPane().add(tabbed);

	JDesktopPane dp = new JDesktopPane();
	iFrame.setSize(200, 200);
	iFrame.setLocation(0, 0);
	iFrame.setVisible(true);
	dp.add(iFrame);
	wIFrame.setSize(300, 300);
	wIFrame.setLocation(0, 0);
	wIFrame.setVisible(true);
	dp.add(wIFrame);
	try {
	    wIFrame.setSelected(true);
	} catch(PropertyVetoException e) {
	    e.printStackTrace();
	}

	getContentPane().add(dp);

	setSize(400, 400);
	setLocation(0, 0);
    }

    public JTextField getTarget() {
	return(target);
    }

    public static void main(String[] argv) {
	(new Application_028()).show();
	JFrame wFrame = new JFrame("Wrong one");
	wFrame.setLocation(0, 0);
	wFrame.setSize(500, 500);
	wFrame.show();
    }


}
