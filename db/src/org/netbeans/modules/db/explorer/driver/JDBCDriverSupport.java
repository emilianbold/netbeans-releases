/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer.driver;

import java.io.File;
import java.net.URL;
import org.netbeans.api.db.explorer.JDBCDriver;

/**
 * A helper class for working with JDBC Drivers.
 *
 * @author Andrei Badea
 */
public final class JDBCDriverSupport {
    
    private JDBCDriverSupport() {
    }
    
    /**
     * Return if the driver file(s) exists and can be loaded.
     * @return true if defined driver file(s) exists; otherwise false
     */
    public static boolean isAvailable(JDBCDriver driver) {
        URL[] urls = driver.getURLs();
        for (int i = 0; i < urls.length; i++) {
            File f = new File(urls[i].getFile());
            if (!f.exists())
                return false;
        }
        return true;
    }
}
