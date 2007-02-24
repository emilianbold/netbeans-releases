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

package org.netbeans.modules.uml.designpattern;

import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JTextArea;

import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardEndPage;

import javax.swing.*;
import java.awt.event.*;


public class WizardIntro extends WizardEndPage {

	private JTextArea m_IntroText = new JTextArea();

	public WizardIntro(IWizardSheet parent) {
		super(parent);
		createUI();
	}

	protected void createUI() {
		super.createUI();

//		m_IntroText.setFont(new java.awt.Font("SansSerif", 0, 10));
		m_IntroText.setWrapStyleWord(true);
		m_IntroText.append(DefaultDesignPatternResource.getString("IDS_INTROTEXT"));

		m_IntroText.setEditable(false);
		m_IntroText.setFocusable(false);					
                m_IntroText.getAccessibleContext().setAccessibleName(DefaultDesignPatternResource.getString("ACSN_TEXTAREA"));
                m_IntroText.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_TEXTAREA"));
		
		pnlContents.setLayout(new GridBagLayout());
		pnlContents.setBackground(Color.white);
		pnlContents.add(m_IntroText, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(12, 9, 11, 10), 204, 289));
	}	
		
		
	protected boolean onInitDialog() {
		return super.onInitDialog();
	}


	public void onWizardNext() {
		super.onWizardNext();
	}

	public void onWizardBack() {
		// do nothing on the first page
	}

}
