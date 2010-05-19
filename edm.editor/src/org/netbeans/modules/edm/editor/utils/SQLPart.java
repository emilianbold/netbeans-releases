/*
 * BEGIN_HEADER - DO NOT EDIT
 * 
 * The contents of this file are subject to the terms
 * of the Common Development and Distribution License
 * (the "License").  You may not use this file except
 * in compliance with the License.
 *
 * You can obtain a copy of the license at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * See the License for the specific language governing
 * permissions and limitations under the License.
 *
 * When distributing Covered Code, include this CDDL
 * HEADER in each file and include the License file at
 * https://open-jbi-components.dev.java.net/public/CDDLv1.0.html.
 * If applicable add the following below this CDDL HEADER,
 * with the fields enclosed by brackets "[]" replaced with
 * your own identifying information: Portions Copyright
 * [year] [name of copyright owner]
 */
package org.netbeans.modules.edm.editor.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import org.netbeans.modules.edm.model.EDMException;

/**
 * Binds a SQL statement string with an associated statement type (String descriptor) and
 * connection pool name.
 * 
 * @author Sudhendra Seshachala
 * @author Jonathan Giron
 */
public class SQLPart {

    public static final char STATEMENT_SEPARATOR = '\uFFFF';
    public static final String STMT_CHECKTABLEEXISTS = "checkTableExists"; //NOI18N
    public static final String STMT_CREATE = "createStatement"; //NOI18N
    public static final String STMT_CREATEDBLINK = "createDbLinkStatement"; //NOI18N
    public static final String STMT_CREATEEXTERNAL = "createExternalStatement"; //NOI18N
    public static final String STMT_CREATEFLATFILE = "createFlatfileStatement"; //NOI18N
    public static final String STMT_CREATEREMOTELOGDETAILSTABLE = "createRemoteDetailsTableStatement"; //NOI18N
    public static final String STMT_DEFRAG = "defragStatement"; //NOI18N
    public static final String STMT_DELETE = "deleteStatement"; //NOI18N
    public static final String STMT_DELETEBEFOREPROCESS = "deleteBeforeProcessStatement"; //NOI18N
    public static final String STMT_DELETEINVALIDROWFROMSUMMARY = "deleteInvalidRowFromSummaryTable"; //NOI18N
    public static final String STMT_DROP = "dropStatement"; //NOI18N
    public static final String STMT_DROPDBLINK = "dropDbLinkStatement"; //NOI18N
    public static final String STMT_INITIALIZESTATEMENTS = "initializeStatements"; //NOI18N
    public static final String STMT_REMOUNTREMOTETABLE = "remountRemoteTableStatement"; // NOI18N
    public static final String STMT_ROWCOUNT = "rowCountStatement"; //NOI18N
    public static final String STMT_SELECT = "selectStatement"; //NOI18N
    public static final String STMT_TRUNCATE = "truncateStatement"; //NOI18N
    public static final String TAG_SQLPART = "sqlPart"; //NOI18N
    private static final String ATTR_DEFAULT_NAME = "defaultFileName";
    private static final String ATTR_POOLNAME = "connPoolName";
    private static final String ATTR_TABLE_NAME = "tableName";
    private static final String ATTR_TYPE = "stmtType";
    private static final String XML_STATEMENT_SEPARATOR = "{@#END#@}";
    public static final String ATTR_JDBC_TYPE_LIST = "jdbcTypeList";   //NOI18N
    public static final String ATTR_DESTS_SRC = "DestinationsSource";   //NOI18N
    private Map attributes = new HashMap();
    private String connPoolName;
    private String defaultFileName;
    private String sql;
    private Map sqlStmtMap = new HashMap();
    private String tableName;
    private String type;

    public SQLPart(Element sqlElement) throws EDMException {
        parseXML(sqlElement);
    }

    public SQLPart(String theTableName) {
        this.setTableName(theTableName);
    }

    public SQLPart(String statement, String sqlType, String connectionPool) {
        setSQL(statement);
        setType(sqlType);
        setConnectionPoolName(connectionPool);
    }

    public void addSQLStatement(String stmtType, String theSQL) {
        sqlStmtMap.put(stmtType, theSQL);
    }

    public Collection getAllSQLStatements() {
        return sqlStmtMap.values();
    }

    public Attribute getAttribute(String attrName) {
        return (Attribute) attributes.get(attrName);
    }

    public Collection getAttributeNames() {
        return attributes.keySet();
    }

    public Object getAttributeObject(String attrName) {
        Attribute attr = getAttribute(attrName);
        return (attr != null) ? attr.getAttributeValue() : null;
    }

    public String getConnectionPoolName() {
        return this.connPoolName;
    }

    public String getDefaultValue() {
        return this.defaultFileName;
    }

    public Iterator getIterator() {
        List list = new ArrayList();
        if ((this.sql != null) && (!"".equals(this.sql))) {
            StringTokenizer st = new StringTokenizer(this.sql, Character.toString(SQLPart.STATEMENT_SEPARATOR));
            while (st.hasMoreTokens()) {
                list.add(st.nextToken());
            }
        }
        return list.iterator();
    }

    public Map getTypeToStatementMap() {
        return sqlStmtMap;
    }

    public String getSQL() {
        return this.sql;
    }

    public String getSQL(String stmtType) {
        return (String) sqlStmtMap.get(stmtType);
    }

    public String getTableName() {
        return this.tableName;
    }

    public String getType() {
        return this.type;
    }

    public void parseXML(Element element) throws EDMException {
        if (element == null) {
            throw new EDMException("Must supply non-null Element ref for parameter 'element'.");
        }

        connPoolName = element.getAttribute(ATTR_POOLNAME);
        if (StringUtil.isNullString(connPoolName)) {
            throw new EDMException("XML element has an empty or missing value for attribute '" + ATTR_POOLNAME + "'.");
        }

        type = element.getAttribute(ATTR_TYPE);
        if (StringUtil.isNullString(type)) {
            throw new EDMException("XML element has an empty or missing value for attribute '" + ATTR_TYPE + "'.");
        }

        this.tableName = element.getAttribute(ATTR_TABLE_NAME);
        this.defaultFileName = element.getAttribute(ATTR_DEFAULT_NAME);

        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node aNode = children.item(i);

            if (aNode.getNodeType() == Node.ELEMENT_NODE) {
                Element elem = (Element) aNode;
                if (elem.getNodeName().equals(Attribute.TAG_ATTR)) {
                    Attribute attr = new Attribute();
                    attr.parseXMLString(elem);
                    this.attributes.put(attr.getAttributeName(), attr);
                } else if (elem.getNodeName().equals("sql")) {
                    try {
                        // one sql part will contain only one sql string
                        NodeList sqlChildren = elem.getChildNodes();
                        Node sqlNode = sqlChildren.item(0);
                        sql = ((Text) sqlNode).getData();
                        if (sql == null || sql.trim().length() == 0) {
                            throw new EDMException("XML element has no SQL statement!");
                        }
                        sql = StringUtil.replaceInString(sql, XML_STATEMENT_SEPARATOR, Character.toString(STATEMENT_SEPARATOR)).trim();
                    } catch (DOMException e) {
                        throw new EDMException("Caught DOMException while parsing SQLPart.", e);
                    }
                }
            }
        }
    }

    public void setAttribute(String attrName, Object val) {
        Attribute attr = getAttribute(attrName);
        if (attr != null) {
            attr.setAttributeValue(val);
        } else {
            attr = new Attribute(attrName, val);
            attributes.put(attrName, attr);
        }
    }

    public void setConnectionPoolName(String newPoolName) {
        this.connPoolName = newPoolName;
    }

    public void setDefaultValue(String defValue) {
        defaultFileName = defValue;
    }

    public void setSQL(String newSQL) {
        this.sql = newSQL;
    }

    public void setTableName(String theTableName) {
        this.tableName = theTableName;
    }

    public void setType(String newType) {
        this.type = newType;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(60);
        if ((this.sql != null) && (!"".equals(this.sql))) {
            StringTokenizer st = new StringTokenizer(this.sql, Character.toString(SQLPart.STATEMENT_SEPARATOR));
            while (st.hasMoreTokens()) {
                buf.append(st.nextToken() + "\n\n");
            }
        }
        return buf.toString();
    }

    public String toXMLString() {
        return toXMLString("");
    }

    public String toXMLString(String prefix) {
        StringBuffer buf = new StringBuffer(200);

        if (prefix == null) {
            prefix = "";
        }

        buf.append(prefix).append("<" + TAG_SQLPART + " ");
        buf.append(ATTR_POOLNAME + "=\"").append(connPoolName).append("\" ");
        if (!StringUtil.isNullString(this.tableName)) {
            if (this.tableName.startsWith("\"")) {
                this.tableName = XmlUtil.escapeXML(this.tableName);
            }
        }
        buf.append(ATTR_TABLE_NAME + "=\"").append(this.tableName).append("\" ");
        buf.append(ATTR_DEFAULT_NAME + "=\"").append(this.defaultFileName).append("\" ");
        buf.append(ATTR_TYPE + "=\"").append(type).append("\">\n");
        if (sql != null && sql.trim().length() != 0) {
            buf.append(prefix + "\t<sql>").append(
                    XmlUtil.escapeXML(StringUtil.replaceInString(sql.trim(), Character.toString(STATEMENT_SEPARATOR), XML_STATEMENT_SEPARATOR)).trim()).append(
                    "</sql>\n");
        }
        buf.append(toXMLAttributeTags(prefix));
        buf.append(prefix).append("</" + TAG_SQLPART + ">\n");

        return buf.toString();
    }

    protected String toXMLAttributeTags(String prefix) {
        StringBuffer buf = new StringBuffer(100);

        Iterator iter = attributes.values().iterator();
        while (iter.hasNext()) {
            Attribute attr = (Attribute) iter.next();
            if (attr.getAttributeValue() != null) {
                buf.append(attr.toXMLString(prefix + "\t"));
            }
        }
        return buf.toString();
    }
}
