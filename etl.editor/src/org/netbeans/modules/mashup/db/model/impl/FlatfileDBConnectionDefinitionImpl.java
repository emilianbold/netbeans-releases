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

import com.sun.sql.framework.jdbc.DBConnectionParameters;
import java.util.Map;
import org.netbeans.modules.mashup.db.model.FlatfileDBConnectionDefinition;

import org.netbeans.modules.sql.framework.common.utils.TagParserUtility;
import org.netbeans.modules.sql.framework.model.DBConnectionDefinition;
import org.w3c.dom.Element;


/**
 * Implements FlatfileDBConnectionDefinition interface for Flatfile.
 * 
 * @author Jonathan Giron
 * @author Girish Patil
 * @version $Revision$
 */
public class FlatfileDBConnectionDefinitionImpl extends DBConnectionParameters implements FlatfileDBConnectionDefinition {
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

    /* JDBC URL */
    private String url;


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
    public FlatfileDBConnectionDefinitionImpl(DBConnectionDefinition connectionDefn) {
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
    @Override
    public String getConnectionURL() {
        return url;
    }

    @Override
    public void setConnectionURL(String aUrl) {
        url = aUrl;
    }

    public String getUrl() {
        return this.url;
    }

    /**
     * @see org.netbeans.modules.model.database.DBConnectionDefinition#getDBType
     */
    @Override
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
    public synchronized void copyFrom(DBConnectionDefinition source) {
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
    @Override
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
    @Override
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

    @Override
    public void parseXML(Element xmlElement) {
        Map attrs = TagParserUtility.getNodeAttributes(xmlElement);

        this.name = (String) attrs.get(ATTR_NAME);
        this.driverClass = (String) attrs.get(ATTR_DRIVER_CLASS);
        this.url = (String) attrs.get(ATTR_URL);
        this.userName = (String) attrs.get(ATTR_USER_NAME);
        this.password = (String) attrs.get(ATTR_PASSWORD);
    }

    @Override
    public String toXMLString(String prefix) {
        StringBuilder sb = new StringBuilder();
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

