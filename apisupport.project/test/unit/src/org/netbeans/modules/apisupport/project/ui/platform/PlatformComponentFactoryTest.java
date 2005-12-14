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

package org.netbeans.modules.apisupport.project.ui.platform;

import java.io.File;
import org.netbeans.modules.apisupport.project.TestBase;
import org.netbeans.modules.apisupport.project.ui.platform.PlatformComponentFactory.NbPlatformListModel;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;

/**
 * @author Martin Krauskopf
 */
public class PlatformComponentFactoryTest extends TestBase {
    
    public PlatformComponentFactoryTest(String testName) {
        super(testName);
    }
    
    public void testNbPlatformListModelSorting() throws Exception {
        File first = new File(getWorkDir(), "first");
        TestBase.makePlatform(first);
        NbPlatform.addPlatform("first", first, "AAA first");

        File between = new File(getWorkDir(), "between");
        TestBase.makePlatform(between);
        NbPlatform.addPlatform("between", between, "KKK between");

        File last = new File(getWorkDir(), "last");
        TestBase.makePlatform(last);
        NbPlatform.addPlatform("last", last, "ZZZ last");
        
        NbPlatform.reset();
        
        NbPlatformListModel model = new NbPlatformListModel();
        assertEquals("four platforms " + NbPlatform.getPlatforms(), 5, model.getSize());
        assertSame("first (AAA first)", NbPlatform.getPlatformByID("first"), model.getElementAt(0));
        assertSame("second (Invalid Platform)", NbPlatform.getPlatformByID("custom"), model.getElementAt(1));
        assertSame("third (KKK between)", NbPlatform.getPlatformByID("between"), model.getElementAt(2));
        assertSame("fourth (NetBeans IDE....)", NbPlatform.getDefaultPlatform(), model.getElementAt(3));
        assertSame("fifth (ZZZ last)", NbPlatform.getPlatformByID("last"), model.getElementAt(4));
    }
    
}
