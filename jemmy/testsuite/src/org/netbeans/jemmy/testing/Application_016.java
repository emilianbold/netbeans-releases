package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_016 extends TestFrame {

    public Application_016() {
	super("Application_016");

	JTabbedPane tp = new JTabbedPane();

	JPanel pane1 = new JPanel();

	pane1.setLayout(new FlowLayout());

	pane1.add(new JButton("button1"));

	tp.add("Page1", pane1);

	JPanel pane2 = new JPanel();

	pane2.setLayout(new FlowLayout());

	pane2.add(new JButton("button2"));

	tp.add("Page2", pane2);

	JPanel list_pane = new JPanel();

	String[] listItems = {"one", "two", "three"};

	list_pane.add(new JList(listItems));

	tp.add("List Page", list_pane);

	getContentPane().add(tp);

	setSize(200, 200);
    }

    public static void main(String[] argv) {
	(new Application_016()).show();
    }
}
