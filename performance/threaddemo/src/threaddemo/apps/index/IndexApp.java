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
