/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.httpserver;

import java.awt.Component;
import javax.swing.*;
import org.openide.util.NbBundle;

/**
* message panel for granting access to internal HTTP server.
* @author Radim Kubacki
*/
class GrantAccessPanel extends javax.swing.JPanel {
    
    private String msg;

    /** Creates new panel */
    public GrantAccessPanel (String msg) {
        this.msg = msg;
        initComponents ();
    }

    private void initComponents() {
        JTextArea localTopMessage = new javax.swing.JTextArea();

        jCheckBox1 = new javax.swing.JCheckBox();
        
        setLayout(new java.awt.BorderLayout(0, 12));
        
        setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 0)));
        getAccessibleContext().setAccessibleDescription(msg);
        
        localTopMessage.setLineWrap (true);
        localTopMessage.setWrapStyleWord (true);
        localTopMessage.setEditable (false);
        localTopMessage.setEnabled (false);
        localTopMessage.setOpaque (false);
        localTopMessage.setDisabledTextColor (javax.swing.UIManager.getColor ("Label.foreground"));  // NOI18N
        localTopMessage.setFont (javax.swing.UIManager.getFont ("Label.font")); // NOI18N

        StringBuffer lTopMessage = new StringBuffer();
        lTopMessage.append(msg);
        localTopMessage.setText(lTopMessage.toString());
        add(localTopMessage, java.awt.BorderLayout.NORTH);
        
        jCheckBox1.setMnemonic(NbBundle.getMessage (GrantAccessPanel.class, "CTL_DNSTDNT_Mnemonic").charAt (0));
        jCheckBox1.setText(NbBundle.getMessage (GrantAccessPanel.class, "CTL_DNSTDNT"));
        jCheckBox1.getAccessibleContext().setAccessibleDescription(NbBundle.getMessage (GrantAccessPanel.class, "ACSD_CTL_DNSTDNT"));
        add(jCheckBox1, java.awt.BorderLayout.SOUTH);
    }

    private javax.swing.JLabel jLabel;
    private javax.swing.JCheckBox jCheckBox1;

    // main methods ....................................................................................

    public void setShowDialog (boolean show) {
        jCheckBox1.setSelected (!show);
    }

    public boolean getShowDialog () {
        return !jCheckBox1.isSelected ();
    }
}
