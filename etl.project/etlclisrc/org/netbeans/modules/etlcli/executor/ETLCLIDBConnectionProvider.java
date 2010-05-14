/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2003-2007 Sun Microsystems, Inc. All Rights Reserved.
 *
 * The contents of this file are subject to the terms of the Common 
 * Development and Distribution License ("CDDL")(the "License"). You 
 * may not use this file except in compliance with the License.
 *
 * You can obtain a copy of the License at
 * https://open-dm-mi.dev.java.net/cddl.html
 * or open-dm-mi/bootstrap/legal/license.txt. See the License for the 
 * specific language governing permissions and limitations under the  
 * License.  
 *
 * When distributing the Covered Code, include this CDDL Header Notice 
 * in each file and include the License file at
 * open-dm-mi/bootstrap/legal/license.txt.
 * If applicable, add the following below this CDDL Header, with the 
 * fields enclosed by brackets [] replaced by your own identifying 
 * information: "Portions Copyrighted [year] [name of copyright owner]"
 */
package org.netbeans.modules.etlcli.executor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;
import java.util.logging.Logger;
import com.sun.etl.exception.BaseException;
import com.sun.etl.jdbc.DBConnectionFactory;
import com.sun.etl.jdbc.DBConnectionParameters;
import com.sun.etl.engine.spi.DBConnectionProvider;

/**
 * @author Manish Bharani
 *
 */
public class ETLCLIDBConnectionProvider implements DBConnectionProvider {

    private static Logger logger = Logger.getLogger(ETLCLIDBConnectionProvider.class.getName());

    public void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                logger.info(e.getMessage());
            }
        }
    }

    public Connection getConnection(DBConnectionParameters conDef) throws BaseException {

        String jndiref = conDef.getJNDIPath();
        if (jndiref != null) {
            EtlCliPropertiesBuilder parser = EtlCliPropertiesBuilder.getEtlCliParserInstance();
            Connection conn = parser.getCollabConnections(jndiref);
            if (conn != null) {
                System.out.println("Connection Provider resolved conn against jndi ref : " + jndiref);
                try{
                System.out.println("Connection being returned : " + conn.getMetaData().getURL());
                } catch (SQLException e) {
                    logger.severe("Failed to get url from connection. Error : " + e.getMessage());
                    //throw new BaseException(e);
                }                
                return conn;
            }
        }
        return null;
    }

    public Connection getConnection(Properties connProps) throws BaseException {
        String driver = connProps.getProperty(DBConnectionFactory.PROP_DRIVERCLASS);
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {
            logger.info(e.getMessage());
        }
        String username = connProps.getProperty(DBConnectionFactory.PROP_USERNAME);
        String password = connProps.getProperty(DBConnectionFactory.PROP_PASSWORD);
        String url = connProps.getProperty(DBConnectionFactory.PROP_URL);
        try {
            return DriverManager.getConnection(url, username, password);
        } catch (SQLException e) {
            logger.info(e.getMessage());
            logger.severe("Failed to get connection for url:" + url);
            throw new BaseException(e);
        }
    }
}
