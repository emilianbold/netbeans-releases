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

package org.netbeans.modules.httpserver;

import java.awt.Component;
import javax.swing.*;
import org.openide.awt.Mnemonics;
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
        
        Mnemonics.setLocalizedText(jCheckBox1, NbBundle.getMessage (GrantAccessPanel.class, "CTL_DNSTDNT"));
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
