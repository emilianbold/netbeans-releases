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
