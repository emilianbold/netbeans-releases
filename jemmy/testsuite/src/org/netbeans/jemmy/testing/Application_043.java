package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

import java.util.*;

public class Application_043 extends TestFrame {

    Container contentPane;
    JPanel panel;
    JButton jbutton;
    JTextArea jtextArea;
    ScrollPane nativeScroll;

    public Application_043() {
	super("Application_043");

        contentPane = getContentPane();
        contentPane.setLayout(new BorderLayout());

        JPanel swingPane = new JPanel();
        swingPane.setLayout(new BorderLayout());

        jbutton = new JButton("Button");
        swingPane.add(jbutton, BorderLayout.NORTH);

        jtextArea = new JTextArea();
        swingPane.add(jtextArea, BorderLayout.CENTER);

        //        nativeScroll =  new ScrollPane();
        //        nativeScroll.add(swingPane);

        //        contentPane.add(nativeScroll, BorderLayout.CENTER);
        contentPane.add(swingPane, BorderLayout.CENTER);

	setSize(200, 200);
    }

    public static void main(String[] argv) {
        Application_043 frame = new Application_043();
	frame.show();
    }

}
