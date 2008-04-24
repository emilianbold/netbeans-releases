package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_047 extends TestFrame {

    JSpinner one, two, three, four;

    public Application_047() {
	super("Application_047");

	getContentPane().setLayout(new GridLayout(4, 1));

        one = new JSpinner();
        getContentPane().add(one);

        two = new JSpinner();
        two.setModel(new SpinnerDateModel());
        two.setEditor(new JSpinner.DateEditor(two));
        getContentPane().add(two);

        three = new JSpinner();
        three.setModel(new SpinnerListModel(new String[] {"one", "two", "three"}));
        three.setEditor(new JSpinner.ListEditor(three));
        getContentPane().add(three);

        four = new JSpinner();
        four.setEditor(new JSpinner.NumberEditor(four, "##.00"));
        four.setModel(new SpinnerNumberModel(5, 0, 10, 1));
        getContentPane().add(four);

	setSize(200, 200);
    }

    public static void main(String[] argv) {
        Application_047 app = new Application_047();
	app.show();
        System.out.println(app.one.getValue().getClass().getName());
        System.out.println(app.two.getValue().getClass().getName());
        System.out.println(app.three.getValue().getClass().getName());
        System.out.println(app.four.getValue().getClass().getName());
    }
}
