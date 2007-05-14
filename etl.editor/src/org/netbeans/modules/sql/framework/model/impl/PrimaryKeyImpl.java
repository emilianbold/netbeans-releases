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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.model.database.DBColumn;
import org.netbeans.modules.model.database.DBTable;
import org.netbeans.modules.model.database.ForeignKey;
import org.netbeans.modules.model.database.PrimaryKey;
import org.w3c.dom.Element;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;

/**
 * Implements PrimaryKey interface.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class PrimaryKeyImpl implements Cloneable, PrimaryKey {
    /**
     * Object wrapper to bind the name and sequence ID of a table column participating in
     * a primary key.
     */
    public static class Column implements Comparable {
        private String name;
        private int sequence;

        /**
         * Constructs a new instance of Column using the given DBColumn and sequence.
         * 
         * @param col DBColumn from which to obtain column metadata
         * @param colSequence one-based value indicating this column's sequential order in
         *        a composite key; should be 1 if this column is the only one in the PK
         */
        public Column(DBColumn col, int colSequence) {
            this(col.getName(), colSequence);
        }

        /**
         * Constructs a new instance of Column with the given name and sequence.
         * 
         * @param colName name of new Column
         * @param colSequence one-based value indicating this column's sequential order in
         *        a composite key; should be 1 if this column is the only one in the PK
         */
        public Column(String colName, int colSequence) {
            if (colName == null || colName.trim().length() == 0) {
                throw new IllegalArgumentException("Must supply non-empty String value for parameter colName.");
            }

            if (colSequence <= 0) {
                throw new IllegalArgumentException("Must supply positive integer value for parameter colSequence.");
            }

            name = colName;
            sequence = colSequence;
        }

        /**
         * Compares this object with the specified object for order. Returns a negative
         * integer, zero, or a positive integer as this object is less than, equal to, or
         * greater than the specified object.
         * <p>
         * Note: this class has a natural ordering that is inconsistent with equals.
         * 
         * @param o the Object to be compared.
         * @return a negative integer, zero, or a positive integer as this object is less
         *         than, equal to, or greater than the specified object.
         */
        public int compareTo(Object o) {
            return (this.sequence - ((Column) o).sequence);
        }

        /**
         * Gets name of this Column.
         * 
         * @return column name
         */
        public String getName() {
            return name;
        }

        /**
         * Gets sequence of this Column within its containing PK.
         * 
         * @return this column's sequence relative to other columns in the containing PK.
         */
        public int getSequence() {
            return sequence;
        }
    }

    /** Name of attribute used for marshalling out PK column names to XML */
    public static final String COLUMNS_ATTR = "columns"; // NOI18N

    /** Document element tag name for marshalling out this object to XML */
    public static final String ELEMENT_TAG = "primaryKey"; // NOI18N

    /** Name of attribute used for marshalling out primary key name to XML */
    public static final String NAME_ATTR = "name"; // NOI18N

    /* List of column names in key sequence order. */
    private List columnNames;

    /* (optional) DOM element used to construct this instance of PrimaryKey */
    private transient Element element;

    /* Name of this key; may be null */
    private String name;

    /* DBTable to which this PK belongs */
    private DBTable parent;

    /**
     * Creates a new instance of PrimaryKeyImpl, using the keyElement as a source for
     * reconstituting its contents. Caller must invoke parseXml() after this constructor
     * returns in order to unmarshal and reconstitute the instance object.
     * 
     * @param keyElement DOM element containing XML marshalled version of a PrimaryKeyImpl
     *        instance
     */
    public PrimaryKeyImpl(Element keyElement) {
        this();
        element = keyElement;
    }

    /**
     * Creates a new instance of PrimaryKeyImpl, cloning the contents of the given
     * PrimaryKey implementation instance.
     * 
     * @param src PrimaryKey to be cloned
     */
    public PrimaryKeyImpl(PrimaryKey src) {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null PrimaryKey instance for src.");
        }

        copyFrom(src);
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
    public PrimaryKeyImpl(String keyName, List keyColumnNames, boolean isStringList) {
        this();
        name = keyName;

        if (isStringList) {
            columnNames.addAll(keyColumnNames);
        } else {
            Collections.sort(keyColumnNames);
            Iterator iter = keyColumnNames.iterator();
            while (iter.hasNext()) {
                Column col = (Column) iter.next();
                columnNames.add(col.getName());
            }
        }
    }

    private PrimaryKeyImpl() {
        name = null;
        columnNames = new ArrayList();
    }

    /**
     * Create a clone of this PrimaryKeyImpl.
     * 
     * @return cloned copy of DBColumn.
     */
    public Object clone() {
        try {
            PrimaryKeyImpl impl = (PrimaryKeyImpl) super.clone();
            impl.columnNames = new ArrayList(this.columnNames);
            return impl;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#contains(DBColumn)
     */
    public boolean contains(DBColumn col) {
        return contains(col.getName());
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#contains(java.lang.String)
     */
    public boolean contains(String columnName) {
        return columnNames.contains(columnName);
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     * 
     * @param refObj Object against which we compare this instance
     * @return true if refObj is functionally identical to this instance; false otherwise
     */
    public boolean equals(Object refObj) {
        if (this == refObj) {
            return true;
        }

        if (!(refObj instanceof PrimaryKeyImpl)) {
            return false;
        }

        PrimaryKeyImpl ref = (PrimaryKeyImpl) refObj;

        boolean result = (name != null) ? name.equals(ref.name) : (ref.name == null);

        result &= (columnNames != null) ? columnNames.equals(ref.columnNames) : (ref.columnNames != null);

        return result;
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getColumnCount
     */
    public int getColumnCount() {
        return columnNames.size();
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getColumnNames
     */
    public List getColumnNames() {
        return Collections.unmodifiableList(columnNames);
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getDBColumnName
     */
    public String getDBColumnName(int iColumn) {
        return (String) columnNames.get(iColumn);
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getName
     */
    public String getName() {
        return name;
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getParent
     */
    public DBTable getParent() {
        return parent;
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getSequence(DBColumn)
     */
    public int getSequence(DBColumn col) {
        if (col == null || col.getName() == null) {
            return -1;
        }

        return getSequence(col.getName().trim());
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#getSequence(java.lang.String)
     */
    public int getSequence(String columnName) {
        return columnNames.indexOf(columnName);
    }

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        int myHash = (name != null) ? name.hashCode() : 0;
        myHash += (columnNames != null) ? columnNames.hashCode() : 0;

        return myHash;
    }

    /**
     * @see org.netbeans.modules.model.database.PrimaryKey#isReferencedBy
     */
    public boolean isReferencedBy(ForeignKey fk) {
        return (fk != null) ? fk.references(this) : false;
    }

    /**
     * Parses the XML content, if any, represented by the DOM element member variable.
     * 
     * @exception BaseException thrown while parsing XML, or if member variable element is
     *            null
     */
    public void parseXML() throws BaseException {
        if (this.element == null) {
            throw new BaseException("No <" + ELEMENT_TAG + "> element found.");
        }

        this.name = element.getAttribute(NAME_ATTR);

        String colNames = element.getAttribute(COLUMNS_ATTR);
        columnNames.addAll(StringUtil.createStringListFrom(colNames));
    }

    /**
     * Replaces the current List of column names with the contents of the given String
     * array.
     * 
     * @param newColNames array of names to supplant current list of column names
     */
    public void setColumnNames(String[] newColNames) {
        if (newColNames == null) {
            throw new IllegalArgumentException("Must supply non-null String[] for param newColNames.");
        }

        columnNames.clear();
        for (int i = 0; i < newColNames.length; i++) {
            columnNames.add(newColNames[i]);
        }
    }

    /**
     * Writes contents of this PrimaryKeyImpl instance out as an XML element, using the
     * default prefix.
     * 
     * @return String containing XML representation of this PrimaryKeyImpl instance
     */
    public synchronized String toXMLString() {
        return toXMLString(null);
    }

    /**
     * Writes contents of this PrimaryKeyImpl instance out as an XML element, using the
     * given prefix String.
     * 
     * @param prefix String used to prefix each new line of the XML output
     * @return String containing XML representation of this PrimaryKeyImpl instance
     */
    public synchronized String toXMLString(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        StringBuilder buf = new StringBuilder(100);

        buf.append(prefix).append("<").append(ELEMENT_TAG).append(" ");
        if (name != null && name.trim().length() != 0) {
            buf.append(NAME_ATTR).append("=\"").append(name.trim()).append("\" ");
        }

        if (columnNames.size() != 0) {
            buf.append(COLUMNS_ATTR).append("=\"");
            for (int i = 0; i < columnNames.size(); i++) {
                if (i != 0) {
                    buf.append(",");
                }
                buf.append(((String) columnNames.get(i)).trim());
            }
            buf.append("\" ");
        }

        buf.append("/>\n");

        return buf.toString();
    }

    /**
     * Sets reference to DBTable that owns this primary key.
     * 
     * @param newParent new parent of this primary key.
     */
    void setParent(DBTable newParent) {
        parent = newParent;
    }

    /*
     * Copies contents of given PrimaryKey implementation. @param src PrimaryKey whose
     * contents are to be copied
     */
    private void copyFrom(PrimaryKey src) {
        name = src.getName();
        parent = src.getParent();

        columnNames.clear();
        columnNames.addAll(src.getColumnNames());
    }
}

