package org.netbeans.jemmy.testing;

import java.awt.event.*;

import javax.swing.*;

public class TestFrame extends JFrame {

    public TestFrame(String title) {
	super(title);
	addDisposeListener();
    }

    public void addDisposeListener() {
	addWindowListener(new WindowListener() {
		public void windowActivated(WindowEvent e) {}
		public void windowClosed(WindowEvent e) {}
		public void windowClosing(WindowEvent e) {
		    dispose();
		    try {
			finalize();
		    } catch(Throwable ex) {
			ex.printStackTrace();
		    }
		}
		public void windowDeactivated(WindowEvent e) {}
		public void windowDeiconified(WindowEvent e) {}
		public void windowIconified(WindowEvent e) {}
		public void windowOpened(WindowEvent e) {}
	    });
    }
}
