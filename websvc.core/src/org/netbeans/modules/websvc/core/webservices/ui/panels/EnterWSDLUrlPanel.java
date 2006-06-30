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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.websvc.core.webservices.ui.panels;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JComboBox;
import org.openide.util.NbBundle;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.Dimension;

public class EnterWSDLUrlPanel extends JPanel {
    private String defaultWSDLUrl;

    public EnterWSDLUrlPanel(String defaultWSDLUrl) {
        this.defaultWSDLUrl = defaultWSDLUrl;
        initComponents();
        populateWSDLUrls();

    }
    
    private void populateWSDLUrls() {
        String[] urls = new String[]{defaultWSDLUrl};  //FIX-ME:what else shd we include?
        for(int i = 0; i < urls.length; i++) {
            wsdlURLComboBox.addItem(urls[i]);
        }
    }
    
    public String getSelectedWSDLUrl() {
        return wsdlURLComboBox.getSelectedItem().toString();
    }
    
    private void initComponents() {
        inputLabel = new JLabel(NbBundle.getMessage(EnterWSDLUrlPanel.class, "LBL_Input_WSDL_Url"));
        wsdlURLComboBox = new JComboBox();
        wsdlURLComboBox.setEditable(true);
        
        setLayout(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(6,6,6,6);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        add(inputLabel, gbc);
        gbc.gridy = 1;
        gbc.weightx = 2.0;
        add(wsdlURLComboBox, gbc);
    }
    
    private JLabel inputLabel;
    private JComboBox wsdlURLComboBox;
    
}
