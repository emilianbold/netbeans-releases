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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.SystemColor;

import javax.swing.BorderFactory;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import org.netbeans.modules.uml.core.metamodel.core.foundation.INamespace;
import org.netbeans.modules.uml.core.metamodel.infrastructure.ICollaboration;
import org.netbeans.modules.uml.core.metamodel.structure.IProject;
import org.netbeans.modules.uml.ui.support.wizard.IWizardSheet;
import org.netbeans.modules.uml.ui.support.wizard.WizardEndPage;


public class WizardSummary extends WizardEndPage {

	private JTextArea helpCaption1 = new JTextArea();
	private JTextArea helpCaption2 = new JTextArea();
	private JTextArea m_SummaryBox = new JTextArea();

	public WizardSummary(IWizardSheet parent) {
		super(parent);
		createUI();
	}

	protected void createUI() {
		super.createUI();

		helpCaption1.setOpaque(false);
		helpCaption1.setBackground(SystemColor.control);
		helpCaption1.setLineWrap(true);
		helpCaption1.setEditable(false);
		helpCaption1.setFocusable(false);
//		helpCaption1.setFont(new java.awt.Font("SansSerif", 0, 10));
		helpCaption1.setText(DefaultDesignPatternResource.getString("IDS_SUMMARYTEXT1"));
		helpCaption1.getAccessibleContext().setAccessibleName(DefaultDesignPatternResource.getString("ACSN_TEXTAREA"));
		helpCaption1.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_TEXTAREA"));

		helpCaption2.setOpaque(false);
		helpCaption2.setBackground(SystemColor.control);
		helpCaption2.setLineWrap(true);
		helpCaption2.setEditable(false);
		helpCaption2.setFocusable(false);
//		helpCaption2.setFont(new java.awt.Font("SansSerif", 0, 10));
		helpCaption2.append(DefaultDesignPatternResource.getString("IDS_SUMMARYTEXT2"));
		helpCaption2.getAccessibleContext().setAccessibleName(DefaultDesignPatternResource.getString("ACSN_TEXTAREA"));
		helpCaption2.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_TEXTAREA"));

                m_SummaryBox.setEditable(false);
		m_SummaryBox.setFocusable(false);
		m_SummaryBox.setBorder(BorderFactory.createEtchedBorder());
		m_SummaryBox.getAccessibleContext().setAccessibleName(DefaultDesignPatternResource.getString("ACSN_TEXTAREA"));
		m_SummaryBox.getAccessibleContext().setAccessibleDescription(DefaultDesignPatternResource.getString("ACSD_TEXTAREA"));

		pnlContents.setLayout(new GridBagLayout());
		pnlContents.add(helpCaption1, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(20, 20, 0, 20), 5, 5));
		pnlContents.add(new JScrollPane(m_SummaryBox), new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(0, 20, 0, 20), 300, 125));
		pnlContents.add(helpCaption2, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 20, 0, 20), 10, 10));

		this.addActionListeners();
	}

	private void addActionListeners() {

	}

	protected boolean onInitDialog() {
		return super.onInitDialog();
	}

	public void onWizardBack() {
		super.onWizardBack();
	}

	public void onWizardNext() {
		//do nothing for the last page
	}

	public void onSetActive() {
		super.onSetActive();
		populate();
	}

	/**
	 * Populate the page - sets up the text in the summary list box
	 *
	 * @return
	 */
	private void populate() {

		IWizardSheet parent = getParentSheet();

		Wizard wiz = (Wizard) parent;

		if (wiz != null) {
			IDesignPatternDetails pDetails = wiz.getDetails();
			if (pDetails != null) {
				// Get the information from the wizard pages
				// format the text to go into the summary screen
				ICollaboration pCollab = pDetails.getCollaboration();
				IProject pProj = pCollab.getProject();
				String projName = pProj.getName();
				String patName = pCollab.getName();
				String line1 = DesignPatternUtilities.translateString("IDS_PROJECTOFPATTERN");
				line1 += projName;
				String line2 = DesignPatternUtilities.translateString("IDS_PATTERN");
				line2 += patName;

				IProject pProj2 = pDetails.getProject();
				String projName2 = pProj2.getName();
				String line3 = DesignPatternUtilities.translateString("IDS_PROJECTWHEREAPPLIED");
				line3 += projName2;

				INamespace pName = pDetails.getNamespace();
				String name = pName.getName();
				String line4 = DesignPatternUtilities.translateString("IDS_PACKAGEWHEREAPPLIED");
				line4 += name;

				String diagName = "";
				if (pDetails.getCreateDiagram())
				{
					diagName = pDetails.getDiagramName();
				}
				else
				{
					diagName = DesignPatternUtilities.translateString("IDS_NONE");
				}
				String line5 = DesignPatternUtilities.translateString("IDS_DIAGRAMTOCREATE");
				line5 += diagName;

				String str = "";
				str += line1;
				str += "\n";
				str += line2;
				str += "\n\n";
				str += line3;
				str += "\n";
				str += line4;
				str += "\n\n";
				str += line5;
				str += "\n";

				m_SummaryBox.setText(str);
			}
		}
	}

}
