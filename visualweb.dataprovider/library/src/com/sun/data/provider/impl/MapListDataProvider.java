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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */


package com.sun.data.provider.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import com.sun.data.provider.DataListener;
import com.sun.data.provider.TableDataProvider;
import com.sun.data.provider.TableDataListener;

public class MapListDataProvider {} /*implements TableDataProvider {

    protected List list;

    public MapListDataProvider() {}

    public MapListDataProvider(List list) {
        this.list = list;
    }

    public void setList(List list) {
        this.list = list;
    }

    public List getList() {
        return list;
    }

    public Map getMap(RowKey row) {
        return (Map)list.get(row);
    }

    public void setMap(RowKey row, Map map) {
        list.set(row, map);
    }

    public Object getData(String fieldKey) {
        return getData(cursorIndex, fieldKey);
    }

    public Class getDataType(String fieldKey) {
        return null;
    }

    public boolean isReadOnly(String fieldKey) {
        return false;
    }

    public void setData(String fieldKey, Object data) {
        setData(cursorIndex, fieldKey, data);
    }

    public Object getData(RowKey row, String fieldKey) {
        Object o = list.get(row);
        if (o instanceof Map) {
            Map map = (Map)o;
            return map.get(fieldKey);
        }
        return o; // ignore fieldKey if the list item isn't a map
    }

    public Class getDataType(RowKey row, String fieldKey) {
        return null;
    }

    public boolean isReadOnly(RowKey row, String fieldKey) {
        return false;
    }

    public void setData(RowKey row, String fieldKey, Object data) {
        Object o = list.get(row);
        if (o instanceof Map) {
            Map map = (Map)o;
            map.put(fieldKey, data);
        } else {
            HashMap map = new HashMap();
            map.put(fieldKey, data);
            list.set(row, map);
        }
        fireDataChanged(row, fieldKey, data);
        if (row == cursorIndex) {
            fireDataChanged(fieldKey, data);
        }
    }

    protected int cursorIndex = 0;
    public int getCursorIndex() {
        return cursorIndex;
    }

    public void setCursorIndex(RowKey row) {
        if (row < 0 || row >= list.size()) {
            throw new IllegalArgumentException("Invalid row: " + row + ".  Size is " +
                list.size());
        }
        int oldRow = this.cursorIndex;
        fireCursorIndexChanging(oldRow, row);
        this.cursorIndex = row;
        fireCursorIndexChanged(oldRow, cursorIndex);
    }

    public void cursorNext() {
        setCursorIndex(cursorIndex + 1);
    }

    public void cursorPrevious() {
        setCursorIndex(cursorIndex - 1);
    }

    public int getRowCount() {
        return list.size();
    }

    public String[] getFieldKeys() {
        Object o = list.get(cursorIndex);
        if (o instanceof Map) {
            Map map = (Map)o;
            Set keys = map.keySet();
            return (String[])keys.toArray(new String[keys.size()]);
        }
        return null;
    }

    protected ArrayList dsEars = new ArrayList();
    public void addDataListener(DataListener dsl) {
        dsEars.add(dsl);
    }

    public void removeDataListener(DataListener dsl) {
        dsEars.remove(dsl);
    }

    public DataListener[] getDataListeners() {
        return (DataListener[])dsEars.toArray(new DataListener[dsEars.size()]);
    }

    protected ArrayList vdsEars = new ArrayList();
    public void addTableDataListener(TableDataListener vdsl) {
        vdsEars.add(vdsl);
    }

    public void removeTableDataListener(TableDataListener vdsl) {
        vdsEars.remove(vdsl);
    }

    public TableDataListener[] getTableDataListeners() {
        return (TableDataListener[])vdsEars.toArray(new TableDataListener[
            vdsEars.size()]);
    }

    protected void fireDataChanged(String fieldKey, Object data) {
        DataListener[] dsls = getDataListeners();
        for (int i = 0; i < dsls.length; i++) {
            dsls[i].dataChanged(this, fieldKey, data);
        }
    }

    protected void fireDataChanged(RowKey row, String fieldKey, Object data) {
        TableDataListener[] vdsls = getTableDataListeners();
        for (int i = 0; i < vdsls.length; i++) {
            vdsls[i].dataChanged(this, row, fieldKey, data);
        }
    }

    protected void fireCursorIndexChanging(RowKey oldRow, RowKey newRow) {
        TableDataListener[] vdsls = getTableDataListeners();
        for (int i = 0; i < vdsls.length; i++) {
            vdsls[i].cursorIndexChanging(this, oldRow, newRow);
        }
    }

    protected void fireCursorIndexChanged(RowKey oldRow, RowKey newRow) {
        TableDataListener[] vdsls = getTableDataListeners();
        for (int i = 0; i < vdsls.length; i++) {
            vdsls[i].cursorIndexChanged(this, oldRow, newRow);
        }
    }
}
*/
