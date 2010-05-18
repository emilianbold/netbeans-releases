/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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



package org.netbeans.modules.uml.ui.controls.newdialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardInteriorPage;


/**
 * @author sumitabhk
 *
 */
public class NewDialogWizardPage extends WizardInteriorPage
{
	private String origLocation = null;

	protected JLabel m_title = new JLabel();
	protected JTextArea m_subTitle = new JTextArea();
	protected JLabel m_headerImage = new JLabel();
	protected JPanel m_HeaderPanel = new JPanel();
	protected JPanel m_DetailPanel = new JPanel();

	public NewDialogWizardPage(IWizardSheet parent, String caption, String headerTitle, String headerSubTitle) {
		super(parent);//, caption, headerTitle, headerSubTitle);
	}

	public NewDialogWizardPage(IWizardSheet parent) {
		this(parent, "", "", "");
	}

	protected void createUI() 
	{
		super.createUI();
//		JLabel m_Watermark = new JLabel();
//		m_Watermark.setIcon(getParentSheet().getBmpWatermark());
//		setLayout(new BorderLayout());
//		add(m_Watermark,  BorderLayout.WEST);
//		add(pnlContents, BorderLayout.CENTER);
		
		m_subTitle.setEditable(false);
		pnlContents.setLayout(new GridBagLayout());

		JPanel panelHeaderText = new JPanel();
		JPanel panelHeaderImage = new JPanel();

		m_title.setFont(new java.awt.Font("SansSerif", 1, 12));
		m_subTitle.setFont(new java.awt.Font("SansSerif", 0, 10));
		m_subTitle.setLineWrap(true);
		m_subTitle.setWrapStyleWord(false);

		m_HeaderPanel.setBackground(Color.white);
		panelHeaderText.setBackground(Color.white);
		panelHeaderImage.setBackground(Color.white);

		m_HeaderPanel.setLayout(new GridBagLayout());
		panelHeaderText.setLayout(new GridBagLayout());
		panelHeaderImage.setLayout(new BorderLayout(5, 5));

		m_headerImage.setIcon(getParentSheet().getBmpHeader());

		panelHeaderImage.add(m_headerImage, BorderLayout.CENTER);

		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panelHeaderText.add(m_title, gridBagConstraints);//, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(8, 20, 0, 16), 263, 0));

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 0.9;
		gridBagConstraints.weighty = 0.9;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		panelHeaderText.add(m_subTitle, gridBagConstraints);//, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 35, 6, 16), 232, 20));

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.anchor = GridBagConstraints.WEST;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.9;
		m_HeaderPanel.add(panelHeaderText, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.NONE;
		gridBagConstraints.anchor = GridBagConstraints.EAST;
		gridBagConstraints.gridx = 1;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.insets = new java.awt.Insets(0, 5, 0, 0);
		m_HeaderPanel.add(panelHeaderImage, gridBagConstraints);
		m_HeaderPanel.setBorder(new TitledBorder(null, "", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Dialog", 0, 12)));
		

		pnlContents.setBackground(Color.white);
		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 0;
		gridBagConstraints.weightx = 0.1;
		gridBagConstraints.weighty = 0.1;
		pnlContents.add(m_HeaderPanel, gridBagConstraints);

		gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.fill = GridBagConstraints.BOTH;
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridy = 1;
		gridBagConstraints.weightx = 0.9;
		gridBagConstraints.weighty = 0.9;
		pnlContents.add(m_DetailPanel, gridBagConstraints);
	}

	protected boolean onInitDialog() 
	{
		return super.onInitDialog();
	}

	public void onWizardBack() 
	{
		super.onWizardBack();
	}

	public void onWizardNext() 
	{
		super.onWizardNext();
	}

}



