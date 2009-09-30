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
package org.netbeans.modules.sql.framework.model.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.GUIInfo;
import org.netbeans.modules.sql.framework.model.SQLCondition;
import org.netbeans.modules.sql.framework.model.SQLConstants;
import org.netbeans.modules.sql.framework.model.SQLDBModel;
import org.netbeans.modules.sql.framework.model.SQLDefinition;
import org.netbeans.modules.sql.framework.model.SQLModelObjectFactory;
import org.netbeans.modules.sql.framework.model.SQLObject;
import org.netbeans.modules.sql.framework.model.SourceColumn;
import org.netbeans.modules.sql.framework.model.SourceTable;
import org.netbeans.modules.sql.framework.model.utils.ConditionUtil;
import org.netbeans.modules.sql.framework.model.utils.SQLObjectUtil;
import org.netbeans.modules.sql.framework.model.visitors.SQLVisitor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.SQLGroupBy;

/**
 * Concrete implementation of SourceTable and SQLConnectableObject classes / interfaces,
 * representing table metadata and linking information for a target table.
 *
 * @author Jonathan Giron
 * @version $Revision$
 */
public class SourceTableImpl extends AbstractDBTable implements SourceTable {
    
    /* Log4J category string */
    static final String LOG_CATEGORY = AbstractDBTable.class.getName();
    
    private static final String ATTR_BATCHSIZE = "batchSize";
    
    private static final String ATTR_DROP_STAGING_TABLE = "deleteTemporaryTable";
    
    private static final String ATTR_FULLY_QUALIFIED_NAME = "fullyQualifiedName";
    private static final String ATTR_TRUNCATE_STAGING_TABLE = "tuncateStagingTable";
    
    private static final String ATTR_DISTINCT = "distinct";
    
    private static final String ATTR_EXTRACTION_TYPE = "extractionType";
    
    private static final String ATTR_TEMPORARY_TABLE_NAME = "temporaryTableName";
    
    private static final String ATTR_USED_IN_JOIN = "usedInJoin";
    
    private SQLCondition dataValidationCondition;
    
    private SQLCondition extractionCondition;
    
    private SQLGroupBy groupBy;
    
    /** No-arg constructor; initializes Collections-related member variables. */
    public SourceTableImpl() {
        super();
        init();
    }
    
    /**
     * Creates a new instance of AbstractDBTable, cloning the contents of the given
     * DBTable implementation instance.
     *
     * @param src DBTable instance to be cloned
     */
    public SourceTableImpl(DBTable src) {
        this();
        
        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null DBTable instance for src param.");
        }
        copyFrom(src);
    }
    
    /**
     * Creates a new instance of AbstractDBTable with the given name.
     *
     * @param aName name of new DBTable instance
     * @param aSchema schema of new DBTable instance; may be null
     * @param aCatalog catalog of new DBTable instance; may be null
     */
    public SourceTableImpl(String aName, String aSchema, String aCatalog) {
        super(aName, aSchema, aCatalog);
        init();
    }
    
    /*
     * Setters and non-API helper methods for this implementation.
     */
    
    /**
     * Adds an AbstractDBColumn instance to this table.
     *
     * @param theColumn column to be added.
     * @return true if successful. false if failed.
     */
    public boolean addColumn(SourceColumn theColumn) {
        if (theColumn != null) {
            theColumn.setParent(this);
            columns.put(theColumn.getName(), theColumn);
            return true;
        }
        
        return false;
    }
    
    /**
     * Clone a deep copy of SourceTableImpl
     *
     * @return a copy of SourceTableImpl
     */
    public Object clone() {
        try {
            SourceTableImpl aClone = new SourceTableImpl(this);
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
                addColumn(new SourceColumnImpl((DBColumn) iter.next()));
            }
        }
        
        if (source instanceof SourceTableImpl) {
            SourceTableImpl sTable = (SourceTableImpl) source;
            extractionCondition = sTable.getExtractionCondition();
            setExtractionConditionText(sTable.getExtractionConditionText());
            dataValidationCondition = sTable.getDataValidationCondition();
        }
        
        if(source instanceof SourceTable) {
            SQLGroupBy grpBy = ((SourceTable)source).getSQLGroupBy();
            if(grpBy != null) {
                groupBy =  new SQLGroupByImpl(grpBy);
            }
        }
    }
    
    /**
     * Convenience class to create SourceColumn instance (with the given column name, data
     * source name, JDBC type, scale, precision, and nullable), and add it to this
     * SourceTableImpl instance.
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
    public SourceColumn createColumn(String columnName, int jdbcType, int scale, int precision, boolean isPK, boolean isFK, boolean isIndexed,
            boolean nullable) {
        SourceColumn impl = new SourceColumnImpl(columnName, jdbcType, scale, precision, isPK, isFK, isIndexed, nullable);
        impl.setParent(this);
        columns.put(columnName, impl);
        
        return impl;
    }
    
    /**
     * Overrides default implementation to return value based on memberwise comparison.
     *
     * @param obj Object against which we compare this instance
     * @return true if obj is functionally identical to this SQLTable instance; false
     *         otherwise
     */
    public boolean equals(Object obj) {
        // Check for reflexivity first.
        if (this == obj) {
            return true;
        } else if (!(obj instanceof SourceTable)) {
            return false;
        }
        
        boolean result = super.equals(obj);
        SourceTable src = (SourceTable) obj;
        
        result &= (extractionCondition != null) ? extractionCondition.equals(src.getExtractionCondition()) : (src.getExtractionCondition() == null);
        
        result &= (dataValidationCondition != null) ? dataValidationCondition.equals(src.getDataValidationCondition()) : (src.getDataValidationCondition() == null);
        
        return result;
    }
    
    /**
     * set the data validation condition.
     *
     * @param condition data validation condition
     */
    public SQLCondition getDataValidationCondition() {
        return dataValidationCondition;
    }
    
    /**
     * Gets the Validation conidition text.
     *
     * @return sql condition
     */
    public String getDataValidationConditionText() {
        if (this.dataValidationCondition != null) {
            return this.dataValidationCondition.getConditionText();
        }
        return "";
    }
    
    /**
     * Gets the extraction condition.
     *
     * @return filter to apply while doing extraction
     */
    public SQLCondition getExtractionCondition() {
        // if there are no objects in graph then populate graph with the objects
        // obtained from sql text
        Collection objC = extractionCondition.getAllObjects();
        if (objC == null || objC.size() == 0) {
            SQLDefinition def = SQLObjectUtil.getAncestralSQLDefinition(this);
            try {
                ConditionUtil.populateCondition(extractionCondition, def, this.getExtractionConditionText());
            } catch (Exception ex) {
                // ignore this if condition typed by user is invalid
            }
        }
        
        return extractionCondition;
    }
    
    /**
     * Gets the extraction conidition text.
     *
     * @return sql condition
     */
    public String getExtractionConditionText() {
        return this.extractionCondition.getConditionText();
    }
    
    /**
     * Gets extraction type.
     *
     * @return extraction type
     */
    public String getExtractionType() {
        return (String) this.getAttributeObject(ATTR_EXTRACTION_TYPE);
    }
    
    /**
     * Overrides parent implementation to return SourceColumn, if any, that corresponds to
     * the given argument name.
     *
     * @param argName argument name of linkable SQLObject
     * @return linkable SQLObject corresponding to argName
     * @throws BaseException if argName is null
     * @see SQLObject#getOutput(java.lang.String)
     */
    public SQLObject getOutput(String argName) throws BaseException {
        if (argName == null) {
            throw new BaseException("Must supply non-empty String value for parameter 'argName'.");
        }
        
        SQLObject column = (SQLObject) columns.get(argName);
        return (column == null) ? this : column;
    }
    
    /**
     * Gets the SourceColumn, if any, associated with the given name
     *
     * @param columnName column name
     * @return SourceColumn associated with columnName, or null if none exists
     */
    public SourceColumn getSourceColumn(String columnName) {
        return (SourceColumn) columns.get(columnName);
    }
    
    /**
     * Gets temporary table name.
     *
     * @return temp table name
     */
    public String getTemporaryTableName() {
        // If the Staging Table Name is set, return it.
        if(getStagingTableName().length() != 0) {
            return (String) getStagingTableName();
        } else {
            return (String) this.getAttributeObject(ATTR_TEMPORARY_TABLE_NAME);
        }
    }
    
    /**
     * @see org.netbeans.modules.sql.framework.model.SourceTable#getSQLGroupBy()
     */
    public SQLGroupBy getSQLGroupBy() {
        return groupBy;
    }    
    
    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     *
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        return super.hashCode() + ((extractionCondition != null) ? extractionCondition.hashCode() : 0)
                + ((getExtractionConditionText() != null) ? getExtractionConditionText().hashCode() : 0)
                + ((getDataValidationCondition() != null) ? getDataValidationCondition().hashCode() : 0);
    }
    
    /**
     * Indicates whether to delete temporary table before extraction.
     *
     * @return delete whether to delete temp table
     */
    public boolean isDropStagingTable() {
        Boolean delete = (Boolean) this.getAttributeObject(ATTR_DROP_STAGING_TABLE);
        if (delete != null) {
            return delete.booleanValue();
        }
        
        return true;
    }
    
    /**
     * Indicates whether distinct rows of a column need to be selected.
     *
     * @return distinct
     */
    public boolean isSelectDistinct() {
        Boolean distinct = (Boolean) this.getAttributeObject(ATTR_DISTINCT);
        if (distinct != null) {
            return distinct.booleanValue();
        }
        
        return false;
    }
    
    /**
     * Indicates whether this table is used in a join view.
     *
     * @return boolean
     */
    public boolean isUsedInJoin() {
        Boolean used = (Boolean) this.getAttributeObject(SourceTableImpl.ATTR_USED_IN_JOIN);
        if (used != null) {
            return used.booleanValue();
        }
        
        return false;
    }
    
    /**
     * Parses elements which require a second pass to resolve their values.
     *
     * @param element DOM element containing XML marshalled version of this SQLObject
     *        instance
     * @throws BaseException if element is null or error occurs during parsing
     */
    public void secondPassParse(Element element) throws BaseException {
    }
    
    /**
     * get the data validation condition.
     *
     * @return data validation condition.
     */
    public void setDataValidationCondition(SQLCondition condition) {
        this.dataValidationCondition = condition;
        if (this.dataValidationCondition != null) {
            this.dataValidationCondition.setParent(this);
            this.dataValidationCondition.setDisplayName(SourceTable.DATA_VALIDATION_CONDITION);
        }
    }
    
    /**
     * Sets the validation condition text.
     *
     * @param cond condition text
     */
    public void setDataValidationConditionText(String cond) {
        if (this.extractionCondition != null) {
            this.extractionCondition.setConditionText(cond);
        }
    }
    
    /**
     * Set whether to delete temporary table before extraction.
     *
     * @param drop whether to delete temp table
     */
    public void setDropStagingTable(boolean drop) {
        this.setAttribute(ATTR_DROP_STAGING_TABLE, new Boolean(drop));
    }
    
       /**
     * Set whether to delete temporary table before extraction.
     *
     * @param drop whether to delete temp table
     */
    public void setDropStagingTable(Boolean drop) {
        this.setAttribute(ATTR_DROP_STAGING_TABLE,drop);
    }
    
    /**
     * Sets the extraction condition.
     *
     * @param condition condition
     */
    public void setExtractionCondition(SQLCondition condition) {
        this.extractionCondition = condition;
        if (this.extractionCondition != null) {
            this.extractionCondition.setParent(this);
            this.extractionCondition.setDisplayName(SourceTable.EXTRACTION_CONDITION);
        }
    }
    
    /**
     * Sets the extraction condition text.
     *
     * @param cond extraction condition text
     */
    public void setExtractionConditionText(String cond) {
        this.extractionCondition.setConditionText(cond);
    }
    
    /**
     * Sets the extraction type.
     *
     * @param eType extraction type
     */
    public void setExtractionType(String eType) {
        this.setAttribute(ATTR_EXTRACTION_TYPE, eType);
    }
    
    /**
     * Sets wehether to select distinct rows of a column.
     *
     * @param distinct distinct
     */
    public void setSelectDistinct(boolean distinct) {
        this.setAttribute(ATTR_DISTINCT, new Boolean(distinct));
    }
    
    /**
     * Sets wehether to select distinct rows of a column.
     *
     * @param distinct distinct
     */
    public void setSelectDistinct(Boolean distinct) {
        this.setAttribute(ATTR_DISTINCT, distinct);
    }    
    
    /**
     * Sets the temporary table name.
     *
     * @param tName temp table name
     */
    public void setTemporaryTableName(String tName) {
        this.setAttribute(SourceTableImpl.ATTR_TEMPORARY_TABLE_NAME, tName);
    }
    
    /**
     * Sets whether this table is used in a join view.
     *
     * @param used boolean
     */
    public void setUsedInJoin(boolean used) {
        this.setAttribute(ATTR_USED_IN_JOIN, new Boolean(used));
        this.getGUIInfo().setVisible(!used);
    }
    
    /**
     * @see org.netbeans.modules.sql.framework.model.SourceTable#setSQLGroupBy(org.netbeans.modules.sql.framework.model.SQLGroupBy)
     */
    public void setSQLGroupBy(SQLGroupBy groupBy) {
        this.groupBy = groupBy;
    }    
    
    /**
     * Returns XML representation of table metadata.
     *
     * @param prefix prefix for the xml.
     * @return XML representation of the table metadata.
     * @exception BaseException - exception
     */
    public String toXMLString(String prefix) throws BaseException {
        return toXMLString(prefix, false);
    }
    
    /**
     * Returns XML representation of table metadata.
     *
     * @param prefix prefix for the xml.
     * @param tableOnly flag for generating table only metadata.
     * @return XML representation of the table metadata.
     * @exception BaseException - exception
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
        
        xml.append(toXMLAttributeTags(prefix));
        
        if (!tableOnly) {
            writeColumns(prefix, xml);
            writePrimaryKey(prefix, xml);
            writeForeignKeys(prefix, xml);
            writeIndices(prefix, xml);
        }
        
        // write out extraction condition
        if (extractionCondition != null) {
            xml.append(extractionCondition.toXMLString(prefix + INDENT));
        }
        
        // write out data validation condition
        if (dataValidationCondition != null) {
            xml.append(dataValidationCondition.toXMLString(prefix + INDENT));
        }
        
        // write out group by statement.
        if (groupBy != null) {
            xml.append(groupBy.toXMLString(prefix + INDENT));
        }        
        
        if (guiInfo != null) {
            xml.append(guiInfo.toXMLString(prefix + INDENT));
        }
        
        xml.append(prefix).append("</").append(TABLE_TAG).append(">\n");
        
        return xml.toString();
    }
    
    public List validate() {
        throw new UnsupportedOperationException("Use validation visitor framework to validate this object.");
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
     * @see org.netbeans.modules.sql.framework.model.impl.AbstractDBTable#parseChildren
     */
    protected void parseChildren(NodeList childNodeList) throws BaseException {
        for (int i = 0; i < childNodeList.getLength(); i++) {
            if (childNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                Element childElement = (Element) childNodeList.item(i);
                String tagName = childElement.getTagName();
                
                if (AbstractDBColumn.ELEMENT_TAG.equals(tagName)) {
                    SourceColumn columnInstance = new SourceColumnImpl();
                    
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
                    
                    if (conditionName != null && conditionName.equals(SourceTable.EXTRACTION_CONDITION)) {
                        SQLCondition exCondition = SQLModelObjectFactory.getInstance().createSQLCondition(EXTRACTION_CONDITION);
                        SQLDBModel parentDbModel = (SQLDBModel) this.getParentObject();
                        if (parentDbModel != null) {
                            exCondition.setParent(this);
                            exCondition.parseXML(childElement);
                            this.setExtractionCondition(exCondition);
                        }
                    } else if (conditionName != null && conditionName.equals(SourceTable.DATA_VALIDATION_CONDITION)) {
                        SQLCondition dValidationCondition = SQLModelObjectFactory.getInstance().createSQLCondition(DATA_VALIDATION_CONDITION);
                        SQLDBModel parentDbModel = (SQLDBModel) this.getParentObject();
                        if (parentDbModel != null) {
                            dValidationCondition.setParent(this);
                            dValidationCondition.parseXML(childElement);
                            this.setDataValidationCondition(dValidationCondition);
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
        
        if (this.getAttributeObject(SourceTableImpl.ATTR_EXTRACTION_TYPE) == null) {
            setExtractionType(SQLConstants.EXTRACTION_CONDITIONAL);
        }
        
        if (this.getAttributeObject(SourceTableImpl.ATTR_DISTINCT) == null) {
            setSelectDistinct(false);
        }
        
        if (this.getAttributeObject(ATTR_BATCHSIZE) == null) {
            setBatchSize(5000);
        }
        
        if (this.getAttributeObject(ATTR_DROP_STAGING_TABLE) == null) {
            setDropStagingTable(true);
        }
        
    }
    
    /**
     * Write columns
     *
     * @param prefix - prefix
     * @param xml - buffer
     * @throws BaseException - exception
     */
    protected void writeColumns(String prefix, StringBuilder xml) throws BaseException {
        Comparator cmp = new AbstractDBTable.StringComparator();
        // Ensure columns are written out in ascending name order.
        List colList = new ArrayList(columns.keySet());
        Collections.sort(colList, cmp);
        
        Iterator iter = colList.listIterator();
        while (iter.hasNext()) {
            String key = (String) iter.next();
            SourceColumn column = (SourceColumn) columns.get(key);
            xml.append(column.toXMLString(prefix + INDENT));
        }
    }
    
    /**
     * Write foreign key
     *
     * @param prefix - prefix
     * @param xml - buffer
     */
    protected void writeForeignKeys(String prefix, StringBuilder xml) {
        Comparator cmp = new AbstractDBTable.StringComparator();
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
     * Write indices
     *
     * @param prefix - prefix
     * @param xml - buffer
     */
    protected void writeIndices(String prefix, StringBuilder xml) {
        Comparator cmp = new AbstractDBTable.StringComparator();
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
     * Write primary key
     *
     * @param prefix - prefix
     * @param xml - buffer
     */
    protected void writePrimaryKey(String prefix, StringBuilder xml) {
        if (primaryKey != null) {
            xml.append(primaryKey.toXMLString(prefix + INDENT));
        }
    }
    
    /*
     * Performs sql framework initialization functions for constructors which cannot first
     * call this().
     */
    private void init() {
        type = SQLConstants.SOURCE_TABLE;
        setDefaultAttributes();
        extractionCondition = SQLModelObjectFactory.getInstance().createSQLCondition(EXTRACTION_CONDITION);
        setExtractionCondition(extractionCondition);
        
        dataValidationCondition = SQLModelObjectFactory.getInstance().createSQLCondition(DATA_VALIDATION_CONDITION);
        setDataValidationCondition(dataValidationCondition);
    }
    
    public boolean isTruncateStagingTable() {
        Boolean truncate = (Boolean) this.getAttributeObject(ATTR_TRUNCATE_STAGING_TABLE);
        if (truncate != null) {
            return truncate.booleanValue();
        }
        
        return true;
    }
    
     public boolean isUsingFullyQualifiedName() {
        Boolean fullName = (Boolean) this.getAttributeObject(ATTR_FULLY_QUALIFIED_NAME);
        if (fullName != null) {
            return fullName.booleanValue();
        }
        return true;
    }
    
    public void setTruncateStagingTable(boolean truncate) {
        this.setAttribute(ATTR_TRUNCATE_STAGING_TABLE, new Boolean(truncate));
    }
    
     public void setTruncateStagingTable(Boolean truncate) {
        this.setAttribute(ATTR_TRUNCATE_STAGING_TABLE,truncate);
    }
    
    public void setBatchSize(int newsize){
        this.setAttribute(ATTR_BATCHSIZE,new Integer(newsize));
    }
            
    public void setBatchSize(Integer newsize) {
         this.setAttribute(ATTR_BATCHSIZE,newsize);
    }
    
    public void setUsingFullyQualifiedName(boolean usesFullName) {
        this.setAttribute(ATTR_FULLY_QUALIFIED_NAME, new Boolean(usesFullName));
    }
    
    public void setUsingFullyQualifiedName(Boolean usesFullName) {
        this.setAttribute(ATTR_FULLY_QUALIFIED_NAME,usesFullName);
    }
  
}
