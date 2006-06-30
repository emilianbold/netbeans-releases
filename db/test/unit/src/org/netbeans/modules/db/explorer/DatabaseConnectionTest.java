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
