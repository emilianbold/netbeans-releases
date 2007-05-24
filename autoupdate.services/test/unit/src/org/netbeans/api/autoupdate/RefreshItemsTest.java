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

import java.io.IOException;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.autoupdate.services.UpdateManagerImpl;

/**
 * @author Jirka Rechtacek
 */
public class RefreshItemsTest extends DefaultTestCase {
    
    public RefreshItemsTest (String testName) {
        super (testName);
    }
    
    public void testRefreshItems () throws IOException {
        List<UpdateUnitProvider> result = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);
        assertEquals(result.toString(), 2, result.size());
        int updateUnitsCount = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).size();
        
        UpdateUnit toTest = UpdateManagerImpl.getInstance ().getUpdateUnit ("org.yourorghere.refresh-providers-test");
        assertNotNull ("UpdateUnit for org.yourorghere.refresh-providers-test found.", toTest);
        UpdateElement toTestElement = toTest.getAvailableUpdates().get (0);
        assertNotNull ("UpdateElement for org.yourorghere.refresh-providers-test found.", toTestElement);
        assertTrue (toTestElement + " needs restart.", toTestElement.impl.getInstallInfo().needsRestart ());
        
        populateCatalog(TestUtils.class.getResourceAsStream("data/updates-subset.xml"));
        UpdateUnitProviderFactory.getDefault ().refreshProviders(null, true);
        assertEquals(UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).toString(), 
                updateUnitsCount-1, UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).size());
        
        UpdateUnit toTestAgain = UpdateManagerImpl.getInstance ().getUpdateUnit ("org.yourorghere.refresh-providers-test");
        assertNotNull ("Unit for org.yourorghere.refresh-providers-test found.", toTestAgain);
        
        UpdateElement toTestAgainElement = toTestAgain.getAvailableUpdates().get (0);
        assertNotNull ("UpdateElement for org.yourorghere.refresh-providers-test found.", toTestAgainElement);
        
        assertFalse ("First unit is not as same as second unit.", 
                System.identityHashCode(toTest) == System.identityHashCode(toTestAgain));
        assertFalse ("First element is not as same as second element.",
                System.identityHashCode(toTestElement) == System.identityHashCode(toTestAgainElement));
        assertFalse ("IMPLS: First unit is not as same as second unit.", 
                System.identityHashCode(toTest.impl) == System.identityHashCode(toTestAgain.impl));
        assertFalse ("IMPLS: First element is not as same as second element.",
                System.identityHashCode(toTestElement.impl) == System.identityHashCode(toTestAgainElement.impl));
        
        //assertFalse ("First unit is not as same as second unit.", toTest.equals (toTestAgain));
        //assertFalse ("First element is not as same as second element.", toTestElement.equals (toTestAgainElement));
        
        assertFalse (toTestAgainElement + " doesn't need restart now.", toTestAgainElement.impl.getInstallInfo ().needsRestart ());
    }
}
