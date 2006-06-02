/*
 *                 Sun Public License Notice
 *
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 *
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.openide.util;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import java.util.jar.Attributes;
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
    
    public static void testGetLocalizedValue() throws Exception {
        Map<String,String> m = new HashMap<String,String>();
        m.put("k1", "v1");
        m.put("k1_ja", "v1_ja");
        m.put("k1_ja_JP", "v1_ja_JP");
        m.put("k2", "v2");
        m.put("k3_ja", "v3_ja");
        assertEquals("v1", NbBundle.getLocalizedValue(m, "k1", Locale.ENGLISH));
        assertEquals("v1_ja", NbBundle.getLocalizedValue(m, "k1", Locale.JAPANESE));
        assertEquals("v1_ja_JP", NbBundle.getLocalizedValue(m, "k1", Locale.JAPAN));
        assertEquals("v2", NbBundle.getLocalizedValue(m, "k2", Locale.ENGLISH));
        assertEquals("v2", NbBundle.getLocalizedValue(m, "k2", Locale.JAPANESE));
        assertEquals("v2", NbBundle.getLocalizedValue(m, "k2", Locale.JAPAN));
        assertEquals(null, NbBundle.getLocalizedValue(m, "k3", Locale.ENGLISH));
        assertEquals("v3_ja", NbBundle.getLocalizedValue(m, "k3", Locale.JAPANESE));
        assertEquals("v3_ja", NbBundle.getLocalizedValue(m, "k3", Locale.JAPAN));
        Attributes attr = new Attributes();
        attr.putValue("k1", "v1");
        attr.putValue("k1_ja", "v1_ja");
        attr.putValue("k1_ja_JP", "v1_ja_JP");
        attr.putValue("k2", "v2");
        attr.putValue("k3_ja", "v3_ja");
        assertEquals("v1", NbBundle.getLocalizedValue(attr, new Attributes.Name("k1"), Locale.ENGLISH));
        assertEquals("v1_ja", NbBundle.getLocalizedValue(attr, new Attributes.Name("k1"), Locale.JAPANESE));
        assertEquals("v1_ja_JP", NbBundle.getLocalizedValue(attr, new Attributes.Name("k1"), Locale.JAPAN));
        assertEquals("v2", NbBundle.getLocalizedValue(attr, new Attributes.Name("k2"), Locale.ENGLISH));
        assertEquals("v2", NbBundle.getLocalizedValue(attr, new Attributes.Name("k2"), Locale.JAPANESE));
        assertEquals("v2", NbBundle.getLocalizedValue(attr, new Attributes.Name("k2"), Locale.JAPAN));
        assertEquals(null, NbBundle.getLocalizedValue(attr, new Attributes.Name("k3"), Locale.ENGLISH));
        assertEquals("v3_ja", NbBundle.getLocalizedValue(attr, new Attributes.Name("k3"), Locale.JAPANESE));
        assertEquals("v3_ja", NbBundle.getLocalizedValue(attr, new Attributes.Name("k3"), Locale.JAPAN));
    }
    
}
