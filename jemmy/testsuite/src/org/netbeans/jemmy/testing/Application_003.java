package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_003 extends TestFrame {

    JLabel buttonLabel;

    public Application_003() {
	super("Application_003");

	getContentPane().setLayout(new BorderLayout());

	buttonLabel = new JLabel("Button has not been pushed yet");
	getContentPane().add(buttonLabel, BorderLayout.NORTH);

	JPanel panel = new JPanel();
	panel.setLayout(new GridLayout(4, 4));
	getContentPane().add(panel, BorderLayout.CENTER);

	JButton butt;
	for(int i = 0; i < 4; i++) {
	    for(int j = 0; j < 4; j++) {
		butt = new JButton(Integer.toString(i) + "-" + Integer.toString(j));
		butt.setToolTipText(butt.getText() + " button");
		butt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
			    buttonLabel.setText("Button \"" + ((JButton)event.getSource()).getText() + "\" has been pushed");
			}
		    });
		panel.add(butt);
	    }
	}

	setSize(400, 400);
    }

    public static void main(String[] argv) {
	(new Application_003()).show();
    }
}
