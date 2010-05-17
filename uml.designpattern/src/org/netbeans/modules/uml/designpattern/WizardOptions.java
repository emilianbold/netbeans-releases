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

package org.netbeans.modules.uml.designpattern;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JCheckBox;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.netbeans.modules.uml.common.generics.ETPairT;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.diagramsupport.IProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.diagramsupport.ProxyDiagramManager;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardInteriorPage;

public class WizardOptions extends WizardInteriorPage {

	private static final String PG_CAPTION = DefaultDesignPatternResource.getString("IDS_WIZARDCAPTION");
	private static final String PG_TITLE = DefaultDesignPatternResource.getString("IDS_OPTIONS");
	private static final String PG_SUBTITLE = DefaultDesignPatternResource.getString("IDS_OPTIONSHELP");

	private Wizard m_Wizard = null;

	private JCheckBox m_Create = new JCheckBox();
	private JTextField m_DiagramName = new JTextField();
	private JTextArea helpCaption = new JTextArea();

	public WizardOptions(IWizardSheet parent, String caption, String headerTitle, String headerSubTitle) {
		super(parent, caption, headerTitle, headerSubTitle);
		createUI();
	}

	public WizardOptions(IWizardSheet parent) {
		this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
	}

	protected void createUI() {
		super.createUI();

		pnlContents.setLayout(new GridBagLayout());

		m_Create.setText(DefaultDesignPatternResource.determineText(DefaultDesignPatternResource.getString("IDS_CREATECLASSDIAGRAM")));
		DefaultDesignPatternResource.setMnemonic(m_Create, DefaultDesignPatternResource.getString("IDS_CREATECLASSDIAGRAM"));
		m_Create.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_CREATE_CHECKBOX"));

		helpCaption.setLineWrap(true);
		helpCaption.setOpaque(false);
		helpCaption.setPreferredSize(new Dimension(300, 30));
		helpCaption.setMinimumSize(new Dimension(300, 30));
		helpCaption.setFont(new java.awt.Font("SansSerif", 0, 10));
		helpCaption.setEditable(false);
		helpCaption.setFocusable(false);
		helpCaption.getAccessibleContext().setAccessibleName(DefaultDesignPatternResource.getString("ACSN_TEXTAREA"));
		helpCaption.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_TEXTAREA"));

		m_DiagramName.setEnabled(false);
                m_DiagramName.getAccessibleContext().setAccessibleName(DefaultDesignPatternResource.getString("ACSN_DIAGRAMNAME"));
                m_DiagramName.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_DIAGRAMNAME"));

		pnlContents.add(m_Create, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(20, 7, 3, 205), 32, 0));
		pnlContents.add(m_DiagramName, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 10), 284, 0));
		pnlContents.add(helpCaption, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 20, 200, 40), 47, 1));

		pnlContents.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));

		this.addActionListeners();
		this.onInitDialog();
	}

	private void addActionListeners() {

		m_Create.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(java.awt.event.ActionEvent evt) {
				m_Create_actionPerformed(evt);
			}
		});

	}
	private void m_Create_actionPerformed(ActionEvent e) {
		onCreateDiagram();
	}


	/**
	 * Called when dialog is initialized
	 *
	 *
	 *
	 * @return BOOL
	 *
	 */
	 protected boolean onInitDialog() {
		super.onInitDialog();

		IWizardSheet parent = getParentSheet();
		m_Wizard = (Wizard) parent;

		return true; // return TRUE unless you set the focus to a control
	}
	/**
	 * Called when the user clicks back
	 *
	 *
	 *
	 * @return LRESULT		Whether or not to continue to the next page
	 *
	 */
	public void onWizardBack() {

		if (m_Wizard != null) {
			m_Wizard.m_RefreshPages = false;
		}
		super.onWizardBack();
	}
	/**
	 * Called when the user clicks next
	 *
	 *
	 *
	 * @return LRESULT		Whether or not to continue to the next page
	 *
	 */
	public void onWizardNext()
	{
		// validate the page information
		ETList <String> errorList = validatePage();
		if (errorList != null && errorList.size() == 0)
		{
			IDesignPatternDetails pDetails = m_Wizard.getDetails();
			if (pDetails != null)
			{
				if (m_Create.isSelected())
				{
					pDetails.setCreateDiagram(true);
				}
				else
				{
					pDetails.setCreateDiagram(false);
				}
				// store the diagram name
				String diagName = m_DiagramName.getText();
				pDetails.setDiagramName(diagName);
				super.onWizardNext();
			}
		}
		else if (errorList != null && errorList.size() > 0)
		{
			// display the errors
			String msg = DesignPatternUtilities.formatErrorMessage(errorList);
			DesignPatternUtilities.displayErrorMessage(m_Wizard, msg);
		}
	}
	/**
	 * Performs page validations -
	 *
	 *
	 * @param errList[out]		An array of errors that occurred on the page
	 *
	 * @return HRESULT
	 *
	 */
	private ETList<String> validatePage()
	{
		ETList<String> tempList = new ETArrayList<String>();
		if (m_Create.isSelected())
		{
			String diagName = m_DiagramName.getText();
			IProxyDiagramManager pDiagManager = ProxyDiagramManager.instance();
			if (pDiagManager != null)
			{
				ETPairT<Boolean, String> pResult = pDiagManager.isValidDiagramName(diagName);
				if (pResult != null)
				{
					boolean bIsCorrect = pResult.getParamOne().booleanValue();
					if (!bIsCorrect)
					{
						String err = DesignPatternUtilities.translateString("IDS_INVALIDDIAGRAMNAME");
						tempList.add(err);
					}
				}
			}
		}
		return tempList;
	}

	private void onCreateDiagram()
	{
		if (m_Create.isSelected())
		{
			m_DiagramName.setEnabled(true);
			populateDiagramName();
			m_DiagramName.requestFocus();
			m_DiagramName.selectAll();
		}
		else
		{
			m_DiagramName.setText("");
			m_DiagramName.setEnabled(false);
		}
	}
	/**
	 * Populate the diagram name edit box
	 *
	 * @return HRESULT
	 */
	private void populateDiagramName()
	{
		if (m_Wizard != null)
		{
			IDesignPatternDetails pDetails = m_Wizard.getDetails();
			if (pDetails != null)
			{
				ICollaboration pCollab = pDetails.getCollaboration();
				if (pCollab != null)
				{
					String name = pCollab.getName();
					name += DefaultDesignPatternResource.getString("IDS_DIAGRAM");
					m_DiagramName.setText(name);
				}
			}
		}
	}

}
