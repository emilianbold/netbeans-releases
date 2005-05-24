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
/*
 * LoginConfigEntryTest.java
 * JUnit based test
 *
 * Created on March 18, 2004, 5:04 PM
 */

package org.netbeans.modules.j2ee.sun.share.configbean.customizers;

import java.util.ResourceBundle;
import junit.framework.*;
import org.netbeans.modules.schema2beans.BaseBean;
import org.netbeans.modules.j2ee.sun.common.dd.WebserviceEndpoint;
import org.netbeans.modules.j2ee.sun.common.dd.LoginConfig;
import org.netbeans.modules.j2ee.sun.share.configbean.ServletRef;
import org.netbeans.modules.j2ee.sun.share.configbean.customizers.common.GenericTableModel;

/**
 *
 * @author vkraemer
 */
public class LoginConfigEntryTest extends TestCase {
    public void testCreate() {
        LoginConfigEntry foo =
            new LoginConfigEntry();
    }
    
    public LoginConfigEntryTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(LoginConfigEntryTest.class);
        return suite;
    }
    
}
