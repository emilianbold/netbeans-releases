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

/**
 * Represents a column ref
 * 
 * @author Ritesh Adval
 */
public interface ColumnRef extends SQLObject, SQLCanvasObject {

    /** Constant for column metadata name tag. */
    public static final String ELEMENT_TAG = "dbColumnRef"; // NOI18N

    public void copyFrom(ColumnRef src);

    /**
     * get the column stored in this column ref
     * 
     * @return column
     */
    public SQLObject getColumn();

    /**
     * set the column
     * 
     * @param column column
     */
    public void setColumn(SQLObject column);

}

