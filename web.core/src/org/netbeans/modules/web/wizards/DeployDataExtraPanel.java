/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.wizards;

import java.awt.GridBagConstraints; 
import java.awt.Insets;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;

/* Wizard panel that collects additional info for Filters
 *
 * @author Ana von Klopp 
 */

class DeployDataExtraPanel extends BaseWizardPanel {

    private ServletData deployData; 
    private JLabel jLinstruction;
    private InitParamPanel paramPanel;

    private static final long serialVersionUID = -2720213209076965116L;

    DeployDataExtraPanel(TargetEvaluator evaluator) { 

	deployData = (ServletData)(evaluator.getDeployData()); 
	setName(NbBundle.getMessage(DeployDataExtraPanel.class, 
				    "TITLE_ddpanel_filter_2")); 
	getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataExtraPanel.class, "ACSD_deployment_2"));
	initComponents ();
	fireChangeEvent();
    }

    private void initComponents () {

	// Layout description
	setLayout(new java.awt.GridBagLayout());
	setPreferredSize(new java.awt.Dimension(450, 250));

	// Entity covers entire row
	GridBagConstraints fullRowC = new GridBagConstraints();
	fullRowC.gridx = 0;                               
	fullRowC.gridy = GridBagConstraints.RELATIVE;     
	fullRowC.gridwidth = 10; 
	fullRowC.weightx = 1.0; 
	fullRowC.anchor = GridBagConstraints.WEST;         
	fullRowC.fill = GridBagConstraints.HORIZONTAL; 
	fullRowC.insets = new Insets(4, 0, 4, 60);

	// Table panel 
	GridBagConstraints tablePanelC = new GridBagConstraints();
	tablePanelC.gridx = 0;
	tablePanelC.gridy = GridBagConstraints.RELATIVE;     
	tablePanelC.gridheight = 5; 
	tablePanelC.gridwidth = 10; 
	tablePanelC.fill = GridBagConstraints.BOTH; 
	tablePanelC.weightx = 1.0;
	//tablePanelC.weighty = 1.0; 
	tablePanelC.anchor = GridBagConstraints.WEST; 
	tablePanelC.insets = new Insets(4, 0, 4, 0);

	// Filler
	GridBagConstraints fillerC = new GridBagConstraints();
	fillerC.gridx = 0;
	fillerC.weighty = 1.0; 
	fillerC.gridy = GridBagConstraints.RELATIVE;     
	fillerC.fill = GridBagConstraints.HORIZONTAL;

	// Component Initialization by row
	// 1. Instruction
	jLinstruction = new JLabel(NbBundle.getMessage(DeployDataExtraPanel.class, "LBL_dd_filter_2")); 
	this.add(jLinstruction, fullRowC); 

	// 2. Init param table 
	paramPanel = new InitParamPanel(deployData, this); 
	this.add(paramPanel, tablePanelC); 

	// 3. Add vertical filler at the bottom
	JPanel filler2 = new JPanel ();
	this.add(filler2, fillerC); 
    }

    public void setData() { 
	jLinstruction.setEnabled(deployData.makeEntry()); 
	paramPanel.setEnabled(deployData.makeEntry()); 
    } 

    public void log(String s) { 
	System.out.println("DeployDataExtraPanel" + s); //NOI18N
    } 

    public HelpCtx getHelp() {
        return new HelpCtx(DeployDataExtraPanel.class); 
    }
} 


