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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
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

package org.netbeans.modules.j2ee.persistence.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.j2ee.persistence.dd.common.Persistence;
import org.netbeans.modules.j2ee.persistence.dd.common.Property;

/**
 * This class represents a persistence provider.
 *
 * @author Erno Mononen
 */
public abstract class Provider {
    
    // constants for properties
    public static final String TABLE_GENERATION_CREATE = "tableGenerationCreate";
    public static final String TABLE_GENERATION_DROPCREATE = "tableGenerationDropCreate";
    public static final String TABLE_GENERATTION_UNKOWN = "tableGenerationUnknown";
    
    
    /**
     * Fully qualified class name of the provider.
     */
    private final String providerClass;
    
    private final Set vendorSpecificProperties;
    
    
    /**
     * Creates a new instance of Provider
     */
    protected Provider(String providerClass) {
        assert !(null == providerClass || "".equals(providerClass.trim())) : "Provider class must be given!";
        this.providerClass = providerClass;
        this.vendorSpecificProperties = initPropertyNames();
    }
    
    public abstract String getDisplayName();
    
    /**
     * @see #providerClass
     */
    public final String getProviderClass(){
        return this.providerClass;
    }
    
    private Set initPropertyNames(){
        Set result = new HashSet();
        result.add(getJdbcDriver());
        result.add(getJdbcUsername());
        result.add(getJdbcUrl());
        result.add(getJdbcPassword());
        result.add(getTableGenerationPropertyName());
        for (Iterator it = getUnresolvedVendorSpecificProperties().keySet().iterator(); it.hasNext();) {
            String propertyName = (String) it.next();
            result.add(propertyName);
        }
        return result;
    }
    
    /**
     * Gets the names of all provider specific properties.
     * @return Set of Strings representing names of provider specific properties.
     */
    public Set getPropertyNames(){
        return this.vendorSpecificProperties;
    }
    
    /**
     * @return the property that represents table generation strategy.
     */
    public final Property getTableGenerationProperty(String strategy, String version){
        if ("".equals(getTableGenerationPropertyName())){
            // provider doesn't support table generation
            return null;
        }
        Property result = Persistence.VERSION_2_0.equals(version) ? new org.netbeans.modules.j2ee.persistence.dd.persistence.model_2_0.Property() : new org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Property();
        result.setName(getTableGenerationPropertyName());
        if (TABLE_GENERATION_CREATE.equals(strategy)){
            result.setValue(getTableGenerationCreateValue());
        } else if (TABLE_GENERATION_DROPCREATE.equals(strategy)){
            result.setValue(getTableGenerationDropCreateValue());
        } else {
            return null;
        }
        return result;
    }
    
    /**
     * @return name of the default datasource.
     */
    public final String getDefaultJtaDatasource(){
        return "jdbc/__default";
    }
    
    /**
     * @return name of the property representing JDBC URL.
     */
    public String getJdbcUrl() {
        return "javax.persistence.jdbc.url";
    }
    
    /**
     * @return name of the property representing JDBC driver.
     */
    public String getJdbcDriver() {
        return "javax.persistence.jdbc.driver";
    }
    
    /**
     * @return name of the property representing JDBC user name.
     */
    public String getJdbcUsername() {
        return "javax.persistence.jdbc.user";
    }
    
    /**
     * @return name of the property representing JDBC password.
     */
    public String getJdbcPassword() {
        return "javax.persistence.jdbc.password";
    }
    
    /**
     * @return name of the property representing table generation strategy.
     */
    public abstract String getTableGenerationPropertyName();
    
    /**
     * @return value of the property that represents <tt>create tables</tt> strategy.
     */
    public abstract String getTableGenerationCreateValue();
    
    /**
     * @return value of the property that represents <tt>create and drop tables</tt> strategy.
     */
    public abstract String getTableGenerationDropCreateValue();
    
    /**
     * @return Map<String, String> containing vendor specific properties.
     */
    public abstract Map getUnresolvedVendorSpecificProperties();
    
    /**
     * @return Map<String, String> containing vendor specific properties
     * which should be set on a new unit by default.
     */
    public abstract Map getDefaultVendorSpecificProperties();
    
    /**
     * Gets a map containing provider specific name / values pairs of given
     * database connection's properties. If given connection was null, will
     * return a map containing keys (names) of properties but empty Strings as values.
     * @param connection
     * @return Map (key String representing name of the property, value String
     *  representing value of the property).
     */
    public final Map<String, String> getConnectionPropertiesMap(DatabaseConnection connection){
        Map<String, String> result = new HashMap<String, String>();
        result.put(getJdbcDriver(), connection != null ? connection.getDriverClass() : "");
        result.put(getJdbcUrl(), connection != null ? connection.getDatabaseURL() : "");
        result.put(getJdbcUsername(), connection != null ? connection.getUser(): "");
        // must set an empty string for password if a null password
        // was returned from the connection, see #81729
        result.put(getJdbcPassword(), 
                connection != null && connection.getPassword() != null ? connection.getPassword() : "");
        return result;
    }
    
    /**
     * @return true if this provider support table generation, false otherwise.
     */
    public final boolean supportsTableGeneration(){
        return getTableGenerationPropertyName() != null && !"".equals(getTableGenerationPropertyName().trim());
    }
    
    public String toString() {
        return getDisplayName();
    }
    
    public int hashCode() {
        return providerClass.hashCode();
    }
    
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof Provider)){
            return false;
        }
        Provider that = (Provider) obj;
        return getClass().equals(that.getClass()) && providerClass.equals(that.providerClass);
    }
    
    
}
