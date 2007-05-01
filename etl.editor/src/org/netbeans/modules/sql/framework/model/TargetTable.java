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
 * Defines methods required for a target table representation.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public interface TargetTable extends SQLDBTable, SQLConnectableObject {

    public static final String JOIN_CONDITION = "condition";
    
    public static final String FILTER_CONDITION = "filterCondition";
    
    /**
     * get the target table join condition
     * 
     * @return target table join conidiotn
     */
    public SQLCondition getJoinCondition();

    /**
     * get join condition text
     * 
     * @return sql condition
     */
    public String getJoinConditionText();
    
    
    /**
     * get the target table filter condition
     * 
     * @return target table filter conidiotn
     */
    public SQLCondition getFilterCondition();

    /**
     * get filter condition text
     * 
     * @return sql condition
     */
    public String getFilterConditionText();

    /**
     * get the single join view which is mapped to this target table in case of multiple
     * source table mapping to this target table.
     */
    public SQLJoinView getJoinView();

    /**
     * get a list of columns which are mapped by the user
     * 
     * @return list of mapped columns
     */
    public List getMappedColumns();

    /**
     * Gets the Source Table List
     * 
     * @return List All source tables
     * @exception BaseException thrown while getting Source Table List
     */
    public List getSourceTableList() throws BaseException;

    /**
     * get report group by object
     * 
     * @return SQLGroupBy
     */
    public SQLGroupBy getSQLGroupBy();

    /**
     * get the type of statement type for this table
     * 
     * @return statement type
     */
    public int getStatementType();

    /**
     * get string representation of statement type
     * 
     * @return statement type
     */
    public String getStrStatementType();

    /**
     * get whether to create target table
     * 
     * @return whether to create target table
     */
    public boolean isCreateTargetTable();

    /**
     * Indicates whether contents of target table should be truncated before loading new
     * data.
     * 
     * @return true if contents should be truncated; false otherwise
     */
    public boolean isTruncateBeforeLoad();

    /**
     * set the target table join condition
     * 
     * @param cond target table join condition
     */
    public void setJoinCondition(SQLCondition cond);

    /**
     * set the join condition text
     * 
     * @param cond sql condition
     */
    public void setJoinConditionText(String cond);
    
    /**
     * set the target table filter condition
     * 
     * @param cond target table filter condition
     */
    public void setFilterCondition(SQLCondition cond);

    /**
     * set the filter condition text
     * 
     * @param cond sql condition
     */
    public void setFilterConditionText(String cond);

    /**
     * set whether to create target table if does not exist
     * 
     * @param create whether to create target table
     */
    public void setCreateTargetTable(boolean create);

    /**
     * set group by object
     * 
     * @param groupBy - SQLGroupBy
     */
    public void setSQLGroupBy(SQLGroupBy groupBy);

    /**
     * set the type of statement type for this table
     * 
     * @param sType statement type
     */
    public void setStatementType(int sType);

    /**
     * set string representation of statement type
     * 
     * @param stType statement type
     */
    public void setStrStatementType(String stType);

    /**
     * Sets whether contents of target table should be truncated before loading new data.
     * 
     * @param flag true if contents should be truncated; false otherwise
     */
    public void setTruncateBeforeLoad(boolean flag);
}
