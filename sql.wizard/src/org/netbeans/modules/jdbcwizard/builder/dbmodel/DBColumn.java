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
 * 	http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.netbeans.modules.jdbcwizard.builder.dbmodel;

/**
 * Interface describing column metadata for data sources providing information in a database or
 * database-like format. Implementing classes must support the Cloneable interface.
 * 
 * @author
 */
public interface DBColumn extends Cloneable, Comparable {

   
    /**
     * Gets the column name
     * 
     * @return column name
     */
    public String getName();

    /**
     * Indicates whether this column is part of a primary key.
     * 
     * @return true if this column is part of a primary key; false otherwise.
     */
    public boolean isPrimaryKey();

    /**
     * Indicates whether this column is part of a foreign key.
     * 
     * @return true if this column is part of a foreign key; false otherwise.
     */
    public boolean isForeignKey();

    /**
     * Indicates whether this column is indexed.
     * 
     * @return true if this column is indexed; false otherwise
     */
    public boolean isIndexed();

    /**
     * Indicates whether this column can accept a null value.
     * 
     * @return true if null is a valid value for this column, false otherwise.
     */
    public boolean isNullable();

    /**
     * Gets the parent/owner (DBTable) of this column
     * 
     * @return DBTable containing this column
     */
    public DBTable getParent();

    /**
     * Gets the JDBC datatype for this column, as selected from the enumerated types in
     * java.sql.Types.
     * 
     * @return JDBC type value
     * @see java.sql.Types
     */
    public int getJdbcType();

    /**
     * Gets the JDBC datatype for this column, as a human-readable String.
     * 
     * @return JDBC type value as a String
     */
    public String getJdbcTypeString();

    /**
     * Gets the scale attribute of this column.
     * 
     * @return scale
     */
    public int getScale();

    /**
     * Gets the precision attribute of this column.
     * 
     * @return precision
     */
    public int getPrecision();

    /**
     * Gets the default value
     * 
     * @return defaultValue
     */
    public String getDefaultValue();

    /**
     * Gets the Ordinal Position
     * 
     * @return int
     */
    public int getOrdinalPosition();

    /**
     * @return
     */
    public boolean isSelected();

    /*
     * Gets the status of selection of the column table @return boolean selected
     */
    public boolean isInsertSelected();

    /*
     * Gets the status of selection of the column table @return boolean selected
     */
    public boolean isUpdateSelected();

    /*
     * Gets the status of selection of the column table @return boolean selected
     */
    public boolean isChooseSelected();

    /*
     * Gets the status of selection of the column table @return boolean selected
     */
    public boolean isPollSelected();

    /*
     * Gets the status of the editing of the column table @return boolean editable
     */
    public boolean isEditable();
    
    /*
     * Gets the status of the editing of the column table @return boolean editable
     */
    public boolean isInsertEditable();

    /*
     * set column table editable
     */
    public void setEditable(boolean cedit);
    
    /*
     * set column table editable
     */
    public void setInsertEditable(boolean cedit);


    /*
     * set column table selected
     */
    public void setSelected(boolean cselect);

    /**
     * @param cselect
     */
    public void setInsertSelected(boolean cselect);

    /**
     * @param cselect
     */
    public void setUpdateSelected(boolean cselect);

    /**
     * @param cselect
     */
    public void setChooseSelected(boolean cselect);

    /**
     * @param cselect
     */
    public void setPollSelected(boolean cselect);
   
    /**
     * 
     * @return
     */
    public String getJavaName();
    /**
     * 
     * @param newName
     */
    public void setJavaName(final String newName); 

}
