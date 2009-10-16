/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2009 Sun Microsystems, Inc. All rights reserved.
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
package org.netbeans.modules.sql.framework.model.visitors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLGenericOperator;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.ValidationInfo;
import org.netbeans.modules.sql.framework.model.impl.ValidationInfoImpl;


public class SQLGroupByValidationVisitor {

    private boolean foundAggregateFn = false;
    private boolean foundScalar = false;
    private List groupByNodes;
    private TargetTable targetTable;
    private List<ValidationInfo> validationInfoList = new ArrayList<ValidationInfo>();

    public SQLGroupByValidationVisitor(TargetTable table, List nodes) {
        targetTable = table;
        groupByNodes = nodes;
    }

    public List<ValidationInfo> getValidationInfoList() {
        return this.validationInfoList;
    }

    public void reset() {
        foundAggregateFn = foundScalar = false;
        validationInfoList.clear();
    }

    public void visit(Collection selectList) {
        for (Iterator iter = selectList.iterator(); iter.hasNext();) {
            SQLObject sqlObj = (SQLObject) iter.next();
            visit(sqlObj);
        }

        if (groupByNodes == null || groupByNodes.isEmpty()) {
            foundScalar = true;
        }

        if (foundScalar && foundAggregateFn) {
            ValidationInfo validationInfo = new ValidationInfoImpl(targetTable, "Can't select both scalar values and aggregate functions.", ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(validationInfo);
        } else if (foundScalar && groupByNodes != null && !groupByNodes.isEmpty()) {
            ValidationInfo validationInfo = new ValidationInfoImpl(targetTable, "Invalid Group By Expression...", ValidationInfo.VALIDATION_ERROR);
            validationInfoList.add(validationInfo);
        }
    }

    private void visit(SQLObject obj) {
        if (obj == null) {
            // do nothing
        } else if (obj instanceof TargetColumn) {
            if (groupByNodes != null && groupByNodes.contains(obj)) {
                return;
            }
            visit(((TargetColumn) obj).getValue());
        } else if (obj instanceof SQLGenericOperator && ((SQLGenericOperator) obj).isAggregateFunction()) {
            foundAggregateFn = true;
        } else if (obj instanceof SQLConnectableObject) {
            SQLConnectableObject expObj = (SQLConnectableObject) obj;
            if (groupByNodes != null && groupByNodes.contains(expObj)) {
                return;
            }
            Iterator it = expObj.getInputObjectMap().values().iterator();
            while (it.hasNext()) {
                SQLInputObject inObj = (SQLInputObject) it.next();
                SQLObject sqlObj = inObj.getSQLObject();
                visit(sqlObj);
            }

            List children = expObj.getChildSQLObjects();
            Iterator cIt = children.iterator();
            while (cIt.hasNext()) {
                SQLObject chObj = (SQLObject) cIt.next();
                visit(chObj);
            }
        } else if (obj instanceof SourceColumn && groupByNodes != null) {
            // column is not part of GroupBy column list
            if (!groupByNodes.contains(obj)) {
                foundScalar = true;
            }
        }
    }
}
