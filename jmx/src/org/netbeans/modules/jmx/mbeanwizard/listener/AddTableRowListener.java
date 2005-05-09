/*
 * AddAttributeListener.java
 *
 * Created on March 2, 2005, 6:49 PM
 */

package org.netbeans.modules.jmx.mbeanwizard.listener;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.AbstractJMXTableModel;
import org.netbeans.modules.jmx.mbeanwizard.tablemodel.NotificationTypeTableModel;
import javax.swing.JButton;
import javax.swing.JTable;

/**
 *
 * @author an156382
 */
public class AddTableRowListener implements ActionListener{ 
    
    private AbstractJMXTableModel m = null;
    private JButton b = null;
    private JTable table = null;
    
    /** Creates a new instance of AddAttributeListener */
    public AddTableRowListener(JTable table, AbstractJMXTableModel model, 
            JButton remButton) {
    
        this.table = table;
        this.m = model;
        this.b = remButton;
    }
    
    public void actionPerformed(ActionEvent e) {
        
        m.addRow();
        
        // if the model has at least one line, enable the 
        // corresponding remove button
        if (m.size() != 0) 
            b.setEnabled(true);
    }
    
}
