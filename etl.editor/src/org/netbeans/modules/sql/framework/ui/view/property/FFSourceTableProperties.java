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
import org.openide.nodes.Node;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.utils.Attribute;
import com.sun.sql.framework.utils.StringUtil;

/**
 * @author Ahimanikya Satapathy
 */
public class FFSourceTableProperties {

    protected SourceTableProperties mDeligate;

    public FFSourceTableProperties(SourceTableProperties deligate) {
        mDeligate = deligate;
    }

    /**
     * Gets the alias name for this mDeligate.
     * 
     * @return alias name
     */
    public String getAliasName() {
        return mDeligate.getAliasName();
    }

    /**
     * Gets the batch size for this mDeligate.
     * 
     * @return Integer representing batch size for the table
     */
    public Integer getBatchSize() {
        return mDeligate.getBatchSize();
    }

    public PropertyEditor getCustomEditor(Node.Property property) {
        return mDeligate.getCustomEditor(property);
    }

    /**
     * Gets display name of this mDeligate.
     * 
     * @return table disply name.
     */
    public String getDisplayName() {
        return mDeligate.getDisplayName();
    }

    // RFE-102428
    /**
     * Gets staging name of this mDeligate.
     * 
     * @return staging table name.
     */
    public String getStagingTableName() {
        return mDeligate.getStagingTableName();
    }

    /**
     * Gets data extraction condition.
     * 
     * @return
     */
    public SQLCondition getExtractionCondition() {
        return mDeligate.getExtractionCondition();
    }

    /**
     * get the extraction conidition text
     * 
     * @return sql condition
     */
    public String getExtractionConditionText() {
        return mDeligate.getExtractionConditionText();
    }

    public String getExtractionType() {
        return mDeligate.getExtractionType();
    }

    /**
     * Gets delimited String list of foreign keys associated with this mDeligate.
     * 
     * @return delimited String list of foreign keys
     */
    public String getForeignKeys() {
        return mDeligate.getForeignKeys();
    }

    /**
     * Gets delimited String list of indices associated with this mDeligate.
     * 
     * @return delimited String list of indices
     */
    public String getIndices() {
        return mDeligate.getIndices();
    }

    /**
     * Get the LoadType attribute for this Flat file source table
     * "Delimited", "FixedWidth" etc.
     * 
     * @return LoadType
     */
    public String getFileType() {
        Attribute attr = mDeligate.getOrgProperty("LOADTYPE");
        if (attr != null) {
            return attr.getAttributeValue().toString();
        } else {
            return "";
        }
    }

    /**
     * Get the FileName for this Flat file source table
     * "Delimited", "FixedWidth" etc.
     * 
     * @return LoadType
     */
    public String getFileName() {
        Attribute attr = mDeligate.getOrgProperty("FILENAME");
        if (attr != null) {
            return attr.getAttributeValue().toString();
        } else {
            return "";
        }
    }

    public String getTextQualifier() {
        Attribute attr = mDeligate.getOrgProperty("QUALIFIER");
        if (attr != null) {
            return attr.getAttributeValue().toString();
        } else {
            return "";
        }
    }

    public boolean isFirstLineHeader() {
        Attribute attr = mDeligate.getOrgProperty("ISFIRSTLINEHEADER");
        if (attr != null) {
            return Boolean.valueOf(attr.getAttributeValue().toString());
        } else {
            return false;
        }
    }

    public String getFieldDelimiter() {
        Attribute attr = mDeligate.getOrgProperty("FIELDDELIMITER");
        if (attr != null) {
            return attr.getAttributeValue().toString();
        } else {
            return "";
        }
    }

    public String getRecordDelimiter() {
        Attribute attr = mDeligate.getOrgProperty("RECORDDELIMITER");
        if (attr != null) {
            return StringUtil.escapeControlChars(attr.getAttributeValue().toString());
        } else {
            return "";
        }
    }

    public Integer getRowsToSkip() {
        Attribute attr = mDeligate.getOrgProperty("ROWSTOSKIP");
        if (attr != null) {
            return Integer.valueOf(attr.getAttributeValue().toString());
        } else {
            return 0;
        }
    }

    public boolean isTrimWhiteSpace() {
        Attribute attr = mDeligate.getOrgProperty("TRIMWHITESPACE");
        if (attr != null) {
            return Boolean.valueOf(attr.getAttributeValue().toString());
        } else {
            return false;
        }
    }

    /**
     * RSS Table methods
     */
    /**
     * Get the FileName for this Flat file source table
     * "Delimited", "FixedWidth" etc.
     * 
     * @return LoadType
     */
    public String getUrl() {
        Attribute attr = mDeligate.getOrgProperty("URL");
        if (attr != null) {
            return attr.getAttributeValue().toString();
        } else {
            return "";
        }
    }

    /**
     * Gets name of parent DatabaseModel for this table
     * 
     * @return name of parent DatabaseModel
     */
    public String getModelName() {
        return mDeligate.getModelName();
    }

    /**
     * Gets delimited String list of primary key columns associated with this mDeligate.
     * 
     * @return delimited String list of primary key columns
     */
    public String getPrimaryKeys() {
        return mDeligate.getPrimaryKeys();
    }

    /**
     * Gets data validation condition.
     * 
     * @return
     */
    public SQLCondition getValidationCondition() {
        return mDeligate.getValidationCondition();
    }

    /**
     * get the validation conidition text
     * 
     * @return sql condition
     */
    public String getValitionConditionText() {
        return mDeligate.getValitionConditionText();
    }

    /**
     * get whether to drop staging table before extraction
     * 
     * @return  whether to drop staging table
     */
    public boolean isDropStagingTable() {
        return mDeligate.isDropStagingTable();
    }

    /**
     * get whether to truncate staging table before extraction
     * 
     * @return  whether to truncate staging table
     */
    public boolean isTruncateStagingTable() {
        return mDeligate.isTruncateStagingTable();
    }

    /**
     * check if distinct rows of a column needs to be selected
     * 
     * @return distinct
     */
    public boolean isSelectDistinct() {
        return mDeligate.isSelectDistinct();
    }

    /**
     * Sets the alias name for this table
     * 
     * @param aName alias name
     */
    public void setAliasName(String aName) {
        this.mDeligate.setAliasName(aName);
    }

    public void setFileType(String newFileType) {
        mDeligate.setOrgProperty("LOADTYPE", newFileType);
    }

    public void setFileName(String newFileName) {
        mDeligate.setOrgProperty("FILENAME", newFileName);
    }

    public void setFieldDelimiter(String newDelimiter) {
        mDeligate.setOrgProperty("FIELDDELIMITER", newDelimiter);
    }

    public void setTextQualifier(String newTextQualifier) {
        mDeligate.setOrgProperty("QUALIFIER", newTextQualifier);
    }

    public void setFirstLineHeader(boolean firstLineHeader) {
        Boolean b = new Boolean(firstLineHeader);
        mDeligate.setOrgProperty("ISFIRSTLINEHEADER", b.toString());
    }

    public void setRecordDelimiter(String newDelimter) {
        mDeligate.setOrgProperty("RECORDDELIMITER", newDelimter);
    }

    public void setRowsToSkip(String rowsToSkip) {
        mDeligate.setOrgProperty("ROWSTOSKIP", rowsToSkip);
    }

    public void setTrimWhiteSpace(boolean trimWhiteSpace) {
        Boolean b = new Boolean(trimWhiteSpace);
        mDeligate.setOrgProperty("TRIMWHITESPACE", b.toString());
    }

    public void setBatchSize(int batchS) {
        mDeligate.setBatchSize(batchS);
    }

    /**
     * Sets the batch size for this table
     * 
     * @param newSize new value for batch size
     */
    public void setBatchSize(Integer newSize) {
        mDeligate.setBatchSize(newSize.intValue());
    }

    /**
     * Drop Staging table before extraction
     * 
     * @param  whether to drop staging table
     */
    public void setDropStagingTable(boolean drop) {
        mDeligate.setDropStagingTable(drop);
    }

    public void setExtractionCondition(SQLCondition cond) throws BaseException {
        mDeligate.setExtractionCondition(cond);
    }

    //RFE-102428
    /**
     * set the Staging table name
     * 
     * @param stName stging table name
     */
    public void setStagingTableName(String stName) {
        mDeligate.setStagingTableName(stName);
    }

    /**
     * Truncate Staging table before extraction
     * 
     * @param  whether to truncate staging table
     */
    public void setTruncateStagingTable(boolean truncateTable) {
        mDeligate.setTruncateStagingTable(truncateTable);
    }

    /**
     * set the extraction condition text
     * 
     * @param cond extraction condition text
     */
    public void setExtractionConditionText(String cond) {
        mDeligate.setExtractionConditionText(cond);
    }

    public void setExtractionType(String eType) {
        mDeligate.setExtractionType(eType);
    }

    /**
     * set wehether to select distinct rows of a column
     * 
     * @param distinct distinct
     */
    public void setSelectDistinct(boolean distinct) {
        mDeligate.setSelectDistinct(distinct);
    }

    public void setValidationCondition(SQLCondition cond) throws BaseException {
        mDeligate.setValidationCondition(cond);
    }

    /**
     * set the validation condition text
     * 
     * @param cond extraction condition text
     */
    public void setValidationConditionText(String cond) {
        mDeligate.setValidationConditionText(cond);
    }

    /**
     * RSS Table methods
     */
    /**
     * Get the FileName for this Flat file source table
     * "Delimited", "FixedWidth" etc.
     * 
     * @return LoadType
     */
    public void setUrl(String newUrl) {
        mDeligate.setOrgProperty("URL", newUrl);
    }
}
