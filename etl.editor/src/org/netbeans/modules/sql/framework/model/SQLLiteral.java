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

import java.util.Collections;
import java.util.List;

import org.netbeans.modules.sql.framework.common.jdbc.SQLUtils;


/**
 * Represents a string or number literal value.
 * 
 * @author Ritesh Adval, Sudhi Seshachala
 * @version $Revision$
 */
public interface SQLLiteral extends SQLObject {

    /** Attribute key: JDBC type of literal */
    public static final String ATTR_TYPE = "type";

    /** Attribute key: value of literal */
    public static final String ATTR_VALUE = "value";

    public static final List VALID_INTERVAL_TYPES = Collections.unmodifiableList(SQLUtils.getSupportedIntervalTypes());

    public static final List VALID_TYPE_NAMES = Collections.unmodifiableList(SQLUtils.getSupportedLiteralTypes());

    /* System constant : basically an unqoted varchar */
    public static final int VARCHAR_UNQUOTED = 3345336;

    public static final String VARCHAR_UNQUOTED_STR = "varchar:unquoted";

    public void copyFrom(SQLLiteral src);

    public int getPrecision();

    /**
     * Gets value of this literal.
     * 
     * @return current value
     */
    public String getValue();

    /**
     * Sets value of this literal to the given value.
     * 
     * @param val new value
     */
    public void setValue(String val);

}

