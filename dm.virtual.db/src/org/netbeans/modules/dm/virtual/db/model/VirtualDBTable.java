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
package org.netbeans.modules.dm.virtual.db.model;

import org.netbeans.modules.dm.virtual.db.bootstrap.PropertyKeys;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.netbeans.modules.dm.virtual.db.api.Property;
import java.util.logging.Logger;
import java.util.logging.Level;
import org.openide.util.NbBundle;

/**
 *
 * @author Ahimanikya Satapathy
 */
public class VirtualDBTable extends VirtualDBObject implements Cloneable, Comparable {

    private static final String END_PROPS_DELIMITER = ")";
    private static Logger mLogger = Logger.getLogger(VirtualDBTable.class.getName());
    private static final String LOG_CATEGORY = VirtualDBTable.class.getName();
    private static final String PROP_SEPARATOR = ", ";
    private static final String PROPS_KEYWORD = "ORGANIZATION"; // NOI18N
    private static final String START_PROPS_DELIMITER = "(";
    public static final String PROP_CREATE_IF_NOT_EXIST = "CREATE_IF_NOT_EXIST"; // NOI18N
    public static final String PROP_FILENAME = "FILENAME"; // NOI18N
    public static final String PROP_WIZARD = "WIZARD"; // NOI18N
    private static final Set WIZARD_ONLY_PROPERTIES = new HashSet();
    

    static {
        WIZARD_ONLY_PROPERTIES.add("DEFAULTSQLTYPE");
        WIZARD_ONLY_PROPERTIES.add("FILEPATH");
        WIZARD_ONLY_PROPERTIES.add("FIELDCOUNT");
    }
    /* Encoding of file contents, e.g., utf-8, cp500, etc. */
    private String encoding = "";
    private String fileName = "";
    private transient String localPath = File.separator;
    private String parserType;
    private Map properties;

    static class StringComparator implements Comparator {

        public int compare(Object o1, Object o2) {
            if (o1 instanceof String && o2 instanceof String) {
                return ((String) o1).compareTo((String) o2);
            }
            throw new ClassCastException(NbBundle.getMessage(VirtualDBTable.class, "MSG_StringComparator"));
        }
    }
    protected static final String CATALOG_NAME_ATTR = "catalog"; // NOI18N
    protected static final String DB_TABLE_REF = "dbTableRef"; // NOI18N
    protected static final String DISPLAY_NAME_ATTR = "displayName"; // NOI18N
    protected static final String ID_ATTR = "id"; // NOI18N
    protected static final String INDENT = "\t";
    protected static final int INIT_XMLBUF_SIZE = 500;
    protected static final String MODEL_NAME_TAG = "dbModelName"; // NOI18N
    protected static final String SCHEMA_NAME_ATTR = "schema"; // NOI18N
    protected static final String TABLE_NAME_ATTR = "name"; // NOI18N
    private static final String FQ_TBL_NAME_SEPARATOR = ".";    // RFE-102428
    protected String catalog;
    protected Map<String, VirtualDBColumn> columns;
    protected String description;
    protected Map<String, ForeignKey> foreignKeys;
    protected String name;
    protected VirtualDatabaseModel parentDBModel;
    protected PrimaryKey primaryKey;
    protected String schema;

    public VirtualDBTable() {
        super();
        columns = new LinkedHashMap<String, VirtualDBColumn>();
        foreignKeys = new HashMap<String, ForeignKey>();
        properties = new HashMap();
    }

    public VirtualDBTable(String aName) {
        this();
        this.name = (aName != null) ? aName.trim() : null;
    }

    public VirtualDBTable(String aName, String aSchema, String aCatalog) {
        this();
        this.name = (aName != null) ? aName.trim() : null;
    }

    public boolean addForeignKey(ForeignKey newFk) {
        if (newFk != null) {
            newFk.setParent(this);
            foreignKeys.put(newFk.getName(), newFk);
            return true;
        }
        return false;
    }

    public void clearForeignKeys() {
        foreignKeys.clear();
    }

    public int compareTo(Object refObj) {
        if (refObj == null) {
            return -1;
        }

        if (refObj == this) {
            return 0;
        }

        String refName = (parentDBModel != null) ? parentDBModel.getFullyQualifiedTableName((VirtualDBTable) refObj) : ((VirtualDBTable) refObj).getName();
        String myName = (parentDBModel != null) ? parentDBModel.getFullyQualifiedTableName(this) : name;
        return (myName != null) ? myName.compareTo(refName) : (refName != null) ? 1 : -1;
    }

    public boolean deleteAllColumns() {
        columns.clear();
        return false;
    }

    public boolean deleteColumn(String columnName) {
        if (columnName != null && columnName.trim().length() != 0) {
            return (columns.remove(columnName) != null);
        }
        return false;
    }

    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        // Check for reflexivity first.
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof VirtualDBTable)) {
            return false;
        }

        result = super.equals(obj);

        if (!result) {
            return result;
        }

        VirtualDBTable target = (VirtualDBTable) obj;

        // since now we allow duplicate source tables we need to check the id and if id
        // is not equal then table is not equal
        result &= target.getId() != null ? target.getId().equals(this.getId()) : this.getId() == null;

        // Check for castability (also deals with null obj)
        if (obj instanceof VirtualDBTable) {
            VirtualDBTable aTable = (VirtualDBTable) obj;
            String aTableName = aTable.getName();
            VirtualDatabaseModel aTableParent = aTable.getParent();
            Map<String, VirtualDBColumn> aTableColumns = aTable.getColumns();
            PrimaryKey aTablePK = aTable.getPrimaryKey();
            List<ForeignKey> aTableFKs = aTable.getForeignKeys();

            result &= (aTableName != null && name != null && name.equals(aTableName)) && (parentDBModel != null && aTableParent != null && parentDBModel.equals(aTableParent));

            if (columns != null && aTableColumns != null) {
                Set<String> objCols = aTableColumns.keySet();
                Set<String> myCols = columns.keySet();

                // Must be identical (no subsetting), hence the pair of tests.
                result &= myCols.containsAll(objCols) && objCols.containsAll(myCols);
            } else if (!(columns == null && aTableColumns == null)) {
                result = false;
            }

            result &= (primaryKey != null) ? primaryKey.equals(aTablePK) : aTablePK == null;

            if (foreignKeys != null && aTableFKs != null) {
                Collection<ForeignKey> myFKs = foreignKeys.values();
                // Must be identical (no subsetting), hence the pair of tests.
                result &= myFKs.containsAll(aTableFKs) && aTableFKs.containsAll(myFKs);
            } else if (!(foreignKeys == null && aTableFKs == null)) {
                result = false;
            }

            result &= (encoding != null) ? encoding.equals(aTable.encoding) : (aTable.encoding == null);
            result &= (fileName != null) ? fileName.equals(aTable.fileName) : (aTable.fileName == null);
        }
        return result;
    }

    public String getCatalog() {
        return catalog;
    }

    @Override
    public List<VirtualDBColumn> getChildSQLObjects() {
        return this.getColumnList();
    }

    public VirtualDBColumn getColumn(String columnName) {
        return columns.get(columnName);
    }

    public List<VirtualDBColumn> getColumnList() {
        List<VirtualDBColumn> list = new ArrayList<VirtualDBColumn>();
        list.addAll(columns.values());
        Collections.sort(list, new ColumnOrderComparator());

        return list;
    }

    public Map<String, VirtualDBColumn> getColumns() {
        return columns;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getDisplayName() {
        return this.getQualifiedName();
    }

    public ForeignKey getForeignKey(String fkName) {
        return foreignKeys.get(fkName);
    }

    public List<ForeignKey> getForeignKeys() {
        return new ArrayList<ForeignKey>(foreignKeys.values());
    }

    public String getFullyQualifiedName() {

        String tblName = getName();
        String schName = getSchema();
        String catName = getCatalog();

        if (tblName == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(VirtualDBTable.class, "MSG_Null_tableName"));
        }

        StringBuilder buf = new StringBuilder(50);

        if (catName != null && catName.trim().length() != 0) {
            buf.append(catName.trim());
            buf.append(FQ_TBL_NAME_SEPARATOR);
        }

        if (schName != null && schName.trim().length() != 0) {
            buf.append(schName.trim());
            buf.append(FQ_TBL_NAME_SEPARATOR);
        }

        buf.append(tblName.trim());

        return buf.toString();
    }

    public synchronized String getName() {
        return name;
    }

    public VirtualDBObject getObject(String objectId) {
        List list = this.getColumnList();
        Iterator it = list.iterator();

        while (it.hasNext()) {
            VirtualDBColumn dbColumn = (VirtualDBColumn) it.next();
            // if looking for table then return table
            if (objectId.equals(dbColumn.getId())) {
                return dbColumn;
            }
        }
        return null;
    }

    public VirtualDatabaseModel getParent() {
        return parentDBModel;
    }

    public PrimaryKey getPrimaryKey() {
        return primaryKey;
    }

    public String getQualifiedName() {
        return this.getFullyQualifiedName();
    }

    public Set getReferencedTables() {
        List keys = getForeignKeys();
        Set<VirtualDBTable> tables = new HashSet<VirtualDBTable>(keys.size());

        if (keys.size() != 0) {
            Iterator iter = keys.iterator();
            while (iter.hasNext()) {
                ForeignKey fk = (ForeignKey) iter.next();
                VirtualDBTable pkTable = parentDBModel.getTable(fk.getPKTable(), fk.getPKSchema(), fk.getPKCatalog());
                if (pkTable != null && fk.references(pkTable.getPrimaryKey())) {
                    tables.add(pkTable);
                }
            }

            if (tables.size() == 0) {
                tables.clear();
                tables = Collections.emptySet();
            }
        }

        return tables;
    }

    public ForeignKey getReferenceFor(VirtualDBTable target) {
        if (target == null) {
            return null;
        }

        PrimaryKey targetPK = target.getPrimaryKey();
        if (targetPK == null) {
            return null;
        }

        Iterator iter = foreignKeys.values().iterator();
        while (iter.hasNext()) {
            ForeignKey myFK = (ForeignKey) iter.next();
            if (myFK.references(targetPK)) {
                return myFK;
            }
        }

        return null;
    }

    public String getSchema() {
        return schema;
    }

    @Override
    public int hashCode() {
        int myHash = super.hashCode();
        myHash = (name != null) ? name.hashCode() : 0;
        myHash += (parentDBModel != null) ? parentDBModel.hashCode() : 0;
        myHash += (schema != null) ? schema.hashCode() : 0;
        myHash += (catalog != null) ? catalog.hashCode() : 0;

        // Include hashCodes of all column names.
        if (columns != null) {
            myHash += columns.keySet().hashCode();
        }

        if (primaryKey != null) {
            myHash += primaryKey.hashCode();
        }

        if (foreignKeys != null) {
            myHash += foreignKeys.keySet().hashCode();
        }

        myHash += (displayName != null) ? displayName.hashCode() : 0;
        myHash += (encoding != null) ? encoding.hashCode() : 0;
        myHash += (fileName != null) ? fileName.hashCode() : 0;

        return myHash;
    }

    public boolean references(VirtualDBTable pkTarget) {
        return (getReferenceFor(pkTarget) != null);
    }

    public boolean removeForeignKey(ForeignKey oldKey) {
        if (oldKey != null) {
            return (foreignKeys.remove(oldKey.getName()) != null);
        }

        return false;
    }

    public boolean setAllColumns(Map<String, VirtualDBColumn> theColumns) {
        columns.clear();
        if (theColumns != null) {
            columns.putAll(theColumns);
        }
        return true;
    }

    public void setCatalog(String newCatalog) {
        catalog = newCatalog;
    }

    public void setDescription(String newDesc) {
        description = newDesc;
    }

    public void setName(String newName) {
        name = newName;
    }

    public void setParent(VirtualDatabaseModel newParent) {
        parentDBModel = newParent;
        try {
            setParentObject(newParent);
        } catch (VirtualDBException ex) {
            // do nothing
        }
    }

    public boolean setPrimaryKey(PrimaryKey newPk) {
        if (newPk != null) {
            newPk.setParent(this);
        }

        primaryKey = newPk;
        return true;
    }

    public void setForeignKeyMap(Map<String, ForeignKey> fkMap) {
        foreignKeys = fkMap;
    }

    public void setSchema(String newSchema) {
        schema = newSchema;
    }

    protected void deepCopyReferences(VirtualDBTable source) {
        if (source != null && source != this) {
            primaryKey = null;
            PrimaryKey srcPk = source.getPrimaryKey();
            if (srcPk != null) {
                primaryKey = new PrimaryKey(source.getPrimaryKey());
                primaryKey.setParent(this);
            }

            foreignKeys.clear();
            Iterator iter = source.getForeignKeys().iterator();
            while (iter.hasNext()) {
                ForeignKey impl = new ForeignKey((ForeignKey) iter.next());
                impl.setParent(this);
                foreignKeys.put(impl.getName(), impl);
            }

            columns.clear();
            iter = source.getColumnList().iterator();
            while (iter.hasNext()) {
                try {
                    VirtualDBColumn column = (VirtualDBColumn) iter.next();
                    VirtualDBColumn clonedColumn = (VirtualDBColumn) column.cloneSQLObject();
                    columns.put(clonedColumn.getName(), clonedColumn);
                } catch (Exception ex) {
                    // TODO Log this exception
                }
            }
        }
    }

    final class ColumnOrderComparator implements Comparator<VirtualDBColumn> {

        private ColumnOrderComparator() {
        }

        public int compare(VirtualDBColumn col1, VirtualDBColumn col2) {
            return col1.getOrdinalPosition() - col2.getOrdinalPosition();
        }
    }

    private boolean addColumn(VirtualDBColumn theColumn, boolean ignoreDupCols) {
        if (theColumn != null) {
            if ((!ignoreDupCols) && (columns.containsKey(theColumn.getName()))) {
                //throw new IllegalArgumentException("Column " + theColumn.getName() + " already exist.");
                throw new IllegalArgumentException(NbBundle.getMessage(VirtualDBTable.class, "MSG_Column_exits", theColumn.getName()));
            }
            theColumn.setParent(this);
            this.columns.put(theColumn.getName(), theColumn);
            return true;
        }

        return false;
    }

    public boolean addColumn(VirtualDBColumn theColumn) {
        return addColumn(theColumn, false);
    }

    @Override
    public Object clone() {
        try {
            VirtualDBTable table = (VirtualDBTable) super.clone();
            table.columns = new LinkedHashMap();
            table.deepCopyReferences(this);

            return table;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    public VirtualDBColumn createColumn(String columnName, int jdbcType, int scale, int precision, boolean isPK, boolean isFK, boolean isIndexed,
            boolean nullable) {
        VirtualDBColumn impl = new VirtualDBColumn(columnName, jdbcType, scale, precision, isPK, isFK, isIndexed, nullable);
        impl.setParent(this);
        this.columns.put(columnName, impl);
        return impl;
    }

    public String getCreateStatementSQL() {
        return getCreateStatementSQL(this) + getVirtualTablePropertiesSQL();
    }

    public String getCreateStatementSQL(VirtualDBTable table) {
        String tableName = table.getName();
        List<String> pkList = new ArrayList<String>();
        Iterator it = table.getColumns().values().iterator();
        StringBuilder buffer = new StringBuilder(100);
        buffer.append("CREATE EXTERNAL TABLE IF NOT EXISTS \"").append(tableName).append("\" ("); // NOI18N
        int i = 0;
        while (it.hasNext()) {
            VirtualDBColumn colDef = (VirtualDBColumn) it.next();
            if (i++ != 0) {
                buffer.append(", ");
            }
            buffer.append(colDef.getCreateStatementSQL());
            if (colDef.isPrimaryKey()) {
                pkList.add(colDef.getName());
            }
        }
        if (pkList.size() > 0) {
            StringBuilder pkbuffer = new StringBuilder(20);
            pkbuffer.append(", PRIMARY KEY( "); // NOI18N
            it = pkList.iterator();
            int j = 0;
            while (it.hasNext()) {
                if (j++ != 0) {
                    buffer.append(", ");
                }
                pkbuffer.append((String) it.next());
            }
            pkbuffer.append(") ");
            buffer.append(pkbuffer.toString());
        }

        buffer.append(")");
        return buffer.toString();
    }

    public static String getCreateStatementSQL(VirtualDBTable table, String generatedName) {
        List<String> pkList = new ArrayList<String>();
        Iterator it = table.getColumns().values().iterator();
        StringBuilder buffer = new StringBuilder(100);
        buffer.append("CREATE EXTERNAL TABLE \"").append(generatedName).append("\" ("); // NOI18N
        int i = 0;
        while (it.hasNext()) {
            VirtualDBColumn colDef = (VirtualDBColumn) it.next();
            if (i++ != 0) {
                buffer.append(", ");
            }
            buffer.append(colDef.getCreateStatementSQL());
            if (colDef.isPrimaryKey()) {
                pkList.add(colDef.getName());
            }
        }
        if (pkList.size() > 0) {
            StringBuilder pkbuffer = new StringBuilder(20);
            pkbuffer.append(", PRIMARY KEY( "); // NOI18N
            it = pkList.iterator();
            int j = 0;
            while (it.hasNext()) {
                if (j++ != 0) {
                    buffer.append(", ");
                }
                pkbuffer.append((String) it.next());
            }
            pkbuffer.append(") ");
            buffer.append(pkbuffer.toString());
        }

        buffer.append(")");
        return buffer.toString();
    }

    public String getCreateStatementSQL(String directory, String theTableName, String runtimeName, boolean isDynamicFilePath,
            boolean createDataFileIfNotExist) {
        String sql = null;
        try {
            if (runtimeName != null && runtimeName.trim().length() != 0) {
                setOrPutProperty(VirtualDBTable.PROP_FILENAME, "$" + runtimeName);
            /**
            if (isDynamicFilePath) {
            setOrPutProperty(VirtualDBTable.PROP_FILENAME, "$" + runtimeName);
            } else {
            // NOTE: DO NOT USE java.io.File to generate the file path,
            // as getCanonicalPath() is platform-centric and will hard-code
            // platform-specific root-drive info (e.g., "C:" for M$-Window$)
            // where it's inappropriate.
            setOrPutProperty(VirtualDBTable.PROP_FILENAME, getFullFilePath(directory, "$" + runtimeName));
            }**/
            } else {
                setOrPutProperty(VirtualDBTable.PROP_FILENAME, getFullFilePath(directory, fileName));
            }

            setOrPutProperty(VirtualDBTable.PROP_CREATE_IF_NOT_EXIST, new Boolean(createDataFileIfNotExist));
            sql = VirtualDBTable.getCreateStatementSQL(this, theTableName) + this.getVirtualTablePropertiesSQL();
        } catch (Exception e) {
            mLogger.log(Level.SEVERE, NbBundle.getMessage(VirtualDBTable.class, "MSG_FilaPath", LOG_CATEGORY), e);
        }
        return sql;
    }

    public String getDropStatementSQL() {
        return getDropStatementSQL(this);
    }

    public static String getDropStatementSQL(String generatedTableName) {
        StringBuilder buffer = new StringBuilder("DROP TABLE IF EXISTS \"");
        // NOI18N
        return buffer.append(generatedTableName).append("\"").toString();
    }

    public static String getDropStatementSQL(VirtualDBTable table) {
        String tableName = table.getName();
        StringBuilder buffer = new StringBuilder("DROP TABLE IF EXISTS \"");
        // NOI18N
        return buffer.append(tableName).append("\"").toString();
    }

    public String getEncodingScheme() {
        return encoding;
    }

    public String getFileName() {
        return fileName;
    }

    public String getVirtualTablePropertiesSQL() {
        StringBuilder buf = new StringBuilder(100);

        // Create local copy of Map whose elements can be removed without
        // affecting the master copy.
        Map localProps = new HashMap(properties);

        // Now emit key-value pairs in the localProps Map.
        Iterator iter = localProps.values().iterator();
        if (!iter.hasNext()) {
            return "";
        }

        buf.append(" " + PROPS_KEYWORD + " "); // NOI18N
        buf.append(START_PROPS_DELIMITER); // NOI18N

        int i = 0;
        while (iter.hasNext()) {
            Property aProp = (Property) iter.next();

            // Don't write out properties which are meant only for use inside the wizard.
            if (WIZARD_ONLY_PROPERTIES.contains(aProp.getName().toUpperCase()) || aProp.getName().toUpperCase().startsWith(VirtualDBTable.PROP_WIZARD)) {
                continue;
            }

            if (parserType != null) {
                if (((!(parserType.equals(PropertyKeys.WEB) ||
                        parserType.equals(PropertyKeys.RSS))) &&
                        aProp.getName().equals(PropertyKeys.URL)) ||
                        (((parserType.equals(PropertyKeys.WEB) ||
                        parserType.equals(PropertyKeys.RSS))) && aProp.getName().equals(PropertyKeys.FILENAME))) {
                    continue;
                }
            }

            if (!aProp.isValid()) {
                if (aProp.isRequired()) {
                    return ""; // Required property is invalid; fail.
                }
                mLogger.log(Level.INFO, NbBundle.getMessage(VirtualDBTable.class, "MSG_invalidProperty", aProp.getName(), aProp.getValue()));
                continue; // Log and skip this parameter.
            }

            if (i++ != 0) {
                buf.append(PROP_SEPARATOR);
            }
            if (aProp.getName().equals(PropertyKeys.QUALIFIER) && aProp.getValue().equals("'")) {
                aProp.setValue("''");
            }
            buf.append(aProp.getKeyValuePair());
        }

        buf.append(END_PROPS_DELIMITER); // NOI18N

        return buf.toString();
    }

    public String getLocalFilePath() {
        return localPath;
    }

    public String getParserType() {
        return parserType;
    }

    public Map getProperties() {
        return (properties != null) ? properties : Collections.EMPTY_MAP;
    }

    public String getProperty(String key) {
        Property aProp = (Property) properties.get(key);
        return (aProp != null) ? (aProp.getValue() != null) ? aProp.getValue().toString() : null : null;
    }

    public String getSelectStatementSQL(int rows) {
        if (rows < 0) {
            throw new IllegalArgumentException(NbBundle.getMessage(VirtualDBTable.class, "MSG_Negative_rowValue"));
        }

        StringBuilder buffer = new StringBuilder(100);
        buffer.append("SELECT ");

        Iterator it = getColumnList().iterator();
        int i = 0;
        while (it.hasNext()) {
            VirtualDBColumn colDef = (VirtualDBColumn) it.next();
            if (i++ != 0) {
                buffer.append(", ");
            }

            buffer.append("\"").append(colDef.getName()).append("\"");
        }

        buffer.append(" FROM ").append("\"").append(getTableName()).append("\"");

        if (rows > 0) {
            buffer.append(" LIMIT ").append(rows);
        }

        return buffer.toString();
    }

    public String getTableName() {
        return this.getName();
    }

    public void setEncodingScheme(String newEncoding) {
        encoding = newEncoding;
    }

    public void setFileName(String newName) {
        fileName = newName;
        setOrPutProperty(PropertyKeys.FILENAME, newName);
    }

    public void setLocalFilePath(File localFile) {
        localPath = (localFile.isFile()) ? localFile.getParentFile().getAbsolutePath() : localFile.getAbsolutePath();
    }

    public void setOrPutProperty(String key, Object value) {
        if (value != null && !setProperty(key, value)) {
            Property prop = new Property(key, value.getClass(), true);
            prop.setValue(value);
            properties.put(key, prop);
        }
    }

    public void setParseType(String type) {
        parserType = type;
        setOrPutProperty(PropertyKeys.LOADTYPE, type);
    }

    public void setProperties(Map newProps) {
        if (newProps != null) {
            properties = newProps;
        }
    }

    public boolean setProperty(String key, Object value) {
        Property aProp = (Property) properties.get(key);
        if (aProp != null && key.equals(aProp.getName())) {
            aProp.setValue(value);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        StringBuilder buf = new StringBuilder(50);

        if (parentDBModel != null) {
            buf.append(parentDBModel.getModelName());
            buf.append(":");
            buf.append(parentDBModel.getFullyQualifiedTableName(this));
        } else {
            buf.append(getName());
        }

        return buf.toString();
    }

    public void updateProperties(Map newProps) {
        if (newProps != null) {
            Iterator iter = newProps.keySet().iterator();
            while (iter.hasNext()) {
                String key = (String) iter.next();
                Object value = newProps.get(key);
                setOrPutProperty(key, value);
            }
        }
    }

    // use for test purpose only
    void setTableDefinition(Map props) {
        this.properties = props;
    }

    private String getFullFilePath(String directory, String filename) {
        StringBuilder fullpath = new StringBuilder(50);
        fullpath.append((VirtualDBUtil.isNullString(directory)) ? "" : directory);

        char separator = '/';
        if (directory.indexOf('\\') != -1) {
            separator = '\\';
        }

        // Append a separator to the end of full path if directory doesn't
        // already end with it.
        if (!directory.endsWith(Character.toString(separator))) {
            fullpath.append(separator);
        }
        fullpath.append(filename);

        return fullpath.toString().trim();
    }
}
