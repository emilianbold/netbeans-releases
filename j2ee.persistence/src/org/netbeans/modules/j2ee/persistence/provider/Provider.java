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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.j2ee.persistence.provider;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import org.netbeans.api.db.explorer.DatabaseConnection;
import org.netbeans.modules.j2ee.persistence.dd.persistence.model_1_0.Property;

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
    public final Property getTableGenerationProperty(String strategy){
        if ("".equals(getTableGenerationPropertyName())){
            // provider doesn't support table generation
            return null;
        }
        Property result = new Property();
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
    public abstract String getJdbcUrl();
    
    /**
     * @return name of the property representing JDBC driver.
     */
    public abstract String getJdbcDriver();
    
    /**
     * @return name of the property representing JDBC user name.
     */
    public abstract String getJdbcUsername();
    
    /**
     * @return name of the property representing JDBC password.
     */
    public abstract String getJdbcPassword();
    
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
