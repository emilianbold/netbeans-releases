package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;

public class Application_003 extends TestFrame {

    JLabel buttonLabel;
    JProgressBar progress;
    final static int BUTTON_NUMBER = 4;

    public Application_003() {
	super("Application_003");

	getContentPane().setLayout(new BorderLayout());

	buttonLabel = new JLabel("Button has not been pushed yet");
	getContentPane().add(buttonLabel, BorderLayout.NORTH);

        progress = new JProgressBar(0, BUTTON_NUMBER*BUTTON_NUMBER);
	getContentPane().add(progress, BorderLayout.SOUTH);

	JPanel panel = new JPanel();
	panel.setLayout(new GridLayout(4, 4));
	getContentPane().add(panel, BorderLayout.CENTER);

	JButton butt;
	for(int i = 0; i < BUTTON_NUMBER; i++) {
	    for(int j = 0; j < BUTTON_NUMBER; j++) {
		butt = new JButton(Integer.toString(i) + "-" + Integer.toString(j));
		butt.setToolTipText(butt.getText() + " button");
		butt.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
                            JButton btt = ((JButton)event.getSource());
                            String text = btt.getText();
			    buttonLabel.setText("Button \"" + text + "\" has been pushed");
                            int i = Integer.parseInt(text.substring(0, 1));
                            int j = Integer.parseInt(text.substring(2));
                            progress.setValue(i * BUTTON_NUMBER + j + 1);
                            progress.setString(text);
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
