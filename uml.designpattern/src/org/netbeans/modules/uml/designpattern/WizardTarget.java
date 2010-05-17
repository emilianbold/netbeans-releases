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
import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.core.support.umlutils.ETArrayList;
import org.netbeans.modules.uml.core.support.umlutils.ETList;
import org.netbeans.modules.uml.ui.support.ProductHelper;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProduct;
import org.netbeans.modules.uml.ui.support.applicationmanager.IProductProjectManager;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardInteriorPage;

public class WizardTarget extends WizardInteriorPage {

	private static final String PG_CAPTION = DefaultDesignPatternResource.getString("IDS_WIZARDCAPTION");
	private static final String PG_TITLE = DefaultDesignPatternResource.getString("IDS_TARGETSCOPE");
	private static final String PG_SUBTITLE = DefaultDesignPatternResource.getString("IDS_TARGETSCOPEHELP");

	private Wizard m_Wizard = null;
	private IProject m_Project = null;

	private JComboBox m_ProjectList = new JComboBox();
	private JLabel jLabel1 = new JLabel();
	private JComboBox m_NamespaceList = new JComboBox();
	private JLabel jLabel2 = new JLabel();
	private JTextArea helpCaption = new JTextArea();

	public WizardTarget(IWizardSheet parent, String caption, String headerTitle, String headerSubTitle) {
		super(parent, caption, headerTitle, headerSubTitle);
		createUI();
	}

	public WizardTarget(IWizardSheet parent) {
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

		jLabel2.setText(DefaultDesignPatternResource.determineText(DefaultDesignPatternResource.getString("IDS_NAMESPACE")));
		DefaultDesignPatternResource.setMnemonic(jLabel2, DefaultDesignPatternResource.getString("IDS_NAMESPACE"));
		jLabel2.setLabelFor(m_NamespaceList);
		DefaultDesignPatternResource.setFocusAccelerator(m_NamespaceList, DefaultDesignPatternResource.getString("IDS_NAMESPACE"));
		jLabel2.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 3, 0)));
		jLabel2.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_NAMESPACE"));

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
		pnlContents.add(m_NamespaceList, new GridBagConstraints(0, 3, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 10, 0, 10), 284, 0));
		pnlContents.add(helpCaption, new GridBagConstraints(0, 4, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5, 20, 200, 20), 47, 1));

		m_ProjectList.setEditable(false);
		m_ProjectList.setEnabled(false);
		m_NamespaceList.setEditable(false);

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
	}

	private void m_ProjectList_actionPerformed(ActionEvent e) {
		onSelChangeProjectList();
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
	 * Called when the page becomes active
	 *
	 *
	 *
	 * @return BOOL
	 */
	public void onSetActive()
	{
		if (m_Wizard != null)
		{
			populateProjectList();
			populateProject();
		}
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
	public void onWizardNext()
	{
		// validate the page information
		ETList <String> errorList = validatePage();
		if (errorList != null && errorList.size() == 0)
		{
			IDesignPatternDetails pDetails = m_Wizard.getDetails();
			if (pDetails != null)
			{
				pDetails.setProject(m_Project);
				INamespace pNamespace = DesignPatternUtilities.getSelectedNamespace(m_NamespaceList, m_Project);
				if (pNamespace != null)
				{
					pDetails.setNamespace(pNamespace);
				}
				else
				{
					pDetails.setNamespace(null);
				}
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
	 * Fills in the project list box.
	 *
	 * @return HRESULT
	 */
	private void populateProjectList()
	{
		if (m_ProjectList.getItemCount() == 0){
			DesignPatternUtilities.populateProjectListWithUserProjects(m_ProjectList, true);
		}
	}
	/**
	 * Fills in the project list box default.
	 *
	 * @return HRESULT
	 */
	private void populateProject()
        {
            // default the list box to the current project
            // In c++, we were doing the commented out code, but it doesn't make sense to do it
            // jUML because we only have one project.  In c++, if the user adds a user defined project
            // to the design center, it does in fact become the current project, but the list box
            // setSelectedItem fails to set, and the user has a drop down combo of the projects in the
            // project tree.
            // In jUML, we disabled the combo box (because there is only one project).  But going into
            // this code, would get the new current project (the newly added user defined pattern project)
            // and not set the selected to anything.  Not allowing the user to continue in the wizard.
            // So, always set the value in the disabled combo box to the first (and only) project
            IProduct pProduct = ProductHelper.getProduct();
            if (pProduct != null)
            {
                IProductProjectManager pManager = pProduct.getProjectManager();
                if (pManager != null)
                {
                    IProject theProject = pManager.getCurrentProject();
                    if (theProject != null)
                    {
                        String name = theProject.getName();
                        if (name != null && name.length() > 0)
                        {
                            m_ProjectList.setSelectedItem(name);
                            onSelChangeProjectList();
                        }
                    }
                }
            }
//		if (m_ProjectList.getItemCount() > 0)
//		{
//			m_ProjectList.setSelectedIndex(0);
//			onSelChangeProjectList();
//		}
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
				String oldSel = (String)m_NamespaceList.getSelectedItem();
				DesignPatternUtilities.populateNamespaceList(m_NamespaceList, pProject);
				m_NamespaceList.setSelectedItem(oldSel);
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
		if (m_ProjectList.getSelectedIndex() == -1)
		{
			String err = DesignPatternUtilities.translateString("IDS_SCOPE_NOSELECT");
			tempList.add(err);
		}
		return tempList;
	}

}
