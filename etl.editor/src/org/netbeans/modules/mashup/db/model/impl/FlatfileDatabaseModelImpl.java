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

import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import org.netbeans.modules.mashup.db.common.FlatfileDBConnectionFactory;
import org.netbeans.modules.mashup.db.model.FlatfileDBConnectionDefinition;
import org.netbeans.modules.mashup.db.model.FlatfileDBTable;
import org.netbeans.modules.mashup.db.model.FlatfileDatabaseModel;
import org.netbeans.modules.mashup.db.model.FlatfileDefinition;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.sun.sql.framework.exception.BaseException;
import net.java.hulp.i18n.Logger;
import org.netbeans.modules.etl.logger.Localizer;
import org.netbeans.modules.etl.model.ETLObject;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.model.DBTable;
import org.netbeans.modules.sql.framework.model.DatabaseModel;
import org.netbeans.modules.sql.framework.model.impl.SQLDBModelImpl;

/**
 * Flatfile DB specific concrete implementation of DatabaseModel interface.
 * 
 * @author Jonathan Giron
 * @author Girish Patil
 * @author Ahimanikya Satapathy
 * @version $Revision$
 */
public class FlatfileDatabaseModelImpl extends SQLDBModelImpl implements FlatfileDatabaseModel, Cloneable {

    /** Constants used in XML tags * */
    private static final String ATTR_MAJOR_VERSION = "majorVersion";
    private static final String ATTR_MICRO_VERSION = "microVersion";
    private static final String ATTR_MINOR_VERSION = "minorVersion";
    private static final String ATTR_NAME = "name";
    private static transient final Logger mLogger = Logger.getLogger(FlatfileDatabaseModelImpl.class.getName());
    private static transient final Localizer mLoc = Localizer.get();
    private static final String DRIVER_NAME = "org.axiondb.jdbc.AxionDriver";
    private static final List DRIVER_LIST;
    

    static {
        List aList = new ArrayList(1);
        aList.add(DRIVER_NAME);
        DRIVER_LIST = Collections.unmodifiableList(aList);
    }
    private static final int DRIVER_TYPE = 4;
    private static final String END_QUOTE_SPACE = "\" ";
    private static final String EQUAL_START_QUOTE = "=\"";
    /*
     * String used to separate name, schema, and/or catalog Strings in a fully-qualified
     * table name.
     */
    private static final String FQ_TBL_NAME_SEPARATOR = ".";
    private static final String LOG_CATEGORY = FlatfileDatabaseModelImpl.class.getName();
    private static final String QUOTE = "\"";
    private static final String TAB = "\t";
    private static final String TAG_CONNECTION_DEFINITION = "connectionDefinition";
    private static final String TAG_MODEL = "stcdbDatabaseModel";
    private static final String TAG_STCDB_TABLE = "stcdbTable";
    private static final String XML_DOC_HEADER = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n";
    /** Connection name */
    protected volatile String connectionName;

    /* Major version number of DatabaseModel metadata */
    private transient int majorVersion = 5;

    /* Micro (implementation) version number of DatabaseModel metadata */
    private transient int microVersion = 0;

    /* Major version number of DatabaseModel metadata */
    private transient int minorVersion = 1;

    /** Constructs a new default instance of FlatfileDatabaseModelImpl. */
    public FlatfileDatabaseModelImpl() {
        tables = new HashMap<String, FlatfileDBTable>();
    }

    /**
     * Creates a new instance of FlatfileDatabaseModelImpl, cloning the contents of the
     * given DatabaseModel implementation instance.
     * 
     * @param src DatabaseModel instance to be cloned
     */
    public FlatfileDatabaseModelImpl(FlatfileDatabaseModel src) {
        this();

        if (src == null) {
            throw new IllegalArgumentException("Must supply non-null DatabseModel instance for src param.");
        }

        copyFrom(src);
    }

    /**
     * Constructs a new instance of FlatfileDatabaseModelImpl of type SOURCE_DBMODEL,
     * using the given name and DBConnectionDefinition.
     * 
     * @param modelName name of new DatabaseModel
     * @param connDef FlatfileDBConnectionDefinition for this new instance
     */
    public FlatfileDatabaseModelImpl(String modelName, DBConnectionDefinition connDef) {
        this();

        if (connDef == null) {
            throw new IllegalArgumentException("connDef must be non-null");
        }

        String connName = connDef.getName();
        if (connName == null || connName.trim().length() == 0) {
            throw new IllegalArgumentException("connDef must have a name.");
        }

        if (modelName == null || modelName.trim().length() == 0) {
            throw new IllegalArgumentException("modelName must be a non-empty String");
        }

        name = modelName;
        connectionDefinition = new FlatfileDBConnectionDefinitionImpl(connDef);
    }

    /**
     * Adds new SourceTable to the model.
     * 
     * @param table new DBTable to add
     */
    public void addTable(FlatfileDBTable table) {
        if (table != null) {
            table.setParent(this);
            tables.put(getFullyQualifiedTableName(table), table);
        }
    }

    /**
     * Clones this object.
     * 
     * @return shallow copy of this ETLDataSource
     */
    public Object clone() {
        FlatfileDatabaseModelImpl myClone = (FlatfileDatabaseModelImpl) super.clone();

        myClone.name = name;
        myClone.description = description;
        myClone.source = source;

        myClone.tables = new HashMap<String, FlatfileDBTable>();
        tables.putAll(tables);

        myClone.connectionName = connectionName;

        return myClone;
    }

    /**
     * Copies member values from those contained in the given DatabaseModel instance.
     */
    public void copyFrom(DatabaseModel src) {
        if (src == null || src == this) {
            return;
        }

        copyPrimitivesFrom(src);
        copyConnectionDefinitionFrom(src);
        copyTablesFrom(src);
    }

    /**
     * Copies member values from those contained in the given FlatfileDatabaseModel
     * instance.
     * 
     * @param src DatabaseModel whose contents are to be copied into this instance
     */
    public void copyFrom(FlatfileDatabaseModel src) {
        copyFrom((DatabaseModel) src);
    }

    /**
     * Create DBTable instance with the given table, schema, and catalog names.
     * 
     * @param tableName table name of new table
     * @param schemaName schema name of new table
     * @param catalogName catalog name of new table
     * @return an instance of ETLTable if successful, null if failed.
     */
    public DBTable createTable(String tableName, String schemaName, String catalogName) {
        FlatfileDBTableImpl table = null;

        if (tableName == null || tableName.length() == 0) {
            throw new IllegalArgumentException("tableName cannot be null");
        }

        table = new FlatfileDBTableImpl(tableName, schemaName, catalogName);
        addTable(table);

        return table;
    }

    /**
     * @see java.lang.Object#equals
     */
    public boolean equals(Object refObj) {
        // Check for reflexivity.
        if (this == refObj) {
            return true;
        }

        boolean result = false;

        // Ensure castability (also checks for null refObj)
        if (refObj instanceof FlatfileDatabaseModelImpl) {
            FlatfileDatabaseModelImpl aSrc = (FlatfileDatabaseModelImpl) refObj;

            result = ((aSrc.name != null) ? aSrc.name.equals(name) : (name == null));
            mLogger.infoNoloc(mLoc.t("EDIT063: equals(): Do model names match? {0}" + result, LOG_CATEGORY));

            boolean connCheck = (aSrc.connectionName != null) ? aSrc.connectionName.equals(connectionName) : (connectionName == null);
            mLogger.infoNoloc(mLoc.t("EDIT064: equals(): Do connection names match? {0}" + connCheck, LOG_CATEGORY));
            result &= connCheck;

            connCheck = ((aSrc.connectionDefinition != null) ? aSrc.connectionDefinition.equals(connectionDefinition) : (connectionDefinition == null));
            mLogger.infoNoloc(mLoc.t("EDIT065: equals(): Do connection defs match? {0}" + connCheck, LOG_CATEGORY));
            result &= connCheck;

            if (tables != null && aSrc.tables != null) {
                Set objTbls = aSrc.tables.keySet();
                Set myTbls = tables.keySet();

                // Must be identical (no subsetting), hence the pair of tests.
                boolean tblCheck = myTbls.containsAll(objTbls) && objTbls.containsAll(myTbls);
                mLogger.infoNoloc(mLoc.t("EDIT066: equals(): Do table names match? {0}" + tblCheck, LOG_CATEGORY));
                result &= tblCheck;
            }
        }

        mLogger.infoNoloc(mLoc.t("EDIT067: equals(): Is refObj equal to this? {0}" + result, LOG_CATEGORY));
        return result;
    }

    public DBConnectionDefinition getFlatfileDBConnectionDefinition(boolean download) {
        if (download) {
            return getConnectionDefinition();
        } else {
            return connectionDefinition;
        }
    }

    /**
     * Gets name of DBConnectionDefinition associated with this database model.
     * 
     * @return name of associated DBConnectionDefinition instance
     */
    public String getConnectionName() {
        return this.connectionName;
    }

    public Map getFlatfileTablePropertyMap(String flatfileName) {
        FlatfileDBTable table = (FlatfileDBTable) this.getTableMap().get(flatfileName);
        return table == null ? Collections.EMPTY_MAP : table.getProperties();
    }

    public Map getFlatfileTablePropertyMaps() {
        Iterator iter = this.getTables().iterator();
        Map propMap = Collections.EMPTY_MAP;
        if (iter.hasNext()) {
            propMap = new HashMap(this.getTables().size());
            do {
                FlatfileDBTable flatfileDBTable = (FlatfileDBTable) iter.next();
                propMap.put(flatfileDBTable.getName(), flatfileDBTable.getProperties());
            } while (iter.hasNext());
        }

        return propMap;
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getFullyQualifiedTableName(DBTable)
     */
    public String getFullyQualifiedTableName(DBTable tbl) {
        return (tbl != null) ? getFullyQualifiedTableName(tbl.getName(), tbl.getSchema(), tbl.getCatalog()) : "";
    }

    /**
     * @see org.netbeans.modules.model.database.DatabaseModel#getFullyQualifiedTableName(String,
     *      String, String)
     */
    public String getFullyQualifiedTableName(String tblName, String schName, String catName) {
        if (tblName == null) {
            throw new IllegalArgumentException("Must supply non-null String value for tblName.");
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

    public Connection getJDBCConnection() throws Exception {
        return getJDBCConnection((ClassLoader) null);
    }

    public Connection getJDBCConnection(ClassLoader cl) throws Exception {
        Connection conn = null;
        String url = null;
        String id = null;
        String pswd = null;

        try {
            DBConnectionDefinition cd = getConnectionDefinition();
            url = cd.getConnectionURL();
            id = cd.getUserName();
            pswd = cd.getPassword();

            if ((id != null) && (!"".equals(id))) {
                conn = FlatfileDBConnectionFactory.getInstance().getConnection(url, id, pswd, cl);
            } else {
                conn = FlatfileDBConnectionFactory.getInstance().getConnection(url, null, cl);
            }
        } catch (BaseException ex) {
            Throwable cause = ex.getCause();
            if (cause == null) {
                cause = ex;
            }
            throw new Exception(cause);
        }

        return conn;
    }

    public Connection getJDBCConnection(Properties props) throws Exception {
        return getJDBCConnection(props, (ClassLoader) null);
    }

    public Connection getJDBCConnection(Properties props, ClassLoader cl) throws Exception {
        Connection conn = null;
        try {
            DBConnectionDefinition cd = getConnectionDefinition();
            String jdbcUrl = null;

            if (cd != null) {
                jdbcUrl = cd.getConnectionURL();
            }

            conn = FlatfileDBConnectionFactory.getInstance().getConnection(jdbcUrl, props, cl);
        } catch (BaseException ex) {
            Throwable cause = ex.getCause();
            if (cause == null) {
                cause = ex;
            }
            throw new Exception(cause);
        }
        return conn;
    }

    public Connection getJDBCConnection(String jdbcUrl, String uid, String passwd) throws Exception {
        return getJDBCConnection(jdbcUrl, uid, passwd, null);
    }

    public Connection getJDBCConnection(String jdbcUrl, String uid, String passwd, ClassLoader cl) throws Exception {
        try {
            return FlatfileDBConnectionFactory.getInstance().getConnection(jdbcUrl, uid, passwd, cl);
        } catch (BaseException ex) {
            Throwable cause = ex.getCause();
            if (cause == null) {
                cause = ex;
            }
            throw new Exception(cause);
        }
    }

    public String getJDBCDriverClassName() throws Exception {
        return FlatfileDBConnectionFactory.getInstance().getDriverClassName();
    }

    public List getJDBCDriverClassNames() throws Exception {
        return DRIVER_LIST;
    }

    public int getJDBCDriverType() throws Exception {
        return DRIVER_TYPE;
    }

    public int getJDBCDriverTypes(String arg0) throws Exception {
        return DRIVER_TYPE;
    }

    public int getMajorVersion() {
        return majorVersion;
    }

    public int getMicroVersion() {
        return microVersion;
    }

    public int getMinorVersion() {
        return minorVersion;
    }

    /**
     * Gets repository object, if any, providing underlying data for this DatabaseModel
     * implementation.
     * 
     * @return RepositoryObject hosting this object's metadata, or null if data are not
     *         held by a Object.
     */
    public ETLObject getSource() {
        return source;
    }

    public String getVersionString() {
        return majorVersion + "." + minorVersion + "." + microVersion;
    }

    /**
     * Overrides default implementation to compute hashCode value for those members used
     * in equals() for comparison.
     * 
     * @return hash code for this object
     * @see java.lang.Object#hashCode
     */
    public int hashCode() {
        int myHash = (name != null) ? name.hashCode() : 0;

        // myHash += (connectionName != null) ? connectionName.hashCode() : 0;
        myHash += (connectionDefinition != null) ? connectionDefinition.hashCode() : 0;

        if (tables != null) {
            myHash += tables.keySet().hashCode();
        }

        return myHash;
    }

    /**
     * Unmarshal this object from XML element.
     * 
     * @param xmlElement
     */
    public void parseXML(Element xmlElement) throws BaseException {
        /*
         * In order to be compliant with legacy JIBX generated XML following structure
         * needs to be maintained. <FlatfileDatabaseModel majorVersion="5" .....> <map
         * size="3"> // tables <entry key="PQ_EMPLOYEE_CSV"> <FlatfileTable
         * name="PQ_EMPLOYEE_CSV" encoding="US-ASCII .... </FlatfileTable> </entry> </map>
         * <connectionDefinition name="FlatfileDB" .../> </FlatfileDatabaseModel>
         * </pre>
         */
        Element tmpElement = null;
        parseAttributes(xmlElement);

        NodeList childNodes = xmlElement.getChildNodes();
        int length = childNodes.getLength();
        // "map"
        for (int i = 0; i < length; i++) {
            if (childNodes.item(i).getNodeType() == Node.ELEMENT_NODE) {
                tmpElement = (Element) (childNodes.item(i));

                if ("map".equals(tmpElement.getNodeName())) {
                    parseTablesMap(tmpElement);
                }

                if (TAG_CONNECTION_DEFINITION.equals(tmpElement.getNodeName())) {
                    this.connectionDefinition = new FlatfileDBConnectionDefinitionImpl();
                    ((FlatfileDBConnectionDefinition) connectionDefinition).parseXML(tmpElement);
                }
            }
        }
    }

    /**
     * Setter for FlatfileDBConnectionDefinition
     * 
     * @param theConnectionDefinition to be set
     */
    public void setConnectionDefinition(DBConnectionDefinition theConnectionDefinition) {
        this.connectionDefinition = theConnectionDefinition;
    }

    /**
     * Sets the Connection Name associated with connection name
     * 
     * @param theConName associated with this DataSource
     */
    public void setConnectionName(String theConName) {
        this.connectionName = theConName;
    }

    /**
     * Sets repository object, if any, providing underlying data for this DatabaseModel
     * implementation.
     * 
     * @param obj Object hosting this object's metadata, or null if data are not
     *        held by a Object.
     */
    public void setSource(FlatfileDefinition obj) {
        source = obj;
    }

    // Methods to enable Binding
    // Castor expects these setters to be present
    /**
     * Setter for tables
     * 
     * @param theTables to be part of Model
     */
    public void setTables(Map theTables) {
        this.tables = theTables;
    }

    /**
     * Overrides default implementation to return name of this DatabaseModel.
     * 
     * @return model name.
     */
    public String toString() {
        // return getModelName();
        return getFullyQualifiedName();
    }

    /**
     * Marshall this object to XML string.
     * 
     * @param prefix
     * @return XML string
     */
    public String toXMLString(String prefix) throws BaseException {
        if (prefix == null) {
            prefix = "";
        }
        StringBuilder sb = new StringBuilder(prefix);
        sb.append(XML_DOC_HEADER);
        sb.append(prefix);
        sb.append("<");
        sb.append(TAG_MODEL);
        sb.append(getAttributeNameValues());
        sb.append(">\n");
        sb.append(getXMLTablesMap(prefix + TAB));
        sb.append(getXMLConnectionDefition(prefix + TAB));
        sb.append(prefix);
        sb.append("</");
        sb.append(TAG_MODEL);
        sb.append(">\n");

        return sb.toString();
    }

    /**
     * Sets major version number.
     * 
     * @param newMajor new major version number
     */
    void setMajorVersion(int newMajor) {
        majorVersion = newMajor;
    }

    /**
     * Sets micro version number.
     * 
     * @param newMicro new micro version number
     */
    void setMicroVersion(int newMicro) {
        microVersion = newMicro;
    }

    /**
     * Sets minor version number.
     * 
     * @param newMinor new minor version number
     */
    void setMinorVersion(int newMinor) {
        minorVersion = newMinor;
    }

    protected void parseAttributes(Element xmlElement) {
        Map atts = TagParserUtility.getNodeAttributes(xmlElement);
        String str = (String) atts.get(ATTR_MAJOR_VERSION);
        if (str != null) {
            try {
                majorVersion = Integer.parseInt(str);
            } catch (Exception ex) {
                mLogger.infoNoloc(mLoc.t("EDIT068: LOG_CATEGORY {0}", ATTR_MAJOR_VERSION), ex);
            }
        }

        str = (String) atts.get(ATTR_MINOR_VERSION);
        if (str != null) {
            try {
                minorVersion = Integer.parseInt(str);
            } catch (Exception ex) {
                mLogger.infoNoloc(mLoc.t("EDIT068: LOG_CATEGORY {0}", ATTR_MINOR_VERSION), ex);
            }
        }

        str = (String) atts.get(ATTR_MICRO_VERSION);
        if (str != null) {
            try {
                microVersion = Integer.parseInt(str);
            } catch (Exception ex) {
                mLogger.infoNoloc(mLoc.t("EDIT068: LOG_CATEGORY {0}", ATTR_MICRO_VERSION), ex);
            }
        }

        name = (String) atts.get(ATTR_NAME);
    }

    protected void parseTablesMap(Element mapNode) throws BaseException {
        // All child "entry" elements under "map" element
        NodeList mapEntryNodeList = mapNode.getChildNodes();
        Element entry = null;
        Element tableElement = null;
        FlatfileDBTableImpl table = null;
        NodeList tableNodeList = null;

        int length = mapEntryNodeList.getLength();
        for (int i = 0; i < length; i++) {
            if (mapEntryNodeList.item(i).getNodeType() == Node.ELEMENT_NODE) {
                entry = (Element) mapEntryNodeList.item(i);
                table = new FlatfileDBTableImpl();
                tableNodeList = entry.getElementsByTagName(TAG_STCDB_TABLE);

                // Expect one child Entry
                tableElement = (Element) (tableNodeList.item(0));
                table.setParent(this);
                table.parseXML(tableElement);
                tables.put(table.getName(), table);
            }
        }
    }

    private void copyConnectionDefinitionFrom(DatabaseModel src) {
        DBConnectionDefinition connDef = src.getConnectionDefinition();
        if (connDef instanceof FlatfileDBConnectionDefinition) {
            connectionDefinition = new FlatfileDBConnectionDefinitionImpl((FlatfileDBConnectionDefinition) src.getConnectionDefinition());
        } else {
            connectionDefinition = (connDef != null) ? new FlatfileDBConnectionDefinitionImpl(connDef.getName(), connDef.getDriverClass(),
                    connDef.getConnectionURL(), connDef.getUserName(), connDef.getPassword(), connDef.getDescription()) : new FlatfileDBConnectionDefinitionImpl(
                    src.getModelName(), FlatfileDBConnectionFactory.DRIVER_NAME, null, "", "", src.getModelName());
        }
    }

    private void copyPrimitivesFrom(DatabaseModel src) {
        name = src.getModelName();
        description = src.getModelDescription();
        source = (FlatfileDefinition) src.getSource();
    }

    private void copyTablesFrom(DatabaseModel src) {
        tables.clear();
        List srcTables = src.getTables();

        if (srcTables != null) {
            Iterator iter = srcTables.iterator();
            while (iter.hasNext()) {
                DBTable tbl = (DBTable) iter.next();

                FlatfileDBTableImpl newTable = null;
                if (tbl instanceof FlatfileDBTableImpl) {
                    newTable = (FlatfileDBTableImpl) ((FlatfileDBTable) tbl).clone();
                } else {
                    newTable = new FlatfileDBTableImpl(tbl);
                }

                addTable(newTable);
            }
        }
    }

    private String getAttributeNameValues() {
        StringBuilder sb = new StringBuilder(" ");
        sb.append(ATTR_MAJOR_VERSION);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.majorVersion);
        sb.append(END_QUOTE_SPACE);
        sb.append(ATTR_MINOR_VERSION);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.minorVersion);
        sb.append(END_QUOTE_SPACE);
        sb.append(ATTR_MICRO_VERSION);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.microVersion);
        sb.append(END_QUOTE_SPACE);
        sb.append(ATTR_NAME);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.name);
        sb.append(QUOTE);
        return sb.toString();
    }

    private String getFullyQualifiedName() {
        return this.getModelName();
    }

    private String getXMLConnectionDefition(String prefix) {
        if (this.connectionDefinition != null) {
            return ((FlatfileDBConnectionDefinition) connectionDefinition).toXMLString(prefix);
        } else {
            return "";
        }
    }

    private String getXMLTableMapEntries(String prefix) throws BaseException {
        StringBuilder sb = new StringBuilder();
        FlatfileDBTable table = null;
        if ((this.tables != null) && (this.tables.size() > 0)) {
            Iterator itr = tables.keySet().iterator();
            String key = null;
            while (itr.hasNext()) {
                key = (String) itr.next();
                table = (FlatfileDBTable) tables.get(key);
                sb.append(prefix);
                sb.append("<entry key=\"");
                sb.append(table.getName());  // Incase User changed the default table name
                sb.append("\">\n");
                sb.append(table.toXMLString(prefix + TAB));
                sb.append(prefix);
                sb.append("</entry>\n");
            }
        }

        return sb.toString();
    }

    private String getXMLTablesMap(String prefix) throws BaseException {
        StringBuilder sb = new StringBuilder(prefix);
        sb.append("<map size=\"");
        sb.append(this.tables.size());
        sb.append("\">\n");
        sb.append(getXMLTableMapEntries(prefix + TAB));
        sb.append(prefix);
        sb.append("</map>\n");
        return sb.toString();
    }

    /**
     * Gets Flatfile instance, if any, whose table name matches the given String.
     * 
     * @param tableName table name to search for
     * @return matching instance, if any, or null if no Flatfile matches
     *         <code>aName</code>
     */
    public FlatfileDBTable getFileMatchingTableName(String tableName) {
        if (tableName == null) {
            return null;
        }

        Iterator iter = getTables().iterator();
        while (iter.hasNext()) {
            FlatfileDBTable file = (FlatfileDBTable) iter.next();
            if (tableName.equals(file.getTableName())) {
                return file;
            }
        }

        return null;
    }

    /**
     * Gets Flatfile instance, if any, whose file name matches the given String
     * 
     * @param aName file name to search for
     * @return matching instance, if any, or null if no Flatfile matches
     *         <code>aName</code>
     */
    public FlatfileDBTable getFileMatchingFileName(String aName) {
        if (aName == null) {
            return null;
        }

        Iterator iter = getTables().iterator();
        while (iter.hasNext()) {
            FlatfileDBTable file = (FlatfileDBTable) iter.next();
            if (aName.equals(file.getFileName())) {
                return file;
            }
        }

        return null;
    }
}

