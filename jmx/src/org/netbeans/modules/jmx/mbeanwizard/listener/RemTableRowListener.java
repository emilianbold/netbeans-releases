/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 2004-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.jmx.mbeanwizard.listener;
import org.netbeans.modules.jmx.WizardHelpers;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.SwingUtilities;

/**
 *
 * @author an156382
 */
public class RemTableRowListener implements ActionListener{ 
    
    private JTable t = null;
    private AbstractJMXTableModel m = null;
    private JButton b = null;
    
    /** Creates a new instance of AddAttributeListener */
    public RemTableRowListener(JTable table, AbstractJMXTableModel model, JButton remButton) {
    
        this.t = table;
        m = model;
        b = remButton;
    }
    
    public void actionPerformed(ActionEvent e) {
        final int selectedRow = t.getSelectedRow();
        
        //No row selected
        if (selectedRow == -1) return;
        
        WizardHelpers.printModel(m, 0);
        try {            
            m.remRow(selectedRow, t);
            m.selectNextRow(selectedRow, t);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        
        // if the model has no rows, disable the remove button
        if (m.size() == 0)
            b.setEnabled(false);

        WizardHelpers.printModel(m, 0);
    }
}
