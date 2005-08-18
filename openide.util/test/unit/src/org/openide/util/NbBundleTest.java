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

package org.openide.util;

import java.util.ResourceBundle;
import junit.framework.TestCase;

/**
 * Test normal-mode functionality of {@link NbBundle}.
 * @author Jesse Glick
 */
public class NbBundleTest extends TestCase {
    
    public NbBundleTest(String name) {
        super(name);
    }
    
    public void testNormalMode() {
        doTestReadIntFromBundle(false);
    }
    
    static void doTestReadIntFromBundle(boolean debug) {
        ResourceBundle bundle = NbBundle.getBundle("org.openide.util.NbBundleTest");
        String val_secure = bundle.getString("INT_VALUE_SECURE");
        String val_insecure = bundle.getString("INT_VALUE_INSECURE");
        
        assertNotNull("Value from bundle not null", val_secure);
        assertNotNull("Value from bundle not null", val_insecure);
        
        if (debug) {
            assertTrue ("Values were read in debug mode.", val_insecure.indexOf (':') > 0);
        } else {
            assertFalse ("Values were read in normal mode.", val_insecure.indexOf (':') > 0);
        }
        
        try {
            assertEquals("Parsed as int", 123, Integer.parseInt(val_insecure));
        } catch (NumberFormatException nfe) {
            if (debug) {
                // OK, fine
            } else {
                // Not fine in normal mode!
                throw nfe;
            }
        }
        
        try {
            assertEquals("Parsed as int", 456, Integer.parseInt(val_secure));
        } catch (NumberFormatException nfe) {
            fail("Cannot throw NumberFormatException when read secure value.");
        }
        
    }
    
}
