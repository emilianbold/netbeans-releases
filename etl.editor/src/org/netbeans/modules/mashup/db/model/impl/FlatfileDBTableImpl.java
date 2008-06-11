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
package org.netbeans.modules.mashup.db.model.impl;

import com.sun.sql.framework.exception.BaseException;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.netbeans.modules.mashup.db.common.Property;
import org.netbeans.modules.mashup.db.common.PropertyKeys;
import org.netbeans.modules.mashup.db.model.FlatfileDBColumn;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.sql.framework.model.DBColumn;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.impl.PrimaryKeyImpl;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import net.java.hulp.i18n.Logger;
import com.sun.sql.framework.utils.StringUtil;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.SQLDBColumn;
import org.netbeans.modules.sql.framework.model.SQLDBTable;
import org.netbeans.modules.sql.framework.model.impl.AbstractDBTable;

/**
 * Reference implementation for interface org.netbeans.modules.etl.model.DBTable
 *
 * @author Jonathan Giron
 * @author Girish Patil
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class FlatfileDBTableImpl extends AbstractDBTable implements FlatfileDBTable, Cloneable, Comparable {

    /** Constants used in XML tags * */
    private static final String ATTR_ENCODING = "encoding";
    private static final String ATTR_FILE_NAME = "fileName";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PARENT = "parent";
    /* Constant: character indicating end of ORGANIZATION property clause */
    private static final String END_PROPS_DELIMITER = ")";
    private static final String END_QUOTE_SPACE = "\" ";
    private static final String EQUAL_START_QUOTE = "=\"";
    private static transient final Logger mLogger = Logger.getLogger(FlatfileDBTableImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    /* Log4J category string */
    private static final String LOG_CATEGORY = FlatfileDBTableImpl.class.getName();
    /* Constant: separator between key-value properties in ORGANIZATION clause */
    private static final String PROP_SEPARATOR = ", ";
    /* Constant: keyword signaling start of properties clause */
    private static final String PROPS_KEYWORD = "ORGANIZATION"; // NOI18N
    /* Constant: character indicating start of ORGANIZATION property clause */
    private static final String START_PROPS_DELIMITER = "(";
    private static final String TAB = "\t";
    private static final String TAG_STCDB_COLUMN = "stcdbColumn";
    // private static final String TAG_STCDB_COLUMN = "FlatfileColumn";
    private static final String TAG_STCDB_TABLE = "stcdbTable";
    // private static final String TAG_STCDB_TABLE = "FlatfileTable";
    /**
     * Holds property keys which are only used by the wizard and will cause errors in
     * Axion during validation of table properties.
     */
    private static final Set WIZARD_ONLY_PROPERTIES = new HashSet();
    

    static {
        WIZARD_ONLY_PROPERTIES.add("DEFAULTSQLTYPE");
        WIZARD_ONLY_PROPERTIES.add("FILEPATH");
        WIZARD_ONLY_PROPERTIES.add("FIELDCOUNT");
    }
    /* Encoding of file contents, e.g., utf-8, cp500, etc. */
    private String encoding = "";
    /* Sample file name (no path) */
    private String fileName = "";
    /* Path to sample file locally */
    private transient String localPath = File.separator;
    private String parserType;
    /* Parse configurator for this flatfile */
    private Map properties;

    /* No-arg constructor; initializes Collections-related member variables. */
    public FlatfileDBTableImpl() {
        super();
        columns = new LinkedHashMap<String, DBColumn>();
        properties = new HashMap();
    }

    /**
     * Creates a new instance of FlatfileDBTableImpl, cloning the contents of the given
     * DBTable implementation instance.
     *
     * @param src DBTable instance to be cloned
     */
    public FlatfileDBTableImpl(DBTable src) {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null DBTable instance for src param.");
        }

        copyFrom(src);
    }

    /**
     * Creates a new instance of FlatfileDBTableImpl, cloning the contents of the given
     * FlatfileDBTable implementation instance.
     *
     * @param src FlatfileDBTable instance to be cloned
     */
    public FlatfileDBTableImpl(FlatfileDBTable src) {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null FlatfileDBTable instance for src param.");
        }

        copyFrom(src);
    }

    public FlatfileDBTableImpl(String aName) {
        this();
        this.name = (aName != null) ? aName.trim() : null;
    }

    /*
     * Implementation of DBTable interface.
     */
    /**
     * Creates a new instance of FlatfileDBTableImpl with the given name.
     *
     * @param aName name of new DBTable instance
     * @param aSchema schema of new DBTable instance; may be null
     * @param aCatalog catalog of new DBTable instance; may be null
     */
    public FlatfileDBTableImpl(String aName, String aSchema, String aCatalog) {
        this();
        this.name = (aName != null) ? aName.trim() : null;
    }

    /**
     * Adds a DBColumn instance to this table.
     *
     * @param theColumn column to be added.
     * @param ignoreDupCols ignore or throw exception when column with same name being added.
     * @return true if successful. false if failed.
     */
    private boolean addColumn(SQLDBColumn theColumn, boolean ignoreDupCols) {
        if (theColumn != null) {
            if ((!ignoreDupCols) && (columns.containsKey(theColumn.getName()))) {
                throw new IllegalArgumentException("Column " + theColumn.getName() + " already exist.");
            }
            theColumn.setParent(this);
            this.columns.put(theColumn.getName(), theColumn);
            return true;
        }

        return false;
    }

    /**
     * Adds a DBColumn instance to this table.
     *
     * @param theColumn column to be added.
     * @return true if successful. false if failed.
     */
    @Override
    public boolean addColumn(SQLDBColumn theColumn) {
        return addColumn(theColumn, false);
    }

    /**
     * Clone a deep copy of DBTable.
     *
     * @return a copy of DBTable.
     */
    @Override
    public Object clone() {
        try {
            FlatfileDBTableImpl table = (FlatfileDBTableImpl) super.clone();
            table.columns = new LinkedHashMap();
            table.deepCopyReferences(this);

            return table;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    /**
     * Compares DBTable with another object for lexicographical ordering. Null objects and
     * those DBTables with null names are placed at the end of any ordered collection
     * using this method.
     *
     * @param refObj Object to be compared.
     * @return -1 if the column name is less than obj to be compared. 0 if the column name
     *         is the same. 1 if the column name is greater than obj to be compared.
     */
    @Override
    public int compareTo(Object refObj) {
        if (refObj == null) {
            return -1;
        }

        if (refObj == this) {
            return 0;
        }

        String refName = (parentDBModel != null) ? parentDBModel.getFullyQualifiedTableName((DBTable) refObj) : ((DBTable) refObj).getName();
        String myName = (parentDBModel != null) ? parentDBModel.getFullyQualifiedTableName(this) : name;
        return (myName != null) ? myName.compareTo(refName) : (refName != null) ? 1 : -1;
    }

    /**
     * Performs deep copy of contents of given DBTable. We deep copy (that is, the method
     * clones all child objects such as columns) because columns have a parent-child
     * relationship that must be preserved internally.
     *
     * @param source DBTable providing contents to be copied.
     */
    @Override
    public void copyFrom(DBTable source) {
        if (source == null) {
            throw new IllegalArgumentException("Must supply non-null ref for source");
        } else if (source == this) {
            return;
        }

        name = source.getName();
        description = source.getDescription();

        deepCopyReferences(source);
    }

    /**
     * Performs deep copy of contents of given FlatfileDBTable. We deep copy (that is, the
     * method clones all child objects such as columns) because columns have a
     * parent-child relationship that must be preserved internally.
     *
     * @param source FlatfileDBTable providing contents to be copied.
     */
    public void copyFrom(FlatfileDBTable source) {
        copyFrom((DBTable) source);
        if (source instanceof FlatfileDBTableImpl) {
            FlatfileDBTableImpl impl = (FlatfileDBTableImpl) source;
            encoding = impl.encoding;
            fileName = impl.fileName;
            name = impl.name;

            deepCopyReferences(impl);
        }
    }

    /**
     * Convenience class to create FlatfileDBColumnImpl instance (with the given column
     * name, data source name, JDBC type, scale, precision, and nullable), and add it to
     * this FlatfileDBTableImpl instance.
     *
     * @param columnName Column name
     * @param jdbcType JDBC type defined in SQL.Types
     * @param scale Scale
     * @param precision Precision
     * @param isPK true if part of primary key, false otherwise
     * @param isFK true if part of foreign key, false otherwise
     * @param isIndexed true if indexed, false otherwise
     * @param nullable Nullable
     * @return new FlatfileDBColumnImpl instance
     */
    public FlatfileDBColumn createColumn(String columnName, int jdbcType, int scale, int precision, boolean isPK, boolean isFK, boolean isIndexed,
            boolean nullable) {
        FlatfileDBColumn impl = new FlatfileDBColumnImpl(columnName, jdbcType, scale, precision, isPK, isFK, isIndexed, nullable);
        impl.setParent(this);
        this.columns.put(columnName, impl);
        return impl;
    }

    /**
     * Overrides default implementation to return value based on memberwise comparison.
     *
     * @param obj Object against which we compare this instance
     * @return true if obj is functionally identical to this ETLTable instance; false
     *         otherwise
     */
    @Override
    public boolean equals(Object obj) {
        boolean result = false;

        // Check for reflexivity first.
        if (this == obj) {
            return true;
        }

        // Check for castability (also deals with null instance)
        boolean response = false;
        if (obj instanceof FlatfileDBTable) {
            FlatfileDBTableImpl aTable = (FlatfileDBTableImpl) obj;
            String aTableName = aTable.getName();
            // DatabaseModel aTableParent = aTable.getParent();
            Map aTableColumns = aTable.getColumns();

            result = (aTableName != null && name != null && name.equals(aTableName));
            // && (parent != null && aTableParent != null && parent.equals(aTableParent));

            if (columns != null && aTableColumns != null) {
                Set objCols = aTableColumns.keySet();
                Set myCols = columns.keySet();

                // Must be identical (no subsetting), hence the pair of tests.
                result &= myCols.containsAll(objCols) && objCols.containsAll(myCols);
            } else if (!(columns == null && aTableColumns == null)) {
                result = false;
            }
            response &= (encoding != null) ? encoding.equals(aTable.encoding) : (aTable.encoding == null);
            response &= (fileName != null) ? fileName.equals(aTable.fileName) : (aTable.fileName == null);

        }

        return result & response;
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getCatalog
     */
    @Override
    public String getCatalog() {
        return "";
    }

    /**
     * Gets the Create Statement SQL for creating table for a flat file
     *
     * @return SQL for this Flatfile with getTableName()
     */
    public String getCreateStatementSQL() {
        return getCreateStatementSQL(this) + getFlatfilePropertiesSQL();
    }

    /**
     * Gets the SQL create statement to create a text table representing this flatfile.
     *
     * @param table to use in synthesizing the create statement; if null,
     *        the current table name yielded by getName() will be used
     * @return SQL statement to create a text table representing the contents of this
     *         flatfile
     */
    public String getCreateStatementSQL(SQLDBTable table) {
        String tableName = table.getName();
        List<String> pkList = new ArrayList<String>();
        Iterator it = table.getColumns().values().iterator();
        StringBuilder buffer = new StringBuilder(100);
        buffer.append("CREATE EXTERNAL TABLE IF NOT EXISTS \"").append(tableName).append("\" (");
        // NOI18N
        int i = 0;
        while (it.hasNext()) {
            FlatfileDBColumn colDef = (FlatfileDBColumn) it.next();
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
            pkbuffer.append(", PRIMARY KEY( ");
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

    /**
     * Gets the SQL create statement to create a text table representing this flatfile.
     *
     * @param table to use in synthesizing the create statement; if null,
     *        the current table name yielded by getName() will be used
     * @return SQL statement to create a text table representing the contents of this
     *         flatfile
     */
    public static String getCreateStatementSQL(SQLDBTable table, String generatedName) {
        String tableName = table.getName();
        List<String> pkList = new ArrayList<String>();
        Iterator it = table.getColumns().values().iterator();
        StringBuilder buffer = new StringBuilder(100);
        buffer.append("CREATE EXTERNAL TABLE \"").append(generatedName).append("\" (");
        // NOI18N
        int i = 0;
        while (it.hasNext()) {
            FlatfileDBColumn colDef = (FlatfileDBColumn) it.next();
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
            pkbuffer.append(", PRIMARY KEY( ");
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

    /**
     * Gets the SQL create statement to create a text table representing this flatfile.
     *
     * @return SQL statement to create a text table representing the contents of this
     *         flatfile
     */
    public String getCreateStatementSQL(String directory, String theTableName, String runtimeName, boolean isDynamicFilePath,
            boolean createDataFileIfNotExist) {
        String sql = null;
        try {
            if (runtimeName != null && runtimeName.trim().length() != 0) {
                setOrPutProperty(FlatfileDBTable.PROP_FILENAME, "$" + runtimeName);
            /**
            if (isDynamicFilePath) {
            setOrPutProperty(FlatfileDBTable.PROP_FILENAME, "$" + runtimeName);
            } else {
            // NOTE: DO NOT USE java.io.File to generate the file path,
            // as getCanonicalPath() is platform-centric and will hard-code
            // platform-specific root-drive info (e.g., "C:" for M$-Window$)
            // where it's inappropriate.
            setOrPutProperty(FlatfileDBTable.PROP_FILENAME, getFullFilePath(directory, "$" + runtimeName));
            }**/
            } else {
                setOrPutProperty(FlatfileDBTable.PROP_FILENAME, getFullFilePath(directory, fileName));
            }

            setOrPutProperty(FlatfileDBTable.PROP_CREATE_IF_NOT_EXIST, new Boolean(createDataFileIfNotExist));
            sql = this.getCreateStatementSQL(this, theTableName) + this.getFlatfilePropertiesSQL();
        } catch (Exception e) {
            mLogger.errorNoloc(mLoc.t("EDIT061: Failed to set the file path.{0}", LOG_CATEGORY), e);
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

    /**
     * Gets the SQL Drop statement to drop the text table representing this flatfile.
     *
     * @param table table to use in synthesizing the drop statement; if null,
     *        uses the value yielded by getName()
     * @return SQLstatement to drop a text table representing the contents of this
     *         flatfile
     */
    public static String getDropStatementSQL(SQLDBTable table) {
        String tableName = table.getName();
        StringBuilder buffer = new StringBuilder("DROP TABLE IF EXISTS \"");
        // NOI18N
        return buffer.append(tableName).append("\"").toString();
    }

    /**
     * Gets the encoding scheme.
     *
     * @return encoding scheme
     */
    public String getEncodingScheme() {
        return encoding;
    }

    /**
     * Gets the file name.
     *
     * @return file name
     */
    public String getFileName() {
        return fileName;
    }

    public String getFlatfilePropertiesSQL() {
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
            if (WIZARD_ONLY_PROPERTIES.contains(aProp.getName().toUpperCase()) || aProp.getName().toUpperCase().startsWith(FlatfileDBTable.PROP_WIZARD)) {
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
                mLogger.infoNoloc(mLoc.t("EDIT062: Value for property {0}is invalid:{1}; skipping.", aProp.getName(), aProp.getValue()));
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

    /**
     * Gets local path to sample file.
     *
     * @return path (in local workstation file system) to file, excluding the filename.
     */
    public String getLocalFilePath() {
        return localPath;
    }

    /**
     * Gets parse type, if any, associated with this flatfile. To set this type, call
     * setParseConfigurator with an appropriate ParseConfigurator instance from the
     * ParseConfiguratorFactory.
     *
     * @return String representing parse type, or null if none has been defined for this
     *         flatfile.
     */
    public String getParserType() {
        return parserType;
    }

    public Map getProperties() {
        return (properties != null) ? properties : Collections.EMPTY_MAP;
    }

    /**
     * Gets property string associated with the given name.
     *
     * @param key property key
     * @return property associated with propName, or null if no such property exists.
     */
    public String getProperty(String key) {
        Property aProp = (Property) properties.get(key);
        return (aProp != null) ? (aProp.getValue() != null) ? aProp.getValue().toString() : null : null;
    }

    /**
     * @see org.netbeans.modules.model.database.DBTable#getSchema
     */
    @Override
    public String getSchema() {
        return "";
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
        if (rows < 0) {
            throw new IllegalArgumentException("Must supply non-negative int value for parameter rows.");
        }

        StringBuilder buffer = new StringBuilder(100);
        buffer.append("SELECT ");

        Iterator it = getColumnList().iterator();
        int i = 0;
        while (it.hasNext()) {
            FlatfileDBColumn colDef = (FlatfileDBColumn) it.next();
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

    /**
     * Gets the table name.
     *
     * @return Table name
     */
    public String getTableName() {
        return this.getName();
    }

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     *
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    @Override
    public int hashCode() {
        int myHash = (name != null) ? name.hashCode() : 0;
        // myHash += (parent != null) ? parent.hashCode() : 0;

        // Include hashCodes of all column names.
        if (columns != null) {
            myHash += columns.keySet().hashCode();
        }
        myHash += (encoding != null) ? encoding.hashCode() : 0;
        myHash += (fileName != null) ? fileName.hashCode() : 0;

        return myHash;
    }

    @Override
    public void parseXML(Element xmlElement) throws BaseException {
        // In order to be compliant with lagacy JIBX generated XML, following structure
        // needs to be adhered to.
        // <pre>
        // <FlatfileTable name="PQ_EMPLOYEE_CSV" encoding="US-ASCII ....
        // <map size="11">
        // <entry key="LNAME">
        // <FlatfileColumn name="LNAME" fieldName="LNAME" .../>
        // </entry>
        // </map>
        // <map size="8">
        // <entry key="LOADTYPE">Delimited</entry>
        // <entry key="ROWSTOSKIP">0</entry> ....
        // </map>
        // </FlatfileTable>
        // </pre>
        Map attrs = TagParserUtility.getNodeAttributes(xmlElement);
        this.name = (String) attrs.get(ATTR_NAME);
        this.encoding = (String) attrs.get(ATTR_ENCODING);
        this.fileName = (String) attrs.get(ATTR_FILE_NAME);

        // Get child "map" elements.
        NodeList childNodes = xmlElement.getElementsByTagName("map");
        parseColumns((Element) (childNodes.item(0)));
        parseProperties((Element) (childNodes.item(1)));
        upgradeProperties();
        setPK();
    }

    /**
     *Set defaults for any new property added in the UI property sheet
     *for flatfile db creation
     **/
    private void upgradeProperties() {
        if (!properties.containsKey(PropertyKeys.TRIMWHITESPACE)) {
            setOrPutProperty(PropertyKeys.TRIMWHITESPACE, "true");
        }
    }

    /**
     * Sets the encoding scheme.
     *
     * @param newEncoding encoding scheme
     */
    public void setEncodingScheme(String newEncoding) {
        encoding = newEncoding;
    }

    /**
     * Sets the file name.
     *
     * @param newName new file name
     */
    public void setFileName(String newName) {
        fileName = newName;
        setOrPutProperty(PropertyKeys.FILENAME, newName);
    }

    /**
     * Sets local path to sample file.
     *
     * @param localFile File representing path to sample file. If localFile represents the
     *        file itself, only the directory path will be stored.
     */
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

    /**
     * Sets MutableParseConfigurator instance associated with this flatfile.
     *
     * @param newConfig new MutableParseConfigurator to associate
     */
    public void setParseType(String type) {
        parserType = type;
        setOrPutProperty(PropertyKeys.LOADTYPE, type);
    }

    public void setProperties(Map newProps) {
        if (newProps != null) {
            properties = newProps;
        }
    }

    /**
     * Sets the property associated with the given String key to the given value.
     *
     * @param key key whose associated value is sought
     * @param value to associate with key
     */
    public boolean setProperty(String key, Object value) {
        Property aProp = (Property) properties.get(key);
        if (aProp != null && key.equals(aProp.getName())) {
            aProp.setValue(value);
            return true;
        }
        return false;
    }

    /**
     * Overrides default implementation to return fully-qualified name of this DBTable
     * (including name of parent DatabaseModel).
     *
     * @return table name.
     */
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

    /**
     * Marshall this object to XML string.
     *
     * @param prefix
     * @return XML string
     */
    @Override
    public String toXMLString(String prefix) throws BaseException {
        StringBuilder sb = new StringBuilder();
        if (prefix == null) {
            prefix = "";
        }

        sb.append(prefix);
        sb.append("<");
        sb.append(TAG_STCDB_TABLE);
        sb.append(getAttributeNameValues());
        sb.append(">\n");
        sb.append(getXMLColumnMap(prefix + TAB));
        sb.append(getXMLTableProperties(prefix + TAB));
        sb.append(prefix);
        sb.append("</");
        sb.append(TAG_STCDB_TABLE);
        sb.append(">\n");

        return sb.toString();
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

    /**
     * Perform deep copy of columns.
     *
     * @param source ETLTable whose columns are to be copied.
     */
    @Override
    protected void deepCopyReferences(DBTable source) {
        if (source != null && source != this) {
            columns.clear();
            Iterator iter = source.getColumnList().iterator();

            // Must do deep copy to ensure correct parent-child relationship.
            while (iter.hasNext()) {
                addColumn(new FlatfileDBColumnImpl((DBColumn) iter.next()));
            }
        }
    }

    protected void parseColumns(Element mapNode) throws BaseException {
        NodeList entryNodeList = mapNode.getElementsByTagName("entry");
        NodeList columnNodeList = null;
        Element entry = null;
        Element columnElement = null;
        String key = null;
        FlatfileDBColumn column = null;

        int length = entryNodeList.getLength();
        for (int i = 0; i < length; i++) {
            entry = (Element) entryNodeList.item(i);
            key = TagParserUtility.getNodeAttributeValue(entry, "key");
            column = new FlatfileDBColumnImpl();
            columnNodeList = entry.getElementsByTagName(TAG_STCDB_COLUMN);
            columnElement = (Element) columnNodeList.item(0);
            column.setParent(this);
            column.parseXML(columnElement);
            columns.put(key, column);
        }
    }

    protected void parseProperties(Element mapNode) {
        // <map size="8">
        // <entry key="LOADTYPE">Delimited</entry>
        // <entry key="ROWSTOSKIP">0</entry> ...
        // </map>
        NodeList entryNodeList = mapNode.getElementsByTagName("entry");
        Element entry = null;
        String key = null;
        String value = null;

        int length = entryNodeList.getLength();
        for (int i = 0; i < length; i++) {
            entry = (Element) entryNodeList.item(i);
            key = TagParserUtility.getNodeAttributeValue(entry, "key");
            Node node = entry.getChildNodes().item(0);
            value = (node != null) ? node.getNodeValue() : "";
            this.setOrPutProperty(key, StringUtil.unescapeControlChars(value));
        }
    }

    private String getAttributeNameValues() {
        StringBuilder sb = new StringBuilder(" ");
        sb.append(ATTR_NAME);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.name);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_ENCODING);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.encoding);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_FILE_NAME);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.fileName);
        sb.append(END_QUOTE_SPACE);

        sb.append(ATTR_PARENT);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.parentDBModel.getModelName());
        sb.append("\"");
        return sb.toString();
    }

    /**
     * @param directory
     * @param runtimeName
     * @return
     */
    private String getFullFilePath(String directory, String filename) {
        StringBuilder fullpath = new StringBuilder(50);
        fullpath.append((StringUtil.isNullString(directory)) ? "" : directory);

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

    private String getXMLColumnMap(String prefix) throws BaseException {
        StringBuilder sb = new StringBuilder(prefix);
        sb.append("<map size=\"");
        sb.append(this.columns.size());
        sb.append("\">\n");
        sb.append(getXMLColumnMapEntries(prefix + TAB));
        sb.append(prefix);
        sb.append("</map>\n");
        return sb.toString();
    }

    private String getXMLColumnMapEntries(String prefix) throws BaseException {
        StringBuilder sb = new StringBuilder();
        FlatfileDBColumn column = null;
        if ((this.columns != null) && (this.columns.size() > 0)) {
            Iterator itr = columns.keySet().iterator();
            String key = null;
            while (itr.hasNext()) {
                key = (String) itr.next();
                column = (FlatfileDBColumn) columns.get(key);
                sb.append(prefix);
                sb.append("<entry key=\"");
                sb.append(key);
                sb.append("\">\n");
                sb.append(column.toXMLString(prefix + TAB));
                sb.append(prefix);
                sb.append("</entry>\n");
            }
        }

        return sb.toString();
    }

    private String getXMLTableProperties(String prefix) {
        StringBuilder sb = new StringBuilder();
        sb.append(prefix);
        sb.append("<map size=\"");
        if ((this.properties != null) && (this.properties.size() > 0)) {
            sb.append(this.properties.size());
            sb.append("\">\n");

            Iterator itr = properties.keySet().iterator();
            String key = null;
            String val = null;
            String entryPrefix = prefix + TAB;
            while (itr.hasNext()) {
                key = (String) itr.next();
                val = getProperty(key);
                sb.append(entryPrefix);
                sb.append("<entry key=\"");
                sb.append(key);
                sb.append("\">");
                sb.append(StringUtil.escapeControlChars(val));
                sb.append("</entry>\n");
            }
        } else {
            sb.append("0\">\n");
        }

        sb.append(prefix);
        sb.append("</map>\n");

        return sb.toString();
    }

    private void setPK() {
        List pkList = new ArrayList();
        SortedSet fields = new TreeSet(this.columns.values());
        Iterator it = fields.iterator();
        FlatfileDBColumn colDef = null;

        while (it.hasNext()) {
            colDef = (FlatfileDBColumn) it.next();
            if (colDef.isPrimaryKey()) {
                pkList.add(colDef.getName());
            }
        }

        if (pkList.size() > 0) {
            this.primaryKey = new PrimaryKeyImpl("pk" + this.name, pkList);
        }
    }

    @Override
    public String toXMLString(String prefix, boolean tableOnly) throws BaseException {
        return toXMLString(prefix, true);
    }

    @Override
    protected String getElementTagName() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    protected void parseChildren(NodeList childNodeList) throws BaseException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
