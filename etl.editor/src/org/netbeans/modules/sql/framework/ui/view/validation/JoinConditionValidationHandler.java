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

import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;


/**
 * Implementation of ValidationHandler interface for join conditions.
 * 
 * @author Ritesh Adval
 */
public class JoinConditionValidationHandler implements ValidationHandler {

    private IGraphView graphView;

    public JoinConditionValidationHandler(IGraphView gView, SQLCondition cond) {
        this.graphView = gView;
    }

    /*
     * @see org.netbeans.modules.sql.framework.ui.view.validation.ValidationHandler#editValue(java.lang.Object)
     */
    public void editValue(Object val) {
        SQLCondition oldCondition = (SQLCondition) val;
        SQLJoinOperator join = (SQLJoinOperator) oldCondition.getParent();

        ConditionBuilderView conditionView = ConditionBuilderUtil.getConditionBuilderView(join,
            (IGraphViewContainer) graphView.getGraphViewContainer());

        DialogDescriptor dd = new DialogDescriptor(conditionView, "Edit Join Condition", true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);

        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            SQLCondition cond = (SQLCondition) conditionView.getPropertyValue();
            if (cond != null) {
                if (join != null && !cond.equals(oldCondition)) {
                    join.setJoinCondition(cond);
                    join.setJoinConditionType(SQLJoinOperator.USER_DEFINED_CONDITION);
                }
            }
        }
    }
}

