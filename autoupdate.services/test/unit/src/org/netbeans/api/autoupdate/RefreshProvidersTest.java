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

import java.io.IOException;
import java.util.List;
import junit.framework.*;

/**
 * @author Radek Matous
 */
public class RefreshProvidersTest extends DefaultTestCase {
    
    public RefreshProvidersTest (String testName) {
        super (testName);
    }
    
    public void testRefreshProviders () throws IOException {
        List<UpdateUnitProvider> result = UpdateUnitProviderFactory.getDefault ().getUpdateUnitProviders (false);
        assertEquals(result.toString(), 2, result.size());
       
        int updateUnitsCount = UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).size();
        populateCatalog(TestUtils.class.getResourceAsStream("data/updates-subset.xml"));
        UpdateUnitProviderFactory.getDefault ().refreshProviders(null, true);
        assertEquals(UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).toString(), 
                updateUnitsCount - 2, UpdateManager.getDefault().getUpdateUnits(UpdateManager.TYPE.MODULE).size());
    }

}
