/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.vmd.game.preview;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.vmd.game.model.Sequence;
import org.openide.util.NbBundle;

/**
 *
 * @author kherink
 */
public class SequenceTimeSpinner extends JSpinner {
	
	private Sequence sequence;
		
	public SequenceTimeSpinner(Sequence sequence) {
		this.sequence = sequence;
		this.sequence.addPropertyChangeListener(new PropertyChangeListener() {
			public void propertyChange(PropertyChangeEvent evt) {
				if (evt.getPropertyName().equals(Sequence.PROPERTY_FRAME_MS)) {
					SequenceTimeSpinner.this.setValue(SequenceTimeSpinner.this.sequence.getFrameMs());
				}
			}
		});
		//System.out.println("MS " + this.sequence.getFrameMs());
		SpinnerNumberModel model = new SpinnerNumberModel(this.sequence.getFrameMs(), 10, Integer.MAX_VALUE, 10);
		this.setModel(model);
		this.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				SpinnerModel model = SequenceTimeSpinner.this.getModel();
				if (model instanceof SpinnerNumberModel) {
					int newMs = ((SpinnerNumberModel)model).getNumber().intValue();
					SequenceTimeSpinner.this.sequence.setFrameMs(newMs);
				}
			}
		});
		this.setEditor(new SpinnerMSEditor(this));
	}
	
	public static class SpinnerMSEditor extends JTextField implements ChangeListener, ActionListener {
		private JSpinner spinner;
		public SpinnerMSEditor(JSpinner spinner) {
			this.spinner = spinner;
			this.spinner.addChangeListener(this);
			this.setToolTipText(NbBundle.getMessage(SequenceTimeSpinner.class, "SequenceTimeSpinner.tooltip"));
			this.setText( ((SpinnerNumberModel) this.spinner.getModel()).getValue()  + " ms"); // NOI18N
			this.setHorizontalAlignment(JTextField.TRAILING);
			this.addActionListener(this);
		}
		public void stateChanged(ChangeEvent e) {
			this.setText( ((SpinnerNumberModel) this.spinner.getModel()).getValue()  + " ms"); // NOI18N
		}

		public void actionPerformed(ActionEvent e) {
			String str = this.getText();
			if (str.endsWith(" ms")) { // NOI18N
				str = str.substring(0, str.lastIndexOf(" ms")); // NOI18N
			}
			try {
				int ms = Integer.parseInt(str);
				this.spinner.getModel().setValue(ms);
				this.setForeground(Color.BLACK);
			}  catch (NumberFormatException nfe) {
				this.setForeground(Color.RED);
			}
		}
	}
}
