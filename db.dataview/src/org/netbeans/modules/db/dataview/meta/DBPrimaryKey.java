/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.db.dataview.meta;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * Implements PrimaryKey interface.
 *
 * @author Ahimanikya Satapathy
 */
public final class DBPrimaryKey extends DBObject {

    /**DatabaseMetaData ResultSet column name used to decode name of associated primary key     */
    //private static final String RS_KEY_NAME = "PK_NAME"; // NOI18N

    private static final String RS_COLUMN_NAME = "COLUMN_NAME"; // NOI18N

    /** DatabaseMetaData ResultSet column name used to decode key sequence number*/
    //private static final String RS_SEQUENCE_NUM = "KEY_SEQ";
    /* List of column names in key sequence order. */
    private List<String> columnNames;
    /* Name of this key; may be null */
    private String name;
    /* DBTable to which this PK belongs */
    private DBTable parent;

    public DBPrimaryKey(ResultSet rs) throws SQLException {
        this();
        if (rs == null) {
            throw new IllegalArgumentException("ResultSet Can't be null"); // NO i18n

        }
        while (rs.next()) {
            columnNames.add(rs.getString(RS_COLUMN_NAME));
        }
    }

    /**
     * Creates a new instance of PrimaryKey with the given key name and referencing the
     * column names in the given List.
     *
     * @param keyName name, if any, of this PrimaryKey
     * @param keyColumnNames List of Column objects, or column names in key sequence
     *        order, depending on state of isStringList
     * @param isStringList true if keyColumnName contains column names in key sequence
     *        order, false if it contains Column objects which need to be sorted in key
     *        sequence order.
     */
    public DBPrimaryKey(String keyName, List<String> keyColumnNames) {
        this();
        name = keyName;
        columnNames.addAll(keyColumnNames);
    }

    private DBPrimaryKey() {
        name = null;
        columnNames = new ArrayList<String>();
    }

    public boolean contains(DBColumn col) {
        return contains(col.getName());
    }

    public boolean contains(String columnName) {
        return columnNames.contains(columnName);
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     *
     * @param refObj Object against which we compare this instance
     * @return true if refObj is functionally identical to this instance; false otherwise
     */
    @Override
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof DBPrimaryKey)) {
            return false;
        }

        DBPrimaryKey ref = (DBPrimaryKey) refObj;
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

    public DBTable getParent() {
        return parent;
    }

    public int getSequence(DBColumn col) {
        if (col == null || col.getName() == null) {
            return -1;
        }

        return getSequence(col.getName().trim());
    }

    public int getSequence(String columnName) {
        return columnNames.indexOf(columnName);
    }

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     *
     * @return hash code for this object
     */
    @Override
    public int hashCode() {
        int myHash = (getName() != null) ? name.hashCode() : 0;
        myHash += (columnNames != null) ? columnNames.hashCode() : 0;

        return myHash;
    }

    public boolean isReferencedBy(DBForeignKey fk) {
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

    /**
     * Replaces the current List of column names with the contents of the given String
     * array.
     *
     * @param newColNames array of names to supplant current list of column names
     */
    void setColumnNames(String[] newColNames) {
        if (newColNames == null) {
            throw new IllegalArgumentException("Must supply non-null String[] for param newColNames.");
        }

        columnNames.clear();
        for (int i = 0; i < newColNames.length; i++) {
            columnNames.add(newColNames[i]);
        }
    }

    /**
     * Sets reference to DBTable that owns this primary key.
     *
     * @param newParent new parent of this primary key.
     */
    void setParent(DBTable newParent) {
        parent = newParent;
    }
}
