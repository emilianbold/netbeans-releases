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
package org.netbeans.modules.sql.framework.ui.view.property;

import java.beans.PropertyEditor;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.ui.view.ConditionPropertyEditor;
import org.netbeans.modules.sql.framework.ui.view.IGraphViewContainer;
import org.netbeans.modules.sql.framework.ui.view.graph.SQLBasicTableArea;
import org.openide.nodes.Node;
import com.sun.etl.exception.BaseException;

/**
 * @author Ritesh Adval
 * @author Ahimanikya Satapathy
 */
public class SourceTableProperties extends TableProperties {

    protected SourceTable table;

    public SourceTableProperties(IGraphViewContainer editor, SQLBasicTableArea gNode, SourceTable table) {
        this.editor = editor;
        this.gNode = gNode;
        this.table = table;
        initializeProperties(table);
    }

    @Override
    public PropertyEditor getCustomEditor(Node.Property property) {
        if (property.getName().equals("extractionCondition")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor(editor, table);
            return f;
        } else if (property.getName().equals("extractionConditionText")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor(editor, table);
            return f;
        } else if (property.getName().equals("validationCondition")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor.Validation(editor, table);
            return f;
        } else if (property.getName().equals("validationConditionText")) {
            ConditionPropertyEditor f = new ConditionPropertyEditor.Validation(editor, table);
            return f;
        } else {
            return super.getCustomEditor(property);
        }
    }

    /**
     * Gets data extraction condition.
     *
     * @return
     */
    public SQLCondition getExtractionCondition() {
        return table.getExtractionCondition();
    }

    /**
     * Gets data validation condition.
     *
     * @return
     */
    public SQLCondition getValidationCondition() {
        return table.getDataValidationCondition();
    }

    /**
     * get the extraction conidition text
     *
     * @return sql condition
     */
    public String getExtractionConditionText() {
        return table.getExtractionConditionText();
    }

    /**
     * get the validation conidition text
     *
     * @return sql condition
     */
    public String getValitionConditionText() {
        return table.getDataValidationConditionText();
    }

    public String getExtractionType() {
        return table.getExtractionType();
    }

    /**
     * get whether to Drop Staging table before extraction
     *
     * @return whether to drop Staging table
     */
    public boolean isDropStagingTable() {
        return table.isDropStagingTable();
    }

    /**
     * get whether to truncate Staging table before extraction
     *
     * @return whether to drop Staging table
     */
    public boolean isTruncateStagingTable() {
        return table.isTruncateStagingTable();
    }

    /**
     * check if distinct rows of a column needs to be selected
     *
     * @return distinct
     */
    public boolean isSelectDistinct() {
        return table.isSelectDistinct();
    }

    public void setBatchSize(int batchS) {
        table.setBatchSize(batchS);
        setDirty(true);
    }

    /**
     * Drop Staging table before extraction
     *
     * @param dropTable whether to drop staging table
     */
    public void setDropStagingTable(boolean dropTable) {
        table.setDropStagingTable(dropTable);
        setDirty(true);
    }

    /**
     * Truncate Staging table before extraction
     *
     * @param truncateTable whether to truncate staging table
     */
    public void setTruncateStagingTable(boolean truncateTable) {
        table.setTruncateStagingTable(truncateTable);
        setDirty(true);
    }

    public void setExtractionCondition(SQLCondition cond) throws BaseException {
        table.setExtractionCondition(cond);
        gNode.setConditionIcons();
        setDirty(true);
    }

    public void setValidationCondition(SQLCondition cond) throws BaseException {
        table.setDataValidationCondition(cond);
        gNode.setConditionIcons();
        setDirty(true);
    }

    /**
     * set the extraction condition text
     *
     * @param cond extraction condition text
     */
    public void setExtractionConditionText(String cond) {
        this.table.setExtractionConditionText(cond);
    }

    /**
     * set the validation condition text
     *
     * @param cond extraction condition text
     */
    public void setValidationConditionText(String cond) {
        this.table.setDataValidationConditionText(cond);
    }

    public void setExtractionType(String eType) {
        table.setExtractionType(eType);
        setDirty(true);
    }

    /**
     * set wehether to select distinct rows of a column
     *
     * @param distinct distinct
     */
    public void setSelectDistinct(boolean distinct) {
        table.setSelectDistinct(distinct);
        setDirty(true);
    }
}