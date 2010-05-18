/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
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
package org.netbeans.modules.edm.model.visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.edm.model.SQLCaseOperator;
import org.netbeans.modules.edm.model.SQLCondition;
import org.netbeans.modules.edm.model.SQLConstants;
import org.netbeans.modules.edm.model.SQLDefinition;
import org.netbeans.modules.edm.model.SQLJoinOperator;
import org.netbeans.modules.edm.model.SQLJoinView;
import org.netbeans.modules.edm.model.SQLWhen;
import org.netbeans.modules.edm.model.SourceTable;
import org.netbeans.modules.edm.model.ValidationInfo;
import org.netbeans.modules.edm.model.impl.ValidationInfoImpl;
import org.openide.util.NbBundle;


/**
 * @author Girish Patil
 */
public class SQLOperatorInfoVisitor {

    private boolean foundUserFunction = false;

    // TODO Add flag to warn when Hex operator is being used on SQLServer or Sybase.
    private boolean validate = false;
    private List<ValidationInfo> validationInfoList = new ArrayList<ValidationInfo>();

    public SQLOperatorInfoVisitor() {
    }

    public SQLOperatorInfoVisitor(boolean validate) {
        this.validate = true;
    }

    public List<ValidationInfo> getValidationInfoList() {
        return this.validationInfoList;
    }

    public boolean isUserFunctionFound() {
        return this.foundUserFunction;
    }

    public void visit(SourceTable sourceTable) {
        SQLCondition eCondition = sourceTable.getFilterCondition();
        visit(eCondition);
    }

    public void visit(SQLCaseOperator caseop) {
        Iterator whenIter = caseop.getWhenList().iterator();
        while (whenIter.hasNext()) {
            SQLWhen when = (SQLWhen) whenIter.next();
            visit(when);
        }
    }

    public void visit(SQLCondition condition) {
        if (condition != null && condition.isConditionDefined()) {

            Collection uFunctions = condition.getObjectsOfType(SQLConstants.CUSTOM_OPERATOR);
            if ((uFunctions != null) && (uFunctions.size() > 0)) {
                this.foundUserFunction = true;
            }
        }
    }

    public void visit(SQLDefinition definition) {

        Iterator it = definition.getObjectsOfType(SQLConstants.CASE).iterator();
        while (it.hasNext()) {
            SQLCaseOperator caseop = (SQLCaseOperator) it.next();
            visit(caseop);
        }

        if (this.validate) {
            if (this.foundUserFunction) {
                String desc = NbBundle.getMessage(SQLOperatorInfoVisitor.class, "WARNING_userfx_usage");
                ValidationInfoImpl validationInfo = new ValidationInfoImpl(definition, desc, ValidationInfo.VALIDATION_WARNING);
                validationInfoList.add(validationInfo);
            }
        }
    }

    public void visit(SQLJoinOperator operator) {
        SQLCondition condition = operator.getJoinCondition();
        visit(condition);
    }

    public void visit(SQLJoinView joinView) {
        SQLJoinOperator jOperator = joinView.getRootJoin();
        if (jOperator != null) {
            visit(jOperator);
        }
    }

    public void visit(SQLWhen when) {
        SQLCondition condition = when.getCondition();
        visit(condition);
    }
}
