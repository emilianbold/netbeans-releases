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

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import org.netbeans.modules.uml.core.metamodel.core.foundation.IElement;
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamedElement;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardInteriorPage;

public class WizardPatternSelection extends WizardInteriorPage {

	private static final String PG_CAPTION = DefaultDesignPatternResource.getString("IDS_WIZARDCAPTION");
	private static final String PG_TITLE = DefaultDesignPatternResource.getString("IDS_PATTERNSELECTION");
	private static final String PG_SUBTITLE = DefaultDesignPatternResource.getString("IDS_PATTERNSELECTIONHELP");

	private Wizard m_Wizard = null;
	private ICollaboration m_Pattern = null;
	private IProject m_Project = null;

	private JComboBox m_ProjectList = new JComboBox();
	private JLabel jLabel1 = new JLabel();
	private JComboBox m_PatternList = new JComboBox();
	private JLabel jLabel2 = new JLabel();
	private JTextArea helpCaption = new JTextArea();

	public WizardPatternSelection(IWizardSheet parent, String caption, String headerTitle, String headerSubTitle) {
		super(parent, caption, headerTitle, headerSubTitle);
		createUI();
	}

	public WizardPatternSelection(IWizardSheet parent) {
		this(parent, PG_CAPTION, PG_TITLE, PG_SUBTITLE);
	}

	protected void createUI() {
		super.createUI();

		jLabel1.setText(DefaultDesignPatternResource.determineText(DefaultDesignPatternResource.getString("IDS_PROJECT")));
		DefaultDesignPatternResource.setMnemonic(jLabel1, DefaultDesignPatternResource.getString("IDS_PROJECT"));
		jLabel1.setLabelFor(m_ProjectList);
		DefaultDesignPatternResource.setFocusAccelerator(m_ProjectList, DefaultDesignPatternResource.getString("IDS_PROJECT"));
		jLabel1.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 3, 0)));
		jLabel1.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_PROJECT_COMBOBOX"));

		jLabel2.setText(DefaultDesignPatternResource.determineText(DefaultDesignPatternResource.getString("IDS_DESIGNPATTERN")));
		DefaultDesignPatternResource.setMnemonic(jLabel2, DefaultDesignPatternResource.getString("IDS_DESIGNPATTERN"));
		jLabel2.setLabelFor(m_PatternList);
		DefaultDesignPatternResource.setFocusAccelerator(m_PatternList, DefaultDesignPatternResource.getString("IDS_DESIGNPATTERN"));
		jLabel2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 3, 0)));
		jLabel2.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_DESIGNPATTERN_COMBOBOX"));

		helpCaption.setLineWrap(true);
		helpCaption.setOpaque(false);
		helpCaption.setPreferredSize(new Dimension(300, 30));
		helpCaption.setMinimumSize(new Dimension(300, 30));
		helpCaption.setFont(new java.awt.Font("SansSerif", 0, 10));
		helpCaption.setEditable(false);
		helpCaption.setFocusable(false);
		helpCaption.getAccessibleContext().setAccessibleName(DefaultDesignPatternResource.getString("ACSN_TEXTAREA"));
		helpCaption.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_TEXTAREA"));

		pnlContents.setLayout(new GridBagLayout());

		pnlContents.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(20, 10, 0, 205), 32, 0));
		pnlContents.add(m_ProjectList, new GridBagConstraints(0, 1, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 10), 284, 0));
		pnlContents.add(jLabel2, new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(20, 10, 0, 205), 32, 0));
		pnlContents.add(m_PatternList, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 10), 284, 0));
		pnlContents.add(helpCaption, new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 20, 200, 20), 47, 1));

		m_ProjectList.setEditable(false);
		m_PatternList.setEditable(false);

		pnlContents.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5)));

		this.addActionListeners();
		this.onInitDialog();
	}

	private void addActionListeners() {
		m_ProjectList.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_ProjectList_actionPerformed(e);
			}
		});
		m_PatternList.addActionListener(new java.awt.event.ActionListener() {
			public void actionPerformed(ActionEvent e) {
				m_PatternList_actionPerformed(e);
			}
		});

	}

	private void m_ProjectList_actionPerformed(ActionEvent e) {
		onSelChangeProjectList();
	}
	private void m_PatternList_actionPerformed(ActionEvent e) {
		onSelChangePatternList();
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
		if (m_Wizard != null)
		{
			IDesignPatternDetails pDetails = m_Wizard.getDetails();
			if (pDetails != null)
			{
				m_Pattern = pDetails.getCollaboration();
			}
		}
		if (m_Pattern != null)
		{
			m_ProjectList.setEnabled(false);
			m_PatternList.setEnabled(false);
		}
		else
		{
			m_ProjectList.setEnabled(true);
			m_PatternList.setEnabled(true);
		}

		return true; // return TRUE unless you set the focus to a control
	}
	/**
	 * Called when the page becomes active
	 *
	 *
	 *
	 * @return BOOL
	 */
	public void onSetActive()
	{
		populateProjectList();
		populateProject();
		populatePattern();
		super.onSetActive();
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
     public void onWizardNext() {
         // validate the page information
         ETList <String> errorList = validatePage();
         if (errorList != null && errorList.size() == 0) {
             String patName = (String)m_PatternList.getSelectedItem();
             IDesignPatternManager pManager = m_Wizard.getManager();
             if (pManager != null) {
                 ETList<IElement> pTemp = pManager.getPatternsInProject(m_Project);
                 if (pTemp != null) {
                     int eleCount = pTemp.size();
                     for (int y = 0; y < eleCount; y++) {
                         IElement pEle = pTemp.get(y);
                         if (pEle != null) {
                             if (pEle instanceof ICollaboration) {
                                 ICollaboration pNamed = (ICollaboration)pEle;
                                 String name = pNamed.getQualifiedName();
                                 if (name != null && name.equals(patName)) {
                                     m_Pattern = pNamed;
                                     IDesignPatternDetails pDetails = m_Wizard.getDetails();
                                     if (pDetails != null) {
                                         pDetails.setCollaboration(m_Pattern);
                                         pDetails.setRoles(null);
                                         pDetails.clearParticipantNames();
                                         pManager.buildPatternDetails(m_Pattern, pDetails);
                                         break;
                                     }
                                 }
                             }
                         }
                     }
                 }
             }
             super.onWizardNext();
         }
         else if (errorList != null && errorList.size() > 0) {
             // display the errors
             String msg = DesignPatternUtilities.formatErrorMessage(errorList);
             DesignPatternUtilities.displayErrorMessage(m_Wizard, msg);
         }
     }
	/**
	 * Fills in the project list box.
	 *
	 * @return HRESULT
	 */
	private void populateProjectList()
	{
		if (m_ProjectList.getItemCount() == 0){
			DesignPatternUtilities.populateProjectListWithDesignCenterProjects(m_ProjectList, false);
		}
	}
	/**
	 * Fills in the project list box default.
	 *
	 * @return HRESULT
	 */
	private void populateProject()
	{
		if (m_Pattern != null)
		{
			IProject pProj = m_Pattern.getProject();
			if (pProj != null)
			{
				DesignPatternUtilities.populateProjectListWithUserProjects(m_ProjectList, false);
				String name = pProj.getName();
				m_ProjectList.setSelectedItem(name);
			}
		}
		onSelChangeProjectList();
	}
	/**
	 * Fills in the pattern list default.
	 *
	 * @return HRESULT
	 */
	private void populatePattern()
	{
		if (m_Pattern != null)
		{
			String name = m_Pattern.getQualifiedName();
			m_PatternList.setSelectedItem(name);
		}
	}

	/**
	 * Event called when an entry in the project list box changes
	 *
	 * @return HRESULT
	 */
	private void onSelChangeProjectList()
	{
		// get the new list entry
		m_Project = null;
		String selText = (String)m_ProjectList.getSelectedItem();
		if (selText != null && selText.length() > 0)
		{
			IProject pProject = DesignPatternUtilities.onSelChangeProjectList(selText, m_Wizard);
			if (pProject != null)
			{
				m_Project = pProject;
				DesignPatternUtilities.populatePatternList(m_PatternList, pProject);
				m_Wizard.m_RefreshPages = true;
			}
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
		if (m_Pattern != null)
		{
		}
		else
		{
			if (m_ProjectList.getSelectedIndex() == -1)
			{
				String err = DesignPatternUtilities.translateString("IDS_SCOPE_NOSELECT");
				tempList.add(err);
			}
			if (m_PatternList.getSelectedIndex() == -1)
			{
				String err = DesignPatternUtilities.translateString("IDS_SCOPE_SELECT");
				tempList.add(err);
			}
		}
		return tempList;
	}

	private void onSelChangePatternList()
	{
		// get the new list entry
		//m_Pattern = null;
		m_Wizard.m_RefreshPages = true;
	}

}
