/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
	
    private static final int STEP = 10;
    private static final int MIN = 10;
    
    
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
		SpinnerNumberModel model = new SpinnerNumberModel(this.sequence.getFrameMs(), MIN, Integer.MAX_VALUE, STEP);
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
                if (ms < MIN) {
                    this.setForeground(Color.RED);
                }
                else {
                    this.spinner.getModel().setValue(ms);
                    this.setForeground(Color.BLACK);
                }
			}  catch (NumberFormatException nfe) {
				this.setForeground(Color.RED);
			}
		}
	}
}
