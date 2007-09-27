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
