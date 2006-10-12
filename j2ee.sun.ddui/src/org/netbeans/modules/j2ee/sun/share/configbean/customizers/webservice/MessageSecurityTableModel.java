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
/*
 * MessageSecurityTableModel.java
 *
 * Created on April 24, 2006, 3:56 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers.webservice;

import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;
import org.netbeans.modules.j2ee.sun.dd.api.common.MessageSecurity;

/**
 *
 * @author Peter Williams
 */
public class MessageSecurityTableModel extends AbstractTableModel {
    
//    private String [] columnNames = { "Operation / Java Method", "Request Protection", "Response Protection" };
    private static final String [] columnNames = { "Operation", "Req Source", "Req Target", "Resp Source", "Resp Target" };
    
    /** Hashset of all the rows.  Stores instances of MessageSecurity
     */
    private ArrayList rowData;
    
    public MessageSecurityTableModel(MessageSecurity [] ms) {
        if(ms != null) {
            rowData = new ArrayList(ms.length);
            for(int i = 0; i < ms.length; i++) {
                rowData.add(ms[i]);
            }
        } else {
            rowData = new ArrayList();
        }
    }
    
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object result = null;
        if(rowIndex >= 0 && rowIndex < rowData.size()) {
            MessageSecurity row = (MessageSecurity) rowData.get(rowIndex);
            if(row != null) {
                result = getFieldByColumn(row, columnIndex);
            }
        }
        return result;
    }

    public int getRowCount() {
        return rowData.size();
    }

    public int getColumnCount() {
        return columnNames.length;
    }

    public Class getColumnClass(int columnIndex) {
        return String.class;
    }

    public String getColumnName(int column) {
        assert column < 0 || column > columnNames.length;
        return (column >= 0 && column < columnNames.length) ? columnNames[column] : "unknown";
    }

    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
    }

    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return (columnIndex > 0) ? true : false;
    }

    private String getFieldByColumn(MessageSecurity row, int columnIndex) {
        assert columnIndex < 0 || columnIndex > columnNames.length;
        switch(columnIndex) {
            case 0:
                return row.getMessage(0).getOperationName();
            case 1:
                return row.getRequestProtectionAuthSource();
            case 2: 
                return row.getRequestProtectionAuthRecipient();
            case 3:
                return row.getResponseProtectionAuthSource();
            case 4:
                return row.getResponseProtectionAuthRecipient();
        }
        return null;
    }

    private void setFieldByColumn(MessageSecurity row, int columnIndex, String field) {
    }
}
