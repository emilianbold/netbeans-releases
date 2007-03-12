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

package org.netbeans.modules.db.sql.execute.ui.util;



import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.util.*;
import org.netbeans.junit.Manager;

/**
 *
 * @author luke
 */


public class DbUtil {
    
    public static  String DRIVER_CLASS_NAME="driver_class_name";
    public static String URL="url";
    public static String USER="user";
    public static String PASSWORD="password";
    
    public static Connection createConnection(Properties p,File[] f) throws Exception{
        String driver_name=p.getProperty(DRIVER_CLASS_NAME);
        String url=p.getProperty(URL);
        String user=p.getProperty(USER);
        String passwd=p.getProperty(PASSWORD);
        ArrayList list=new java.util.ArrayList();
        for(int i=0;i<f.length;i++){
            list.add(f[i].toURI().toURL());
        }
        URL[] driverURLs=(URL[])list.toArray(new URL[0]);
        URLClassLoader l = new URLClassLoader(driverURLs);
        Class c = Class.forName(driver_name, true, l);
        Driver driver=(Driver)c.newInstance();
        Connection con=driver.connect(url,p);
        return con;
    }
    
    
    
    
}

