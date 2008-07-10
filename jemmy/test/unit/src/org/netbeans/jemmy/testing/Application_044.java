package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_044 extends TestFrame {

    JComboBox editable;
    DefaultComboBoxModel editableModel;

    public Application_044() {
	super("Application_044");

	getContentPane().setLayout(new FlowLayout());

        //to hold focus
        getContentPane().add(new JRadioButton("Cursor holder"));

        JButton noncolored = new JButton("Button");
        getContentPane().add(noncolored);
        getContentPane().add(new JButton("Button"));
        getContentPane().add(new JButton("Button1"));


        JButton colored = new JButton("Button");
        if(noncolored.getBackground().equals(Color.black)) {
            if(noncolored.getForeground().equals(Color.white)) {
                colored.setBackground(Color.gray);
            } else {
                colored.setBackground(Color.white);
            }
        } else {
            if(noncolored.getForeground().equals(Color.black)) {
                colored.setBackground(Color.white);
            } else {
                colored.setBackground(Color.black);
            }
        }
        getContentPane().add(colored);

        JButton semicolored = new JButton("Button");
        JButton semicolore2 = new JButton("Button");
        if(noncolored.getForeground().equals(Color.blue)) {
            if(noncolored.getBackground().equals(Color.red)) {
                semicolored.setForeground(Color.gray);
                semicolore2.setForeground(Color.yellow);
            } else {
                semicolored.setForeground(Color.red);
                semicolore2.setForeground(Color.green);
            }
        } else {
            if(noncolored.getBackground().equals(Color.blue)) {
                semicolored.setForeground(Color.red);
                semicolore2.setForeground(Color.yellow);
            } else {
                semicolored.setForeground(Color.blue);
                semicolore2.setForeground(Color.red);
            }
        }
        getContentPane().add(semicolored);
        getContentPane().add(semicolore2);

	setSize(200, 200);
    }

    public static void main(String[] argv) {
	(new Application_044()).show();
    }
}
