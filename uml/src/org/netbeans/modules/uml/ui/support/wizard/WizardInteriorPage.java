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
