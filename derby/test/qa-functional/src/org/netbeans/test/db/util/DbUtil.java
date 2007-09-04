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

package org.netbeans.test.db.util;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import org.netbeans.modules.derby.DbURLClassLoader;
import org.netbeans.modules.derby.DerbyOptions;
import org.openide.util.Exceptions;

/**
 *
 * @author luke
 */
public class DbUtil {
    public static final String DRIVER_CLASS_NAME = "org.apache.derby.jdbc.ClientDriver";
    
    public static Connection createDerbyConnection(String dbURL) {
        // Derby Installation folder
        String location = DerbyOptions.getDefault().getLocation();
        File clientJar = new File(location, "lib/derbyclient.jar");
        Connection con = null;
        try {
            System.out.println("> Creating Derby connection using: "+clientJar.toURL());
            URL[] driverURLs = new URL[]{clientJar.toURL()};
            DbURLClassLoader loader = new DbURLClassLoader(driverURLs);
            Driver driver = (Driver) Class.forName(DRIVER_CLASS_NAME, true, loader).newInstance();
            con = driver.connect(dbURL, null);
        } catch (MalformedURLException ex) {
            Exceptions.attachMessage(ex, "Cannot convert to URL: "+clientJar);
            Exceptions.printStackTrace(ex);
        } catch (SQLException ex) {
            Exceptions.attachMessage(ex, "Cannot conect to: "+dbURL);
            Exceptions.printStackTrace(ex);
        } catch (InstantiationException ex) {
            Exceptions.attachMessage(ex, "Cannot instantiate: "+DRIVER_CLASS_NAME+" from: "+clientJar);
            Exceptions.printStackTrace(ex);
        } catch (IllegalAccessException ex) {
            Exceptions.attachMessage(ex, "Cannot instantiate: "+DRIVER_CLASS_NAME+" from: "+clientJar);
            Exceptions.printStackTrace(ex);
        } catch (ClassNotFoundException ex) {
            Exceptions.attachMessage(ex, "Cannot obtain: "+DRIVER_CLASS_NAME+" from: "+clientJar);
            Exceptions.printStackTrace(ex);
        }
        return con;
    }
    
}
