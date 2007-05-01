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
package org.netbeans.modules.sql.framework.model.impl;

import org.netbeans.modules.model.database.DBConnectionDefinition;
import org.netbeans.modules.sql.framework.common.jdbc.SQLDBConnectionDefinition;
import org.w3c.dom.Element;

import com.sun.sql.framework.exception.BaseException;
import com.sun.sql.framework.jdbc.DBConnectionParameters;
import com.sun.sql.framework.utils.StringUtil;

/**
 * This class implements DBConnectionDefnition
 * 
 * @version $Revision$
 * @author Sudhi Seshachala
 * @author Jonathan Giron
 */
public class SQLDBConnectionDefinitionImpl extends DBConnectionParameters implements Cloneable, Comparable, SQLDBConnectionDefinition {

    /**
     * SQLDBConnectionDefinitionImpl Constructor is used for the potential collection of
     * DBConnectionDefinitions that might be parsed from a given file.
     */
    public SQLDBConnectionDefinitionImpl() {
        super();
    }

    /**
     * Constructs an instance of SQLDBConnectionDefinitionImpl, copying the contents of
     * the given DBConnectionDefinition implementation.
     * 
     * @param connectionDefn DBConnectionDefinition implementation whose contents will be
     *        copied.
     */
    public SQLDBConnectionDefinitionImpl(DBConnectionDefinition connectionDefn) {
        this();

        if (connectionDefn == null) {
            throw new IllegalArgumentException("Must supply non-null DBConnectionDefinition instance for src param.");
        }

        if (connectionDefn instanceof SQLDBConnectionDefinition) {
            copyFrom((SQLDBConnectionDefinition) connectionDefn);
        } else {
            copyFrom(connectionDefn);
        }
    }

    /**
     * Constructs an instance of SQLDBConnectionDefinitionImpl using the information
     * contained in the given XML element.
     * 
     * @param theElement DOM element containing XML representation of this new
     *        SQLDBConnectionDefinitionImpl instance
     * @throws BaseException if error occurs while parsing
     */
    public SQLDBConnectionDefinitionImpl(Element theElement) throws BaseException {
        super(theElement);
    }

    public SQLDBConnectionDefinitionImpl(SQLDBConnectionDefinition connectionDefn) {
        this();

        if (connectionDefn == null) {
            throw new IllegalArgumentException("Must supply non-null DBConnectionDefinition instance for src param.");
        }

        copyFrom(connectionDefn);
    }
    
    SQLDBConnectionDefinitionImpl( String name,
                                   String dbType,
                                   String driverClass, 
                                   String url,
                                   String user,
                                   String password,
                                   String description) {
        
        setName(name);
        setDBType(dbType);
        setDriverClass(driverClass);
        setConnectionURL(url);
        setUserName(user);
        setPassword(password);
        setDescription(description);
    }
    
    /**
     * Creates a clone of this SQLDBConnectionDefinitionImpl object.
     * 
     * @return clone of this object
     */
    public Object clone() {
        try {
            return super.clone();
        } catch (Exception e) {
            throw new InternalError(e.toString());
        }
    }
    
    public Object cloneObject() {
        return clone();
    }

    /**
     * Copies member values to those contained in the given DBConnectionDefinition
     * instance. Does shallow copy of properties and flatfiles collections.
     * 
     * @param source DBConnectionDefinition whose contents are to be copied into this
     *        instance
     */
    public synchronized void copyFrom(DBConnectionDefinition source) {
        if (source == null) {
            throw new IllegalArgumentException("Must supply non-null ref for source.");
        } else if (source == this) {
            return;
        }

        // Use accessors rather than direct assignment because they handle
        // pathological values.
        setDescription(source.getDescription());
        setName(source.getName());
        setDBType(source.getDBType());
        setDriverClass(source.getDriverClass());
        setConnectionURL(source.getConnectionURL());
        setUserName(source.getUserName());
        setPassword(source.getPassword());
    }

    /**
     * Copies member values to those contained in the given SQLDBConnectionDefinition
     * instance. Does shallow copy of properties and flatfiles collections.
     * 
     * @param source SQLDBConnectionDefinition whose contents are to be copied into this
     *        instance
     */
    public synchronized void copyFrom(SQLDBConnectionDefinition source) {
        if (source == null) {
            throw new IllegalArgumentException("Must supply non-null ref for source.");
        } else if (source == this) {
            return;
        }
        this.copyFrom((DBConnectionDefinition) source);
        setOTDPathName(source.getOTDPathName());
        setJNDIPath(source.getJNDIPath());
    }

    /**
     * Doesn't take table name into consideration.
     * 
     * @param refObj SQLColumn to be compared.
     * @return true if the object is identical. false if it is not.
     */
    public boolean equals(Object refObj) {
        if (!(refObj instanceof SQLDBConnectionDefinitionImpl)) {
            return false;
        }

        SQLDBConnectionDefinitionImpl defn = (SQLDBConnectionDefinitionImpl) refObj;

        boolean result = (name != null) ? name.equals(defn.name) : (defn.name == null);
        result &= (dbType != null) ? dbType.equals(defn.dbType) : (defn.dbType == null);
        result &= (driverClass != null) ? this.driverClass.equals(defn.driverClass) : (defn.driverClass == null);
        result &= (jdbcUrl != null) ? jdbcUrl.equals(defn.jdbcUrl) : (defn.jdbcUrl == null);
        result &= (userName != null) ? userName.equals(defn.userName) : (defn.userName == null);
        result &= (password != null) ? this.password.equals(defn.password) : (defn.password == null);

        return result;
    }

    /**
     * Indicates whether contents of given DBConnectionDefinition implementer are
     * identical to this SQLDBConnectionDefinitionImpl object.
     * 
     * @param def DBConnectionDefinition implementer to compare against
     * @return true if contents are identical; false otherwise
     */
    public boolean isIdentical(DBConnectionDefinition def) {
        boolean identical = false;

        if (def != null) {
            identical = StringUtil.isIdentical(jdbcUrl, def.getConnectionURL()) && StringUtil.isIdentical(userName, def.getUserName())
                && StringUtil.isIdentical(password, def.getPassword());
        }

        return identical;
    }

    /**
     * Indicates whether contents of given DBConnectionDefinition implementer are
     * identical to this SQLDBConnectionDefinitionImpl object.
     * 
     * @param def DBConnectionDefinition implementer to compare against
     * @return true if contents are identical; false otherwise
     */
    public boolean isIdentical(SQLDBConnectionDefinition def) {
        boolean identical = false;

        if (def != null) {
            identical = StringUtil.isIdentical(jdbcUrl, def.getConnectionURL()) && StringUtil.isIdentical(userName, def.getUserName())
                && StringUtil.isIdentical(password, def.getPassword()) && StringUtil.isIdentical(otdPathName, def.getOTDPathName(), true)
                && StringUtil.isIdentical(dsJndiPath, def.getJNDIPath(), true);
        }

        return identical;
    }
}

