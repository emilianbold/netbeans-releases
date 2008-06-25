package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;

public class Application_017 extends TestFrame {
    
    private int index = 0;
    private JLabel label;
    private JButton close;

    public Application_017(int index) {
	super("Application_017/" + Integer.toString(index));

	this.index = index;
	
	setSize(300, 300);

	setLocation(index * 50, index * 50);

	getContentPane().setLayout(new FlowLayout());
	label = new JLabel("has not been processed");
	getContentPane().add(label);
	getContentPane().add(new JButton("another button"));
	close = new JButton("process " + Integer.toString(index));
	close.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    label.setText("has been processed");
		}
	    });
    }

    public int getIndex() {
	return(index);
    }

    public void addButton() {
	getContentPane().add(close);
	show();
    }

    public static void main(String[] argv) {
	try {
	    Application_017 app0 = new Application_017(0);
	    app0.show();
	    Thread.currentThread().sleep(3000);
	    Application_017 app1 = new Application_017(1);
	    app1.show();
	    Thread.currentThread().sleep(3000);
	    Application_017 app2 = new Application_017(2);
	    app2.show();
	    Thread.currentThread().sleep(3000);
	    app0.addButton();
	    Thread.currentThread().sleep(3000);
	    app1.addButton();
	    Thread.currentThread().sleep(3000);
	    app2.addButton();
	} catch(Exception e) {
	    e.printStackTrace();
	}
    }

}
