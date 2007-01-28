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
package org.netbeans.modules.visualweb.faces.dt.data;

import java.beans.PropertyEditorSupport;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.StringTokenizer;
import javax.sql.RowSet;
import com.sun.rave.designtime.DesignProperty;
import com.sun.rave.designtime.PropertyEditor2;
import com.sun.rave.faces.data.ColumnBinding;
import com.sun.rave.faces.data.RowSetBindable;

public class BoundRowSetColumnPropertyEditor extends PropertyEditorSupport implements
    PropertyEditor2 {

    protected DesignProperty prop = null;

    public void setDesignProperty(DesignProperty prop) {
        this.prop = prop;
    }

    public String[] getTags() {
        if (prop != null && prop.getDesignBean().getInstance() instanceof RowSetBindable) {
            RowSet rs = ((RowSetBindable)prop.getDesignBean().getInstance()).getBoundRowSet();
            if (rs != null) {
                try {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    ArrayList tagList = new ArrayList();
                    for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                        String tableName = rsmd.getTableName(i);
                        String columnName = rsmd.getColumnName(i);
                        String tag = tableName + "." + columnName; //NOI18N
                        tagList.add(tag);
                    }
                    return (String[])tagList.toArray(new String[tagList.size()]);
                } catch (Exception x) {
                    System.err.println(
                        ">>>>>>>>>> Could not get column meta-data to show column list"); //NOI18N
                    x.printStackTrace();
                }
            }
        }
        return null;
    }

    public void setAsText(String text) {
        // may be one of these:
        //    TABLE.COLUMN
        //    new com.sun.fcl.data.ColumnBinding("TABLE", "COLUMN")
        //    or null!
        if (text != null && text.indexOf("ColumnBinding(") > -1) { //NOI18N
            StringTokenizer st = new StringTokenizer(text, "\""); //NOI18N
            if (st.countTokens() == 5) {
                st.nextToken();
                String tableName = st.nextToken();
                st.nextToken();
                String columnName = st.nextToken();
                setValue(new ColumnBinding(tableName, columnName));
            }
        } else if (text != null && text.indexOf(".") > -1) { //NOI18N
            StringTokenizer st = new StringTokenizer(text, "."); //NOI18N
            if (st.countTokens() == 2) {
                String tableName = st.nextToken();
                String columnName = st.nextToken();
                setValue(new ColumnBinding(tableName, columnName));
            }
        } else if (text == null) {
            setValue(null);
        }
    }

    public String getAsText() {
        ColumnBinding cb = (ColumnBinding)getValue();
        if (cb != null) {
            return cb.getTableName() + "." + cb.getColumnName(); //NOI18N
        }
        return null;
    }

    public String getJavaInitializationString() {
        ColumnBinding cb = (ColumnBinding)getValue();
        if (cb != null) {
            return "new com.sun.fcl.data.ColumnBinding(\"" + //NOI18N
                cb.getTableName() + "\", \"" + //NOI18N
                cb.getColumnName() + "\")"; //NOI18N
        }
        return "null"; //NOI18N
    }
}
