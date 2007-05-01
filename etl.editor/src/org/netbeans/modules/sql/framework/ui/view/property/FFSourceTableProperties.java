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
import org.openide.nodes.Node;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ahimanikya Satapathy
 */
public class FFSourceTableProperties {

    protected SourceTableProperties mDeligate;

    public FFSourceTableProperties(SourceTableProperties deligate) {
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

    // RFE-102428
    /**
     * Gets staging name of this mDeligate.
     * 
     * @return staging table name.
     */
    public String getStagingTableName() {
        return mDeligate.getStagingTableName();
    }
    
    /**
     * Gets data extraction condition.
     * 
     * @return
     */
    public SQLCondition getExtractionCondition() {
        return mDeligate.getExtractionCondition();
    }

    /**
     * get the extraction conidition text
     * 
     * @return sql condition
     */
    public String getExtractionConditionText() {
        return mDeligate.getExtractionConditionText();
    }

    public String getExtractionType() {
        return mDeligate.getExtractionType();
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
     * Gets data validation condition.
     * 
     * @return
     */
    public SQLCondition getValidationCondition() {
        return mDeligate.getValidationCondition();
    }

    /**
     * get the validation conidition text
     * 
     * @return sql condition
     */
    public String getValitionConditionText() {
        return mDeligate.getValitionConditionText();
    }

    /**
     * get whether to drop staging table before extraction
     * 
     * @return  whether to drop staging table
     */
    public boolean isDropStagingTable() {
        return mDeligate.isDropStagingTable();
    }

    /**
     * get whether to truncate staging table before extraction
     * 
     * @return  whether to truncate staging table
     */
    public boolean isTruncateStagingTable() {
        return mDeligate.isTruncateStagingTable();
    }
    
    /**
     * check if distinct rows of a column needs to be selected
     * 
     * @return distinct
     */
    public boolean isSelectDistinct() {
        return mDeligate.isSelectDistinct();
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
        mDeligate.setBatchSize(batchS);
    }

    /**
     * Sets the batch size for this table
     * 
     * @param newSize new value for batch size
     */
    public void setBatchSize(Integer newSize) {
        mDeligate.setBatchSize(newSize.intValue());
    }

    /**
     * Drop Staging table before extraction
     * 
     * @param  whether to drop staging table
     */
    public void setDropStagingTable(boolean drop) {
        mDeligate.setDropStagingTable(drop);
    }

    public void setExtractionCondition(SQLCondition cond) throws BaseException {
        mDeligate.setExtractionCondition(cond);
    }

    //RFE-102428
    /**
     * set the Staging table name
     * 
     * @param stName stging table name
     */
    public void setStagingTableName(String stName) {
        mDeligate.setStagingTableName(stName);
    }
    
    /**
     * Truncate Staging table before extraction
     * 
     * @param  whether to truncate staging table
     */
    public void setTruncateStagingTable(boolean truncateTable) {
        mDeligate.setTruncateStagingTable(truncateTable);
    }
    
    /**
     * set the extraction condition text
     * 
     * @param cond extraction condition text
     */
    public void setExtractionConditionText(String cond) {
        mDeligate.setExtractionConditionText(cond);
    }

    public void setExtractionType(String eType) {
        mDeligate.setExtractionType(eType);
    }

    /**
     * set wehether to select distinct rows of a column
     * 
     * @param distinct distinct
     */
    public void setSelectDistinct(boolean distinct) {
        mDeligate.setSelectDistinct(distinct);
    }

    public void setValidationCondition(SQLCondition cond) throws BaseException {
        mDeligate.setValidationCondition(cond);
    }

    /**
     * set the validation condition text
     * 
     * @param cond extraction condition text
     */
    public void setValidationConditionText(String cond) {
        mDeligate.setValidationConditionText(cond);
    }

}
