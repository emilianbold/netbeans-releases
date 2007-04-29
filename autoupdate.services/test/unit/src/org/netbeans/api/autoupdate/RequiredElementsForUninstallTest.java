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
package org.netbeans.api.autoupdate;

import java.util.List;
import org.netbeans.Module;
import org.netbeans.api.autoupdate.OperationContainer.OperationInfo;
import org.netbeans.modules.autoupdate.services.OperationsTestImpl;
import org.netbeans.modules.autoupdate.services.UpdateManagerImpl;

/**
 *
 * @author Radek Matous
 */
public class RequiredElementsForUninstallTest extends OperationsTestImpl {

    public RequiredElementsForUninstallTest(String testName) {
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
    public void testSelf() throws Exception {
        OperationContainer<OperationSupport> installContainer = OperationContainer.createForDirectInstall();
        UpdateUnit engineUnit = getUpdateUnit("org.yourorghere.engine");
        assertNull("cannot be installed",engineUnit.getInstalled());
        UpdateElement engineElement = getAvailableUpdate(engineUnit,0);
        OperationInfo engineInfo = installContainer.add(engineElement);
        assertNotNull(engineInfo);

        UpdateUnit independentUnit = getUpdateUnit("org.yourorghere.independent");
        assertNull("cannot be installed",independentUnit.getInstalled());
        UpdateElement independentElement = getAvailableUpdate(independentUnit,0);
        OperationInfo independentInfo = installContainer.add(independentElement);
        assertNotNull(independentInfo);

        UpdateUnit dependingUnit = getUpdateUnit("org.yourorghere.depending");
        assertNull("cannot be installed",dependingUnit.getInstalled());
        UpdateElement dependingElement = getAvailableUpdate(dependingUnit,0);
        OperationInfo dependingInfo = installContainer.add(dependingElement);
        assertNotNull(dependingInfo);

        assertEquals(0, installContainer.listInvalid().size());
        assertEquals(3, installContainer.listAll().size());
        installModule(independentUnit, null);
        installModule(engineUnit, null);
        installModule(dependingUnit, null);

        Module independentModule = org.netbeans.modules.autoupdate.services.Utilities.toModule(independentUnit.getCodeName(), null);
        assertTrue(independentModule.isEnabled());        
        Module engineModule = org.netbeans.modules.autoupdate.services.Utilities.toModule(engineUnit.getCodeName(), null);
        assertTrue(engineModule.isEnabled());
        Module dependingModule = org.netbeans.modules.autoupdate.services.Utilities.toModule(dependingUnit.getCodeName(), null);
        assertTrue(dependingModule.isEnabled());
        OperationContainer<OperationSupport> uninstallContainer = OperationContainer.createForUninstall();
        independentInfo = uninstallContainer.add(independentUnit.getInstalled());
        assertEquals("engine && depending needs independent",2, independentInfo.getRequiredElements().size());
        
        uninstallContainer.add(engineUnit.getInstalled());
        assertEquals("engine && depending needs independent",1, independentInfo.getRequiredElements().size());
        
        uninstallContainer.add(dependingUnit.getInstalled());
        assertEquals("engine && depending needs independent",0, independentInfo.getRequiredElements().size());        
    }
}
