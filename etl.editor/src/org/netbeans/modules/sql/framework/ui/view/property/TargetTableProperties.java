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
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.view.ConditionPropertyEditor;
import org.netbeans.modules.sql.framework.ui.view.GroupByPropertyEditor;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea;
import org.openide.nodes.Node;

import com.sun.sql.framework.exception.BaseException;

/**
 * @author Ritesh Adval
 */
public class TargetTableProperties extends TableProperties {
    private TargetTable table;

    public TargetTableProperties(IGraphViewContainer editor, SQLBasicTableArea gNode, TargetTable table) {
        this.editor = editor;
        this.gNode = gNode;
        this.table = table;
        initializeProperties(table);
    }

    public SQLCondition getJoinCondition() {
        SQLCondition joinCond = table.getJoinCondition();
        return joinCond;
    }

    /**
     * get condition text
     * 
     * @return sql condition
     */
    public String getJoinConditionText() {
        return this.table.getJoinConditionText();
    }
    
    public SQLCondition getFilterCondition() {
        SQLCondition joinCond = table.getFilterCondition();
        return joinCond;
    }

    /**
     * get condition text
     * 
     * @return sql condition
     */
    public String getFilterConditionText() {
        return this.table.getFilterConditionText();
    }

    public PropertyEditor getCustomEditor(Node.Property property) {
        if (property.getName().equals("joinCondition")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor(editor, table);
            return f;
        } else if (property.getName().equals("joinConditionText")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor(editor, table);
            return f;
        } else if (property.getName().equals("filterCondition")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor.OuterFilter(editor, table);
            return f;
        } else if (property.getName().equals("filterConditionText")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor.OuterFilter(editor, table);
            return f;
        } else if (property.getName().equals("SQLGroupBy")) {
            return new GroupByPropertyEditor(editor, table);
        } else {
            return super.getCustomEditor(property);
        }
    }

    /**
     * get report group by object
     * 
     * @return SQLGroupBy
     */
    public SQLGroupBy getSQLGroupBy() {
        return table.getSQLGroupBy();
    }

    /**
     * get string representation of statement type
     * 
     * @return statement type
     */
    public String getStrStatementType() {
        return table.getStrStatementType();
    }

    /**
     * get whether to create target table
     * 
     * @return whether to create target table
     */
    public boolean isCreateTargetTable() {
        return table.isCreateTargetTable();
    }

    /**
     * Indicates whether contents of target table should be truncated before loading new
     * data.
     * 
     * @return true if contents should be truncated; false otherwise
     */
    public boolean isTruncateBeforeLoad() {
        return this.table.isTruncateBeforeLoad();
    }

    public void setJoinCondition(SQLCondition filter) throws BaseException {
        table.setJoinCondition(filter);
        gNode.setConditionIcons();
        setDirty(true);
    }

    /**
     * set the condition text
     * 
     * @param cond sql condition
     */
    public void setJoinConditionText(String cond) {
        this.table.setJoinConditionText(cond);
    }
    
    
    public void setFilterCondition(SQLCondition filter) throws BaseException {
        table.setFilterCondition(filter);
        gNode.setConditionIcons();
        setDirty(true);
    }

    /**
     * set the condition text
     * 
     * @param cond sql condition
     */
    public void setFilterConditionText(String cond) {
        this.table.setFilterConditionText(cond);
    }

    /**
     * set whether to create target table if does not exist
     * 
     * @param create whether to create target table
     */
    public void setCreateTargetTable(boolean create) {
        table.setCreateTargetTable(create);
        setDirty(true);
    }

    /**
     * set group by object
     * 
     * @param groupBy - SQLGroupBy
     */
    public void setSQLGroupBy(SQLGroupBy groupBy) {
        table.setSQLGroupBy(groupBy);
        setDirty(true);
    }

    public void setStrStatementType(String sType) {
        table.setStrStatementType(sType);
        setDirty(true);
    }

    /**
     * Sets whether contents of target table should be truncated before loading new data.
     * 
     * @param flag true if contents should be truncated; false otherwise
     */
    public void setTruncateBeforeLoad(boolean flag) {
        this.table.setTruncateBeforeLoad(flag);
        setDirty(true);
    }
}
