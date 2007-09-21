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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import org.netbeans.api.autoupdate.DefaultTestCase;
import org.netbeans.api.autoupdate.UpdateManager;
import org.netbeans.api.autoupdate.UpdateUnit;

/**
 *
 * @author Radek Matous
 */
public class UpdateManagerImplTest extends DefaultTestCase {
    
    public UpdateManagerImplTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        keepItNotToGC = null;
    }
            
    public void testNoMemoryLeak() throws Exception {
        List<UpdateUnit> units = UpdateManagerImpl.getInstance().getUpdateUnits();
        assertTrue(units.size() != 0);
        Reference ref = UpdateManagerImpl.getInstance().getCacheReference();
        assertNotNull(ref);
        assertNotNull(ref.get());
        
        units = null;
        assertGC("", ref);        
        assertNotNull(ref);
        assertNull(ref.get());        
        
        units = UpdateManagerImpl.getInstance().getUpdateUnits();
        ref = UpdateManagerImpl.getInstance().getCacheReference();
        assertNotNull(ref);
        assertNotNull(ref.get());
        
        UpdateManagerImpl.getInstance().clearCache();
        ref = UpdateManagerImpl.getInstance().getCacheReference();
        assertNull(ref);        
    }    
}

