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
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.ui.view.ConditionPropertyEditor;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea;
import org.openide.nodes.Node;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 */
public class SourceTableProperties extends TableProperties {

    protected SourceTable table;

    public SourceTableProperties(IGraphViewContainer editor, SQLBasicTableArea gNode, SourceTable table) {
        this.editor = editor;
        this.gNode = gNode;
        this.table = table;
        initializeProperties(table);
    }

    public PropertyEditor getCustomEditor(Node.Property property) {
        if (property.getName().equals("extractionCondition")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor(editor, table);
            return f;
        } else if (property.getName().equals("extractionConditionText")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor(editor, table);
            return f;
        } else if (property.getName().equals("validationCondition")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor.Validation(editor, table);
            return f;
        } else if (property.getName().equals("validationConditionText")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor.Validation(editor, table);
            return f;
        } else {
            return super.getCustomEditor(property);
        }
    }

    /**
     * Gets data extraction condition.
     * 
     * @return
     */
    public SQLCondition getExtractionCondition() {
        return table.getExtractionCondition();
    }

    /**
     * Gets data validation condition.
     * 
     * @return
     */
    public SQLCondition getValidationCondition() {
        return table.getDataValidationCondition();
    }

    /**
     * get the extraction conidition text
     * 
     * @return sql condition
     */
    public String getExtractionConditionText() {
        return table.getExtractionConditionText();
    }

    /**
     * get the validation conidition text
     * 
     * @return sql condition
     */
    public String getValitionConditionText() {
        return table.getDataValidationConditionText();
    }

    public String getExtractionType() {
        return table.getExtractionType();
    }

    /**
     * get whether to Drop Staging table before extraction
     * 
     * @return whether to drop Staging table
     */
    public boolean isDropStagingTable() {
        return table.isDropStagingTable();
    }
    
    /**
     * get whether to truncate Staging table before extraction
     * 
     * @return whether to drop Staging table
     */
    public boolean isTruncateStagingTable() {
        return table.isTruncateStagingTable();
    }

    /**
     * check if distinct rows of a column needs to be selected
     * 
     * @return distinct
     */
    public boolean isSelectDistinct() {
        return table.isSelectDistinct();
    }

    public void setBatchSize(int batchS) {
        table.setBatchSize(batchS);
        setDirty(true);
    }

    /**
     * Drop Staging table before extraction
     * 
     * @param dropTable whether to drop staging table
     */
    public void setDropStagingTable(boolean dropTable) {
        table.setDropStagingTable(dropTable);
        setDirty(true);
    }
    
    /**
     * Truncate Staging table before extraction
     * 
     * @param truncateTable whether to truncate staging table
     */
    public void setTruncateStagingTable(boolean truncateTable) {
        table.setTruncateStagingTable(truncateTable);
        setDirty(true);
    }

    public void setExtractionCondition(SQLCondition cond) throws BaseException {
        table.setExtractionCondition(cond);
        gNode.setConditionIcons();
        setDirty(true);
    }

    public void setValidationCondition(SQLCondition cond) throws BaseException {
        table.setDataValidationCondition(cond);
        gNode.setConditionIcons();
        setDirty(true);
    }

    /**
     * set the extraction condition text
     * 
     * @param cond extraction condition text
     */
    public void setExtractionConditionText(String cond) {
        this.table.setExtractionConditionText(cond);
    }

    /**
     * set the validation condition text
     * 
     * @param cond extraction condition text
     */
    public void setValidationConditionText(String cond) {
        this.table.setDataValidationConditionText(cond);
    }

    public void setExtractionType(String eType) {
        table.setExtractionType(eType);
        setDirty(true);
    }

    /**
     * set wehether to select distinct rows of a column
     * 
     * @param distinct distinct
     */
    public void setSelectDistinct(boolean distinct) {
        table.setSelectDistinct(distinct);
        setDirty(true);
    }

}
