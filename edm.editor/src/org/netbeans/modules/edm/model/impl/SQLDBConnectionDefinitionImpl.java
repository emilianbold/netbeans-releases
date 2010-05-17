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
package org.netbeans.modules.edm.model.impl;

import org.netbeans.modules.edm.editor.utils.SQLDBConnectionDefinition;
import org.w3c.dom.Element;

import org.netbeans.modules.edm.model.EDMException;
import org.netbeans.modules.edm.editor.utils.DBConnectionParameters;
import org.netbeans.modules.edm.editor.utils.StringUtil;
import org.netbeans.modules.edm.model.DBConnectionDefinition;
import org.openide.util.NbBundle;

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
            throw new IllegalArgumentException(NbBundle.getMessage(SQLDBConnectionDefinitionImpl.class, "ERROR_null_DBConnectionDefinition"));
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
     * @throws EDMException if error occurs while parsing
     */
    public SQLDBConnectionDefinitionImpl(Element theElement) throws EDMException {
        super(theElement);
    }

    public SQLDBConnectionDefinitionImpl(SQLDBConnectionDefinition connectionDefn) {
        this();

        if (connectionDefn == null) {
            throw new IllegalArgumentException(NbBundle.getMessage(SQLDBConnectionDefinitionImpl.class, "ERROR_null_DBConnectionDefinition"));
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
    
    @Override
    public void setConnectionURL(String url){
        super.setConnectionURL(url);
    }
    
    @Override
    public void setName(String name){
        if(name.length() > 60) {
            super.setName(name.substring(name.length() - 60));
        } else {
            super.setName(name);
        }
    }
    
    /**
     * Creates a clone of this SQLDBConnectionDefinitionImpl object.
     * 
     * @return clone of this object
     */
    @Override
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
            throw new IllegalArgumentException(NbBundle.getMessage(SQLDBConnectionDefinitionImpl.class, "ERROR_null_ref_for_source"));
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
            throw new IllegalArgumentException(NbBundle.getMessage(SQLDBConnectionDefinitionImpl.class, "ERROR_null_ref_for_source"));
        } else if (source == this) {
            return;
        }
        this.copyFrom((DBConnectionDefinition) source);
    }

    /**
     * Doesn't take table name into consideration.
     * 
     * @param refObj SQLColumn to be compared.
     * @return true if the object is identical. false if it is not.
     */
    @Override
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
    
    @Override
    public int hashCode(){
        return super.hashCode();
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
            identical = StringUtil.isIdentical(jdbcUrl, def.getConnectionURL()) 
                && StringUtil.isIdentical(userName, def.getUserName())
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
            identical = StringUtil.isIdentical(jdbcUrl, def.getConnectionURL()) 
                && StringUtil.isIdentical(userName, def.getUserName())
                && StringUtil.isIdentical(password, def.getPassword());
        }

        return identical;
    }
}

