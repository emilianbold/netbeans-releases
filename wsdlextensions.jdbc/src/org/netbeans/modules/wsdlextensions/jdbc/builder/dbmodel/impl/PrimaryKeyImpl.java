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

/*
 * 
 * Copyright 2005 Sun Microsystems, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *  http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.impl;

import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBTable;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.DBColumn;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.PrimaryKey;
import org.netbeans.modules.wsdlextensions.jdbc.builder.dbmodel.ForeignKey;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;

/**
 * Implements PrimaryKey interface.
 * 
 * @author Jonathan Giron
 */
public class PrimaryKeyImpl implements PrimaryKey, Cloneable {

    public static final PrimaryKeyImpl NULL = new PrimaryKeyImpl();

    public static final String ELEMENT_TAG = "primaryKey"; // NOI18N

    public static final String NAME_ATTR = "name"; // NOI18N

    public static final String COLUMNS_ATTR = "columns"; // NOI18N

    /* Name of this key; may be null */
    private String name;

    /* DBTable to which this PK belongs */
    private DBTable parent;

    /* List of column names in key sequence order. */
    private List columnNames;

    private PrimaryKeyImpl() {
        this.name = null;
        this.columnNames = new ArrayList();
    }

    /**
     * Creates a new instance of PrimaryKey with the given key name and referencing the column names
     * in the given List.
     * 
     * @param keyName name, if any, of this PrimaryKey
     * @param keyColumnNames List of Column objects, or column names in key sequence order,
     *            depending on state of isStringList
     * @param isStringList true if keyColumnName contains column names in key sequence order, false
     *            if it contains Column objects which need to be sorted in key sequence order.
     */
    public PrimaryKeyImpl(final String keyName, final List keyColumnNames, final boolean isStringList) {
        this();
        this.name = keyName;

        if (isStringList) {
            this.columnNames.addAll(keyColumnNames);
        } else {
            Collections.sort(keyColumnNames);
            final Iterator iter = keyColumnNames.iterator();
            while (iter.hasNext()) {
                final PrimaryKeyImpl.Column col = (PrimaryKeyImpl.Column) iter.next();
                this.columnNames.add(col.getName());
            }
        }
    }

    /**
     * Creates a new instance of PrimaryKeyImpl, cloning the contents of the given PrimaryKey
     * implementation instance.
     * 
     * @param src PrimaryKey to be cloned
     */
    public PrimaryKeyImpl(final PrimaryKey src) {
        this();

        if (src == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(PrimaryKeyImpl.class);
            throw new IllegalArgumentException(cMessages.getString("ERROR_NULL_PK") + "ERROR_NULL_PK");// NO
            // i18n
        }

        this.copyFrom(src);
    }

    /**
     * @see com.stc.model.database.PrimaryKey#getName
     */
    public String getName() {
        return this.name;
    }

    /**
     * @see com.stc.model.database.PrimaryKey#getColumnNames
     */
    public List getColumnNames() {
        return Collections.unmodifiableList(this.columnNames);
    }

    /**
     * @see com.stc.model.database.PrimaryKey#getParent
     */
    public DBTable getParent() {
        return this.parent;
    }

    /**
     * Sets reference to DBTable that owns this primary key.
     * 
     * @param newParent new parent of this primary key.
     */
    void setParent(final DBTable newParent) {
        this.parent = newParent;
    }

    /**
     * @see com.stc.model.database.PrimaryKey#contains(String)
     */
    public boolean contains(final String columnName) {
        return this.columnNames.contains(columnName);
    }

    /**
     * @see com.stc.model.database.PrimaryKey#contains(DBColumn)
     */
    public boolean contains(final DBColumn col) {
        return this.contains(col.getName());
    }

    /**
     * Create a clone of this PrimaryKeyImpl.
     * 
     * @return cloned copy of DBColumn.
     */
    public Object clone() {
        try {
            final PrimaryKeyImpl impl = (PrimaryKeyImpl) super.clone();
            impl.columnNames = new ArrayList(this.columnNames);
            return impl;
        } catch (final CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     * 
     * @param refObj Object against which we compare this instance
     * @return true if refObj is functionally identical to this instance; false otherwise
     */
    public boolean equals(final Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof PrimaryKeyImpl)) {
            return false;
        }

        final PrimaryKeyImpl ref = (PrimaryKeyImpl) refObj;

        boolean result = this.name != null ? this.name.equals(ref.name) : ref.name == null;

        result &= this.columnNames != null ? this.columnNames.equals(ref.columnNames) : ref.columnNames != null;

        return result;
    }

    /**
     * Overrides default implementation to compute hashCode value for those members used in equals()
     * for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        int myHash = this.name != null ? this.name.hashCode() : 0;
        myHash += this.columnNames != null ? this.columnNames.hashCode() : 0;

        return myHash;
    }

    /**
     * @see com.stc.model.database.PrimaryKey#getSequence(DBColumn)
     */
    public int getSequence(final DBColumn col) {
        if (col == null || col.getName() == null) {
            return -1;
        }

        return this.getSequence(col.getName().trim());
    }

    /**
     * @see com.stc.model.database.PrimaryKey#getSequence(String)
     */
    public int getSequence(final String columnName) {
        return this.columnNames.indexOf(columnName);
    }

    /**
     * Replaces the current List of column names with the contents of the given String array.
     * 
     * @param newColNames array of names to supplant current list of column names
     */
    public void setColumnNames(final String[] newColNames) {
        if (newColNames == null) {
            final ResourceBundle cMessages = NbBundle.getBundle(PrimaryKeyImpl.class);
            throw new IllegalArgumentException(cMessages.getString("ERROR_COL_NAMES") + "ERROR_COL_NAMES");// NO
            // i18n
        }

        this.columnNames.clear();
        for (int i = 0; i < newColNames.length; i++) {
            this.columnNames.add(newColNames[i]);
        }
    }

    /**
     * @see com.stc.model.database.PrimaryKey#getColumnCount
     */
    public int getColumnCount() {
        return this.columnNames.size();
    }

    /**
     * @see com.stc.model.database.PrimaryKey#getDBColumn
     */
    public String getDBColumnName(final int iColumn) {
        return (String) this.columnNames.get(iColumn);
    }

    /**
     * @see com.stc.model.database.PrimaryKey#isReferencedBy
     */
    public boolean isReferencedBy(final ForeignKey fk) {
        return fk != null ? fk.references(this) : false;
    }

    private void copyFrom(final PrimaryKey src) {
        this.name = src.getName();
        this.parent = src.getParent();

        this.columnNames.clear();
        this.columnNames.addAll(src.getColumnNames());
    }

    public static class Column implements Comparable {
        private String name;

        private int sequence;

        public Column(final String colName, final int colSequence) {
            final ResourceBundle cMessages = NbBundle.getBundle(PrimaryKeyImpl.class);

            if (colName == null || colName.trim().length() == 0) {
                throw new IllegalArgumentException(cMessages.getString("ERROR_COL_NAME") + "ERROR_COL_NAME");// NO
                // i18n

            }

            if (colSequence <= 0) {
                throw new IllegalArgumentException(cMessages.getString("ERROR_COL_SEQ") + "ERROR_COL_SEQ");// NO
                // i18n
            }

            this.name = colName;
            this.sequence = colSequence;
        }

        public Column(final DBColumn col, final int colSequence) {
            this(col.getName(), colSequence);
        }

        public String getName() {
            return this.name;
        }

        public int getSequence() {
            return this.sequence;
        }

        /**
         * Compares this object with the specified object for order. Returns a negative integer,
         * zero, or a positive integer as this object is less than, equal to, or greater than the
         * specified object.
         * <p>
         * Note: this class has a natural ordering that is inconsistent with equals.
         * 
         * @param o the Object to be compared.
         * @return a negative integer, zero, or a positive integer as this object is less than,
         *         equal to, or greater than the specified object.
         */
        public int compareTo(final Object o) {
            return this.sequence - ((Column) o).sequence;
        }
    }
}
