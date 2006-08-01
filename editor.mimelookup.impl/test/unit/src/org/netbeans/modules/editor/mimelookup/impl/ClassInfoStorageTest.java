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

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Set;
import org.netbeans.junit.NbTestCase;

/**
 *
 * @author vita
 */
public class ClassInfoStorageTest extends NbTestCase {

    /** Creates a new instance of ClassPathsTest */
    public ClassInfoStorageTest(String name) {
        super(name);
    }

    protected void setUp() throws java.lang.Exception {
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(new String[0], getWorkDir(), new Object[] {},
            getClass().getClassLoader(),
            null
        );
    }
    
    public void testNoMapper() {
        ClassInfoStorage.Info info = ClassInfoStorage.getInstance().getInfo(DummySetting.class.getName());
        assertTrue("There should be no mapper registered", isEmpty(info));
    }
    
    public void testDummyMapper() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolderWithInstanceProvider.instance");
        TestUtilities.sleepForWhile();
        
        ClassInfoStorage.Info info = ClassInfoStorage.getInstance().getInfo(DummySetting.class.getName());
        assertNotNull("The  mapper not found", info);
        assertEquals("Wrang mapper class", DummySetting.class.getName(), info.getClassName());
        assertEquals("Wrong wrapper extra path", "DummyFolder", info.getExtraPath());
        assertEquals("Wrong instance provider class", DummyInstanceProvider.class.getName(), info.getInstanceProviderClass());
        assertNotNull("Instance provider should not be null", info.getInstanceProvider());
        assertTrue("Wrong instance provider", info.getInstanceProvider() instanceof DummyInstanceProvider);
    }

    public void testAddingMapper() throws Exception {
        ClassInfoStorage.Info info = ClassInfoStorage.getInstance().getInfo(DummySetting.class.getName());
        assertTrue("There should be no mapper registered", isEmpty(info));
        
        L listener = new L();
        ClassInfoStorage.getInstance().addPropertyChangeListener(listener);
        try {
            // Add the mapper
            TestUtilities.createFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolderWithInstanceProvider.instance");
            TestUtilities.sleepForWhile();
            
            assertEquals("Wrong number of change events", 1, listener.changeEventsCnt);
            assertNotNull("Invalid change event", listener.events.get(0));
            assertEquals("Wrong change event", 
                ClassInfoStorage.PROP_CLASS_INFO_ADDED, ((PropertyChangeEvent)listener.events.get(0)).getPropertyName());
            
            Set value = (Set) ((PropertyChangeEvent)listener.events.get(0)).getNewValue();
            assertEquals("Invalid number of class in the change event", 1, value.size());
            assertTrue("Wrong change event value", value.contains(DummySetting.class.getName()));
        } finally {
            ClassInfoStorage.getInstance().removePropertyChangeListener(listener);
        }

        info = ClassInfoStorage.getInstance().getInfo(DummySetting.class.getName());
        checkInfo(info, "DummyFolder", DummySetting.class, DummyInstanceProvider.class);
    }

    public void testRemovingMapper() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolderWithInstanceProvider.instance");
        TestUtilities.sleepForWhile();
        
        ClassInfoStorage.Info info = ClassInfoStorage.getInstance().getInfo(DummySetting.class.getName());
        checkInfo(info, "DummyFolder", DummySetting.class, DummyInstanceProvider.class);
        
        L listener = new L();
        ClassInfoStorage.getInstance().addPropertyChangeListener(listener);
        try {
            // Remove the mapper
            TestUtilities.deleteFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolderWithInstanceProvider.instance");
            TestUtilities.sleepForWhile();
            
            assertEquals("Wrong number of change events", 1, listener.changeEventsCnt);
            assertNotNull("Invalid change event", listener.events.get(0));
            assertEquals("Wrong change event", 
                ClassInfoStorage.PROP_CLASS_INFO_REMOVED, ((PropertyChangeEvent)listener.events.get(0)).getPropertyName());
            
            Set value = (Set) ((PropertyChangeEvent)listener.events.get(0)).getNewValue();
            assertEquals("Invalid number of class in the change event", 1, value.size());
            assertTrue("Wrong change event value", value.contains(DummySetting.class.getName()));
        } finally {
            ClassInfoStorage.getInstance().removePropertyChangeListener(listener);
        }

        info = ClassInfoStorage.getInstance().getInfo(DummySetting.class.getName());
        assertTrue("There should be no mapper registered", isEmpty(info));
    }
    
    private void checkInfo(ClassInfoStorage.Info info, String extraPath, Class infoClass, Class instanceProviderClass) {
        assertNotNull("The  mapper not found", info);
        assertEquals("Wrang mapper class", infoClass.getName(), info.getClassName());
        assertEquals("Wrong wrapper extra path", extraPath, info.getExtraPath());
        assertEquals("Wrong instance provider class", instanceProviderClass.getName(), info.getInstanceProviderClass());
        assertNotNull("Instance provider should not be null", info.getInstanceProvider());
        assertEquals("Wrong instance provider", instanceProviderClass, info.getInstanceProvider().getClass());
    }

    private boolean isEmpty(ClassInfoStorage.Info info) {
        return  info.getExtraPath().length() == 0 && 
                info.getInstanceProviderClass() == null &&
                info.getInstanceProvider() == null;
    }
    
    private static class L implements PropertyChangeListener {
        
        public int changeEventsCnt = 0;
        public ArrayList events = new ArrayList();
        
        public void propertyChange(PropertyChangeEvent evt) {
            changeEventsCnt++;
            events.add(evt);
        }
        
        public void reset() {
            changeEventsCnt = 0;
            events.clear();
        }
    }
}
