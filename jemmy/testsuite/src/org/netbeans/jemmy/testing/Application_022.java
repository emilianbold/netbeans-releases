package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class Application_022 extends TestFrame {

    
    public Application_022() {
	super("Application_022");

	JInternalFrame frame1 = new JInternalFrame("Frame 1", true, true, true, true);
	JInternalFrame frame2 = new JInternalFrame("Frame 2", true, true, true, true);

	JDesktopPane desk = new JDesktopPane();
	frame1.setSize(200, 200);
	frame1.setLocation(0, 0);
	frame1.setVisible(true);
	desk.add(frame1);
	frame2.setSize(200, 200);
	frame2.setLocation(25, 25);
	frame2.setVisible(true);
	desk.add(frame2);

        try {
            frame1.setSelected(true);
        } catch (java.beans.PropertyVetoException e) {}

	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(new JScrollPane(desk), BorderLayout.CENTER);

	frame1.getContentPane().add(new JButton("Button 1"));
	try {
	    frame1.setIcon(true);
	} catch (java.beans.PropertyVetoException e) {
	    e.printStackTrace();
	}

	frame2.getContentPane().add(new JButton("Button 2"));

	setSize(400, 400);
    }

    public static void main(String[] argv) {
	(new Application_022()).show();
    }

}
