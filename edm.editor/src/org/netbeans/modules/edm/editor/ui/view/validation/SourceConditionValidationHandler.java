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
package org.netbeans.modules.edm.editor.ui.view.validation;

import org.netbeans.modules.edm.editor.ui.model.SQLUIModel;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.editor.ui.view.conditionbuilder.ConditionBuilderUtil;
import org.netbeans.modules.edm.editor.ui.view.conditionbuilder.ConditionBuilderView;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;

/**
 * Handles request to edit a source table condition as referenced by a validation error
 * message.
 *
 * @author Ritesh Adval
 * @version $Revision$
 */
public class SourceConditionValidationHandler implements ValidationHandler {

    SQLUIModel model;

    /**
     * Constructs a new instance of SourceConditionValidationHandler, referencing the
     * given IGraphView instance and SQLCondition.
     *
     * @param gView IGraphView instance in which target table is displayed
     * @param cond SQLCOndition to be edited
     */
    public SourceConditionValidationHandler(SQLUIModel model) {
        this.model = model;
    }

    public void editValue(Object val) {
        SQLCondition oldCondition = (SQLCondition) val;
        SourceTable sTable = (SourceTable) oldCondition.getParent();

        ConditionBuilderView conditionView = null;
        DialogDescriptor dd = null;
        String title = null;
        
        title = NbBundle.getMessage(SourceConditionValidationHandler.class, "TITLE_Filter_Condition");
        conditionView = ConditionBuilderUtil.getConditionBuilderView(sTable, model);
        dd = new DialogDescriptor(conditionView, title, true, NotifyDescriptor.OK_CANCEL_OPTION, null, null);

        //do validation
        conditionView.doValidation();

        if (DialogDisplayer.getDefault().notify(dd) == NotifyDescriptor.OK_OPTION) {
            SQLCondition cond = (SQLCondition) conditionView.getPropertyValue();
            if (cond != null) {
                if (sTable != null && !cond.equals(oldCondition)) {
                    sTable.setFilterCondition(cond);

                    //Object srcTableArea = this.graphView.findGraphNode(sTable);
                    // ((SQLSourceTableArea) srcTableArea).setConditionIcons();
                }
            }
        }
    }
}
