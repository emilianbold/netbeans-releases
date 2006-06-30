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
package org.netbeans.modules.jmx.mbeanwizard.listener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.NotificationTypeTableModel;
import javax.swing.JButton;
import javax.swing.JTable;

/**
 * Generic class handling the listeners which add rows to the different JTables
 * contained in the wizard
 *
 */
public class AddTableRowListener implements ActionListener{

    private AbstractJMXTableModel m = null;
    private JButton b = null;
    private JTable table = null;
    
    /**
     * Constructor
     * @param table the Jtable in which a row is to add
     * @param model the corresponding table model
     * @param remButton a reference to the remove line button
     */
    public AddTableRowListener(JTable table, AbstractJMXTableModel model, 
            JButton remButton) {
    
        this.table = table;
        this.m = model;
        this.b = remButton;
    }
    
    /**
     * Method handling what to do if the listener has been invoked
     * Here: adds a row
     * @param e an ActionEvent
     */
    public void actionPerformed(ActionEvent e) {
        
        m.addRow();
        
        // if the model has at least one line, enable the 
        // corresponding remove button
        if (m.size() != 0) 
            b.setEnabled(true);
    }
    
}
