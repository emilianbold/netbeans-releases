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
package org.netbeans.modules.sql.framework.codegen.oracle8;

import java.util.ArrayList;
import java.util.List;

import org.apache.velocity.VelocityContext;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.codegen.TemplateBuilder;
import org.netbeans.modules.sql.framework.codegen.base.BaseJoinGenerator;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.model.SQLJoinTable;
import org.netbeans.modules.sql.framework.model.SQLObject;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.StringUtil;

/**
 * Overrides parent implementation to handle Oracle 8-specific join syntax.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class Oracle8JoinGenerator extends BaseJoinGenerator {

    public String generate(SQLObject obj, StatementContext context) throws BaseException {
        SQLJoinOperator join = (SQLJoinOperator) obj;

        SQLObject left = join.getSQLObject(SQLJoinOperator.LEFT);
        SQLObject right = join.getSQLObject(SQLJoinOperator.RIGHT);
        SQLCondition joinCondition = join.getJoinCondition();
        SQLObject predicate = joinCondition.getRootPredicate();

        if (left == null || right == null) {
            throw new BaseException("Bad SQLJoin object. No left and right input.");
        }

        if (left.getObjectType() == SQLConstants.JOIN_TABLE) {
            left = ((SQLJoinTable) left).getSourceTable();
        }

        if (right.getObjectType() == SQLConstants.JOIN_TABLE) {
            right = ((SQLJoinTable) right).getSourceTable();
        }

        String leftObj = this.getGeneratorFactory().generate(left, context);
        String rightObj = this.getGeneratorFactory().generate(right, context);

        // Add join condition(s) to the where list in the statement context.
        List whereList = (List) context.getClientProperty(StatementContext.WHERE_CONDITION_LIST);
        if (whereList == null) {
            whereList = new ArrayList(3);
            context.putClientProperty(StatementContext.WHERE_CONDITION_LIST, whereList);
        }

        String condition = "";
        if (predicate != null) {
            context.putClientProperty(StatementContext.JOIN_OPERATOR, join);
            condition = this.getGeneratorFactory().generate(predicate, context);
            if (!StringUtil.isNullString(condition)) {
                whereList.add(condition);
            }
            context.putClientProperty(StatementContext.JOIN_OPERATOR, null);
        }

        VelocityContext vContext = new VelocityContext();
        vContext.put("leftObj", leftObj);
        vContext.put("rightObj", rightObj);

        return TemplateBuilder.generateSql(this.getDB().getTemplateFileName("join"), vContext);
    }
}
