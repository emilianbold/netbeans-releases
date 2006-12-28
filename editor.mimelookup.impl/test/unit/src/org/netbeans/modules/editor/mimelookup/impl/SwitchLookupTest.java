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

import java.util.Collection;
import java.util.List;
import org.netbeans.api.editor.mimelookup.MimePath;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author vita
 */
public class SwitchLookupTest extends NbTestCase {

    /** Creates a new instance of FolderPathLookupTest */
    public SwitchLookupTest(String name) {
        super(name);
    }

    protected void setUp() throws java.lang.Exception {
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(new String[0], getWorkDir(), new Object[] {},
            getClass().getClassLoader(), 
            null
        );
    }
    
    protected void tearDown() {
        TestUtilities.gc();
    }
    
    public void testSimple() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        // Creating lookup for an existing mime path
        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Collection instances = lookup.lookupAll(DummySetting.class);
        
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
        
        // Now create lookup over a non-existing mime path
        lookup = new SwitchLookup(MimePath.parse("text/xml"));
        instances = lookup.lookupAll(Object.class);
        
        assertEquals("Wrong number of instances", 0, instances.size());
    }

    public void testAddingMimePath() throws Exception {
        // Create lookup over a non-existing mime path
        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Lookup.Result result = lookup.lookupResult(DummySetting.class);
        L listener = new L();

        result.addLookupListener(listener);
        Collection instances = result.allInstances();
        
        assertEquals("There should be no change events", 0, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 0, instances.size());

        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        // Lookup the instances again
        instances = lookup.lookupAll(DummySetting.class);
        
        assertEquals("Wrong number of change events", 1, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
    }

    public void testRemovingMimePath() throws Exception {
        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        // Create lookup over an existing mime path
        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Lookup.Result result = lookup.lookupResult(DummySetting.class);
        L listener = new L();
        
        result.addLookupListener(listener);
        Collection instances = result.allInstances();

        assertEquals("There should be no change events", 0, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
        
        // Delete the mime path folder
        TestUtilities.deleteFile(getWorkDir(), "Editors/text/x-jsp/text");
        TestUtilities.sleepForWhile();

        // Lookup the instances again
        instances = lookup.lookupAll(DummySetting.class);
        
        assertEquals("Wrong number of change events", 1, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 0, instances.size());
    }

    // test hierarchy - instances in lower levels are not visible in higher levels,
    // but instances from higher levels are visible in lower levels
    
    public void testHierarchyInheritance() throws Exception {
        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/");
        TestUtilities.sleepForWhile();

        {
            Lookup jspLookup = new SwitchLookup(MimePath.parse("text/x-jsp"));
            Collection jspInstances = jspLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, jspInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, jspInstances.iterator().next().getClass());
        }
        
        {
            Lookup javaLookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
            Collection javaInstances = javaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, javaInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, javaInstances.iterator().next().getClass());
        }
    }

    public void testHierarchyRootInheritance() throws Exception {
        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/");
        TestUtilities.createFile(getWorkDir(), "Editors/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        {
            Lookup lookup = new SwitchLookup(MimePath.parse(""));
            Collection instances = lookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, instances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
        }
        
        {
            Lookup jspLookup = new SwitchLookup(MimePath.parse("text/x-jsp"));
            Collection jspInstances = jspLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, jspInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, jspInstances.iterator().next().getClass());
        }
        
        {
            Lookup javaLookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
            Collection javaInstances = javaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, javaInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, javaInstances.iterator().next().getClass());
        }
    }
    
    public void testHierarchyLeaks() throws Exception {
        // Create the mime path folders and add some instance
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/");
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/text/x-java/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-java/");
        TestUtilities.sleepForWhile();

        {
            Lookup jspLookup = new SwitchLookup(MimePath.parse("text/x-jsp"));
            Collection jspInstances = jspLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 0, jspInstances.size());
        }
        
        {
            Lookup javaLookup = new SwitchLookup(MimePath.parse("text/x-java"));
            Collection javaInstances = javaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 0, javaInstances.size());
        }
        
        {
            Lookup jspJavaLookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
            Collection jspJavaInstances = jspJavaLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 1, jspJavaInstances.size());
            assertEquals("Wrong instance", DummySettingImpl.class, jspJavaInstances.iterator().next().getClass());
        }

        {
            Lookup javaJspLookup = new SwitchLookup(MimePath.parse("text/x-java/text/x-jsp"));
            Collection javaJspInstances = javaJspLookup.lookupAll(DummySetting.class);
            assertEquals("Wrong number of instances", 0, javaJspInstances.size());
        }
    }
    
    // test that FolderPathLookups are shared and discarded when they are not needed anymore
    
    // test that instances of a class with a Class2LayerFolder provider are really read from the proper folder
    
    public void testReadFromSpecialFolders() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/DummyFolder/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.createFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolder.instance");
        TestUtilities.sleepForWhile();

        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Collection instances = lookup.lookupAll(DummySetting.class);
        
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
    }

    // test that adding/removing a Class2LayerFolder provider updates the lookup for its class
    
    public void testChangeInMappers() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Editors/text/x-jsp/DummyFolder/org-netbeans-modules-editor-mimelookup-impl-DummySettingImpl.instance");
        TestUtilities.sleepForWhile();

        Lookup lookup = new SwitchLookup(MimePath.parse("text/x-jsp/text/x-java"));
        Lookup.Result result = lookup.lookupResult(DummySetting.class);
        L listener = new L();
        
        result.addLookupListener(listener);
        Collection instances = result.allInstances();
        
        assertEquals("Wrong number of change events", 0, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 0, instances.size());

        // Add the mapper
        TestUtilities.createFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolder.instance");
        TestUtilities.sleepForWhile();

        instances = result.allInstances();
        assertEquals("Wrong number of change events", 1, listener.resultChangedCnt);
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummySettingImpl.class, instances.iterator().next().getClass());
        
        // Reset the listener
        listener.resultChangedCnt = 0;
        
        // Remove the mapper
        TestUtilities.deleteFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolder.instance");
        TestUtilities.sleepForWhile();

        assertEquals("Wrong number of change events", 1, listener.resultChangedCnt);
        instances = result.allInstances();
        assertEquals("Wrong number of instances", 0, instances.size());
    }

    // Test mime path -> path[] conversion
    
    public void testNoMapper() {
        MimePath mimePath = MimePath.parse("text/x-jsp/text/x-java/text/x-javadoc");
        List paths = SwitchLookup.computePaths(mimePath, ClassInfoStorage.getInstance().getInfo(DummySetting.class.getName()).getExtraPath());
        checkPaths(mimePath, null, paths);
    }
    
    public void testDummyMapper() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Services/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolder.instance");
        TestUtilities.sleepForWhile();
        
        MimePath mimePath = MimePath.parse("text/x-jsp/text/x-java/text/x-javadoc");
        List paths = SwitchLookup.computePaths(mimePath, ClassInfoStorage.getInstance().getInfo(DummySetting.class.getName()).getExtraPath());
        checkPaths(mimePath, "DummyFolder", paths);
    }
    
    private void checkPaths(MimePath mimePath, String extraPath, List paths) {
        int j = 0;
        for (int i = mimePath.size(); i >= 0; i--) {
            String path1 = mimePath.getPrefix(i).getPath();
            String path2 = (String) paths.get(j++);
            
            if (path1.length() != 0) {
                path1 = SwitchLookup.ROOT_FOLDER + "/" + path1;
            } else {
                path1 = SwitchLookup.ROOT_FOLDER;
            }

            if (extraPath != null) {
                path1 += "/" + extraPath;
            }
            
            assertEquals("Invalid path", path1, path2);
        }
    }
    
    private static final class L implements LookupListener {
        public int resultChangedCnt = 0;
        public void resultChanged(LookupEvent ev) {
            resultChangedCnt++;
        }
    }
}
