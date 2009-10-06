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

package org.netbeans.modules.sql.framework.ui.view.property;

import com.sun.sql.framework.exception.BaseException;
import java.beans.PropertyEditor;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLJoinOperator;
import org.netbeans.modules.sql.framework.ui.view.BasicTopView;
import org.netbeans.modules.sql.framework.ui.view.ConditionPropertyEditor;
import org.openide.nodes.Node;


/**
 * @author Nithya Radhakrishnan
 */
public class SQLJoinProperties {

    SQLJoinOperator joinOp;
    BasicTopView editor;

    public SQLJoinProperties(SQLJoinOperator join, BasicTopView editor) {
        this.joinOp = join;
        this.editor = editor;
    }

    public PropertyEditor getCustomEditor(Node.Property property) {
        if (property.getName().equals("joinCondition")) {
            return  new ConditionPropertyEditor.JoinConditionEditor(editor, joinOp);
        } else {
            return null;
        }
    }

    /**
     * Gets JoinType code set for this collaboration.
     * @return JoinType
     */
    public Integer getJoinType() {
        return this.joinOp.getJoinType();
    }

    /**
     * Sets join type to given value.
     *
     * @param joinType new joinType
     */
    public void setJoinType(Integer joinType) {
        this.joinOp.setJoinType(joinType);
    }

    /**
     * get join condition
     *
     * @return join condition
     */
    public SQLCondition getJoinCondition() {
        SQLCondition joinCond = joinOp.getJoinCondition();
        return joinCond;
    }

    /**
     * get condition text
     *
     * @return sql condition
     */
    public String getJoinConditionText() {
        return this.joinOp.getJoinCondition().getConditionText();
    }

    public void setJoinCondition(SQLCondition join) throws BaseException {
        joinOp.setJoinCondition(join);
    }

    /**
     * set the condition text
     *
     * @param cond sql condition
     */
    public void setJoinConditionText(String cond) {
        this.joinOp.getJoinCondition().setConditionText(cond);
    }
}
