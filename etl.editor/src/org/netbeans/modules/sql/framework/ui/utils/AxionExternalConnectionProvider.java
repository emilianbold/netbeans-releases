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
package org.netbeans.modules.sql.framework.ui.utils;

import java.sql.Connection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import org.axiondb.AxionException;
import org.axiondb.ExternalConnectionProvider;

import com.sun.sql.framework.jdbc.DBConnectionFactory;

/**
 * @author gpatil
 * @version Revision
 */
public class AxionExternalConnectionProvider implements ExternalConnectionProvider {
    public static final String PROP_DBTYPE = "DBNAME";

    private static Map AXION_TO_ETL_PROPERTY_MAP;
    static {
        Map tmpMap = new HashMap();
        tmpMap.put(ExternalConnectionProvider.PROP_JDBCURL, DBConnectionFactory.PROP_URL);
        tmpMap.put(ExternalConnectionProvider.PROP_USERNAME, DBConnectionFactory.PROP_USERNAME);
        tmpMap.put(ExternalConnectionProvider.PROP_PASSWORD, DBConnectionFactory.PROP_PASSWORD);
        tmpMap.put(ExternalConnectionProvider.PROP_DRIVERCLASS, DBConnectionFactory.PROP_DRIVERCLASS);
        tmpMap.put(DBConnectionFactory.PROP_OTD_PATH.toUpperCase(), DBConnectionFactory.PROP_OTD_PATH);
        tmpMap.put(PROP_DBTYPE, DBConnectionFactory.PROP_DBTYPE);

        AXION_TO_ETL_PROPERTY_MAP = Collections.unmodifiableMap(tmpMap);
    }

    /**
     * Keep the Constructor public, as Axion will try to instantiate, no-arg constructor.
     */
    public AxionExternalConnectionProvider() {
        super();
    }

    private Properties convertAxionProperties(Properties spec) {
        Properties prop = new Properties();
        if (AXION_TO_ETL_PROPERTY_MAP != null) {
            Iterator itr = AXION_TO_ETL_PROPERTY_MAP.keySet().iterator();
            String axionKey = null;
            String eTLKey = null;
            String val = null;
            while (itr.hasNext()) {
                axionKey = (String) itr.next();
                val = spec.getProperty(axionKey);
                if (val != null) {
                    eTLKey = (String) AXION_TO_ETL_PROPERTY_MAP.get(axionKey);
                    prop.setProperty(eTLKey, val);
                }
            }
        }

        return prop;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.axiondb.ExternalConnectionProvider#getConnection(java.util.Properties)
     */
    public Connection getConnection(Properties spec) throws AxionException {
        Properties prop = convertAxionProperties(spec);
        DBConnectionFactory dbcf = DBConnectionFactory.getInstance();
        try {
            return dbcf.getConnection(prop);
        } catch (Exception ex) {
            throw new AxionException(ex.getMessage());
        }
    }
}
