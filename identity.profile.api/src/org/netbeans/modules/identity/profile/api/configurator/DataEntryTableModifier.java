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

import java.util.Collection;
import java.util.Vector;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;

/**
 * This Modifier is used to synchronize the value between a JTable used for
 * data entry and a Configurable. 
 *
 * Created on July 14, 2006, 4:19 PM
 *
 * @author ptliu
 */
class DataEntryTableModifier extends Modifier {
    
    private DataEntryTableModel model;
    
    /** Creates a new instance of TableModifier */
    public DataEntryTableModifier(final Enum configurable, final JTable table,
            final Configurator configurator) {
        super(configurable, table, configurator);
 
        this.model = (DataEntryTableModel) table.getModel();
     
        setValue(configurator.getValue(configurable));
        
        model.addTableModelListener(new TableModelListener() {
            public void tableChanged(TableModelEvent event) {
                configurator.setValue(configurable, getValue());
            }
        });
    
    }
    
    public void setValue(Object value) {
        Collection<Vector> vectors = (Collection<Vector>) value;
        
        for (Vector v: vectors) {
            model.addRow(v);
        }
    }
    
    
    public Object getValue() {
        return model.getDataVector();
    }
}
