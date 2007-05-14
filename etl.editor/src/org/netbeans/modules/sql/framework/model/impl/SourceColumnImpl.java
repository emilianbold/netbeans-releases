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

import org.netbeans.modules.model.database.DBColumn;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SourceColumn;


/**
 * Concrete implementation of SourceColumn describing column metadata for source columns.
 * 
 * @author Sudhendra Seshachala, Jonathan Giron
 * @version $Revision$
 */
public class SourceColumnImpl extends AbstractDBColumn implements SourceColumn {

    /* Log4J category name */
    static final String LOG_CATEGORY = SourceColumnImpl.class.getName();

    /** Constructs default instance of SourceColumnImpl. */
    public SourceColumnImpl() {
        super();
        init();
    }

    /**
     * Constructs a new instance of SourceColumnImpl, cloning the contents of the given
     * DBColumn implementation instance.
     * 
     * @param src DBColumn instance to be cloned
     */
    public SourceColumnImpl(DBColumn src) {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null DBColumn instance for src.");
        }

        copyFrom(src);
    }

    /**
     * Constructs a new instance of SourceColumnImpl using the given parameters and
     * assuming that the column is not part of a foreign key or primary key, and that it
     * accepts null values.
     * 
     * @param colName name of this column
     * @param sqlJdbcType JDBC type of this column
     * @param colScale scale of this column
     * @param colPrecision precision of this column
     * @param isNullable true if nullable, false otherwise
     * @see java.sql.Types
     */
    public SourceColumnImpl(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isNullable) {
        super(colName, sqlJdbcType, colScale, colPrecision, isNullable);
        init();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn
     *      (java.lang.String,int,int,boolean,int,boolean,boolean,boolean)
     */
    public SourceColumnImpl(String colName, int sqlJdbcType, int colScale, int colPrecision, boolean isPrimaryKey, boolean isForeignKey,
            boolean isIndexed, boolean isNullable) {
        super(colName, sqlJdbcType, colScale, colPrecision, isPrimaryKey, isForeignKey, isIndexed, isNullable);
        init();
    }

    /**
     * Clone a deep copy of SourceColumnImpl.
     * 
     * @return a copy of SourceColumnImpl.
     */
    public Object clone() {
        return new SourceColumnImpl(this);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn#equals(java.lang.Object)
     */
    public boolean equals(Object refObj) {
        if (!(refObj instanceof SourceColumn)) {
            return false;
        }

        return super.equals(refObj);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn#hashCode
     */
    public int hashCode() {
        return super.hashCode();
    }

    /**
     * Overrides default implementation to return evaluated column name.
     * 
     * @return evaluated column name.
     */
    public String toString() {
        return super.toString();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SQLObject#toXMLString
     */
    public String toXMLString(String prefix) {
        StringBuilder xml = new StringBuilder(50);

        xml.append(prefix).append("<").append(ELEMENT_TAG);

        // Allow superclass to write its attributes out first.
        appendXMLAttributes(xml);
        xml.append(" >\n");

        // write out attributes
        xml.append(super.toXMLAttributeTags(prefix));
        xml.append(prefix).append("</").append(ELEMENT_TAG).append(">\n");

        return xml.toString();
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn#getElementTagName
     */
    protected String getElementTagName() {
        return ELEMENT_TAG;
    }

    /*
     * Performs sql framework initialization functions for constructors which cannot first
     * call this().
     */
    private void init() {
        type = SQLConstants.SOURCE_COLUMN;
    }

}

