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
import java.util.*;
import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.*;
import org.openide.util.Mutex;
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
        final SortedMap/*<String,int>*/ data = (SortedMap)index.getMutex().readAccess(new Mutex.Action() {
            public Object run() {
                return new TreeMap(index.getData());
            }
        });
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                // XXX clumsy
                int rows = tableModel.getRowCount();
                for (int i = 0; i < rows; i++) {
                    tableModel.removeRow(0);
                }
                Iterator it = data.entrySet().iterator();
                while (it.hasNext()) {
                    Map.Entry entry = (Map.Entry)it.next();
                    tableModel.addRow(new Object[] {
                        entry.getKey(),
                        entry.getValue(),
                    });
                }
            }
        });
    }
    
}
