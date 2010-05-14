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
package org.netbeans.modules.sql.framework.ui.view.property;

import java.beans.PropertyEditor;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLGroupBy;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.ui.view.ConditionPropertyEditor;
import org.netbeans.modules.sql.framework.ui.view.GroupByPropertyEditor;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea;
import org.openide.nodes.Node;
import com.sun.etl.exception.BaseException;

/**
 * @author Ritesh Adval
 */
public class TargetTableProperties extends TableProperties {

    private TargetTable table;

    public TargetTableProperties(IGraphViewContainer editor, SQLBasicTableArea gNode, TargetTable table) {
        this.editor = editor;
        this.gNode = gNode;
        this.table = table;
        initializeProperties(table);
    }

    public SQLCondition getJoinCondition() {
        SQLCondition joinCond = table.getJoinCondition();
        return joinCond;
    }

    /**
     * get condition text
     *
     * @return sql condition
     */
    public String getJoinConditionText() {
        return this.table.getJoinConditionText();
    }

    public SQLCondition getFilterCondition() {
        SQLCondition joinCond = table.getFilterCondition();
        return joinCond;
    }

    /**
     * get condition text
     *
     * @return sql condition
     */
    public String getFilterConditionText() {
        return this.table.getFilterConditionText();
    }

    @Override
    public PropertyEditor getCustomEditor(Node.Property property) {
        if (property.getName().equals("joinCondition")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor(editor, table);
            return f;
        } else if (property.getName().equals("joinConditionText")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor(editor, table);
            return f;
        } else if (property.getName().equals("filterCondition")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor.OuterFilter(editor, table);
            return f;
        } else if (property.getName().equals("filterConditionText")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor.OuterFilter(editor, table);
            return f;
        } else if (property.getName().equals("SQLGroupBy")) {
            return new GroupByPropertyEditor(editor, table);
        } else {
            return super.getCustomEditor(property);
        }
    }

    /**
     * get report group by object
     *
     * @return SQLGroupBy
     */
    public SQLGroupBy getSQLGroupBy() {
        return table.getSQLGroupBy();
    }

    /**
     * get string representation of statement type
     *
     * @return statement type
     */
    public String getStrStatementType() {
        return table.getStrStatementType();
    }

    /**
     * get whether to create target table
     *
     * @return whether to create target table
     */
    public boolean isCreateTargetTable() {
        return table.isCreateTargetTable();
    }

    /**
     * Indicates whether contents of target table should be truncated before loading new
     * data.
     *
     * @return true if contents should be truncated; false otherwise
     */
    public boolean isTruncateBeforeLoad() {
        return this.table.isTruncateBeforeLoad();
    }

    /**
     * Indicates whether constraints on target table should be disabled (cascade)
     * before loading data. The constraints will be enabled once the data is loaded
     * successfully
     * 
     * @return true if contraints should be disabled; false otherwise
     */
    public boolean isDisableConstraints() {
        return this.table.isDisableConstraints();
    }
    
    public void setJoinCondition(SQLCondition filter) throws BaseException {
        table.setJoinCondition(filter);
        gNode.setConditionIcons();
        setDirty(true);
    }

    /**
     * set the condition text
     *
     * @param cond sql condition
     */
    public void setJoinConditionText(String cond) {
        this.table.setJoinConditionText(cond);
    }

    public void setFilterCondition(SQLCondition filter) throws BaseException {
        table.setFilterCondition(filter);
        gNode.setConditionIcons();
        setDirty(true);
    }

    /**
     * set the condition text
     *
     * @param cond sql condition
     */
    public void setFilterConditionText(String cond) {
        this.table.setFilterConditionText(cond);
    }

    /**
     * set whether to create target table if does not exist
     *
     * @param create whether to create target table
     */
    public void setCreateTargetTable(boolean create) {
        table.setCreateTargetTable(create);
        setDirty(true);
    }

    /**
     * set group by object
     *
     * @param groupBy - SQLGroupBy
     */
    public void setSQLGroupBy(SQLGroupBy groupBy) {
        table.setSQLGroupBy(groupBy);
        setDirty(true);
    }

    public void setStrStatementType(String sType) {
        table.setStrStatementType(sType);
        setDirty(true);
    }

    /**
     * Sets whether contents of target table should be truncated before loading new data.
     *
     * @param flag true if contents should be truncated; false otherwise
     */
    public void setTruncateBeforeLoad(boolean flag) {
        this.table.setTruncateBeforeLoad(flag);
        setDirty(true);
    }
    
    /**
     * Sets whether constraints on target table should be disabled (cascade)
     * before loading data. The constraints will be enabled once the data is loaded
     * successfully
     * 
     * @param flag true if constraints should be truncated; false otherwise
     */
    public void setDisableConstraints(boolean flag) {
        this.table.setDisableConstraints(flag);
    }
}