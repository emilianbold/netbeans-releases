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
package org.netbeans.modules.mashup.db.model;

import java.util.Map;

import org.netbeans.modules.model.database.DBColumn;
import org.w3c.dom.Element;


/**
 * Extends DBColumn to hold metadata required for parsing a flatfile field as a column in
 * a database table.
 * 
 * @author Jonathan Giron
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public interface FlatfileDBColumn extends DBColumn {

    int getCardinalPosition();

    /**
     * Gets the SQL create statement to create a column representing this flatfile field.
     * 
     * @return SQL statement fragment to create of a column representing this field
     */
    String getCreateStatementSQL();

    /**
     * Gets Map of current properties associated with this field.
     * 
     * @return unmodifiable Map of current properties.
     */
    Map getProperties();

    /**
     * Gets property string associated with the given name.
     * 
     * @param propName property key
     * @return property associated with propName, or null if no such property exists.
     */
    String getProperty(String propName);

    /**
     * Indicates whether column is selected
     * 
     * @return true if selected, false otherwise
     */
    boolean isSelected();

    void parseXML(Element xmlElement);
    void setCardinalPosition(int theCardinalPosition);

    /**
     * sets the default value
     * 
     * @param default value to be set
     */
    void setDefaultValue(String defValue);

    /**
     * Sets whether this column is flagged as part of a foreign key.
     * 
     * @param newFlag true if this column is part of a foreign key; false otherwise
     */
    void setForeignKey(boolean newFlag);

    /**
     * Sets whether this column is flagged as indexed.
     * 
     * @param newFlag true if this column is indexed; false otherwise
     */
    void setIndexed(boolean newFlag);

    /**
     * Indicates whether this DBColumn references the given DBColumn in a FK -> PK
     * relationship.
     * 
     * @param column PK whose relationship to this column is to be checked
     * @return true if this column is a FK reference to column; false otherwise
     */
    // public boolean references(DBColumn column);
    /**
     * Indicates whether this DBColumn is referenced by the given DBColumn in a FK -> PK
     * relationship.
     * 
     * @param column potential FK reference to be checked
     * @return true if column is referenced as a PK by the given column, false otherwise
     */
    // public boolean isReferencedBy(DBColumn column);
    /**
     * Sets SQL type code.
     * 
     * @param newCode SQL code
     * @throws FlatfileDBException if newCode is not a recognized SQL type code
     */
    void setJdbcType(int newType);

    void setName(String theName);

    /**
     * Sets whether this column is flagged as nullable.
     * 
     * @param newFlag true if this column is nullable; false otherwise
     */
    void setNullable(boolean newFlag);

    /**
     * Sets reference to DBTable that owns this DBColumn.
     * 
     * @param newParent new parent of this column.
     */
    void setParent(FlatfileDBTable newParent);

    void setPrecision(int thePrecision);

    /**
     * Sets whether this column is flagged as part of a primary key.
     * 
     * @param newFlag true if this column is part of a primary key; false otherwise
     */
    void setPrimaryKey(boolean newFlag);

    void setScale(int theScale);

    /**
     * Marshall this object to XML string.
     * 
     * @param prefix
     * @return XML string
     */
    String toXMLString(String prefix);

}

