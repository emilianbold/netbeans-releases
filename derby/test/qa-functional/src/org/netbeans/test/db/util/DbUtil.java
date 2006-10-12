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
import java.util.Properties;
import org.netbeans.modules.derby.DbURLClassLoader;

/**
 *
 * @author luke
 */
public class DbUtil {
    public static final String DRIVER_CLASS_NAME = "org.apache.derby.jdbc.ClientDriver";
    private String location;
    
    public DbUtil(String location){
        this.location=location;
    }
    
    private File getDerbyFile(String relPath) {
        return new File(location, relPath);
    }
    
    private URL[] getDerbyNetDriverURLs() throws MalformedURLException {
        URL[] driverURLs = new URL[1];
        driverURLs[0] = getDerbyFile("lib/derbyclient.jar").toURI().toURL();
        return driverURLs;
    }
    
    private Driver getDerbyNetDriver() throws Exception {
        URL[] driverURLs = getDerbyNetDriverURLs();
        DbURLClassLoader l = new DbURLClassLoader(driverURLs);
        Class c = Class.forName(DRIVER_CLASS_NAME, true, l);
        return (Driver)c.newInstance();
    }
    
    public Connection createConnection(String url) throws Exception{
        Driver driver=getDerbyNetDriver();
        Properties p=new Properties();
        Connection con=driver.connect(url,new Properties());
        return con;
    }
    
}
