/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package org.netbeans.modules.edm.editor.ui.view.conditionbuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.edm.model.RuntimeDatabaseModel;
import org.netbeans.modules.edm.model.RuntimeInput;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLGroupBy;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.netbeans.modules.edm.model.SQLModelObjectFactory;
import org.netbeans.modules.edm.model.SQLObject;
import org.netbeans.modules.edm.model.SQLWhen;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.impl.SQLCustomOperatorImpl;
import org.netbeans.modules.edm.model.impl.ValidationInfoImpl;
import org.netbeans.modules.edm.editor.utils.SQLObjectUtil;
import org.netbeans.modules.edm.editor.graph.jgo.IOperatorXmlInfoModel;
import org.netbeans.modules.edm.editor.ui.model.CollabSQLUIModel;

import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.ValidationInfo;

/**
 * Utility class for generating ConditionBuilderView instances for various situations.
 *
 * @author Ritesh Adval
 * @version $Revision$
 */
public class ConditionBuilderUtil {
    
    public static ConditionBuilderView getConditionBuilderView(SourceTable sTable, SQLUIModel model) {
        ArrayList tables = new ArrayList(1);
        tables.add(sTable);
        addRuntimeInput(sTable, tables);
        ConditionBuilderView cView = new ConditionBuilderView(model, tables, sTable.getFilterCondition(),
                IOperatorXmlInfoModel.CATEGORY_FILTER);
        return cView;
    }
    
    public static ConditionBuilderView getConditionBuilderView(SQLJoinOperator join, SQLUIModel model) {
        ArrayList tables = new ArrayList(1);
        tables.addAll(join.getAllSourceTables());
        addRuntimeInput(join, tables);
        ConditionBuilderView cView = new ConditionBuilderView(model, tables, join.getJoinCondition(), IOperatorXmlInfoModel.CATEGORY_FILTER);
        return cView;
    }
    
    public static ConditionBuilderView getHavingConditionBuilderView(SQLObject obj, SQLUIModel model) {
        ArrayList tables = new ArrayList(1);
        SQLGroupBy groupBy = null;
        if(obj instanceof SourceTable) {            
            tables.add((SourceTable)obj);
            groupBy = ((SourceTable)obj).getSQLGroupBy();
        } else if(obj instanceof SQLJoinView) {
            tables.addAll(((SQLJoinView)obj).getSourceTables());
            groupBy = ((SQLJoinView)obj).getSQLGroupBy();
        }       
        
        SQLCondition havingByCondition = groupBy.getHavingCondition();
        if (havingByCondition == null){
            havingByCondition = SQLModelObjectFactory.getInstance().createSQLCondition("Having Condition");            
        }
        ConditionBuilderView cView = new ConditionBuilderView(model, tables, havingByCondition, IOperatorXmlInfoModel.CATEGORY_FILTER);
        return cView;
    }
        
    public static ConditionBuilderView getValidationConditionBuilderView(SourceTable sTable, SQLUIModel model) {
        ArrayList tables = new ArrayList(1);
        tables.add(sTable);
        addRuntimeInput(sTable, tables);
        ConditionBuilderView cView = new ConditionBuilderView(model, tables, sTable.getFilterCondition(),
                IOperatorXmlInfoModel.CATEGORY_VALIDATION);
        return cView;
    }
    
    public static ConditionBuilderView getConditionBuilderView(SQLWhen whenCondition, SQLUIModel model) {
        SQLDefinition sqlDef = ((CollabSQLUIModel) model).getSQLDefinition();
        List tables = new ArrayList(sqlDef.getSourceTables());
        
        RuntimeInput runtimeInput = getRuntimeInput(sqlDef);
        if (runtimeInput != null) {
            tables.add(runtimeInput);
        }
        
        SQLCondition condition = whenCondition.getCondition();
        ConditionBuilderView cView = new ConditionBuilderView(model, tables, condition, IOperatorXmlInfoModel.CATEGORY_FILTER);
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
    public static List<ValidationInfo> filterValidations(List validationList) {
        Iterator iter = validationList.iterator();
        List<ValidationInfo> newList = new ArrayList<ValidationInfo>();
        while(iter.hasNext()) {
            ValidationInfo valInfoImpl = (ValidationInfoImpl) iter.next();
            Object valObj = valInfoImpl.getValidatedObject();
            if(!(valObj instanceof SQLCustomOperatorImpl)) {
                newList.add(valInfoImpl);
            }
        }
        return newList;
    }
}
