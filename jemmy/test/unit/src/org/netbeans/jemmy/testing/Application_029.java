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


public class Application_029 extends TestFrame {
    
    JTextField target;

    public Application_029() {
	super("Right one");

	JButton button = new JButton("Button");
	button.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    getContentPane().add(new JLabel("label"));
		}
	    });

	JButton showModal = new JButton("Show modal dialog");
	showModal.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    new MyModalDialog(getSelf()).show();
		}
	    });

	getContentPane().setLayout(new FlowLayout());
	getContentPane().add(button);
	getContentPane().add(showModal);

	JMenuItem menuItem = new JMenuItem("MenuItem");
	menuItem.addActionListener(new ActionListener(){
		public void actionPerformed(ActionEvent e) {
		    new MyModalDialog(getSelf()).show();
		}
	    });

	JMenu menu = new JMenu("Menu");
	menu.add(menuItem);

	JMenuBar menuBar = new JMenuBar();
	menuBar.add(menu);

	setJMenuBar(menuBar);

	setLocation(0, 0);
	setSize(500, 500);
	show();
    }

    private Application_029 getSelf() {
	return(this);
    }

    public static void main(String[] argv) {
	Application_029 frame = new Application_029();
	frame.show();
    }

    class MyModalDialog extends JDialog {
	public MyModalDialog(JFrame win) {
	    super(win, "Modal dialog");
	    JButton button = new JButton("Close");
	    button.addActionListener(new ActionListener(){
		    public void actionPerformed(ActionEvent e) {
			setVisible(false);
		    }
		});
	    getContentPane().add(button);
	    setSize(100, 100);
	    setModal(true);
	}
    }
}
