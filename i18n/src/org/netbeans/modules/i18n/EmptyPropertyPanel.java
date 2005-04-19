/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
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
