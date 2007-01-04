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

package  org.netbeans.modules.cnd.makewizard;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextArea;

public class CompilerTypePanel extends MakefileWizardPanel {

    /** Serial version number */
    static final long serialVersionUID = -8802837465543215976L;

    // the fields in the first panel...
    private boolean	    initialized;
    private JPanel panel2;
    private JTextArea message1;
    private JLabel makefileTypeLabel;
    private ButtonGroup buttonGroup;
    private JRadioButton sunCollectionRadioButton;
    private JRadioButton gnuCollectionRadioButton;
    private JRadioButton bothCollectionRadioButton;
    private JTextArea message2;

    /**
     * Constructor for the Makefile name panel. Remember, most of the panel is
     * inherited from WizardDescriptor.
     */
    CompilerTypePanel(MakefileWizard wd) {
	super(wd);
	String subtitle = getString("LBL_CompilerTypePanel"); //NOI18N
	setSubTitle(subtitle);
	this.getAccessibleContext().setAccessibleDescription(subtitle);
	initialized = false;
    }


    /** Defer widget creation until the panel needs to be displayed */
    private void create() {
        setLayout(new GridBagLayout());
	GridBagConstraints gridBagConstraints;
	JPanel panel = new JPanel(new GridBagLayout());

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
	add(panel, gridBagConstraints);

	panel2 = new JPanel();
        message1 = new JTextArea();
	makefileTypeLabel = new JLabel();
        sunCollectionRadioButton = new JRadioButton();
        gnuCollectionRadioButton = new JRadioButton();
        bothCollectionRadioButton = new JRadioButton();
        message2 = new JTextArea();
        panel2.setLayout(new java.awt.GridBagLayout());

        makefileTypeLabel.setText(getString("LBL_CompilerType"));		    // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(makefileTypeLabel, gridBagConstraints);

        sunCollectionRadioButton.setText(getString("RB_CompilerTypeSun"));		    // NOI18N
	sunCollectionRadioButton.setMnemonic(getString("MNEM_CompilerTypeSun").charAt(0));  // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(sunCollectionRadioButton, gridBagConstraints);

        gnuCollectionRadioButton.setText(getString("RB_CompilerTypeGNU"));		    // NOI18N
	gnuCollectionRadioButton.setMnemonic(getString("MNEM_CompilerTypeGNU").charAt(0));  // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(gnuCollectionRadioButton, gridBagConstraints);

        bothCollectionRadioButton.setText(getString("RB_CompilerTypeBoth"));		    // NOI18N
	bothCollectionRadioButton.setMnemonic(getString("MNEM_CompilerTypeBoth").charAt(0)); // NOI18N
        bothCollectionRadioButton.setSelected(true);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(bothCollectionRadioButton, gridBagConstraints);

        message1.setEditable(false);
        message1.setLineWrap(true);
        message1.setWrapStyleWord(true);
        message1.setFocusable(false);
        message1.setText(getString("TXT_CompilerTypeMsg1")); // NOI18N
        message1.setFocusable(false);
        message1.setBackground(panel.getBackground());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.insets = new java.awt.Insets(6, 0, 0, 0);
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panel2.add(message1, gridBagConstraints);

        message2.setEditable(false);
        message2.setLineWrap(true);
        message2.setWrapStyleWord(true);
        message2.setFocusable(false);
        message2.setText(getString("TXT_CompilerTypeMsg2")); // NOI18N
        message2.setFocusable(false);
        message2.setBackground(panel.getBackground());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        panel2.add(message2, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        panel.add(panel2, gridBagConstraints);

	// Create button group and add the three radio buttons ...
	buttonGroup = new ButtonGroup();
	buttonGroup.add(sunCollectionRadioButton);
	buttonGroup.add(gnuCollectionRadioButton);
	buttonGroup.add(bothCollectionRadioButton);
    }


    /** Create the widgets if not initialized. Also set the RadioButtons */
    public void addNotify() {
	if (!initialized) {
	    create();
	    initialized = true;
	    getMakefileData().setToolset(MakefileData.SUNGNU_TOOLSET_TYPE);
	    getMakefileData().setMakefileOS(MakefileData.UNIX_OS_TYPE);
	}

	if (getMakefileData().getToolset() == MakefileData.SUN_TOOLSET_TYPE) {
	    sunCollectionRadioButton.setSelected(true); 
	}
	else if (getMakefileData().getToolset() == MakefileData.GNU_TOOLSET_TYPE) {
	    gnuCollectionRadioButton.setSelected(true); 
	}
	else if (getMakefileData().getToolset() == MakefileData.SUNGNU_TOOLSET_TYPE) {
	    bothCollectionRadioButton.setSelected(true); 
	}
	else {
	    ; // FIXUP - error
	}

	super.addNotify();
	sunCollectionRadioButton.requestFocus();
    }


    public void removeNotify() {
	super.removeNotify();
	if (sunCollectionRadioButton.isSelected()) {
	    getMakefileData().setToolset(MakefileData.SUN_TOOLSET_TYPE);
	}
	else if (gnuCollectionRadioButton.isSelected()) {
	    getMakefileData().setToolset(MakefileData.GNU_TOOLSET_TYPE);
	}
	else if (bothCollectionRadioButton.isSelected()) {
	    getMakefileData().setToolset(MakefileData.SUNGNU_TOOLSET_TYPE);
	}
	else {
	    ; // FIXUP - error
	}
    }

}
