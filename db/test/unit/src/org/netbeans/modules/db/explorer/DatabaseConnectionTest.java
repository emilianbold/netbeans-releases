/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2004 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.db.explorer;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import org.netbeans.modules.db.test.TestBase;

/**
 *
 * @author Andrei Badea
 */
public class DatabaseConnectionTest extends TestBase {
    
    public DatabaseConnectionTest(String testName) {
        super(testName);
    }
    
    public void testPropertyChange() {
        
        DatabaseConnection dbconn = new DatabaseConnection();
        
        MyPCL pcl = new MyPCL();
        dbconn.addPropertyChangeListener(pcl);
        
        dbconn.setDriver("driver");
        dbconn.setDatabase("database");
        dbconn.setSchema("schema");
        dbconn.setUser("user");
        
        assertTrue("Not all the property changes were fired", pcl.fired >= 4);
    }
    
    private final class MyPCL implements PropertyChangeListener {
        int fired = 0;
        
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getPropertyName().equals(DatabaseConnection.PROP_DRIVER)) {
                fired++;
                assertEquals("driver", evt.getNewValue());
            } else if (evt.getPropertyName().equals(DatabaseConnection.PROP_DATABASE)) {
                fired++;
                assertEquals("database", evt.getNewValue());
            } else if (evt.getPropertyName().equals(DatabaseConnection.PROP_SCHEMA)) {
                fired++;
                assertEquals("schema", evt.getNewValue());
            } else if (evt.getPropertyName().equals(DatabaseConnection.PROP_USER)) {
                fired++;
                assertEquals("user", evt.getNewValue());
            }
        }
    }
}
