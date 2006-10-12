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

package org.netbeans.modules.db.test.jdbcstub;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.netbeans.modules.j2ee.persistence.editor.completion.*;
import org.netbeans.test.stub.api.StubDelegate;

/**
 *
 * @author Andrei Badea
 */
public class ResultSetImpl extends StubDelegate {
    
    private List/*<List<Object>>*/ columns;
    private Map/*<String, List<Object>*/ names2iterators = new HashMap();
    private Map/*<String, Object>*/ names2values; // current row values

    public ResultSetImpl(List columns) {
        this.columns = columns;

        for (Iterator it = columns.iterator(); it.hasNext();) {
            List column = (List)it.next();
            Iterator columnIterator = column.iterator();
            String columnName = columnIterator.next().toString();
            names2iterators.put(columnName, columnIterator);
        }
    }

    public boolean next() {
        if (names2values != null) {
            names2values.clear();
        } else {
            names2values = new HashMap();
        }
        
        Iterator it = names2iterators.entrySet().iterator();
        if (!it.hasNext()) {
            return false;
        }

        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry)it.next();
            String columnName = (String)entry.getKey();
            Iterator columnIterator = (Iterator)entry.getValue();

            if (!columnIterator.hasNext()) {
                return false;
            }

            Object value = columnIterator.next();
            names2values.put(columnName, value);
        }
        
        return true;
    }
    
    public Object getObject(String columnName) throws SQLException {
        if (names2values == null) {
            throw new SQLException("The next() method has not been called yet");
        }
        if (!names2values.containsKey(columnName)) {
            throw new SQLException("Unknown column name " + columnName + ".");
        }
        return names2values.get(columnName);
    }
    
    public short getShort(String columnName) throws SQLException {
        Object value = getObject(columnName);
        if (value instanceof Short) {
            return ((Short)value).shortValue();
        } else {
            throw new SQLException(value + "is not a short.");
        }
    }
    
    public int getInt(String columnName) throws SQLException {
        Object value = getObject(columnName);
        if (value instanceof Integer){
            return ((Integer)value).intValue();
        } else {
            throw new SQLException(value + " is not an int.");
        }
    }
    
    public boolean getBoolean(String columnName) throws SQLException {
        Object value = getObject(columnName);
        if (value instanceof Boolean) {
            return ((Boolean)value).booleanValue();
        } else {
            throw new SQLException(value + " is not a boolean.");
        }
    }

    public String getString(String columnName) throws SQLException {
        Object value = getObject(columnName);
        return value != null ? value.toString() : null;
    }
    
    public void close() {
    }
}
