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
        if(noncolored.getForeground().equals(Color.blue)) {
            if(noncolored.getBackground().equals(Color.red)) {
                semicolored.setForeground(Color.gray);
            } else {
                semicolored.setForeground(Color.red);
            }
        } else {
            if(noncolored.getBackground().equals(Color.blue)) {
                semicolored.setForeground(Color.red);
            } else {
                semicolored.setForeground(Color.blue);
            }
        }
        getContentPane().add(semicolored);

	setSize(200, 200);
    }

    public static void main(String[] argv) {
	(new Application_044()).show();
    }
}
