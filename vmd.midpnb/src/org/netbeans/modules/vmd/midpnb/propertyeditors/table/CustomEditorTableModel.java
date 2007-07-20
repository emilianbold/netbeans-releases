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
 * Portions Copyrighted 2007 Sun Microsystems, Inc.
 */
package org.netbeans.modules.vmd.midpnb.propertyeditors.table;

import java.util.Vector;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Anton Chechel
 */
class CustomEditorTableModel extends DefaultTableModel {
    
    public void removeLastColumn() {
        int columnCount = getColumnCount();
        if (columnCount > 0) {
            columnIdentifiers.remove(columnCount - 1);
            int size = dataVector.size();
            for (int i = 0; i < size; i++) {
                Vector row = (Vector) dataVector.elementAt(i);
                row.remove(columnCount - 1);
            }
            
            fireTableStructureChanged();
        }
    }
}
