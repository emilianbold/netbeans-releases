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
package org.netbeans.modules.sql.framework.ui.view.conditionbuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.sql.framework.model.RuntimeDatabaseModel;
import org.netbeans.modules.sql.framework.model.RuntimeInput;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLGroupBy;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLWhen;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.impl.SQLCustomOperatorImpl;
import org.netbeans.modules.sql.framework.model.impl.ValidationInfoImpl;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.graph.IOperatorXmlInfoModel;
import org.netbeans.modules.sql.framework.ui.model.CollabSQLUIModel;
import org.netbeans.modules.sql.framework.ui.model.SQLUIModel;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;

import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.sql.framework.model.SQLJoinView;

/**
 * Utility class for generating ConditionBuilderView instances for various situations.
 *
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ConditionBuilderUtil {
    
    public static ConditionBuilderView getConditionBuilderView(SourceTable sTable, IGraphViewContainer gContainer) {
        ArrayList tables = new ArrayList(1);
        tables.add(sTable);
        addRuntimeInput(sTable, tables);
        ConditionBuilderView cView = new ConditionBuilderView(gContainer, tables, sTable.getExtractionCondition(),
                IOperatorXmlInfoModel.CATEGORY_FILTER);
        return cView;
    }
    
    public static ConditionBuilderView getConditionBuilderView(SQLJoinOperator join, IGraphViewContainer gContainer) {
        ArrayList tables = new ArrayList(1);
        tables.addAll(join.getAllSourceTables());
        addRuntimeInput(join, tables);
        ConditionBuilderView cView = new ConditionBuilderView(gContainer, tables, join.getJoinCondition(), IOperatorXmlInfoModel.CATEGORY_FILTER);
        return cView;
    }
    
    public static ConditionBuilderView getHavingConditionBuilderView(SQLObject obj, IGraphViewContainer gContainer) {
        ArrayList tables = new ArrayList(1);
        SQLGroupBy groupBy = null;
        if(obj instanceof SourceTable) {            
            tables.add((SourceTable)obj);
            groupBy = ((SourceTable)obj).getSQLGroupBy();
        } else if(obj instanceof TargetTable) {
            try {
                tables.addAll(((TargetTable)obj).getSourceTableList());
            }catch(BaseException be) {
                throw new IllegalStateException("Unable to get " +
                        "Source tables list for the target table: " + ((TargetTable)obj).getDisplayName());
            }
            groupBy = ((TargetTable)obj).getSQLGroupBy();
        } else if(obj instanceof SQLJoinView) {
            tables.addAll(((SQLJoinView)obj).getSourceTables());
            groupBy = ((SQLJoinView)obj).getSQLGroupBy();
        }       
        
        SQLCondition havingByCondition = groupBy.getHavingCondition();
        if (havingByCondition == null){
            havingByCondition = SQLModelObjectFactory.getInstance().createSQLCondition("Having Condition");            
        }
        ConditionBuilderView cView = new ConditionBuilderView(gContainer, tables, havingByCondition, IOperatorXmlInfoModel.CATEGORY_HAVING);
        return cView;
    }
    
    public static ConditionBuilderView getJoinConditionBuilderView(TargetTable tTable, IGraphViewContainer gContainer) {
        ArrayList tables = new ArrayList(1);
        tables.add(tTable);
        
        try {
            tables.addAll(tTable.getSourceTableList());
        }catch(BaseException be) {
            throw new IllegalStateException("Unable to get Source tables list for the target table: " + tTable.getDisplayName());
        }
        
        addRuntimeInput(tTable, tables);
        ConditionBuilderView cView = new ConditionBuilderView(gContainer, tables, tTable.getJoinCondition(), IOperatorXmlInfoModel.CATEGORY_FILTER);
        return cView;
    }
    
    public static ConditionBuilderView getFilterConditionBuilderView(TargetTable tTable, IGraphViewContainer gContainer) {
        ArrayList tables = new ArrayList(1);
        tables.add(tTable);
        
        try {
            tables.addAll(tTable.getSourceTableList());
        }catch(BaseException be) {
            throw new IllegalStateException("Unable to get Source tables list for the target table: " + tTable.getDisplayName());
        }
        
        addRuntimeInput(tTable, tables);
        ConditionBuilderView cView = new ConditionBuilderView(gContainer, tables, tTable.getFilterCondition(), IOperatorXmlInfoModel.CATEGORY_FILTER);
        return cView;
    }
    
    public static ConditionBuilderView getValidationConditionBuilderView(SourceTable sTable, IGraphViewContainer gContainer) {
        ArrayList tables = new ArrayList(1);
        tables.add(sTable);
        addRuntimeInput(sTable, tables);
        ConditionBuilderView cView = new ConditionBuilderView(gContainer, tables, sTable.getDataValidationCondition(),
                IOperatorXmlInfoModel.CATEGORY_VALIDATION);
        return cView;
    }
    
    public static ConditionBuilderView getConditionBuilderView(SQLWhen whenCondition, IGraphViewContainer gContainer) {
        IGraphView view = gContainer.getGraphView();
        if (view == null) {
            throw new IllegalStateException("No IGraphView instance associated with given IGraphViewContainer.");
        }
        
        SQLUIModel model = (SQLUIModel) view.getGraphModel();
        if (!(model instanceof CollabSQLUIModel)) {
            throw new IllegalStateException("No CollabSQLUIModel instance associated with IGraphView.");
        }
        
        SQLDefinition sqlDef = ((CollabSQLUIModel) model).getSQLDefinition();
        List tables = new ArrayList(sqlDef.getSourceTables());
        
        RuntimeInput runtimeInput = getRuntimeInput(sqlDef);
        if (runtimeInput != null) {
            tables.add(runtimeInput);
        }
        
        SQLCondition condition = whenCondition.getCondition();
        ConditionBuilderView cView = new ConditionBuilderView(gContainer, tables, condition, IOperatorXmlInfoModel.CATEGORY_FILTER);
        return cView;
    }
    
    private static void addRuntimeInput(SQLObject obj, List tables) {
        RuntimeInput rInput = getRuntimeInput(obj);
        if (rInput != null) {
            tables.add(rInput);
        }
    }
    
    private static RuntimeInput getRuntimeInput(SQLObject obj) {
        return getRuntimeInput(SQLObjectUtil.getAncestralSQLDefinition(obj));
    }
    
    private static RuntimeInput getRuntimeInput(SQLDefinition sqlDefinition) {
        if (sqlDefinition != null) {
            RuntimeDatabaseModel runModel = sqlDefinition.getRuntimeDbModel();
            if (runModel != null) {
                RuntimeInput rInput = runModel.getRuntimeInput();
                return rInput;
            }
        }
        return null;
    }
    
    /**
     * This method examines and filters the validation errors(?) for
     * CustomOperator. Reasoning behind this is that custom operators
     * are defined by the user and does not have a template to validate
     * at the condition builder level. It can be effectively be validated
     * only at the Test colloboration level where the sql gets executed
     * at the host database of the custom function. Hence ignoring the
     * validation against the dummy userFx template at the condition builder
     * level.
     * @param validationList collection of validationInfoImpl objects
     * generated by the parser.
     * @return
     */
    public static List filterValidations(List validationList) {
        Iterator iter = validationList.iterator();
        List newList = new ArrayList();
        while(iter.hasNext()) {
            ValidationInfoImpl valInfoImpl = (ValidationInfoImpl) iter.next();
            Object valObj = valInfoImpl.getValidatedObject();
            if(!(valObj instanceof SQLCustomOperatorImpl)) {
                newList.add(valInfoImpl);
            }
        }
        return newList;
    }
}
