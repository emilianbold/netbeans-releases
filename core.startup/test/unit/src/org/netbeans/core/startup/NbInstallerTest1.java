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

package org.netbeans.core.startup;

/** Test the NetBeans module installer implementation.
 * Broken into pieces to ensure each runs in its own VM.
 * @author Jesse Glick
 */
public class NbInstallerTest1 extends SetupHid {
    
    public NbInstallerTest1(String name) {
        super(name);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        System.setProperty("org.netbeans.core.modules.NbInstaller.noAutoDeps", "true");
        
        System.setProperty("org.netbeans.core.startup.specialResource", "test1,foo.bar2");
    }
    
    /** Tests mainly because of #65244 */
    public void testIsSpecialResource() throws Exception {
        Main.getModuleSystem (); // init module system
        final FakeEvents ev = new FakeEvents();
        org.netbeans.core.startup.NbInstaller installer = new org.netbeans.core.startup.NbInstaller(ev);
        assertTrue("Parts of openide are speical", installer.isSpecialResource("org/openide/windows/"));
        assertTrue("test1 is in the sys property", installer.isSpecialResource("test1"));
        assertFalse("foo.bar is regular", installer.isSpecialResource("foo.bar"));
        assertTrue("foo.bar2 is in the sys property", installer.isSpecialResource("foo.bar2"));
    }

}
