/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2002 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.web.wizards;

import java.awt.Color;
import java.awt.GridBagConstraints; 
import java.awt.Insets;
import java.awt.event.FocusEvent; 
import java.awt.event.FocusAdapter; 
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/* 
 * Wizard panel that collects deployment data for Servlets and Filters
 * @author Ana von Klopp 
 */

class DeployDataPanel extends BaseWizardPanel implements ItemListener, 
							 KeyListener { 

    private TargetEvaluator evaluator = null; 

    private ServletData deployData; 
    private FileType fileType; 
    private boolean edited = false;

    private static final boolean debug = false; 

    public DeployDataPanel(TargetEvaluator e) { 
	    
	evaluator = e; 
	fileType = evaluator.getFileType(); 
	deployData = (ServletData)(evaluator.getDeployData()); 
	setName(NbBundle.getMessage(DeployDataPanel.class, 
				    "TITLE_ddpanel_".concat(fileType.toString()))); 
	getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataPanel.class, "ACSD_deployment"));
	initComponents ();
	fireChangeEvent();
    }

    private void initComponents () {

	if(debug) log("::initComponents()"); //NOI18N
	// Layout description
        setPreferredSize(new java.awt.Dimension(450, 250));
	setLayout(new java.awt.GridBagLayout());

	// Entity covers entire row
	GridBagConstraints fullRowC = new GridBagConstraints();
	fullRowC.gridx = 0;                               
	fullRowC.gridy = GridBagConstraints.RELATIVE;     
	fullRowC.gridwidth = 8; 
	fullRowC.anchor = GridBagConstraints.WEST;         
	fullRowC.fill = GridBagConstraints.HORIZONTAL; 
	fullRowC.insets = new Insets(4, 0, 4, 60);

	// Initial label
	GridBagConstraints firstC = new GridBagConstraints();
	firstC.gridx = 0;
	firstC.gridy = GridBagConstraints.RELATIVE;     
	firstC.gridwidth = 1; 
	firstC.anchor = GridBagConstraints.WEST; 
	firstC.insets = new Insets(4, 20, 4, 0);
	//firstC.weighty = 0.1; 

	// Long textfield
	GridBagConstraints tfC = new GridBagConstraints();
	tfC.gridx = GridBagConstraints.RELATIVE;
	tfC.gridy = 0; 
	tfC.gridwidth = 7; 
	tfC.fill = GridBagConstraints.HORIZONTAL;     
	tfC.insets = new Insets(4, 20, 4, 60);

	// Short textfield
	GridBagConstraints stfC = new GridBagConstraints();
	stfC.gridx = GridBagConstraints.RELATIVE;
	stfC.gridy = 0; 
	//stfC.gridwidth = 7; 
	stfC.gridwidth = 5; 
	stfC.weightx = 1.0; 
	stfC.fill = GridBagConstraints.HORIZONTAL;     
	stfC.insets = new Insets(4, 20, 4, 0);

	// Table panel 
	GridBagConstraints tablePanelC = new GridBagConstraints();
	tablePanelC.gridx = 0;
	tablePanelC.gridy = GridBagConstraints.RELATIVE;
	tablePanelC.gridwidth = 8;
	tablePanelC.fill = GridBagConstraints.BOTH;
	tablePanelC.weightx = 1.0;
	tablePanelC.weighty = 1.0;
	tablePanelC.insets = new Insets(4, 20, 4, 0);

	// Component Initialization by row
	// 1. Instruction
	jLinstruction = new JLabel(NbBundle.getMessage(DeployDataPanel.class, "LBL_dd_".concat(fileType.toString()))); 
	this.add(jLinstruction, fullRowC); 

	// 2. Checkbox row - add this? 

	tfC.gridy++; 
	// PENDING - whether it's selected needs to depend on the
	// previous panel... 
	jCBservlet = new JCheckBox(NbBundle.getMessage(DeployDataPanel.class, "LBL_addtodd"), true);
	jCBservlet.setMnemonic(NbBundle.getMessage(DeployDataPanel.class, "LBL_add_mnemonic").charAt (0));
	jCBservlet.addItemListener(this);
	jCBservlet.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataPanel.class, "ACSD_addtodd")); // NOI18N	

	this.add(jCBservlet, fullRowC); 	    

	// 3. Classname
	tfC.gridy++; 
	jTFclassname = new JTextField(25);
	jTFclassname.setEnabled(false); 
	jTFclassname.setBackground(this.getBackground()); 
	jLclassname = new JLabel(NbBundle.getMessage(DeployDataPanel.class, "LBL_ClassName"));
	jLclassname.setLabelFor(jTFclassname);
	jLclassname.setDisplayedMnemonic(NbBundle.getMessage(DeployDataPanel.class, "LBL_Class_Mnemonic").charAt(0));
	jTFclassname.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataPanel.class,"ACSD_ClassName"));
	    
	this.add(jLclassname, firstC); 
	this.add(jTFclassname, tfC); 

	// 4. Servlet or filter name 
	tfC.gridy++; 
	jTFname = new JTextField(25);
	jTFname.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataPanel.class,"ACSD_name_".concat(fileType.toString()))); 
	jTFname.addKeyListener (this);
	jTFname.unregisterKeyboardAction
	    (KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
	jTFname.addFocusListener(new FocusAdapter() {
		public void focusGained(FocusEvent evt) {
		    if(debug) log("\tjTFname got focus");  //NOI18N
		    jTFname.selectAll(); 
		}
	    }); 
	    
	jLname = new JLabel(NbBundle.getMessage(DeployDataPanel.class, "LBL_name_".concat(fileType.toString()))); 
	jLname.setLabelFor(jTFname);
	jLname.setDisplayedMnemonic(NbBundle.getMessage(DeployDataPanel.class, "LBL_name_".concat(fileType.toString()).concat("_mnem")).charAt(0));

	this.add(jLname, firstC); 
	this.add(jTFname, tfC); 

	// 5. URL Mappings (servlet only)
	if(fileType == FileType.SERVLET) {
	    tfC.gridy++; 
	    jTFmapping = new JTextField(25);
	    jTFmapping.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(DeployDataPanel.class,"ACSD_url_mapping"));
	    jTFmapping.addKeyListener (this);
	    jTFmapping.unregisterKeyboardAction
		(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0));
	    jTFmapping.addFocusListener(new FocusAdapter() {
		    public void focusGained(FocusEvent evt) {
			if(debug) log("\tjTFmapping got focus");  //NOI18N
			jTFmapping.selectAll(); 
		    }
		    public void focusLost(FocusEvent evt) {
			if(debug) log("\tjTFmapping lost focus");  //NOI18N
			deployData.parseUrlMappingString(jTFmapping.getText().trim()); 
			fireChangeEvent(); 
		    }
		}); 
	    
	    jLmapping = new JLabel(NbBundle.getMessage(DeployDataPanel.class, "LBL_url_mapping"));
	    jLmapping.setLabelFor (jTFmapping);
	    jLmapping.setDisplayedMnemonic(NbBundle.getMessage (DeployDataPanel.class, "LBL_mapping_mnemonic").charAt (0));

	    this.add(jLmapping, firstC); 
	    this.add(jTFmapping, tfC); 
	}

	// 7. Init parameter
	if(fileType == FileType.SERVLET) { 
	    paramPanel = new InitParamPanel(deployData, this); 
	    this.add(paramPanel, tablePanelC); 
	} 
	else if(fileType == FileType.FILTER) { 
	    mappingPanel = new MappingPanel(deployData, this); 
	    this.add(mappingPanel, tablePanelC); 
	}
	// Add vertical filler at the bottom
	GridBagConstraints fillerC = new GridBagConstraints();
	fillerC.gridx = 0;
	fillerC.gridy = GridBagConstraints.RELATIVE;     
	fillerC.weighty = 1.0;
        fillerC.fill = GridBagConstraints.BOTH;   
	this.add(new javax.swing.JPanel (), fillerC);
    }

    void setData() { 
	if(debug) log("::setData()"); //NOi18N

	deployData.setClassName(evaluator.getClassName()); 
	jTFclassname.setText(deployData.getClassName());
		
	if(!edited) { 
	    if(debug) log("\tUser has not edited dd data yet"); //NOi18N

	    deployData.setName(evaluator.getFileName()); 
	    if(fileType == FileType.SERVLET) { 
		if(debug) log("\tData type is servlet"); //NOi18N
		deployData.parseUrlMappingString("/" + //NOI18N
						 evaluator.getFileName()); 
	    } 
	}

	jTFname.setText(deployData.getName()); 

	if(fileType == FileType.SERVLET) 
	    jTFmapping.setText(deployData.getUrlMappingsAsString()); 
	else if(fileType == FileType.FILTER) 
	    mappingPanel.setData(); 
    } 

    public void itemStateChanged (java.awt.event.ItemEvent itemEvent) {
	if(itemEvent.getSource() == jCBservlet) { 
	    boolean enabled = 
		(itemEvent.getStateChange() == ItemEvent.SELECTED); 
	    enableInput(enabled); 
	    deployData.setMakeEntry(enabled);
            deployData.setAddToDD(enabled);
            if(fileType == FileType.SERVLET) {
                paramPanel.setEnabled();
            }
	}
	fireChangeEvent ();
    }

    private void enableInput(boolean enable) { 

	if(debug) log("::enableInput()"); 

	jTFname.setEnabled(enable);
	jLinstruction.setEnabled(enable);
	jLclassname.setEnabled(enable);
	jLname.setEnabled(enable);
	if(fileType == FileType.SERVLET) {
	    jTFmapping.setEnabled(enable);
	    jLmapping.setEnabled(enable);
	    paramPanel.setEnabled(enable); 
	}
	else if (fileType == FileType.FILTER) {
	    mappingPanel.setEnabled(enable); 
	} 

	if(enable) { 
	    jTFclassname.setDisabledTextColor(Color.black); 
	    jTFclassname.repaint(); 
	    jTFname.setBackground(Color.white); 
	    if(fileType == FileType.SERVLET) 
		jTFmapping.setBackground(Color.white); 
	} 
	else { 
	    jTFclassname.setDisabledTextColor
		(jTFname.getDisabledTextColor()); 
	    jTFclassname.repaint(); 
	    jTFname.setBackground(this.getBackground()); 
	    if(fileType == FileType.SERVLET) 
		jTFmapping.setBackground(this.getBackground()); 
	} 
    } 

    public void keyPressed (java.awt.event.KeyEvent keyEvent) {
    }
        
    public void keyReleased (java.awt.event.KeyEvent keyEvent) {
	edited = true; 
	if(keyEvent.getSource() == jTFname) {
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			// PENDING - this is way too heavy weight,
			// just append until we get the focus lost.
			deployData.setName(jTFname.getText().trim()); 
			if(fileType == FileType.FILTER) 
			    mappingPanel.setData(); 
			fireChangeEvent();
		    }
		});
	    return;
	}
	else if(keyEvent.getSource() == jTFmapping) {
	    SwingUtilities.invokeLater(new Runnable() {
		    public void run() {
			// PENDING - this is way too heavy weight,
			// just append until we get the focus lost.
			deployData.parseUrlMappingString(jTFmapping.getText().trim()); 
			fireChangeEvent();
		    }
		});
	    return;
	}
	fireChangeEvent();
    }
        
    public void keyTyped (java.awt.event.KeyEvent keyEvent) {
    }
    

    public void log(String s) { 
	System.out.println("DeployDataPanel" + s); //NOI18N
    } 


    public HelpCtx getHelp() {
        return new HelpCtx(this.getClass().getName()+"."+evaluator.getFileType().toString()); //NOI18N
    }

    // Variables declaration
    private JCheckBox  jCBservlet;
    private JTextField jTFclassname;
    private JTextField jTFname;
    private JTextField jTFmapping;
    private JLabel jLinstruction;
    private JLabel jLclassname;
    private JLabel jLname;
    private JLabel jLmapping;
    private InitParamPanel paramPanel; 
    private MappingPanel mappingPanel; 
    
    private static final long serialVersionUID = -2704206901170711687L;
    
} 
