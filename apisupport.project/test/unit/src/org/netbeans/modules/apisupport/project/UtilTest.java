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

package org.netbeans.modules.apisupport.project;

/**
 * Tests {@link Util}.
 *
 * @author Martin Krauskopf
 */
public class UtilTest extends TestBase {
    
    public UtilTest(String name) {
        super(name);
    }
    
    public void testDebugFix() throws Exception {
        assertEquals("space test", "spacetest", Util.normalizeCNB("space test"));
        assertEquals("slash test", "slashtest", Util.normalizeCNB("slash\\test"));
        assertEquals("lowercase test", "org.capital.test", Util.normalizeCNB("org.Capital.test"));
        assertEquals("dot-space test", "org.example.package", Util.normalizeCNB("org...example   ... package..."));
        assertEquals("org.example.hmmmm.misc.test339", Util.normalizeCNB("org.example.hmMMm.misc. TEst3*3=9"));
    }
    
}
