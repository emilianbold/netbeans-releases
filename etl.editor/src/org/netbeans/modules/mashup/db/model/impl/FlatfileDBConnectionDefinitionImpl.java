/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 * 
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 * 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 * 
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */
package org.netbeans.modules.mashup.db.model.impl;

import java.util.Map;

import org.netbeans.modules.mashup.db.model.FlatfileDBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.w3c.dom.Element;


/**
 * Implements FlatfileDBConnectionDefinition interface for Flatfile.
 * 
 * @author Jonathan Giron
 * @author Girish Patil
 * @version $Revision$
 */
public class FlatfileDBConnectionDefinitionImpl implements FlatfileDBConnectionDefinition {
    /** Constants used in XML tags * */
    private static final String ATTR_DRIVER_CLASS = "driverClass";
    private static final String ATTR_NAME = "name";
    private static final String ATTR_PASSWORD = "password";
    private static final String ATTR_URL = "url";
    private static final String ATTR_USER_NAME = "userName";
    private static final String END_QUOTE_SPACE = "\" ";
    private static final String EQUAL_START_QUOTE = "=\"";
    private static final String TAG_CONNECTION_DEFINITION = "connectionDefinition";

    /* JDBC driver class name for Axion */
    private static final String AXION_DRIVER = "org.axiondb.jdbc.AxionDriver";

    /* User-supplied connection name */
    private String name;

    /* JDBC driver name */
    private String driverClass;

    /* JDBC URL */
    private String url;

    /* Username for authentication */
    private String userName;

    /* Password for authentication */
    private String password;

    /* User-supplied description (optional) for this connection */
    private String description;

    /** Creates a new default instance of FlatfileDBConnectionDefinitionImpl. */
    public FlatfileDBConnectionDefinitionImpl() {
        userName = "sa";
        password = "sa";
        driverClass = AXION_DRIVER; 
    }

    /** Creates a new default instance of FlatfileDBConnectionDefinitionImpl. */
    public FlatfileDBConnectionDefinitionImpl(String connName) {
        name = connName;
        userName = "sa";
        password = "sa";
        driverClass = AXION_DRIVER;
        description = connName;
    }

    /**
     * Creates a new instance of FlatfileDBConnectionDefinitionImpl with the given
     * attributes.
     * 
     * @param connName connection name
     * @param driverName driver name
     * @param connUrl JDBC URL for this connection
     * @param uname username used to establish connection
     * @param passwd password used to establish connection
     * @param desc description of connection
     */
    public FlatfileDBConnectionDefinitionImpl(String connName, String driverName, String connUrl, String uname, String passwd, String desc) {
        name = connName;

        driverClass = driverName;
        url = connUrl;
        userName = uname;
        password = passwd;
        description = desc;
    }

    /**
     * Creates a new instance of FlatfileDBConnectionDefinitionImpl using the values in
     * the given FlatfileDBConnectionDefinition.
     * 
     * @param connectionDefn DBConnectionDefinition to be copied
     */
    public FlatfileDBConnectionDefinitionImpl(FlatfileDBConnectionDefinition connectionDefn) {
        if (connectionDefn == null) {
            throw new IllegalArgumentException("Must supply non-null DBConnectionDefinition instance for connectionDefn param.");
        }

        if (connectionDefn instanceof FlatfileDBConnectionDefinitionImpl) {
            copyFrom((FlatfileDBConnectionDefinitionImpl) connectionDefn);
        }
    }

    /**
     * @see org.netbeans.modules.model.database.DBConnectionDefinition#getConnectionURL()
     */
    public String getConnectionURL() {
        return url;
    }

    public void setConnectionURL(String aUrl) {
        url = aUrl;
    }

    /**
     * Gets user-defined description, if any, for this DBConnectionDefinition.
     * 
     * @return user-defined description, possibly null if none was defined
     */
    public String getDescription() {
        return description;
    }

    public String getUrl() {
        return this.url;
    }

    /**
     * @see org.netbeans.modules.model.database.DBConnectionDefinition#getDriverClass
     */
    public String getDriverClass() {
        return AXION_DRIVER;
    }

    /**
     * @see org.netbeans.modules.model.database.DBConnectionDefinition#getName
     */
    public String getName() {
        return name;
    }

    /**
     * Sets new name for this DBConnectionDefinition.
     * 
     * @param newName new name for DBConnectionDefinition
     */
    public void setName(String newName) {
        name = newName;
    }

    /**
     * @see org.netbeans.modules.model.database.DBConnectionDefinition#getUserName
     */
    public String getUserName() {
        return userName;
    }

    /**
     * @see org.netbeans.modules.model.database.DBConnectionDefinition#getPassword
     */
    public String getPassword() {
        return password;
    }

    /**
     * @see org.netbeans.modules.model.database.DBConnectionDefinition#getDBType
     */
    public String getDBType() {
        return "Internal";
    }

    /**
     * Copies member values to those contained in the given DBConnectionDefinition
     * instance. Does shallow copy of properties and flatfile collections.
     * 
     * @param source DBConnectionDefinition whose contents are to be copied into this
     *        instance
     */
    public synchronized void copyFrom(FlatfileDBConnectionDefinitionImpl source) {
        if (source == null) {
            throw new IllegalArgumentException("Must supply non-null ref for source.");
        } else if (source == this) {
            return;
        }

        this.description = source.getDescription();
        this.name = source.getName();
        this.driverClass = source.getDriverClass();
        this.url = source.getConnectionURL();
        this.userName = source.getUserName();
        this.password = source.getPassword();
    }

    /**
     * Overrides default implementation.
     * 
     * @param o Object to compare for equality against this instance.
     * @return true if o is equivalent to this, false otherwise
     */
    public boolean equals(Object o) {
        // Check for reflexivity.
        if (this == o) {
            return true;
        } else if (!(o instanceof FlatfileDBConnectionDefinitionImpl)) {
            return false;
        }

        boolean response = true;
        FlatfileDBConnectionDefinitionImpl impl = (FlatfileDBConnectionDefinitionImpl) o;

        boolean nameEqual = (name != null) ? name.equals(impl.name) : (impl.name == null);
        response &= nameEqual;

        boolean driverClassEqual = (driverClass != null) ? driverClass.equals(impl.driverClass) : (impl.driverClass == null);
        response &= driverClassEqual;

        boolean urlEqual = (url != null) ? url.equals(impl.url) : (impl.url == null);
        response &= urlEqual;

        boolean userNameEqual = (userName != null) ? userName.equals(impl.userName) : (impl.userName == null);
        response &= userNameEqual;

        boolean passwordEqual = (password != null) ? password.equals(impl.password) : (impl.password == null);
        response &= passwordEqual;

        boolean descEqual = (description != null) ? description.equals(impl.description) : (impl.description == null);
        response &= descEqual;

        return response;
    }

    /**
     * Overrides default implementation to compute its value based on member variables.
     * 
     * @return computed hash code
     */
    public int hashCode() {
        int hashCode = 0;

        hashCode += (name != null) ? name.hashCode() : 0;
        hashCode += (driverClass != null) ? driverClass.hashCode() : 0;
        hashCode += (url != null) ? url.hashCode() : 0;
        hashCode += (userName != null) ? userName.hashCode() : 0;
        hashCode += (password != null) ? password.hashCode() : 0;
        hashCode += (description != null) ? description.hashCode() : 0;

        return hashCode;
    }

    public void parseXML(Element xmlElement) {
        Map attrs = TagParserUtility.getNodeAttributes(xmlElement);

        this.name = (String) attrs.get(ATTR_NAME);
        this.driverClass = (String) attrs.get(ATTR_DRIVER_CLASS);
        this.url = (String) attrs.get(ATTR_URL);
        this.userName = (String) attrs.get(ATTR_USER_NAME);
        this.password = (String) attrs.get(ATTR_PASSWORD);
    }

    public String toXMLString(String prefix) {
        StringBuffer sb = new StringBuffer();
        sb.append(prefix);
        sb.append("<");
        sb.append(TAG_CONNECTION_DEFINITION);
        sb.append(" ");
        sb.append(ATTR_NAME);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.getName());
        sb.append(END_QUOTE_SPACE);
        sb.append(ATTR_DRIVER_CLASS);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.getDriverClass());
        sb.append(END_QUOTE_SPACE);
        sb.append(ATTR_URL);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.getUrl());
        sb.append(END_QUOTE_SPACE);
        sb.append(ATTR_USER_NAME);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.getUserName());
        sb.append(END_QUOTE_SPACE);
        sb.append(ATTR_PASSWORD);
        sb.append(EQUAL_START_QUOTE);
        sb.append(this.getPassword());
        sb.append("\"/>\n");
        return sb.toString();
    }
}

