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

import java.io.IOException;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

/**
 *
 * @author Radek Matous
 */
public class TestPropertiesStorage extends TestFileStorage {
    private PropertiesStorage storage;
    private NbPreferences pref;
    
    public TestPropertiesStorage(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        assertSame(new NbPreferencesFactory().userRoot(), Preferences.userRoot());
        pref = getPreferencesNode();
        assertNotNull(pref);
        storage = (PropertiesStorage)pref.fileStorage;
        assertNotNull(storage);        
    }
    
    protected NbPreferences.FileStorage getInstance() {
        return PropertiesStorage.instanceReadOnly("/PropertiesStorageTest/" + getName());//NOI18N);
    }
    
    void noFileRepresentationAssertion() throws IOException {
        super.noFileRepresentationAssertion();
        assertNull(((PropertiesStorage)instance).toFolder());
        assertNull(((PropertiesStorage)instance).toPropertiesFile());
    }
    
    void fileRepresentationAssertion() throws IOException {
        super.fileRepresentationAssertion();
        assertNotNull(((PropertiesStorage)instance).toFolder());
        assertNotNull(((PropertiesStorage)instance).toPropertiesFile());
    }
    
    private NbPreferences getPreferencesNode() {
        return (NbPreferences)Preferences.userNodeForPackage(TestPropertiesStorage.class).node(getName());
    }
    
    public void testNode() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());
        pref.flush();
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());        
        pref.put("key","value");
    }
    
    public void testNode2() throws BackingStoreException {
        testNode();
        pref.flush();
        assertNotNull(storage.toPropertiesFile());
    }
    
    public void testNode3() throws BackingStoreException {
        testNode();
        pref.flushTask.waitFinished();
        assertNotNull(storage.toPropertiesFile());                
    }

    public void testNode4() throws BackingStoreException {
        pref.node("a");
        testNode();
    }

    public void testNode5() throws BackingStoreException {
        Preferences child = pref.node("a");
        child.put("key","value");
        child.flush();
        assertNotNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                
    }

    public void testRemove() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                
        
        pref.put("key","value");
        pref.flush();
        assertNotNull(storage.toPropertiesFile());
        pref.remove("key");
        assertTrue(pref.properties.isEmpty());
        pref.flush();
        assertNull(storage.toPropertiesFile());                
    }

    public void testRemove2() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());                

        pref.put("key","value");
        pref.put("key1","value1");

        pref.flush();
        assertNotNull(storage.toPropertiesFile());
        pref.remove("key");
        assertFalse(pref.properties.isEmpty());
        pref.flush();
        assertNotNull(storage.toPropertiesFile());                
    }

    public void testClear() throws BackingStoreException {
        assertNull(storage.toFolder());
        assertNull(storage.toPropertiesFile());
        pref.put("key","value");
        pref.put("key1","value");
        pref.put("key2","value");
        pref.put("key3","value");
        pref.put("key5","value");
        pref.flush();
        assertNotNull(storage.toPropertiesFile());                

        pref.clear();
        pref.flush();
        assertNull(storage.toPropertiesFile());                
    }
    
}
