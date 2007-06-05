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

/**
 * This class defines joins on tables
 *
 * @author Ritesh Adval
 * @version $Revision$
 */
public interface SQLJoinOperator extends SQLConnectableObject, SQLCanvasObject {
    
    public static final String ATTR_JOINCONDITION_TYPE = "joinConditionType";
    
    /** Attribute constant: join type */
    public static final String ATTR_JOINTYPE = "type";
    
    /** Key constant: condition input */
    public static final String CONDITION = "condition";
    
    /** extraction condition tag */
    public static final String JOIN_CONDITION = "joinCondition";
    
    /** Key constant: left argument */
    public static final String LEFT = "left";
    
    public static final int NO_CONDITION = 2;
    
    /** Key constant: right argument */
    public static final String RIGHT = "right";
    
    public static final int SYSTEM_DEFINED_CONDITION = 0;
    
    public static final int USER_DEFINED_CONDITION = 1;
    
    
    
    /**
     * get a list of all tables which are used in this join or any of its input join. This
     * method recursively goes through LEFT and RIGHT inputs if they are join operator and
     * finds out all the SourceTables
     *
     * @return list of all participating SourceTables for this join
     */
    public List getAllSourceTables();
    
    /**
     * get join condition
     *
     * @return join condition
     */
    public SQLCondition getJoinCondition();
    
    /**
     * get the type for join condition it will be one of following
     * SYSTEM_DEFINED_CONDITION USER_DEFINED_CONDITION NO_CONDITION
     *
     * @return join condition type
     */
    public int getJoinConditionType();
    
    /**
     * Get type of join (inner, left outer, right outer, full outer)
     *
     * @return type of join.
     * @see SQLConstants
     */
    public int getJoinType();
    
    /**
     * method isRoot returns true if the root is set.
     *
     * @return boolean true if root is set.
     */
    public boolean isRoot();
    
    /**
     * set the join condition
     *
     * @param condition join condition
     */
    public void setJoinCondition(SQLCondition condition);
    
    public void setJoinConditionType(int type);
    
    /**
     * Sets the join type to the given value
     *
     * @param newType new join type
     */
    public void setJoinType(int newType);
    
    public String getJoinTypeString();
    
    public void setRoot(SQLJoinOperator rJoin);
    
    public void setJoinType(String joinType);
    
}

