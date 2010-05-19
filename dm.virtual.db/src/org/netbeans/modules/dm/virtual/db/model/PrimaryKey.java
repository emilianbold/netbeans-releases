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
package org.netbeans.modules.dm.virtual.db.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.openide.util.Exceptions;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Locale;
import java.util.ResourceBundle;
import org.openide.util.NbBundle;

/**
 * @author Ahimanikya Satapathy
 */
public class PrimaryKey extends VirtualDBObject implements Cloneable {

    protected static final String RS_KEY_NAME = "PK_NAME"; // NOI18N
    private static final String RS_COLUMN_NAME = "COLUMN_NAME"; // NOI18N
    protected static final String RS_SEQUENCE_NUM = "KEY_SEQ"; // NOI18N
    private List<String> columnNames;
    private String name;
    private VirtualDBTable parent;

    public PrimaryKey(ResultSet rs){
        this();
        try {
            if (rs == null) {
                Locale locale = Locale.getDefault();
                ResourceBundle cMessages = ResourceBundle.getBundle("org/netbeans/modules/dm/virtual/db/model/impl/Bundle", locale); // NO i18n
                throw new IllegalArgumentException(cMessages.getString("ERROR_VALID_RS") + "(ERROR_VALID_RS)"); // NO i18n
            }
            while (rs.next()) {                
                columnNames.add(rs.getString(RS_COLUMN_NAME));
                String tmpName = rs.getString(RS_KEY_NAME);
                if(!VirtualDBUtil.isNullString(tmpName) && name == null) {
                    name = tmpName;
                }
            }
        } catch (SQLException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    public PrimaryKey(PrimaryKey src) {
        this();
        if (src == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(PrimaryKey.class, "MSG_Null_PrimaryKey"));
        }
        copyFrom(src);
    }

    public PrimaryKey(String keyName, List<String> keyColumnNames) {
        this();
        name = keyName;
        columnNames.addAll(keyColumnNames);
    }

    private PrimaryKey() {
        name = null;
        columnNames = new ArrayList<String>();
    }

    @Override
    public Object clone() {
        try {
            PrimaryKey impl = (PrimaryKey) super.clone();
            impl.columnNames = new ArrayList<String>(this.columnNames);
            return impl;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    public boolean contains(VirtualDBColumn col) {
        return contains(col.getName());
    }

    public boolean contains(String columnName) {
        return columnNames.contains(columnName);
    }

    @Override
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof PrimaryKey)) {
            return false;
        }

        PrimaryKey ref = (PrimaryKey) refObj;
        boolean result = (getName() != null) ? name.equals(ref.name) : (ref.name == null);
        result &= (columnNames != null) ? columnNames.equals(ref.columnNames) : (ref.columnNames != null);
        return result;
    }

    public int getColumnCount() {
        return columnNames.size();
    }

    public List<String> getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    public String getDBColumnName(int iColumn) {
        return columnNames.get(iColumn);
    }

    public String getName() {
        if (name == null && parent != null) {
            name = "PK_" + parent.getName();
        }
        return name;
    }

    public VirtualDBTable getParent() {
        return parent;
    }

    public int getSequence(VirtualDBColumn col) {
        if (col == null || col.getName() == null) {
            return -1;
        }

        return getSequence(col.getName().trim());
    }

    public int getSequence(String columnName) {
        return columnNames.indexOf(columnName);
    }

    @Override
    public int hashCode() {
        int myHash = (getName() != null) ? name.hashCode() : 0;
        myHash += (columnNames != null) ? columnNames.hashCode() : 0;

        return myHash;
    }

    public boolean isReferencedBy(ForeignKey fk) {
        return (fk != null) ? fk.references(this) : false;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(100);
        for (int i = 0; i < columnNames.size(); i++) {
            if (i != 0) {
                buf.append(",");
            }
            buf.append((columnNames.get(i)).trim());
        }
        return buf.toString();
    }

    public void setColumnNames(String[] newColNames) {
        if (newColNames == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(PrimaryKey.class, "MSG_Null_NewColNames"));
        }

        columnNames.clear();
        for (int i = 0; i < newColNames.length; i++) {
            columnNames.add(newColNames[i]);
        }
    }

    void setParent(VirtualDBTable newParent) {
        parent = newParent;
    }

    private void copyFrom(PrimaryKey src) {
        name = src.getName();
        parent = src.getParent();

        columnNames.clear();
        columnNames.addAll(src.getColumnNames());
    }
}
