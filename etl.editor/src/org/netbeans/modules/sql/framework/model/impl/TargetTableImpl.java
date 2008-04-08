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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.codegen.AbstractGeneratorFactory;
import org.netbeans.modules.sql.framework.codegen.DB;
import org.netbeans.modules.sql.framework.codegen.DBFactory;
import org.netbeans.modules.sql.framework.codegen.SQLOperatorFactory;
import org.netbeans.modules.sql.framework.codegen.StatementContext;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLCaseOperator;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConnectableObject;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLGroupBy;
import org.netbeans.modules.sql.framework.model.SQLInputObject;
import org.netbeans.modules.sql.framework.model.SQLJoinView;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SQLPredicate;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.TargetColumn;
import org.netbeans.modules.sql.framework.model.TargetTable;
import org.netbeans.modules.sql.framework.model.utils.ConditionUtil;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;

/**
 * Concrete implementation of TargetTable and SQLConnectableObject classes / interfaces,
 * representing table metadata and linking information for a target table.
 * 
 * @author Jonathan Giron
 * @version $Revision$
 */
public class TargetTableImpl extends AbstractDBTable implements TargetTable {

    /**
     * type of statement to generate while loading target table
     * 
     */
    public static final String ATTR_STATEMENT_TYPE = "statementType";
    private static final String ATTR_BATCHSIZE = "batchSize";
    private static final String ATTR_FULLY_QUALIFIED_NAME = "fullyQualifiedName";
    private static final String ATTR_CREATE_TARGET_TABLE = "createTargetTable";
    private static final String ATTR_TRUNCATE_BEFORE_LOAD = "truncateBeforeLoad";
    private static transient final Logger mLogger = Logger.getLogger(TargetTableImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private SQLCondition joinCondition;
    private SQLCondition filterCondition;
    private SQLCondition havingCondition;
    /** having condition tag */
    public static final String HAVING_CONDITION = "havingCondition";
    private SQLGroupBy groupBy;

    /** Constructs a new default instance of TargetTableImpl. */
    public TargetTableImpl() {
        super();
        init();
    }

    /**
     * Constructs a new instance of TargetTable, cloning the contents of the given DBTable
     * implementation instance.
     * 
     * @param src DBTable instance to be cloned
     */
    public TargetTableImpl(DBTable src) {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null DBTable instance for src param.");
        }
        copyFrom(src);
    }

    /**
     * Constructs a new instance of TargetTable with the given name.
     * 
     * @param aName name of new DBTable instance
     * @param aSchema schema of new DBTable instance; may be null
     * @param aCatalog catalog of new DBTable instance; may be null
     */
    public TargetTableImpl(String aName, String aSchema, String aCatalog) {
        super(aName, aSchema, aCatalog);
        init();
    }

    /*
     * Implementation of DBTable interface.
     */
    /**
     * Adds a TargetColumn instance to this table.
     * 
     * @param aColumn column to be added.
     * @return true if successful. false if failed.
     */
    public boolean addColumn(TargetColumn aColumn) {
        if (aColumn != null) {
            aColumn.setParent(this);
            columns.put(aColumn.getName(), aColumn);

            return true;
        }

        return false;
    }

    /**
     * @see SQLConnectableObject#addInput
     */
    public void addInput(String argName, SQLObject newInput) throws BaseException {
        if (argName == null || argName.trim().length() == 0) {
            throw new IllegalArgumentException("Must supply non-empty String ref for parameter argName.");
        }

        if (newInput == null) {
            throw new IllegalArgumentException("Must supply non-null SQLObject ref for parameter newInput.");
        }

        int newType = newInput.getObjectType();
        String objType = TagParserUtility.getStringType(newType);

        if (!isInputValid(argName, newInput)) {
            throw new BaseException("Cannot link " + objType + " '" + newInput.getDisplayName() + "' as input to '" + argName + "' in " + TagParserUtility.getDisplayStringFor(this.type) + " '" + this.getDisplayName() + "'");
        }

        // Now locate associated column and set newInput as its value.
        TargetColumn col = (TargetColumn) columns.get(argName);
        if (col != null) {
            col.setValue(newInput);
        } else {
            throw new BaseException("Could not locate column associated with argName: " + argName);
        }

        // Now if the target column where we added input is primary key or unique
        // then we need to add a constraint check filter  by default
        if (col != null && col.isPrimaryKey() && newInput instanceof SQLDBColumn) {
            try {
                SQLCondition cond = this.getJoinCondition();
                if (cond == null) {
                    cond = SQLModelObjectFactory.getInstance().createSQLCondition(JOIN_CONDITION);
                }
                cond.addEqualityPredicate(newInput, col);
            } catch (BaseException ex) {
                // we should not throw this exception; this is just fail to
                // create an automatic filter so we can ignore now.
                mLogger.errorNoloc(mLoc.t("EDIT120: Could not create auto joinCondition for target table.{0}", TargetTableImpl.class.getName()), ex);
            }
        }
    }

    /**
     * Clone a copy of TargetTableImpl.
     * 
     * @return a copy of TargetTableImpl.
     */
    public Object clone() {
        try {
            TargetTableImpl aClone = new TargetTableImpl(this);
            return aClone;
        } catch (Exception e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Sets the various member variables and collections using the given DBTable instance
     * as a source object. Concrete implementations should override this method, call
     * super.copyFrom(DBColumn) to pick up member variables defined in this class and then
     * implement its own logic for copying member variables defined within itself.
     * 
     * @param source DBTable from which to obtain values for member variables and
     *        collections
     */
    public void copyFrom(DBTable source) {
        super.copyFrom(source);

        List sourceColumns = source.getColumnList();
        if (sourceColumns != null) {
            Iterator iter = sourceColumns.iterator();

            // Must do deep copy to ensure correct parent-child relationship.
            while (iter.hasNext()) {
                addColumn(SQLModelObjectFactory.getInstance().createTargetColumn((DBColumn) iter.next()));
            }
        }

        if (source instanceof TargetTable) {
            TargetTable tgtSource = (TargetTable) source;
            SQLCondition srcJoinCondition = tgtSource.getJoinCondition();
            if (srcJoinCondition != null) {
                try {
                    joinCondition = (SQLCondition) srcJoinCondition.cloneSQLObject();
                } catch (CloneNotSupportedException ignore) {
                    // ignore
                }
            }

            SQLCondition srcFilterCondition = tgtSource.getFilterCondition();
            if (srcFilterCondition != null) {
                try {
                    filterCondition = (SQLCondition) srcFilterCondition.cloneSQLObject();
                } catch (CloneNotSupportedException ignore) {
                    // ignore
                }
            }

            SQLGroupBy grpBy = tgtSource.getSQLGroupBy();
            if (grpBy != null) {
                groupBy = new SQLGroupByImpl(grpBy);
            }
        }
    }

    /**
     * Convenience class to create DBColumnImpl instance (with the given column name, data
     * source name, JDBC type, scale, precision, and nullable), and add it to this
     * TargetTable instance.
     * 
     * @param columnName Column name
     * @param jdbcType JDBC type defined in SQL.Types
     * @param scale Scale
     * @param precision Precision
     * @param isPK true if part of primary key, false otherwise
     * @param isFK true if part of foreign key, false otherwise
     * @param isIndexed true if indexed, false otherwise
     * @param nullable Nullable
     * @return new DBColumnImpl instance
     */
    public TargetColumn createColumn(String columnName, int jdbcType, int scale, int precision, boolean isPK, boolean isFK, boolean isIndexed,
            boolean nullable) {
        TargetColumn impl = SQLModelObjectFactory.getInstance().createTargetColumn(columnName, jdbcType, scale, precision, isPK, isFK, isIndexed, nullable);
        addColumn(impl);
        return impl;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBColumn#equals(java.lang.Object)
     */
    public boolean equals(Object obj) {
        // Check for reflexivity first.
        if (this == obj) {
            return true;
        } else if (!(obj instanceof TargetTable)) {
            return false;
        }

        boolean result = super.equals(obj);
        TargetTable src = (TargetTable) obj;

        result &= (joinCondition != null) ? joinCondition.equals(src.getJoinCondition()) : (src.getJoinCondition() == null);
        result &= (filterCondition != null) ? filterCondition.equals(src.getFilterCondition()) : (src.getFilterCondition() == null);

        return result;
    }

    /**
     * Gets list of child sql objects.
     * 
     * @return child sql objects
     */
    public List getChildSQLObjects() {
        return this.getColumnList();
    }

    /**
     * Gets the target table joinCondition.
     * 
     * @return target table conidiotn
     */
    public SQLCondition getJoinCondition() {
        // if there are no objects in graph then populate graph with the objects
        // obtained from sql text
        Collection objC = joinCondition.getAllObjects();
        if (objC == null || objC.size() == 0) {
            SQLDefinition def = SQLObjectUtil.getAncestralSQLDefinition(this);
            try {
                ConditionUtil.populateCondition(joinCondition, def, this.getJoinConditionText());
            } catch (Exception ex) {
                // ignore this if joinCondition typed by user is invalid
            }
        }

        return joinCondition;
    }

    /**
     * Gets joinCondition text.
     * 
     * @return sql joinCondition
     */
    public String getJoinConditionText() {
        return joinCondition.getConditionText();
    }

    /**
     * @see SQLConnectableObject#addInput
     */
    public SQLInputObject getInput(String argName) {
        if (argName == null || argName.trim().length() == 0) {
            throw new IllegalArgumentException("Must supply non-empty String ref for parameter argName.");
        }

        TargetColumn col = (TargetColumn) columns.get(argName);
        return (col != null) ? new SQLInputObjectImpl(argName, argName, col) : null;
    }

    /**
     * @see SQLConnectableObject#getInputObjectMap
     */
    public Map getInputObjectMap() {
        Map inputMap = Collections.EMPTY_MAP;

        if (columns.size() != 0) {
            inputMap = new LinkedHashMap(columns.size());

            Iterator iter = columns.values().iterator();
            while (iter.hasNext()) {
                TargetColumn col = (TargetColumn) iter.next();
                SQLInputObject input = new SQLInputObjectImpl(col.getName(), col.getDisplayName(), col.getValue());
                inputMap.put(col.getName(), input);
            }
        }

        return inputMap;
    }

    /**
     * Gets the single join view which is mapped to this target table in case of multiple
     * source table mapping to this target table.
     * 
     * @return associated SQLJoinView, if any.
     */
    public SQLJoinView getJoinView() {
        SQLJoinView joinView = null;

        try {
            List cols = this.getColumnList();
            Iterator it = cols.iterator();
            while (it.hasNext()) {
                TargetColumn tc = (TargetColumn) it.next();
                SQLObject sqlObj = tc.getValue();
                // we only need to search for first mapped target column
                if (sqlObj != null) {
                    joinView = discoverJoinView(sqlObj);
                    if (joinView != null) {
                        break;
                    }
                }
            }
        } catch (BaseException ex) {
            mLogger.errorNoloc(mLoc.t("EDIT121: Could not find a join view for this target table{0}", this.getName()), ex);
        // Logger.printThrowable(Logger.ERROR, TargetTableImpl.class.getName(), "getJoinView", "Could not find a join view for this target table "
        //    + this.getName(), ex);
        }

        return joinView;
    }

    public List getMappedColumns() {
        ArrayList mappedColumns = new ArrayList();

        Iterator it = this.getColumnList().iterator();
        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            if (column.getValue() != null) {
                mappedColumns.add(column);
            }
        }

        return mappedColumns;
    }

    /**
     * @see SQLObject#getOutput(java.lang.String)
     */
    public SQLObject getOutput(String argName) throws BaseException {
        throw new BaseException("TargetTable cannot supply an output SQLObject.");
    }

    public List getSourceColumnsUsed() {
        return new ArrayList();
    }

    /**
     * Gets the source table list.
     * 
     * @return List of all source tables
     * @throws BaseException if error occurs while getting Source Table List
     */
    public List getSourceTableList() throws BaseException {

        if (this.getJoinView() != null) {
            return this.getJoinView().getSourceTables();
        }

        List cols = this.getColumnList();
        List tables = new ArrayList();
        Iterator it = cols.iterator();
        while (it.hasNext()) {
            TargetColumn tc = (TargetColumn) it.next();
            SQLObject sqlObj = tc.getValue();
            if (sqlObj != null) {
                discoverTables(sqlObj, tables);
            }
        }
        return tables;
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SourceTable#getSQLGroupBy()
     */
    public SQLGroupBy getSQLGroupBy() {
        return groupBy;
    }

    /**
     * @see SQLConnectableObject#getSQLObject
     */
    public SQLObject getSQLObject(String argName) {
        TargetColumn col = (TargetColumn) columns.get(argName);
        return (col != null) ? col.getValue() : null;
    }

    /**
     * @see SQLConnectableObject#getSQLObjectMap
     */
    public Map getSQLObjectMap() {
        Map objectMap = new LinkedHashMap(10);
        Iterator iter = columns.entrySet().iterator();

        while (iter.hasNext()) {
            Map.Entry entry = (Map.Entry) iter.next();

            String argName = (String) entry.getKey();
            TargetColumn col = (TargetColumn) entry.getValue();

            if (argName != null && col.getValue() != null) {
                objectMap.put(argName, col.getValue());
            }
        }

        return objectMap;
    }

    /**
     * Gets the type of statement type for this table.
     * 
     * @return statement type
     */
    public int getStatementType() {
        Integer sType = (Integer) this.getAttributeObject(ATTR_STATEMENT_TYPE);
        if (sType != null) {
            return sType.intValue();
        }

        return SQLConstants.INSERT_STATEMENT;
    }

    /**
     * Gets string representation of statement type.
     * 
     * @return statement type
     */
    public String getStrStatementType() {
        if (this.getStatementType() == SQLConstants.INSERT_STATEMENT) {
            return SQLConstants.STR_INSERT_STATEMENT;
        } else if (this.getStatementType() == SQLConstants.INSERT_UPDATE_STATEMENT) {
            return SQLConstants.STR_INSERT_UPDATE_STATEMENT;
        } else if (this.getStatementType() == SQLConstants.UPDATE_STATEMENT) {
            return SQLConstants.STR_UPDATE_STATEMENT;
        } else if (this.getStatementType() == SQLConstants.DELETE_STATEMENT) {
            return SQLConstants.STR_DELETE_STATEMENT;
        }

        return null;
    }

    /**
     * Gets the TargetColumn, if any, associated with the given name
     * 
     * @param columnName column name
     * @return TargetColumn associated with columnName, or null if none exists
     */
    public TargetColumn getTargetColumn(String columnName) {
        return (TargetColumn) columns.get(columnName);
    }

    public List getTargetColumnsUsed() {
        return getMappedColumns();
    }

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        return super.hashCode() + ((joinCondition != null) ? joinCondition.hashCode() : 0) + ((getJoinConditionText() != null) ? getJoinConditionText().hashCode() : 0) + ((filterCondition != null) ? filterCondition.hashCode() : 0) + ((getFilterConditionText() != null) ? getFilterConditionText().hashCode() : 0);
    }

    public boolean hasSourceColumn() {
        return false;
    }

    public boolean hasTargetColumn() {
        return true;
    }

    /**
     * Indicates whether to create target table if it does not already exist.
     * 
     * @return whether to create target table
     */
    public boolean isCreateTargetTable() {
        Boolean create = (Boolean) this.getAttributeObject(ATTR_CREATE_TARGET_TABLE);
        if (create != null) {
            return create.booleanValue();
        }

        return true;
    }

    /**
     * Indicates whether this is an expression object.
     * 
     * @return true if the object is a Expression Object
     */
    public boolean isExpressionObject() {
        return true;
    }

    /**
     * @see SQLConnectableObject#isInputCompatible
     */
    public int isInputCompatible(String argName, SQLObject input) {
        int srcType = SQLConstants.JDBCSQL_TYPE_UNDEFINED;

        TargetColumn targetCol = (TargetColumn) columns.get(argName);
        if (targetCol != null) {
            switch (input.getObjectType()) {
                case SQLConstants.GENERIC_OPERATOR:
                case SQLConstants.CAST_OPERATOR:
                case SQLConstants.DATE_DIFF_OPERATOR:
                case SQLConstants.DATE_ADD_OPERATOR:
                case SQLConstants.SOURCE_COLUMN:
                case SQLConstants.VISIBLE_LITERAL:
                case SQLConstants.CASE:
                    srcType = input.getJdbcType();
                    break;
                case SQLConstants.CUSTOM_OPERATOR:
                    srcType = ((SQLCustomOperatorImpl) input).getOutputJdbcType();
                    break;
                case SQLConstants.LITERAL:
                    return SQLConstants.TYPE_CHECK_SAME;

                default:
                    return SQLConstants.TYPE_CHECK_INCOMPATIBLE;
            }

            return SQLOperatorFactory.getDefault().getCastingRuleFor(srcType, targetCol.getJdbcType());
        }
        return SQLConstants.TYPE_CHECK_INCOMPATIBLE;
    }

    /**
     * @see SQLConnectableObject#isInputValid
     */
    public boolean isInputValid(String argName, SQLObject input) {
        if (input == null) {
            return false;
        }

        switch (input.getObjectType()) {
            case SQLConstants.GENERIC_OPERATOR:
            case SQLConstants.CAST_OPERATOR:
            case SQLConstants.CUSTOM_OPERATOR:
            case SQLConstants.DATE_DIFF_OPERATOR:
            case SQLConstants.DATE_ADD_OPERATOR:
            case SQLConstants.LITERAL:
            case SQLConstants.VISIBLE_LITERAL:
            case SQLConstants.CASE:
            case SQLConstants.SOURCE_COLUMN:
                TargetColumn col = (TargetColumn) columns.get(argName);
                return (col != null && col.getValue() == null);

            default:
                return false;
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.TargetTable#isTruncateBeforeLoad()
     */
    public boolean isTruncateBeforeLoad() {
        Boolean shouldTruncate = (Boolean) this.getAttributeObject(ATTR_TRUNCATE_BEFORE_LOAD);
        return (shouldTruncate != null) ? shouldTruncate.booleanValue() : false;
    }

    public boolean isUsingFullyQualifiedName() {
        Boolean fullName = (Boolean) this.getAttributeObject(ATTR_FULLY_QUALIFIED_NAME);
        if (fullName != null) {
            return fullName.booleanValue();
        }
        return true;
    }

    /**
     * @see SQLConnectableObject#removeInputByArgName
     */
    public SQLObject removeInputByArgName(String argName, SQLObject sqlObj) throws BaseException {
        if (argName == null) {
            throw new BaseException("Must supply non-null SQLObject ref for argName.");
        }

        SQLObject victim = null;
        TargetColumn col = (TargetColumn) columns.get(argName);

        if (col != null) {
            victim = col.getValue();
            col.setValue(null);
        }

        // Now if the target column where we removed input is primary key or unique
        // then we need to remove a filter.
        try {
            if (col != null && col.isPrimaryKey() && victim instanceof SQLDBColumn) {
                SQLCondition cond = this.getJoinCondition();

                if (cond != null) {
                    cond.removeEqualsPredicate(col, victim);
                }

                // Now get the joinCondition text and set it
                int currentMode = cond.getGuiMode();
                // As SQLCondition mode may be text or graphical depending on whether this
                // method is invoked from main eTL canvas or Condition editor canvas.
                cond.setGuiMode(SQLCondition.GUIMODE_GRAPHICAL);
                SQLPredicate pred = cond.getRootPredicate();
                cond.setGuiMode(currentMode);

                if (pred != null) {
                    DB db = DBFactory.getInstance().getDatabase(DB.BASEDB);
                    AbstractGeneratorFactory genFactory = db.getGeneratorFactory();
                    StatementContext stmtContext = new StatementContext();
                    stmtContext.setUseSourceTableAliasName(true);
                    stmtContext.setUseTargetTableAliasName(true);
                    this.setJoinConditionText(genFactory.generate(pred, stmtContext));
                } else {
                    this.setJoinConditionText("");
                }
            }

        } catch (BaseException ex) {
            // we should not throw this exception; this is just fail to
            // create an automatic filter so we can ignore now.
            //Logger.printThrowable(Logger.ERROR, TargetTableImpl.class.getName(), "addInput", "Could not create auto joinCondition for target table.", ex);
            mLogger.errorNoloc(mLoc.t("EDIT120: Could not create auto joinCondition for target table.{0}", TargetTableImpl.class.getName()), ex);
        }

        return victim;
    }

    /**
     * Clear all column references
     */
    public void reset() {
        super.reset();
        Iterator it = getColumns().values().iterator();
        while (it.hasNext()) {
            TargetColumn column = (TargetColumn) it.next();
            column.setValue(null);
        }
    }

    /**
     * Parses elements which require a second pass to resolve their values.
     * 
     * @param element DOM element containing XML marshalled version of this SQLObject
     *        instance
     * @throws BaseException if element is null or error occurs during parsing
     */
    public void secondPassParse(Element element) throws BaseException {
        SQLDBModel parentDbModel = (SQLDBModel) parentObject;
        SQLDefinition definition = (SQLDefinition) parentDbModel.getParentObject();

        SQLObject obj = TagParserUtility.parseXMLObjectRefTag(definition, element);

        // if obj is null it may not be parsed yet so
        // do a second parse... This will take for any second parse for SQL objects
        if (obj == null) {
            definition.addSecondPassSQLObject(this, element);
        }
    }

    /**
     * Sets the target table joinCondition.
     * 
     * @param cond target table joinCondition
     */
    public void setJoinCondition(SQLCondition cond) {
        this.joinCondition = cond;
        if (this.joinCondition != null) {
            this.joinCondition.setParent(this);
            this.joinCondition.setDisplayName(TargetTable.JOIN_CONDITION);
        }
    }

    /**
     * Sets the joinCondition text.
     * 
     * @param cond sql joinCondition
     */
    public void setJoinConditionText(String cond) {
        joinCondition.setConditionText(cond);
    }

    /**
     * Sets whether to create target table if it does not already exist.
     * 
     * @param create whether to create target table
     */
    public void setCreateTargetTable(boolean create) {
        this.setAttribute(ATTR_CREATE_TARGET_TABLE, new Boolean(create));
    }

    /**
     * Sets whether to create target table if it does not already exist.
     * 
     * @param create whether to create target table
     */
    public void setCreateTargetTable(Boolean create) {
        this.setAttribute(ATTR_CREATE_TARGET_TABLE, create);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.SourceTable#setSQLGroupBy(org.netbeans.modules.sql.framework.model.SQLGroupBy)
     */
    public void setSQLGroupBy(SQLGroupBy groupBy) {
        this.groupBy = groupBy;
    }

    /**
     * Sets the type of statement type for this table.
     * 
     * @param sType statement type
     */
    public void setStatementType(int sType) {
        this.setAttribute(ATTR_STATEMENT_TYPE, new Integer(sType));
    }

    /**
     * Sets string representation of statement type.
     * 
     * @param stType statement type
     */
    public void setStrStatementType(String stType) {
        if (stType.equals(SQLConstants.STR_INSERT_STATEMENT)) {
            this.setStatementType(SQLConstants.INSERT_STATEMENT);
        } else if (stType.equals(SQLConstants.STR_INSERT_UPDATE_STATEMENT)) {
            this.setStatementType(SQLConstants.INSERT_UPDATE_STATEMENT);
        } else if (stType.equals(SQLConstants.STR_UPDATE_STATEMENT)) {
            this.setStatementType(SQLConstants.UPDATE_STATEMENT);
        } else if (stType.equals(SQLConstants.STR_DELETE_STATEMENT)) {
            this.setStatementType(SQLConstants.DELETE_STATEMENT);
        }
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.TargetTable#setTruncateBeforeLoad(boolean)
     */
    public void setTruncateBeforeLoad(boolean flag) {
        this.setAttribute(ATTR_TRUNCATE_BEFORE_LOAD, (flag ? Boolean.TRUE : Boolean.FALSE));
    }

    public void setTruncateBeforeLoad(Boolean flag) {
        this.setAttribute(ATTR_TRUNCATE_BEFORE_LOAD, (flag ? Boolean.TRUE : Boolean.FALSE));
    }

    /**
     * Returns XML representation of table metadata.
     * 
     * @param prefix prefix for the xml.
     * @param tableOnly flag for generating table only metadata.
     * @return XML representation of the table metadata.
     * @throws BaseException if error occurs
     */
    public String toXMLString(String prefix, boolean tableOnly) throws BaseException {
        StringBuilder xml = new StringBuilder(INIT_XMLBUF_SIZE);

        xml.append(prefix).append("<").append(TABLE_TAG);
        xml.append(" ").append(TABLE_NAME_ATTR).append("=\"").append(name).append("\"");

        xml.append(" ").append(ID_ATTR).append("=\"").append(id).append("\"");

        if (displayName != null && displayName.trim().length() != 0) {
            xml.append(" ").append(DISPLAY_NAME_ATTR).append("=\"").append(displayName).append("\"");
        }

        if (schema != null && schema.trim().length() != 0) {
            xml.append(" ").append(SCHEMA_NAME_ATTR).append("=\"").append(schema).append("\"");
        }

        if (catalog != null && catalog.trim().length() != 0) {
            xml.append(" ").append(CATALOG_NAME_ATTR).append("=\"").append(catalog).append("\"");
        }

        xml.append(">\n");

        xml.append(toXMLAttributeTags(prefix)).append("\n");

        if (!tableOnly) {
            writeColumns(prefix, xml);
            writePrimaryKey(prefix, xml);
            writeForeignKeys(prefix, xml);
            writeIndices(prefix, xml);
        }

        // write out joinCondition
        if (joinCondition != null) {
            xml.append(joinCondition.toXMLString(prefix + INDENT));
        }

        // write out filterCondition
        if (filterCondition != null) {
            xml.append(filterCondition.toXMLString(prefix + INDENT));
        }

        if (groupBy != null) {
            xml.append(groupBy.toXMLString(prefix + INDENT));
        }

        if (guiInfo != null) {
            xml.append(guiInfo.toXMLString(prefix + INDENT));
        }

        xml.append(prefix).append("</").append(TABLE_TAG).append(">\n");

        return xml.toString();
    }

    public void visit(SQLVisitor visitor) {
        visitor.visit(this);
    }

    /**
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBTable#getElementTagName
     */
    protected String getElementTagName() {
        return TABLE_TAG;
    }

    /**
     * Parses node elements to extract child components to various collections (columns,
     * PK, FK, indexes).
     * 
     * @param childNodeList Nodes to be unmarshalled
     * @throws BaseException if error occurs while parsing
     */
    protected void parseChildren(NodeList childNodeList) throws BaseException {
        for (int i = 0; i < childNodeList.getLength(); i++) {
            if (childNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNodeList.item(i);
                String tagName = childElement.getTagName();

                if (AbstractDBColumn.ELEMENT_TAG.equals(tagName)) {
                    TargetColumn columnInstance = new TargetColumnImpl();

                    columnInstance.setParentObject(this);
                    columnInstance.parseXML(childElement);

                    addColumn(columnInstance);
                } else if (PrimaryKeyImpl.ELEMENT_TAG.equals(tagName)) {
                    PrimaryKeyImpl pkInstance = new PrimaryKeyImpl(childElement);
                    pkInstance.parseXML();
                    setPrimaryKey(pkInstance);
                } else if (ForeignKeyImpl.ELEMENT_TAG.equals(tagName)) {
                    ForeignKeyImpl fkInstance = new ForeignKeyImpl(childElement);
                    fkInstance.parseXML();
                    addForeignKey(fkInstance);
                } else if (IndexImpl.ELEMENT_TAG.equals(tagName)) {
                    IndexImpl indexInstance = new IndexImpl(childElement);
                    indexInstance.parseXML();
                    addIndex(indexInstance);
                } else if (TagParserUtility.TAG_OBJECTREF.equals(tagName)) {
                    secondPassParse(childElement);
                } else if (GUIInfo.TAG_GUIINFO.equals(tagName)) {
                    guiInfo = new GUIInfo(childElement);
                } else if (SQLCondition.TAG_CONDITION.equals(tagName)) {
                    String conditionName = childElement.getAttribute(SQLCondition.DISPLAY_NAME);
                    if (conditionName != null && conditionName.equals(TargetTableImpl.JOIN_CONDITION)) {
                        SQLCondition cond1 = SQLModelObjectFactory.getInstance().createSQLCondition(JOIN_CONDITION);
                        SQLDBModel parentDbModel = (SQLDBModel) this.getParentObject();
                        if (parentDbModel != null) {
                            cond1.setParent(this);
                            cond1.parseXML(childElement);
                            this.setJoinCondition(cond1);
                        }
                    } else if (conditionName != null && conditionName.equals(TargetTableImpl.FILTER_CONDITION)) {
                        SQLCondition cond1 = SQLModelObjectFactory.getInstance().createSQLCondition(FILTER_CONDITION);
                        SQLDBModel parentDbModel = (SQLDBModel) this.getParentObject();
                        if (parentDbModel != null) {
                            cond1.setParent(this);
                            cond1.parseXML(childElement);
                            this.setFilterCondition(cond1);
                        }
                    }
                } else if (SQLGroupByImpl.ELEMENT_TAG.equals(tagName)) {
                    groupBy = new SQLGroupByImpl();
                    groupBy.setParentObject(this);
                    groupBy.parseXML(childElement);
                }
            }
        }
    }

    /**
     * Overrides parent implementation to also initialize locally-defined attributes.
     */
    protected void setDefaultAttributes() {
        super.setDefaultAttributes();

        if (this.getAttributeObject(ATTR_STATEMENT_TYPE) == null) {
            setStatementType(SQLConstants.INSERT_STATEMENT);
        }

        if (this.getAttributeObject(ATTR_CREATE_TARGET_TABLE) == null) {
            setCreateTargetTable(true);
        }

        if (this.getAttributeObject(ATTR_COMMIT_BATCH_SIZE) == null) {
            setBatchSize(-1);
        }
    }

    /**
     * Writes out column information to the given StringBuilder.
     * 
     * @param prefix String to prepend to each column element or child node
     * @param xml StringBuilder in which to write column information
     * @throws BaseException if error occurs during writing
     */
    protected void writeColumns(String prefix, StringBuilder xml) throws BaseException {
        Comparator cmp = new StringComparator();
        // Ensure columns are written out in ascending name order.
        List colList = new ArrayList(columns.keySet());
        Collections.sort(colList, cmp);

        Iterator iter = colList.listIterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            TargetColumn column = (TargetColumn) columns.get(key);
            xml.append(column.toXMLString(prefix + INDENT));
        }
    }

    /**
     * Writes out foreign key information to the given StringBuilder.
     * 
     * @param prefix String to prepend to each foreign key element or child node
     * @param xml StringBuilder in which to write foreign key information
     */
    protected void writeForeignKeys(String prefix, StringBuilder xml) {
        Comparator cmp = new StringComparator();
        List fkNames = new ArrayList(foreignKeys.keySet());
        Collections.sort(fkNames, cmp);
        Iterator iter = fkNames.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            ForeignKeyImpl fk = (ForeignKeyImpl) foreignKeys.get(key);
            xml.append(fk.toXMLString(prefix + INDENT));
        }
    }

    /**
     * Writes out index information to the given StringBuilder.
     * 
     * @param prefix String to prepend to each index element or child node
     * @param xml StringBuilder in which to write index information
     */
    protected void writeIndices(String prefix, StringBuilder xml) {
        Comparator cmp = new StringComparator();
        List indexNames = new ArrayList(indexes.keySet());
        Collections.sort(indexNames, cmp);
        Iterator iter = indexNames.iterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            IndexImpl index = (IndexImpl) indexes.get(key);
            xml.append(index.toXMLString(prefix + INDENT));
        }
    }

    /**
     * Writes out primary key information to the given StringBuilder.
     * 
     * @param prefix String to prepend to each primary key element or child node
     * @param xml StringBuilder in which to write primary key information
     */
    protected void writePrimaryKey(String prefix, StringBuilder xml) {
        if (primaryKey != null) {
            xml.append(primaryKey.toXMLString(prefix + INDENT));
        }
    }

    private SQLJoinView discoverJoinView(SQLObject obj) throws BaseException {
        if (obj.getObjectType() == SQLConstants.SOURCE_COLUMN) {
            SourceColumn column = (SourceColumn) obj;
            SourceTable table = (SourceTable) column.getParent();

            SQLDefinition def = TagParserUtility.getAncestralSQLDefinition(column);

            if (def != null) {
                Collection jViews = def.getObjectsOfType(SQLConstants.JOIN_VIEW);
                Iterator it = jViews.iterator();
                while (it.hasNext()) {
                    SQLJoinView jView = (SQLJoinView) it.next();
                    if (jView.containsSourceTable(table)) {
                        return jView;
                    }
                }
            }
        } else if (obj instanceof SQLConnectableObject) {
            SQLConnectableObject expObj = (SQLConnectableObject) obj;

            Map inputs = expObj.getInputObjectMap();
            Iterator it = inputs.values().iterator();

            while (it.hasNext()) {
                SQLInputObject inObj = (SQLInputObject) it.next();
                SQLObject sqlObj = inObj.getSQLObject();
                if (sqlObj != null) {
                    SQLJoinView jView = discoverJoinView(sqlObj);
                    if (jView != null) {
                        return jView;
                    }
                }
            }
            // go through children
            Iterator cIt = expObj.getChildSQLObjects().iterator();
            while (cIt.hasNext()) {
                SQLObject child = (SQLObject) cIt.next();
                SQLJoinView jView = discoverJoinView(child);
                if (jView != null) {
                    return jView;
                }
            }
        }

        return null;
    }

    private void discoverTables(SQLObject sqlObj, List sourceTables) throws BaseException {
        int objType = sqlObj.getObjectType();
        switch (objType) {
            case SQLConstants.PREDICATE:
            case SQLConstants.VISIBLE_PREDICATE:
            case SQLConstants.GENERIC_OPERATOR:
            case SQLConstants.CUSTOM_OPERATOR:
            case SQLConstants.CAST_OPERATOR:
            case SQLConstants.DATE_DIFF_OPERATOR:
            case SQLConstants.DATE_ADD_OPERATOR:
            case SQLConstants.CASE:
            case SQLConstants.JOIN:
                SQLConnectableObject expressionObj = (SQLConnectableObject) sqlObj;
                Map inputs = expressionObj.getSQLObjectMap();
                if (objType == SQLConstants.CASE) {
                    SQLCaseOperator myCase = (SQLCaseOperator) sqlObj;
                    List whenList = myCase.getChildSQLObjects();
                    Iterator whenItr = whenList.iterator();
                    while (whenItr.hasNext()) {
                        SQLConnectableObject whenExprObj = (SQLConnectableObject) whenItr.next();
                        inputs.putAll(whenExprObj.getSQLObjectMap());
                    }
                }
                Iterator it = inputs.values().iterator();
                while (it.hasNext()) {
                    SQLObject obj = (SQLObject) it.next();
                    discoverTables(obj, sourceTables);
                }
                break;

            case SQLConstants.SOURCE_COLUMN:
                SQLObject parent = (SQLObject) sqlObj.getParentObject();
                discoverTables(parent, sourceTables);

                break;

            case SQLConstants.SOURCE_TABLE:
                // I am adding parent here if column, How to get the column's parent here
                // ????
                if (!sourceTables.contains(sqlObj)) {
                    sourceTables.add(sqlObj);

                    // find the root join if any for this table and find other tables
                    SQLDBModel parentDbModel = (SQLDBModel) parentObject;
                    SQLDefinition definition = (SQLDefinition) parentDbModel.getParentObject();

                    if (definition != null) {
                        SQLObject rootJoin = definition.getRootJoin(sourceTables);
                        if (rootJoin != null) {
                            discoverTables(rootJoin, sourceTables);
                        }
                    }
                }

                break;

            default:
        }
    }

    /*
     * Performs sql framework initialization functions for constructors which cannot first
     * call this().
     */
    private void init() {
        type = SQLConstants.TARGET_TABLE;
        setDefaultAttributes();
        groupBy = new SQLGroupByImpl();

        joinCondition = SQLModelObjectFactory.getInstance().createSQLCondition(JOIN_CONDITION);
        setJoinCondition(joinCondition);

        filterCondition = SQLModelObjectFactory.getInstance().createSQLCondition(FILTER_CONDITION);
        setFilterCondition(filterCondition);
    }

    public SQLCondition getFilterCondition() {
        // if there are no objects in graph then populate graph with the objects
        // obtained from sql text
        Collection objC = filterCondition.getAllObjects();
        if (objC == null || objC.size() == 0) {
            SQLDefinition def = SQLObjectUtil.getAncestralSQLDefinition(this);
            try {
                ConditionUtil.populateCondition(filterCondition, def, this.getFilterConditionText());
            } catch (Exception ex) {
                // ignore this if filterCondition typed by user is invalid
            }
        }

        return filterCondition;
    }

    public String getFilterConditionText() {
        return filterCondition.getConditionText();
    }

    public void setFilterCondition(SQLCondition cond) {
        this.filterCondition = cond;
        if (this.filterCondition != null) {
            this.filterCondition.setParent(this);
            this.filterCondition.setDisplayName(TargetTable.FILTER_CONDITION);
        }

    }

    public void setFilterConditionText(String cond) {
        filterCondition.setConditionText(cond);
    }

    public SQLCondition getHavingCondition() {
        return havingCondition;
    }

    public void setHavingCondition(SQLCondition having) {
        this.havingCondition = having;
        if (this.havingCondition != null) {
            this.havingCondition.setParent(this);
            this.havingCondition.setDisplayName(HAVING_CONDITION);
        }
    }

    public void setBatchSize(int newsize) {
        this.setAttribute(ATTR_BATCHSIZE, new Integer(newsize));
    }

    public void setBatchSize(Integer newsize) {
        this.setAttribute(ATTR_BATCHSIZE, newsize);
    }

    public void setUsingFullyQualifiedName(boolean usesFullName) {
        this.setAttribute(ATTR_FULLY_QUALIFIED_NAME, new Boolean(usesFullName));
    }

    public void setUsingFullyQualifiedName(Boolean usesFullName) {
        this.setAttribute(ATTR_FULLY_QUALIFIED_NAME, usesFullName);
    }
}
