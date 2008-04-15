package org.netbeans.jemmy.testing;

import java.awt.*;

import java.awt.event.*;

import javax.swing.*;
import javax.swing.event.*;

public class Application_025 extends TestFrame {

    JLabel label;
    JSlider hSlider;
    JSlider vSlider;
    JSlider hQSlider;
    JSlider vQSlider;

    public Application_025() {
	super("Application_025");

	hSlider = new JSlider(JSlider.HORIZONTAL);
	hSlider.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		label.setText(Integer.toString(hSlider.getValue()));
	    }
	});

	hQSlider = new JSlider(JSlider.HORIZONTAL, 0, 3, 0);
	hQSlider.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		label.setText(Integer.toString(hQSlider.getValue()));
	    }
	});
	hQSlider.setInverted(true);
	hQSlider.setPaintLabels(true);
	hQSlider.setPaintTicks(true);
	hQSlider.setPaintTrack(true);

	vSlider = new JSlider(JSlider.VERTICAL);
	vSlider.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		label.setText(Integer.toString(vSlider.getValue()));
	    }
	});

	vQSlider = new JSlider(JSlider.VERTICAL, 0, 3, 0);
	vQSlider.addChangeListener(new ChangeListener() {
	    public void stateChanged(ChangeEvent e) {
		label.setText(Integer.toString(vQSlider.getValue()));
	    }
	});
	vQSlider.setInverted(true);
	vQSlider.setPaintLabels(true);
	vQSlider.setPaintTicks(true);
	vQSlider.setPaintTrack(true);

	label = new JLabel("0");
	label.setHorizontalAlignment(SwingConstants.CENTER);
	label.setVerticalAlignment(SwingConstants.CENTER);

	JPanel pane = new JPanel();
	pane.setLayout(new BorderLayout());
	pane.add(hSlider, BorderLayout.SOUTH);
	pane.add(hQSlider, BorderLayout.NORTH);
	pane.add(vSlider, BorderLayout.EAST);
	pane.add(vQSlider, BorderLayout.WEST);
	pane.add(label, BorderLayout.CENTER);



	getContentPane().setLayout(new BorderLayout());
	getContentPane().add(pane, BorderLayout.CENTER);

	setSize(400, 400);
    }

    public static void main(String[] argv) {
	(new Application_025()).show();
    }
}
