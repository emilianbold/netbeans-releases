/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
