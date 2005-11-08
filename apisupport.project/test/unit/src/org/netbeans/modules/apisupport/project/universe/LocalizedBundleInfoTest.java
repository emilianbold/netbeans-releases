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

package org.netbeans.modules.apisupport.project.universe;

import org.netbeans.modules.apisupport.project.NbModuleProject;
import org.netbeans.modules.apisupport.project.TestBase;

/**
 * Test functionality of LocalizedBundleInfo.
 *
 * @author Martin Krauskopf
 */
public class LocalizedBundleInfoTest extends TestBase {
    
    public LocalizedBundleInfoTest(String name) {
        super(name);
    }
    
    public void testIsModified() throws Exception {
        NbModuleProject p = TestBase.generateStandaloneModule(getWorkDir(), "module1");
        LocalizedBundleInfo.Provider provider = (LocalizedBundleInfo.Provider) p.getLookup().lookup(LocalizedBundleInfo.Provider.class);
        LocalizedBundleInfo info = provider.getLocalizedBundleInfo();
        assertFalse("just loaded", info.isModified());
        info.setCategory("my new category");
        assertTrue("modified", info.isModified());
        info.setCategory("mistyped category");
        assertTrue("modified", info.isModified());
        info.reload();
        assertFalse("reloaded", info.isModified());
    }
    
}
