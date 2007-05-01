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
package org.netbeans.modules.sql.framework.model;

import org.netbeans.modules.model.database.DBColumn;
import org.netbeans.modules.model.database.DBTable;

/**
 * Extension of DBColumn
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */

public interface SQLDBColumn extends DBColumn, SQLObject {
    public static final String ATTR_EDITABLE = "editable";
    public static final String ATTR_VISIBLE = "visible";

    public void copyFrom(DBColumn source);

    /**
     * get table qualified name
     * 
     * @return qualified column name prefixed with alias
     */
    public String getQualifiedName();

    public boolean isEditable();

    /**
     * is this column visible
     * 
     * @return boolean
     */
    public boolean isVisible();

    public void setDefaultValue(String defaultVal);

    public void setEditable(boolean editable1);

    public void setForeignKey(boolean newFlag);

    public void setIndexed(boolean newFlag);

    public void setName(String theName);

    public void setNullable(boolean newFlag);

    public void setOrdinalPosition(int cardinalPos);

    public void setParent(DBTable newParent);

    public void setPrecision(int thePrecision);

    public void setPrimaryKey(boolean newFlag);

    public void setScale(int theScale);

    /**
     * set this column to be visible
     * 
     * @param visible boolean
     */
    public void setVisible(boolean visible);
}
