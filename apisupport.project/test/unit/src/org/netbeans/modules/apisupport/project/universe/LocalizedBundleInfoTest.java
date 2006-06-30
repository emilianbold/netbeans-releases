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
        NbModuleProject p = generateStandaloneModule("module1");
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
