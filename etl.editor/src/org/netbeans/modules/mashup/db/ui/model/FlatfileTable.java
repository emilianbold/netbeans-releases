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
package org.netbeans.modules.mashup.db.ui.model;

import java.util.List;

import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.sql.framework.model.DBColumn;

import com.sun.sql.framework.utils.StringUtil;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.ForeignKey;
import org.netbeans.modules.sql.framework.model.Index;
import org.netbeans.modules.sql.framework.model.PrimaryKey;

/**
 * Abstract bean wrapper for FlatfileDBTable instances to expose common read-only
 * properties for display in a Flatfile Database definition property sheet.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public abstract class FlatfileTable {
    private FlatfileDBTable mDelegate;
    private static transient final Logger mLogger = Logger.getLogger(FlatfileTable.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    public static final Integer ZERO = new Integer(0);

    /**
     * Creates new instance of FlatfileTable, wrapping the given FlatfileDBTable instance.
     * 
     * @param dbTable FlatfileDBTable instance (delimited type) to be wrapped.
     */
    protected FlatfileTable(FlatfileDBTable dbTable) {
        mDelegate = dbTable;
    }

    /**
     * Gets instance of FlatfileDBTble wrapped by this object.
     * 
     * @return FlatfileDBTable associated with this instance
     */
    public final FlatfileDBTable getSource() {
        return mDelegate;
    }

    /**
     * Gets file name.
     * 
     * @return file name
     */
    public String getFileName() {
        return mDelegate.getFileName();
    }

    /**
     * Gets table name associated with this flatfile.
     * 
     * @return table name
     */
    public String getTableName() {
        return mDelegate.getTableName();
    }

    /**
     * Gets property string associated with the given name.
     * 
     * @param propName property key
     * @return property associated with propName, or null if no such property exists.
     */
    public String getProperty(String propName) {
        return mDelegate.getProperty(propName);
    }

    /**
     * Gets name of this instance - ordinarily the same as getTableName().
     * 
     * @return name
     * @see #getTableName()
     */
    public String getName() {
        return mDelegate.getName();
    }

    /**
     * Gets optional description of this table.
     * 
     * @return table description string
     */
    public String getDescription() {
        String desc = mDelegate.getDescription();
        String nbBundle1 = mLoc.t("BUND182: <None>");
        return (StringUtil.isNullString(desc)) ? nbBundle1.substring(15) : desc;
    }

    /**
     * Gets schema, if any, with which the table is associated.
     * 
     * @return name of associated schema; may return "<None>" if value is null or empty
     *         string
     */
    public String getSchema() {
        String schema = mDelegate.getSchema();
        String nbBundle2 = mLoc.t("BUND182: <None>");
        return (StringUtil.isNullString(schema)) ? nbBundle2.substring(15) : schema;
    }

    /**
     * Gets catalog, if any, with which the table is associated.
     * 
     * @return name of associated catalog; may return "<None>" if value is null or empty
     *         string
     */
    public String getCatalog() {
        String catalog = mDelegate.getCatalog();
        String nbBundle3 = mLoc.t("BUND182: <None>");
        return (StringUtil.isNullString(catalog)) ? nbBundle3.substring(15) : catalog;
    }

    /**
     * Gets column, if any, with the given name.
     * 
     * @param name name of column to get
     * @return DBColumn associated with <code>name</code>, or null if no such column
     *         exists
     */
    public DBColumn getColumn(String name) {
        return mDelegate.getColumn(name);
    }

    /**
     * Gets List of columns contained by this instance.
     * 
     * @return List of FlatfileDBColumns that are part of this instance
     */
    public List getColumnList() {
        return mDelegate.getColumnList();
    }

    /**
     * Gets PrimaryKey instance, if any, associated with this table.
     * 
     * @return PrimaryKey instance, or null if no PK exists
     */
    public PrimaryKey getPrimaryKey() {
        return mDelegate.getPrimaryKey();
    }

    /**
     * Gets List, possibly empty, of foreign keys associated with this table.
     * 
     * @return List, possibly empty, of associated foreign keys
     */
    public List getForeignKeys() {
        return mDelegate.getForeignKeys();
    }

    /**
     * Gets ForeignKey instance, if any, with the given name and associated with this
     * table.
     * 
     * @return ForeignKey instance, or null if no such key exists with the name
     *         <code>keyname</code>
     */
    public ForeignKey getForeignKey(String keyName) {
        return mDelegate.getForeignKey(keyName);
    }

    /**
     * Gets List, possibly empty, of Indexes associated with this table.
     * 
     * @return List, possibly empty, of assocaited Index objects
     */
    public List getIndexes() {
        return mDelegate.getIndexes();
    }

    /**
     * Gets Index instance, if any, associated with the given name.
     * 
     * @param name name of index to get
     * @return Index instance, or null if no such Index exists with the name
     *         <code>name</code>
     */
    public Index getIndex(String name) {
        return mDelegate.getIndex(name);
    }

    /**
     * Gets name of encoding scheme to use in parsing underlying flatfile data
     * 
     * @return current encoding scheme
     */
    public String getEncodingScheme() {
        return mDelegate.getEncodingScheme();
    }

    /**
     * Gets the current file type - Delimited or FixedWidth
     * 
     * @return String representing current file type, one of 'Delimited' or 'FixedWidth'
     */
    public String getFileType() {
        return mDelegate.getProperty(PropertyKeys.LOADTYPE);
    }

    /**
     * Gets number of rows to skip as header information (non-data)
     * 
     * @return Integer representing rows to skip
     */
    public Integer getRowsToSkip() {
        String valStr = mDelegate.getProperty(PropertyKeys.ROWSTOSKIP);
        return (valStr != null) ? Integer.valueOf(valStr) : ZERO;
    }

    /**
     * Gets threshold number of faults, above which further parsing/execution should be
     * halted.
     * 
     * @return Integer representing maximum number of parsing faults to tolerate before
     *         raising an exception.
     */
    public Integer getMaxFaults() {
        String valStr = mDelegate.getProperty(PropertyKeys.MAXFAULTS);
        return (valStr != null) ? Integer.valueOf(valStr) : ZERO;
    }

    /**
     * Gets the character(s) used to delimit records in this file.
     * 
     * @return record delimiter string
     */
    public String getRecordDelimiter() {
        String delimiter = getProperty(PropertyKeys.RECORDDELIMITER);
        String nbBundle4 = mLoc.t("BUND182: <None>");
        if (delimiter == null || delimiter.length() == 0) {
            delimiter = nbBundle4.substring(15);
        } else {
            delimiter = StringUtil.escapeControlChars(delimiter);
        }
        return delimiter;
    }

    /**
     * Indicates whether the first physical record of the flatfile contains field header
     * information.
     * 
     * @return true if first physical record contains field header information; false
     *         otherwise
     */
    public boolean isFirstLineHeader() {
        return Boolean.valueOf(getProperty(PropertyKeys.ISFIRSTLINEHEADER)).booleanValue();
    }


    /**
     * Indicates whether white space trimming is enabled or not
     **/
    public boolean enableWhiteSpaceTrimming() {
        String whiteSpaceStr = getProperty(PropertyKeys.TRIMWHITESPACE);
	    whiteSpaceStr = (whiteSpaceStr == null ) ? "true" : whiteSpaceStr;
	    return Boolean.valueOf(whiteSpaceStr).booleanValue();
    }

    /**
     * Gets the SQL select statement to retrieve a result set displaying this file's
     * contents, using the given value as a limit to the number of rows returned.
     * 
     * @param rowCount number of rows to display; 0 returns all available rows
     * @return SQL statement to select the contents of this file in the column order
     *         specified by this instance's FlatfileFields.
     */
    public String getSelectStatementSQL(int rows) {
        return mDelegate.getSelectStatementSQL(rows);
    }
}
