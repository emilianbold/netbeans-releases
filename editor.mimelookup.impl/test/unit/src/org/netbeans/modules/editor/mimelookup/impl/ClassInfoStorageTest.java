/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2007 Sun Microsystems, Inc. All rights reserved.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */

package org.netbeans.modules.editor.mimelookup.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.netbeans.junit.NbTestCase;
import org.netbeans.junit.RandomlyFails;

/**
 *
 * @author vita
 */
@RandomlyFails // uses TestUtilities.sleepForWhile()
public class ClassInfoStorageTest extends NbTestCase {

    public ClassInfoStorageTest(String name) {
        super(name);
    }

    @Override
    protected void setUp() throws Exception {
        clearWorkDir();
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
        assertTrue("There should be no mapper registered: " + info, isEmpty(info));
        
        L listener = new L();
        ClassInfoStorage.getInstance().addPropertyChangeListener(listener);
        try {
            // Add the mapper
            TestUtilities.createFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolderWithInstanceProvider.instance");
            TestUtilities.sleepForWhile();
            
            assertEquals("Wrong number of change events", 1, listener.changeEventsCnt);
            assertNotNull("Invalid change event", listener.events.get(0));
            assertEquals("Wrong change event", 
                ClassInfoStorage.PROP_CLASS_INFO_ADDED, listener.events.get(0).getPropertyName());
            
            Set value = (Set) listener.events.get(0).getNewValue();
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
                ClassInfoStorage.PROP_CLASS_INFO_REMOVED, listener.events.get(0).getPropertyName());
            
            Set value = (Set) listener.events.get(0).getNewValue();
            assertEquals("Invalid number of class in the change event", 1, value.size());
            assertTrue("Wrong change event value", value.contains(DummySetting.class.getName()));
        } finally {
            ClassInfoStorage.getInstance().removePropertyChangeListener(listener);
        }

        info = ClassInfoStorage.getInstance().getInfo(DummySetting.class.getName());
        assertTrue("There should be no mapper registered: " + info, isEmpty(info));
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
        public List<PropertyChangeEvent> events = new ArrayList<PropertyChangeEvent>();
        
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
