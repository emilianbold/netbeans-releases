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

package org.netbeans.modules.i18n;

import java.util.MissingResourceException;
import org.openide.util.NbBundle;
import javax.swing.BoxLayout;
import javax.swing.Box;

/**
 * @author  or141057
 */
public class EmptyPropertyPanel extends javax.swing.JPanel {

    /** Creates new form EmptyPropertyPanel */
    public EmptyPropertyPanel() {
        initComponents();
    }

    /** This method is called from within the constructor to
     * initialize the form.
     */
    private void initComponents() {
        theLabel = new javax.swing.JLabel();

        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        add(Box.createVerticalStrut(100));

        theLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        theLabel.setText("....");
        theLabel.setAlignmentX(0.5f);
        theLabel.setAlignmentY(0.5f);
        add(theLabel);
    }
    
    
    private javax.swing.JLabel theLabel;
    
    public void setBundleText(String textID) throws MissingResourceException {
        theLabel.setText(NbBundle.getMessage(EmptyPropertyPanel.class, textID));
    }
    
    public String getText() {
        return theLabel.getText();
    }
}
