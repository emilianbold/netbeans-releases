/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.

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



package org.netbeans.modules.uml.ui.support.wizard;

import java.awt.BorderLayout;
import java.awt.Dimension;

import javax.swing.JLabel;
import javax.swing.JPanel;

public abstract class WizardEndPage extends WizardPage {

	private JLabel m_Watermark = new JLabel();
	protected JPanel pnlContents = new JPanel();

	public WizardEndPage(IWizardSheet parent) {
		super(parent);
	}

	protected void createUI() {
		super.createUI();
		
		// m_Watermark.setMaximumSize(new Dimension(0, 0));
		// m_Watermark.setMinimumSize(new Dimension(150, 100));
		// m_Watermark.setPreferredSize(new Dimension(150, 100));
		// m_Watermark.setIcon(getParentSheet().getBmpWatermark());
		// this.add(m_Watermark,  BorderLayout.WEST);
		this.add(pnlContents, BorderLayout.CENTER);
                this.setPreferredSize(new Dimension(300,300));

		this.addActionListeners();
	}

	private void addActionListeners() {

	}

	protected boolean onInitDialog() {

		return super.onInitDialog();
	}

}
