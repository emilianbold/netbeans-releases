/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 *
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.popup;

import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import java.awt.Dialog;
import java.awt.GridLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import org.openide.util.NbBundle;
import java.awt.Insets;
import java.awt.event.*;
import java.util.ResourceBundle;
import org.openide.awt.Mnemonics;

/**
 *
 * Abstract class which implements the generic behaviour of any popup window
 */
public abstract class AbstractPopup extends JDialog implements ActionListener {
    
    protected final int POPUP_WIDTH = 500;
    protected final int POPUP_HEIGHT = 250;
    
    protected ResourceBundle bundle;
    
    protected JTable popupTable;
    protected AbstractJMXTableModel popupTableModel;
    
    protected JTextField textFieldToFill;
    
    protected JButton addJButton;
    protected JButton removeJButton;
    protected JButton closeJButton;
    protected JButton cancelJButton;
    private int winWidth;
    
    /**
     * Constructor
     * @param d the parent dialog window; gives a relationship parent child
     */
    public AbstractPopup(Dialog d) {
        super(d);
        bundle = NbBundle.getBundle(AbstractPopup.class);
    }
    
    /**
     * Sets all the dimensions for the popup (since all popups have the same 
     * size)
     * @param str the title of the popup to set
     */
    protected void setDimensions(String str) {
        setTitle(str);
        setModal(true);
        setResizable(false);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setBounds(350,250,winWidth,POPUP_HEIGHT);
        setVisible(true);
    }
    
    /**
     * Action to make if an action event is caught
     * @param e an Action event
     */
    public void actionPerformed(ActionEvent e) {
        setVisible(false);
    }
    
    protected JButton instanciatePopupButton(String s) {
        JButton button = new JButton();
        Mnemonics.setLocalizedText(button, s);
        return button;
    }
    
    protected void definePanels(JButton[] popupButtons, JTable table, String tableName) {
        java.awt.GridBagLayout mainGridBag = new java.awt.GridBagLayout();
        
        getContentPane().setLayout(mainGridBag);
        java.awt.GridBagConstraints c1 = new java.awt.GridBagConstraints();
        c1.fill = java.awt.GridBagConstraints.BOTH;
        c1.gridwidth = 2;
        c1.weightx = 1.0;
        c1.weighty = 1.0;
        c1.gridx = 1;
        c1.gridy = 1;
        c1.insets = new Insets(0,12,12,12);
        
        // panel definition
        JScrollPane tablePanel = new JScrollPane(table);
        mainGridBag.setConstraints(tablePanel, c1);
        
        winWidth = 12+5+17+5+12 + 100;
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 1, 5, 0));

        //cancelJButton = new JButton(NbBundle.getMessage(AbstractPopup.class,"LBL_Generic_Cancel"));// NOI18N
        cancelJButton = new JButton(bundle.getString("LBL_Generic_Cancel"));// NOI18N
        cancelJButton.setMargin(new Insets(cancelJButton.getMargin().top, 12,
                cancelJButton.getMargin().bottom, 12));
        winWidth = 
                winWidth + (int) cancelJButton.getPreferredSize().getWidth();
        
        cancelJButton.setName("cancelJButton");// NOI18N
        cancelJButton.addActionListener(this);
        for (int i = 0; i < popupButtons.length; i++) {
            ((JButton)popupButtons[i]).setMargin(
                    new Insets(((JButton)popupButtons[i]).getMargin().top, 12, 
                    ((JButton)popupButtons[i]).getMargin().bottom, 12));
            winWidth = 
                winWidth + (int) ((JButton)popupButtons[i]).getPreferredSize().
                    getWidth();
            if(popupButtons[i] != closeJButton) {
                buttonPanel.add((JButton)popupButtons[i]);
            }
        }
        
        JPanel standardButtonPanel = new JPanel();
        standardButtonPanel.setLayout(new GridLayout(1,1, 5, 0));
        standardButtonPanel.add(closeJButton);
        standardButtonPanel.add(cancelJButton);
        
        //JPanel extStandardButtonPanel = new JPanel();
        //extStandardButtonPanel.setLayout(new FlowLayout(FlowLayout.RIGHT));
        
        //extStandardButtonPanel.add(standardButtonPanel);
        //JPanel allButtons = new JPanel();
        //java.awt.GridBagLayout gridbag = new java.awt.GridBagLayout();
        //java.awt.GridBagConstraints c = new java.awt.GridBagConstraints();
        //c.fill = java.awt.GridBagConstraints.BOTH;
        //c.weightx = 1.5;
        //allButtons.setLayout(gridbag);
        //gridbag.setConstraints(buttonPanel, c);
        //allButtons.add(buttonPanel);
        //c.weightx = 1;
        //gridbag.setConstraints(extStandardButtonPanel, c);
        //allButtons.add(extStandardButtonPanel);
        
        add(tablePanel);
        
        java.awt.GridBagConstraints c2 = new java.awt.GridBagConstraints();
        c2.fill = java.awt.GridBagConstraints.NONE;
        c2.anchor = java.awt.GridBagConstraints.WEST;
        c2.gridwidth = 0;
        c2.weightx = 0;
        c2.weighty = 0;
        c2.gridx = 0;
        c2.gridy = 0;
        c2.insets = new Insets(12,12,5,12);
        
        javax.swing.JLabel tableLabel = new javax.swing.JLabel();
        Mnemonics.setLocalizedText(tableLabel, tableName);
        tableLabel.getAccessibleContext().setAccessibleName(bundle.getString("ACCESS_POPUP_TABLE_NAME"));// NOI18N
        tableLabel.getAccessibleContext().setAccessibleDescription(bundle.getString("ACCESS_POPUP_TABLE_NAME_DESCRIPTION"));// NOI18N
        tableLabel.setLabelFor(table);
        mainGridBag.setConstraints(tableLabel, c2);
        add(tableLabel);
        
        c1.gridwidth = 1;
        c1.fill = java.awt.GridBagConstraints.BOTH;
        c1.weightx = 0.5;
        c1.weighty = 0.0;
        c1.gridx = 1;
        c1.gridy = 2;
        c1.insets = new Insets(0,12,12,12);
        
        mainGridBag.setConstraints(buttonPanel, c1);
        add(buttonPanel);
        
        c1.gridx = 2;
        mainGridBag.setConstraints(standardButtonPanel, c1);
        add(standardButtonPanel);
    }
    
    protected abstract void initJTable();
    protected abstract void initComponents();
    protected abstract void readSettings();
    
    /**
     * Method which stores the information contained in the popup
     * @return String containing the infomation formatted
     */
    public abstract String storeSettings();
    
}
