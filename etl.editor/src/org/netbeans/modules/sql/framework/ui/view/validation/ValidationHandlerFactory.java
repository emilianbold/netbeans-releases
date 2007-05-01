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
package org.netbeans.modules.sql.framework.ui.view.validation;

import java.util.Iterator;

import org.netbeans.modules.sql.framework.model.SQLCastOperator;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;


/**
 * Factory for creating ValidationHandler instances, depending on the object type of the object
 * being validated (as referenced in a ValidationInfo instance).
 * 
 * @author Ritesh Adval
 * @author Jonathan Giron
 */
public class ValidationHandlerFactory {

    private IGraphView graphView;

    public ValidationHandlerFactory(IGraphView gView) {
        this.graphView = gView;
    }

    public void higlightInvalidNode(ValidationInfo vInfo) {
        Object val = vInfo.getValidatedObject();
        if (val instanceof SQLCondition) {
            val = ((SQLCondition) val).getParent();

            if (val instanceof SQLJoinOperator) {
                SQLJoinOperator join = (SQLJoinOperator) val;
                Iterator jIt = join.getAllSourceTables().iterator();
                boolean createSelection = true;
                while (jIt.hasNext()) {
                    SourceTable sTable = (SourceTable) jIt.next();
                    this.graphView.highlightInvalidNode(sTable, createSelection);
                    createSelection = false;
                }
            }
        }

        this.graphView.highlightInvalidNode(val, true);
    }

    public ValidationHandler getValidationHandler(ValidationInfo vInfo) {
        Object val = vInfo.getValidatedObject();
        higlightInvalidNode(vInfo);
        
        if (val instanceof SQLCondition) {
            return getConditionValidationHandler((SQLCondition) val);
        } else if (val instanceof SQLCastOperator) {
            return new CastAsValidationHandler(this.graphView);
        }

        return null;
    }

    ValidationHandler getConditionValidationHandler(SQLCondition condition) {
        ValidationHandler vHandler = null;

        Object parent = condition.getParent();

        if (parent instanceof SourceTable) {
            vHandler = new SourceConditionValidationHandler(this.graphView, condition);
        } else if (parent instanceof TargetTable) {
            vHandler = new TargetConditionValidationHandler(this.graphView, condition);
        } else if (parent instanceof SQLJoinOperator) {
            vHandler = new JoinConditionValidationHandler(this.graphView, condition);
        }

        return vHandler;
    }
}
