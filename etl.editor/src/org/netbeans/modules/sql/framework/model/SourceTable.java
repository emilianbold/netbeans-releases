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

import org.netbeans.modules.sql.framework.model.visitors.SQLVisitedObject;


/**
 * Defines methods required for a source table representation.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public interface SourceTable extends SQLDBTable, Cloneable, Comparable, SQLVisitedObject {

    public static final String DATA_VALIDATION_CONDITION = "dataValidationCondition";

    /** extraction condition tag */
    public static final String EXTRACTION_CONDITION = "extractionCondition";

    /**
     * get the data validation condition.
     * 
     * @return data validation condition.
     */
    public SQLCondition getDataValidationCondition();

    /**
     * Gets the validation condition text.
     * 
     * @param cond condition text
     */
    public String getDataValidationConditionText();

    /**
     * get the extraction condition
     * 
     * @return SQLCondition to apply while doing extraction
     */
    public SQLCondition getExtractionCondition();

    /**
     * get the extraction conidition text
     * 
     * @return sql condition
     */
    public String getExtractionConditionText();

    /**
     * get extraction type
     * 
     * @return extraction type
     */
    public String getExtractionType();

    /**
     * get temporary table name
     * 
     * @return temp table name
     */
    public String getTemporaryTableName();
    
    /**
     * get report group by object
     * 
     * @return SQLGroupBy
     */
    public SQLGroupBy getSQLGroupBy();    

    /**
     * get whether to Drop Staging table before extraction
     * 
     * @return  whether to drop staging table
     */
    public boolean isDropStagingTable();
    
    /**
     * get whether to truncate Staging table before extraction
     * 
     * @return  whether to truncate staging table
     */
    public boolean isTruncateStagingTable();

    /**
     * check if distinct rows of a column needs to be selected
     * 
     * @return distinct
     */
    public boolean isSelectDistinct();

    /**
     * determine whether this table is used in a join view
     * 
     * @return boolean
     */
    public boolean isUsedInJoin();

    /**
     * set the data validation condition.
     * 
     * @param condition data validation condition
     */
    public void setDataValidationCondition(SQLCondition condition);

    /**
     * Sets the validation condition text.
     * 
     * @param cond condition text
     */
    public void setDataValidationConditionText(String text);

    /**
     * Drop staging table before extraction
     * 
     * @param  whether to drop temp table
     */
    public void setDropStagingTable(boolean drop);

    /**
     * Truncate staging table before extraction
     * 
     * @param  whether to truncate temp table
     */
    public void setTruncateStagingTable(boolean truncate);
    
    /**
     * set the extraction condition
     * 
     * @param cond filter to appy while extraction
     */
    public void setExtractionCondition(SQLCondition cond);

    /**
     * set the extraction condition text
     * 
     * @param cond extraction condition text
     */
    public void setExtractionConditionText(String cond);

    /**
     * set the extraction type
     * 
     * @param eType extraction type
     */
    public void setExtractionType(String eType);

    /**
     * set wehether to select distinct rows of a column
     * 
     * @param distinct distinct
     */
    public void setSelectDistinct(boolean distinct);

    /**
     * set the temporary table name
     * 
     * @param tName temp table name
     */
    public void setTemporaryTableName(String tName);

    /**
     * set whether this table is used in join view
     * 
     * @param used boolean
     */
    public void setUsedInJoin(boolean used);
    
    /**
     * set group by object
     * 
     * @param groupBy - SQLGroupBy
     */
    public void setSQLGroupBy(SQLGroupBy groupBy);    

    /**
     * do source table validation
     * 
     * @return list of validation infos
     */
    public List validate();
}
