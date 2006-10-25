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
import java.util.prefs.PreferencesFactory;
import org.netbeans.junit.MockServices;
import org.netbeans.junit.NbTestCase;

/**
 * @author Radek Matous
 */
public class TestNbPreferencesFactory extends NbPreferencesTest.TestBasicSetup {
    public TestNbPreferencesFactory(String testName) {
        super(testName);
    }

    /**
     * Test of userRoot method
     */
    public void testUserRoot() {
        assertSame(new NbPreferencesFactory().userRoot().getClass(), Preferences.userRoot().getClass());
        assertNotNull(Preferences.userRoot());
        assertTrue(Preferences.userRoot().isUserNode());
        assertTrue(Preferences.userRoot() instanceof NbPreferences);
    }
    
    /**
     * Test of systemRoot method
     */
    public void testSystemRoot() {
        assertSame(new NbPreferencesFactory().systemRoot(), Preferences.systemRoot());
        assertNotNull(Preferences.systemRoot());
        assertFalse(Preferences.systemRoot().isUserNode());
        assertTrue(Preferences.systemRoot() instanceof NbPreferences);
    }

    protected void setUp() throws Exception {
        super.setUp();
    }
}
