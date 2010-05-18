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
