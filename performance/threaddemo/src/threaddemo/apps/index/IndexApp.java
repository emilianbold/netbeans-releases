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

package threaddemo.apps.index;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import threaddemo.locking.LockAction;
import threaddemo.model.Phadhail;

/**
 * Launcher + GUI class for the index.
 * @author Jesse Glick
 */
public class IndexApp extends JFrame {
    
    private final Index index;
    private final DefaultTableModel tableModel;
    
    public IndexApp(Phadhail root) {
        super("XML Element Index [" + root.getPath() + "]");
        index = new IndexImpl(root);
        index.addChangeListener(new ChangeListener() {
            public void stateChanged(ChangeEvent ev) {
                refreshTable();
            }
        });
        tableModel = new DefaultTableModel(0, 2);
        DefaultTableColumnModel columns = new DefaultTableColumnModel();
        TableColumn column = new TableColumn(0);
        column.setHeaderValue("XML Element Name");
        columns.addColumn(column);
        column = new TableColumn(1);
        column.setHeaderValue("Occurrences");
        columns.addColumn(column);
        index.start();
        getContentPane().add(new JScrollPane(new JTable(tableModel, columns)));
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent ev) {
                index.cancel();
            }
        });
        pack();
    }
    
    private void refreshTable() {
        final SortedMap<String,Integer> data = index.getLock().read(new LockAction<SortedMap<String,Integer>>() {
            public SortedMap<String,Integer> run() {
                return new TreeMap<String,Integer>(index.getData());
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // XXX clumsy
                int rows = tableModel.getRowCount();
                for (int i = 0; i < rows; i++) {
                    tableModel.removeRow(0);
                }
                for (Map.Entry<String, Integer> entry : data.entrySet()) {
                    tableModel.addRow(new Object[] {
                        entry.getKey(),
                        entry.getValue(),
                    });
                }
            }
        });
    }
    
}
