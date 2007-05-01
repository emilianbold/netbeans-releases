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
package org.netbeans.modules.sql.framework.ui.view.property;

import java.beans.PropertyEditor;

import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLGroupBy;
import org.openide.nodes.Node;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ahimanikya Satapathy
 */
public class FFTargetTableProperties {
    protected TargetTableProperties mDeligate;

    public FFTargetTableProperties(TargetTableProperties deligate) {
        mDeligate = deligate;
    }

    /**
     * Gets the alias name for this mDeligate.
     * 
     * @return alias name
     */
    public String getAliasName() {
        return mDeligate.getAliasName();
    }

    /**
     * Gets the batch size for this mDeligate.
     * 
     * @return Integer representing batch size for the table
     */
    public Integer getBatchSize() {
        return mDeligate.getBatchSize();
    }

    public SQLCondition getJoinCondition() {
        return mDeligate.getJoinCondition();
    }
    
    /**
     * get condition text
     * 
     * @return sql condition
     */
    public String getJoinConditionText() {
        return mDeligate.getJoinConditionText();
    }
    
    public SQLCondition getFilterCondition() {
    	return mDeligate.getFilterCondition();
    }

    /**
     * get condition text
     * 
     * @return sql condition
     */
    public String getFilterConditionText() {
    	return mDeligate.getFilterConditionText();
    }

    public PropertyEditor getCustomEditor(Node.Property property) {
        return mDeligate.getCustomEditor(property);
    }

    /**
     * Gets display name of this mDeligate.
     * 
     * @return table disply name.
     */
    public String getDisplayName() {
        return mDeligate.getDisplayName();
    }

    /**
     * Gets delimited String list of foreign keys associated with this mDeligate.
     * 
     * @return delimited String list of foreign keys
     */
    public String getForeignKeys() {
        return mDeligate.getForeignKeys();
    }

    /**
     * Gets delimited String list of indices associated with this mDeligate.
     * 
     * @return delimited String list of indices
     */
    public String getIndices() {
        return mDeligate.getIndices();
    }

    /**
     * Gets name of parent DatabaseModel for this table
     * 
     * @return name of parent DatabaseModel
     */
    public String getModelName() {
        return mDeligate.getModelName();
    }

    /**
     * Gets delimited String list of primary key columns associated with this mDeligate.
     * 
     * @return delimited String list of primary key columns
     */
    public String getPrimaryKeys() {
        return mDeligate.getPrimaryKeys();
    }

    /**
     * get report group by object
     * 
     * @return SQLGroupBy
     */
    public SQLGroupBy getSQLGroupBy() {
        return mDeligate.getSQLGroupBy();
    }

    /**
     * get string representation of statement type
     * 
     * @return statement type
     */
    public String getStrStatementType() {
        return mDeligate.getStrStatementType();
    }

    /**
     * get whether to create target table
     * 
     * @return whether to create target table
     */
    public boolean isCreateTargetTable() {
        return mDeligate.isCreateTargetTable();
    }

    /**
     * Indicates whether contents of target table should be truncated before loading new
     * data.
     * 
     * @return true if contents should be truncated; false otherwise
     */
    public boolean isTruncateBeforeLoad() {
        return mDeligate.isTruncateBeforeLoad();
    }

    /**
     * Sets the alias name for this table
     * 
     * @param aName alias name
     */
    public void setAliasName(String aName) {
        this.mDeligate.setAliasName(aName);
    }

    public void setBatchSize(int batchS) {
        mDeligate.setBatchSize(new Integer(batchS));
    }

    /**
     * Sets the batch size for this table
     * 
     * @param newSize new value for batch size
     */
    public void setBatchSize(Integer newSize) {
        mDeligate.setBatchSize(newSize);
    }

    public void setJoinCondition(SQLCondition filter) throws BaseException {
        mDeligate.setJoinCondition(filter);
    }

    /**
     * set the condition text
     * 
     * @param cond sql condition
     */
    public void setJoinConditionText(String cond) {
        mDeligate.setJoinConditionText(cond);
    }
    
    public void setFilterCondition(SQLCondition filter) throws BaseException {
        mDeligate.setFilterCondition(filter);
    }

    /**
     * set the condition text
     * 
     * @param cond sql condition
     */
    public void setFilterConditionText(String cond) {
        mDeligate.setFilterConditionText(cond);
    }

    /**
     * set whether to create target table if does not exist
     * 
     * @param create whether to create target table
     */
    public void setCreateTargetTable(boolean create) {
        mDeligate.setCreateTargetTable(create);
    }

    /**
     * set group by object
     * 
     * @param groupBy - SQLGroupBy
     */
    public void setSQLGroupBy(SQLGroupBy groupBy) {
        mDeligate.setSQLGroupBy(groupBy);
    }

    public void setStrStatementType(String sType) {
        mDeligate.setStrStatementType(sType);
    }

    /**
     * Sets whether contents of target table should be truncated before loading new data.
     * 
     * @param flag true if contents should be truncated; false otherwise
     */
    public void setTruncateBeforeLoad(boolean flag) {
        mDeligate.setTruncateBeforeLoad(flag);
    }

}
