/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2003 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.form;
import java.awt.BorderLayout;
import java.awt.Color;
import java.util.ResourceBundle;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;

/**
 *
 * Dummy component used to visualize a bean 
 * which class could not be loaded.
 *
 * @author Tomas Stupka
 */
public class InvalidComponent extends JPanel {

    private JLabel label = new JLabel();
    
    public InvalidComponent() {    
        this.setBorder(BorderFactory.createLineBorder(Color.RED));        
        this.setLayout(new BorderLayout());
         
        label.setForeground(Color.RED);
        ResourceBundle bundle = FormUtils.getBundle();
        label.setText("<html><center>" + bundle.getString("CTL_LB_InvalidComponent") + "</center></html>"); // NOI18N
        add(label);                        
        
    }
        
}
