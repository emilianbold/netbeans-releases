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
            vHandler = new SourceConditionValidationHandler(this.graphView);
        } else if (parent instanceof TargetTable) {
            vHandler = new TargetConditionValidationHandler(this.graphView);
        } else if (parent instanceof SQLJoinOperator) {
            vHandler = new JoinConditionValidationHandler(this.graphView);
        }

        return vHandler;
    }
}