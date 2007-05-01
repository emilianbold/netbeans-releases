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
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.graph.IGraphView;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.netbeans.modules.sql.framework.ui.view.conditionbuilder.ConditionBuilderView;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLTargetTableArea;
import org.openide.DialogDescriptor;
import org.openide.NotifyDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.NbBundle;


/**
 * Handles request to edit a target table condition as referenced by a validation error
 * message.
 * 
 * @author Ritesh Adval
 * @version $Revision$
 */
public class TargetConditionValidationHandler implements ValidationHandler {

    private IGraphView graphView;

    /**
     * Constructs a new instance of TargetConditionValidationHandler, referencing the
     * given IGraphView instance and SQLCondition.
     * 
     * @param gView IGraphView instance in which target table is displayed
     * @param cond SQLCOndition to be edited
     */
    public TargetConditionValidationHandler(IGraphView gView, SQLCondition cond) {
        this.graphView = gView;
    }

    /*
     * @see org.netbeans.modules.sql.framework.ui.view.validation.ValidationHandler#editValue(java.lang.Object)
     */
    public void editValue(Object val) {
        SQLCondition oldCondition = (SQLCondition) val;
        TargetTable tTable = (TargetTable) oldCondition.getParent();

        String title = null;
        ConditionBuilderView conditionView = null;
        DialogDescriptor dd = null;
        
        if (TargetTable.JOIN_CONDITION.equals(oldCondition.getDisplayName())) {
        	conditionView = ConditionBuilderUtil.getJoinConditionBuilderView(tTable, (IGraphViewContainer) graphView.getGraphViewContainer());
            title = NbBundle.getMessage(IGraphViewContainer.class, "LBL_edit_target_join_condition");
            dd = new DialogDescriptor(conditionView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
        } else {
            title = NbBundle.getMessage(IGraphViewContainer.class, "LBL_edit_target_filter_condition");
            conditionView = ConditionBuilderUtil.getFilterConditionBuilderView(tTable, (IGraphViewContainer) graphView.getGraphViewContainer());
            dd = new DialogDescriptor(conditionView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);
        }
        
        conditionView.doValidation();

        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            SQLCondition cond = (SQLCondition) conditionView.getPropertyValue();
            if (cond != null) {
                if (tTable != null && !cond.equals(oldCondition)) {
                    tTable.setJoinCondition(cond);

                    Object tgtTableArea = this.graphView.findGraphNode(tTable);
                    ((SQLTargetTableArea) tgtTableArea).setConditionIcons();
                }
            }
        }
    }
}
