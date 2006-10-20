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

package org.netbeans.modules.identity.profile.api.configurator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.TableModel;

/**
 * This Modifier is used to synchronize the value between a JTable used for
 * multi-select and a Configurable.
 *
 * Created on July 14, 2006, 4:19 PM
 * 
 * @author ptliu
 */
class MultiSelectTableModifier extends Modifier {
 
    private JTable table;

    /** Creates a new instance of TableModifier */
    public MultiSelectTableModifier(final Enum configurable, final JTable table,
            final Configurator configurator) {
        super(configurable, table, configurator);
        
        this.table = table;
        
        setValue(configurator.getValue(configurable));
        
        final ListSelectionModel selectionModel = table.getSelectionModel();
        
        selectionModel.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                //Ignore extra messages.
                if (e.getValueIsAdjusting()) return;
                
                if (selectionModel.isSelectionEmpty()) {
                    configurator.setValue(configurable, Collections.EMPTY_LIST);
                } else {
                    int[] selectedRows = table.getSelectedRows();
                    TableModel model = table.getModel();
                    List values = new ArrayList();
                    
                    for (int i = 0; i < selectedRows.length; i++) {
                        int row = selectedRows[i];
                        Object value = model.getValueAt(row, 0);
                        values.add(value);
                    }
                    
                    configurator.setValue(configurable, values);
                }
            }
        });
    }
    
    public void setValue(Object value) {
        table.clearSelection();
        
        if (value == null) return;
        
        if (value instanceof Collection) {
            TableModel model = table.getModel();
            int rowCount = model.getRowCount();
            
            for (int i = 0; i < rowCount; i++) {
                Object rowValue = model.getValueAt(i, 0);
                Iterator iter = ((Collection) value).iterator();
              
                while (iter.hasNext()) {
                    if (rowValue.equals(iter.next())) {
                        table.changeSelection(i, 0, true, false);
                        break;
                    }
                }
            }
        } else {
            // should throw an exception.
        }   
    }
    
    public Object getValue() {
        int[] indices = table.getSelectedRows();
        ArrayList<String> rows = new ArrayList<String>();
        TableModel model = table.getModel();
        
        for (int i : indices) {
            rows.add((String) model.getValueAt(i, 0));
        }
        
        return rows;
    }
    
}
