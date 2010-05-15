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

/*
 * @(#)DBConnectionParameters.java 
 *
 * Copyright 2004-2007 Sun Microsystems, Inc. All Rights Reserved.
 * 
 * END_HEADER - DO NOT EDIT
 */

package org.netbeans.modules.edm.editor.utils;

import java.util.Properties;

import org.w3c.dom.Element;

import org.netbeans.modules.edm.model.EDMException;

/**
 * Holds configuration parameters for a database connection.
 * 
 * @author Ahimanikya Satapathy
 */
public class DBConnectionParameters implements Cloneable, Comparable {

    public static final String CONNECTION_DEFINITION_TAG = "connectiondef"; // NOI18N
    public static final String CONNECTION_DESC_TAG = "description"; // NOI18N
    public static final String CONNECTION_NAME_TAG = "name"; // NOI18N
    public static final String DB_VENDOR_ATTR = "dbName";
    public static final String DRIVER_NAME_ATTR = "driverName";
    public static final String PASSWORD_ATTR = "password";
    public static final String URL_ATTR = "dbUrl";
    public static final String USER_NAME_ATTR = "userName";

    private static final String LOG_CATEGORY = DBConnectionParameters.class.getName();
    protected String dbType = "";
    protected volatile String description;
    protected String driverClass = "";
    protected String jdbcUrl = "";
    protected volatile String name;
    protected String password = "";
    protected String userName = "";

    public DBConnectionParameters() {

    }

    public DBConnectionParameters(Element theElement) throws EDMException {
        parseXML(theElement);
    }

    @Override
    public Object clone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e.toString());
        }
    }

    public int compareTo(Object refObj) {
        DBConnectionParameters defn = (DBConnectionParameters) refObj;
        return name.compareTo(defn.name);
    }

    @Override
    public boolean equals(Object refObj) {
        if (!(refObj instanceof DBConnectionParameters)) {
            return false;
        }

        if (this == refObj) {
            return true;
        }

        DBConnectionParameters defn = (DBConnectionParameters) refObj;

        boolean result = (name != null) ? name.equals(defn.name) : (defn.name == null);
        result &= (dbType != null) ? dbType.equals(defn.dbType) : (defn.dbType == null);
        result &= (driverClass != null) ? this.driverClass.equals(defn.driverClass) : (defn.driverClass == null);
        result &= (jdbcUrl != null) ? jdbcUrl.equals(defn.jdbcUrl) : (defn.jdbcUrl == null);
        result &= (userName != null) ? userName.equals(defn.userName) : (defn.userName == null);
        result &= (password != null) ? this.password.equals(defn.password) : (defn.password == null);

        return result;
    }

    public Properties getConnectionProperties() {
        Properties props = new Properties();
        props.put(DBConnectionFactory.PROP_DBTYPE, getDBType());
        props.put(DBConnectionFactory.PROP_DRIVERCLASS, getDriverClass());
        props.put(DBConnectionFactory.PROP_URL, getConnectionURL());
        props.put(DBConnectionFactory.PROP_USERNAME, getUserName());
        props.put(DBConnectionFactory.PROP_PASSWORD, getPassword());
        return props;
    }

    public String getConnectionURL() {
        return jdbcUrl;
    }

    public String getDBType() {
        return dbType;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDriverClass() {
        return driverClass;
    }

    public synchronized String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getUserName() {
        return userName;
    }

    @Override
    public int hashCode() {
        int myHash = (name != null) ? name.hashCode() : 0;
        myHash += (dbType != null) ? dbType.hashCode() : 0;
        myHash += (driverClass != null) ? driverClass.hashCode() : 0;
        myHash += (jdbcUrl != null) ? jdbcUrl.hashCode() : 0;
        myHash += (userName != null) ? userName.hashCode() : 0;
        myHash += (password != null) ? password.hashCode() : 0;
        return myHash;
    }

    public void parseXML(Element xmlElement) throws EDMException {
        try {
            setName(xmlElement.getAttribute(CONNECTION_NAME_TAG));
            setDescription(xmlElement.getAttribute(CONNECTION_DESC_TAG));

            setConnectionURL(XmlUtil.getAttributeFrom(xmlElement, URL_ATTR, false));
            setDBType(XmlUtil.getAttributeFrom(xmlElement, DB_VENDOR_ATTR, false));
            setUserName(XmlUtil.getAttributeFrom(xmlElement, USER_NAME_ATTR, false));
            String passwd = XmlUtil.getAttributeFrom(xmlElement, PASSWORD_ATTR, false);

            // If password is empty, we should not call encrypt.
            // This was creating a issue StringIndexOutOfBounds during runtime QAI 66935
            if (!StringUtil.isNullString(passwd)) {
                String newPass = ScEncrypt.decrypt(this.getUserName(), passwd);
                setPassword(newPass);
            }
            setDriverClass(XmlUtil.getAttributeFrom(xmlElement, DRIVER_NAME_ATTR, false));

        } catch (Exception ex) {
            throw new EDMException(LOG_CATEGORY + ": Could not parse Connection Definition ", ex);
        }
    }

    public void setConnectionURL(String newUrl) {
        jdbcUrl = newUrl;
    }

    public void setDBType(String newDBType) {
        dbType = (newDBType != null) ? newDBType : "";
    }

    public void setDescription(String newDesc) {
        description = (newDesc != null && newDesc.trim().length() != 0) ? newDesc.trim() : "";
    }

    public void setDriverClass(String newDriverClass) {
        driverClass = newDriverClass;
    }

    public void setName(String newName) {
        name = (newName != null && newName.trim().length() != 0) ? newName.trim() : "";
    }

    public void setPassword(String newPassword) {
        password = newPassword;
    }

    public void setUserName(String newUserName) {
        userName = newUserName;
    }

    @Override
    public String toString() {
        StringBuffer buf = new StringBuffer(1000);
        final String nl = ", ";

        buf.append("{ name: \"").append(name).append("\"").append(nl);
        buf.append("DBType: \"").append(dbType).append("\"").append(nl);
        buf.append("driverClass: \"").append(driverClass).append("\"").append(nl);
        buf.append("jdbcUrl: \"").append(jdbcUrl).append("\"").append(nl);
        buf.append("userName: \"").append(userName).append("\"").append(nl);
        buf.append(" }");
        return buf.toString();
    }

    public synchronized String toXMLString() {
        return toXMLString("\t");
    }

    public synchronized String toXMLString(String prefix) {
        StringBuffer xml = new StringBuffer(1000);
        if (prefix == null) {
            prefix = "";
        }

        xml.append(prefix);
        xml.append("<").append(CONNECTION_DEFINITION_TAG);

        if (!StringUtil.isNullString(name)) {
            xml.append(" ").append(CONNECTION_NAME_TAG).append("=\"").append(this.name.trim()).append("\"");
        }

        if (!StringUtil.isNullString(description)) {
            xml.append(" ").append(CONNECTION_DESC_TAG).append("=\"").append(this.description.trim()).append("\"");
        }

        if (!StringUtil.isNullString(driverClass)) {
            xml.append(" ").append(DRIVER_NAME_ATTR).append("=\"").append(driverClass.trim()).append("\"");
        }

        if (!StringUtil.isNullString(dbType)) {
            xml.append(" ").append(DB_VENDOR_ATTR).append("=\"").append(dbType.trim()).append("\"");
        }

        if (!StringUtil.isNullString(jdbcUrl)) {
            xml.append(" ").append(URL_ATTR).append("=\"").append(jdbcUrl.trim()).append("\"");
        }

        if (!StringUtil.isNullString(userName)) {
            xml.append(" ").append(USER_NAME_ATTR).append("=\"").append(userName.trim()).append("\"");
        }

        if (!StringUtil.isNullString(password) && !StringUtil.isNullString(userName)) {
            String newPass = ScEncrypt.encrypt(userName.trim(), password.trim());
            xml.append(" ").append(PASSWORD_ATTR).append("=\"").append(newPass).append("\"");
        }

        xml.append(">\n");

        xml.append(prefix).append("</").append(CONNECTION_DEFINITION_TAG).append(">").append("\n");

        return xml.toString();
    }
}
