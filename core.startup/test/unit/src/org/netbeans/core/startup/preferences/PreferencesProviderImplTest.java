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

package org.netbeans.core.startup.preferences;

import java.util.prefs.Preferences;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author rmatous
 */
public class PreferencesProviderImplTest extends NbPreferencesTest.BasicSetupTest {    
    public PreferencesProviderImplTest(String testName) {
        super(testName);
    }

    /**
     * Test of preferencesForModule method, of class org.netbeans.core.startup.preferences.PreferencesProviderImpl.
     */
    public void testPreferencesForModule() {
        PreferencesProviderImpl instance = new PreferencesProviderImpl();        
        Preferences result = instance.preferencesForModule(getClass());
        assertNotNull(result);
        assertTrue(NbPreferences.class.isAssignableFrom(result.getClass()));
        assertEquals(result.absolutePath(),"/"+getClass().getPackage().getName().replace('.','/'));
    }

    /**
     * Test of preferencesRoot method, of class org.netbeans.core.startup.preferences.PreferencesProviderImpl.
     */
    public void testPreferencesRoot() {        
        PreferencesProviderImpl instance = new PreferencesProviderImpl();        
        Preferences result = instance.preferencesRoot();
        assertNotNull(result);
        assertTrue(NbPreferences.class.isAssignableFrom(result.getClass()));
        assertEquals(result.absolutePath(),"/");
    }
    
}
