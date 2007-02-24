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
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;

public abstract class WizardInteriorPage extends WizardPage {

	private static final String PG_CAPTION = null;
	private static final String PG_TITLE = "Enter Page Title";
	private static final String PG_SUBTITLE = "Enter Page Subtitle";

	private JPanel pnlHeader = new JPanel();
	private JPanel pnlHeaderText = new JPanel();
	private JPanel pnlHeaderImage = new JPanel();
	protected JPanel pnlContents = new JPanel();

	private JLabel m_Title = new JLabel();
	private JTextArea m_SubTitle = new JTextArea();
	private JLabel m_HeaderImage = new JLabel();

	public WizardInteriorPage(IWizardSheet parent, int nIDCaption, int nIDHeaderTitle, int nIDHeaderSubTitle) {
		this(parent);
	}

	public WizardInteriorPage(IWizardSheet parent, String caption, String headerTitle, String headerSubTitle) {
		super(parent);
		this.setCaption(caption);
		m_Title.setText(headerTitle);
		m_SubTitle.setText(headerSubTitle);
	}

	public WizardInteriorPage(IWizardSheet parent) {
		this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
	}

	public WizardInteriorPage() {
		super();
	}
	protected void createUI() {
		super.createUI();

                //cb
//		m_Title.setFont(new java.awt.Font("SansSerif", 1, 12));
//		m_SubTitle.setFont(new java.awt.Font("SansSerif", 0, 10));
		m_SubTitle.setLineWrap(true);
		m_SubTitle.setWrapStyleWord(true);
		m_SubTitle.setFocusable(false);
		m_SubTitle.getAccessibleContext().setAccessibleName(WizardResouces.getString("ACSN_TEXTAREA"));
		m_SubTitle.getAccessibleContext().setAccessibleDescription(WizardResouces.getString("ACSD_TEXTAREA"));

		pnlHeader.setBackground(Color.white);
		pnlHeaderText.setBackground(Color.white);
		pnlHeaderImage.setBackground(Color.white);

		pnlHeader.setLayout(new BorderLayout());
		pnlHeaderText.setLayout(new GridBagLayout());		
		pnlHeaderImage.setLayout(new BorderLayout(5, 5));

		m_HeaderImage.setIcon(getParentSheet().getBmpHeader());

		this.pnlHeaderImage.add(m_HeaderImage, BorderLayout.CENTER);

		pnlHeaderText.add(m_Title, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 20, 0, 16), 263, 0));
		pnlHeaderText.add(m_SubTitle, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 35, 6, 16), 232, 20));

		pnlHeader.add(pnlHeaderText, BorderLayout.CENTER);
		pnlHeader.add(pnlHeaderImage, BorderLayout.EAST);
      
      pnlHeaderText.setEnabled(false);
      pnlHeader.setEnabled(false);
      m_SubTitle.setEditable(false);
      
		this.add(pnlHeader, BorderLayout.NORTH);
		this.add(pnlContents, BorderLayout.CENTER);
	}

	protected boolean onInitDialog() {

		return super.onInitDialog();
	}

}
