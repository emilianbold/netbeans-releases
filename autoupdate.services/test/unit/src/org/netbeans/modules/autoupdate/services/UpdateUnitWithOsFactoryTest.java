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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.autoupdate.services;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import org.netbeans.api.autoupdate.UpdateUnit;
import org.netbeans.spi.autoupdate.UpdateItem;
import org.netbeans.spi.autoupdate.UpdateProvider;
import org.openide.modules.ModuleInfo;
import org.openide.util.Lookup;

/**
 *
 * @author Jiri Rechtacek
 */
public class UpdateUnitWithOsFactoryTest extends NbmAdvancedTestCase {
    
    public UpdateUnitWithOsFactoryTest (String testName) {
        super (testName);
    }
    
    private UpdateProvider p = null;
    private String testModuleName = "org.netbeans.modules.applemenu";
    private String testModuleVersion = "1.111";
    
    @Override
    protected void setUp () throws IOException {
        Lookup.getDefault ().lookup (ModuleInfo.class);
        clearWorkDir ();
    }
    
    public void testUpdateItemsDoesntContainAlien () throws IOException {
        String os = org.openide.util.Utilities.isUnix () ? "Windows" : "Unix";
        Lookup.getDefault ().lookup (ModuleInfo.class);
        String catalog = generateCatalog (
                generateModuleElementWithRequires ("com.sun.collablet", "1.3", null,
                    "org.openide.filesystems > 6.2",
                    "org.openide.util > 6.2",
                    "org.openide.modules > 6.2",
                    "org.openide.nodes > 6.2",
                    "org.openide.loaders",
                    "org.openide.io"),
                generateModuleElementWithRequires (testModuleName, testModuleVersion, "org.openide.modules.os." + os,
                    "org.netbeans.core.windows/2",
                    "org.netbeans.modules.editor/3",
                    "org.netbeans.modules.java.editor/1 > 1.3",
                    "org.openide.filesystems > 6.2",
                    "org.openide.loaders",
                    "org.openide.modules > 6.2",
                    "org.openide.nodes > 6.2",
                    "org.openide.util > 6.2")
                
                );
        p = createUpdateProvider (catalog);
        p.refresh (true);
        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit> ();
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        assertNotNull ("Some modules are installed.", updates);
        assertFalse ("Some modules are installed.", updates.isEmpty ());
        assertTrue (testModuleName + " found in parsed items.", updates.keySet ().contains (testModuleName + "_" + testModuleVersion));
        
        Map<String, UpdateUnit> newImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (unitImpls, p);
        assertNotNull ("Some units found.", newImpls);
        assertFalse ("Some units found.", newImpls.isEmpty ());
        
        assertFalse (testModuleName + " doesn't found in generated UpdateUnits.", newImpls.keySet ().contains (testModuleName));
    }
    
    public void testUpdateItemsContainsMyModule () throws IOException {
        String os = ! org.openide.util.Utilities.isUnix () ? "Windows" : "Unix";
        String catalog = generateCatalog (
                generateModuleElementWithRequires ("com.sun.collablet", "1.3", null,
                    "org.openide.filesystems > 6.2",
                    "org.openide.util > 6.2",
                    "org.openide.modules > 6.2",
                    "org.openide.nodes > 6.2",
                    "org.openide.loaders",
                    "org.openide.io"),
                generateModuleElementWithRequires (testModuleName, testModuleVersion, "org.openide.modules.os." + os,
                    "org.netbeans.core.windows/2",
                    "org.netbeans.modules.editor/3",
                    "org.netbeans.modules.java.editor/1 > 1.3",
                    "org.openide.filesystems > 6.2",
                    "org.openide.loaders",
                    "org.openide.modules > 6.2",
                    "org.openide.nodes > 6.2",
                    "org.openide.util > 6.2")
                
                );
        p = createUpdateProvider (catalog);
        p.refresh (true);
        Map<String, UpdateUnit> unitImpls = new HashMap<String, UpdateUnit> ();
        Map<String, UpdateItem> updates = p.getUpdateItems ();
        assertNotNull ("Some modules are installed.", updates);
        assertFalse ("Some modules are installed.", updates.isEmpty ());
        assertTrue (testModuleName + " found in parsed items.", updates.keySet ().contains (testModuleName + "_" + testModuleVersion));
        
        Map<String, UpdateUnit> newImpls = UpdateUnitFactory.getDefault ().appendUpdateItems (unitImpls, p);
        assertNotNull ("Some units found.", newImpls);
        assertFalse ("Some units found.", newImpls.isEmpty ());
        
        assertTrue (testModuleName + " must found in generated UpdateUnits.", newImpls.keySet ().contains (testModuleName));
    }
    
}
