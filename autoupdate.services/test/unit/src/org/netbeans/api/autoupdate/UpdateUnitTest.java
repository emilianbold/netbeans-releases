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
package org.netbeans.api.autoupdate;

import java.util.List;
import org.netbeans.modules.autoupdate.services.UpdateManagerImpl;
import org.openide.modules.SpecificationVersion;
import org.openide.modules.SpecificationVersion;
import org.openide.modules.SpecificationVersion;

/**
 *
 * @author Radek Matous
 */
public class UpdateUnitTest extends DefaultTestCase {
    
    public UpdateUnitTest(String testName) {
        super(testName);
    }
    
    public UpdateUnit getUpdateUnit(String codeNameBase) {
        UpdateUnit uu = UpdateManagerImpl.getInstance().getUpdateUnit(codeNameBase);
        assertNotNull(uu);
        return uu;
    }
    public UpdateElement getAvailableUpdate(UpdateUnit updateUnit, int idx) {
        List<UpdateElement> available = updateUnit.getAvailableUpdates();
        assertTrue(available.size() > idx);
        return available.get(idx);
    }
    
    public void testAvailableUpdateSort() {        
        UpdateUnit engineUnit = getUpdateUnit("org.yourorghere.engine");
        assertNull("cannot be installed",engineUnit.getInstalled());
        UpdateElement engineElement = getAvailableUpdate(engineUnit,0);
        assertNotNull(engineElement);
        UpdateElement engineElement1 = getAvailableUpdate(engineUnit,1);
        assertNotNull(engineElement1);
        SpecificationVersion specVer = new SpecificationVersion(engineElement.getSpecificationVersion());        
        SpecificationVersion specVer1 = new SpecificationVersion(engineElement1.getSpecificationVersion());
        assertEquals(1, specVer.compareTo(specVer1));
    }
}
