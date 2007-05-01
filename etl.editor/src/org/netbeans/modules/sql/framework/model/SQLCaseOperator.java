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

import java.util.List;

import com.sun.sql.framework.exception.BaseException;

/**
 * Case operator used when we want to do a join and lookup
 * 
 * @version $Revision$
 * @author Sudhi Seshachala
 */
public interface SQLCaseOperator extends SQLConnectableObject, SQLCanvasObject {

    public static final String DEFAULT = "default";

    public static final String SWITCH = "switch";

    /**
     * addSQLWhen adds an SQLWhen object to the when list.
     * 
     * @param when is the SQLWhen to be added.
     * @return boolean true when added.
     * @throws BaseException when the input is null.
     */
    public boolean addSQLWhen(SQLWhen when) throws BaseException;

    /**
     * createSQLWhen creates a new SQLWhen object and returns it.
     * 
     * @param theName is the name for the SQLWhen object.
     * @param predicate is a comparison object to use.
     * @param thenAction is an operand to use.
     * @return SQLWhen as the newly created object.
     * @throws BaseException if any input params are passed in as null.
     */
    public SQLWhen createSQLWhen() throws BaseException;

    public String generateNewWhenName();

    /**
     * get list of child sql objects
     */
    public List getChildSQLObjects();

    public int getJdbcType();

    /**
     * Gets a specific SQLWhen by name.
     * 
     * @param whenName of the SQLWhen object to return.
     * @return SQLWhen instance with the given name.
     */
    public SQLWhen getWhen(String whenName);

    /**
     * getWhenCount returns the size of the when list.
     * 
     * @return int the size of the list.
     */
    public int getWhenCount();

    public List getWhenList();

    /**
     * removeSQLWhen removes an SQLWhen object to the when list.
     * 
     * @param when is the SQLWhen to be removed.
     * @return boolean true when removed.
     * @throws BaseException when the input is null.
     */
    public boolean removeSQLWhen(SQLWhen when) throws BaseException;

}

