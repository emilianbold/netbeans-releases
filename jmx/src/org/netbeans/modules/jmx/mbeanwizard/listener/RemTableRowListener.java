/*
 * RemTableRowListener.java
 *
 * Created on March 3, 2005, 10:41 AM
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
