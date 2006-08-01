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
import org.netbeans.junit.NbTestCase;
import org.netbeans.spi.editor.mimelookup.Class2LayerFolder;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;

/**
 *
 * @author vita
 */
public class FolderPathLookupTest extends NbTestCase {

    /** Creates a new instance of FolderPathLookupTest */
    public FolderPathLookupTest(String name) {
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
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolder.instance");
        
        // Create lookup over an existing folder
        Lookup lookup = new FolderPathLookup(new String [] { "Tmp/A/B/C/D" });
        Collection instances = lookup.lookupAll(Class2LayerFolder.class);
        
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummyClass2LayerFolder.class, instances.iterator().next().getClass());
        
        // Now create lookup over a non-existing folder
        lookup = new FolderPathLookup(new String [] { "Tmp/X/Y/Z" });
        instances = lookup.lookupAll(Object.class);
        
        assertEquals("Wrong number of instances", 0, instances.size());
    }

    public void testAddingFolders() throws Exception {
        // Create lookup over a non-existing folder
        Lookup lookup = new FolderPathLookup(new String [] { "Tmp/A/B/C/D" });
        Collection instances = lookup.lookupAll(Class2LayerFolder.class);
        
        assertEquals("Wrong number of instances", 0, instances.size());

        // Create the folder and the instance
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolder.instance");
        
        instances = lookup.lookupAll(Class2LayerFolder.class);
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummyClass2LayerFolder.class, instances.iterator().next().getClass());
    }

    public void testRemovingFolders() throws Exception {
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolder.instance");
        
        // Create lookup over an existing folder
        Lookup lookup = new FolderPathLookup(new String [] { "Tmp/A/B/C/D" });
        Collection instances = lookup.lookupAll(Class2LayerFolder.class);
        
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummyClass2LayerFolder.class, instances.iterator().next().getClass());

        // Delete the folders
        TestUtilities.deleteFile(getWorkDir(), "Tmp");
        
        instances = lookup.lookupAll(Class2LayerFolder.class);
        assertEquals("Wrong number of instances", 0, instances.size());
    }
    
    public void testChangeEvents() throws Exception {
        Lookup.Result lr = new FolderPathLookup(new String [] { "Tmp/A/B/C/D" }).lookupResult(Class2LayerFolder.class);
        L listener = new L();
        lr.addLookupListener(listener);

        Collection instances = lr.allInstances();
        assertEquals("Wrong number of instances", 0, instances.size());

        // Create the folder and the instance
        TestUtilities.createFile(getWorkDir(), "Tmp/A/B/C/D/org-netbeans-modules-editor-mimelookup-impl-DummyClass2LayerFolder.instance");

        assertEquals("Wrong number of events", 1, listener.resultChangedCnt);
        
        instances = lr.allInstances();
        assertEquals("Wrong number of instances", 1, instances.size());
        assertEquals("Wrong instance", DummyClass2LayerFolder.class, instances.iterator().next().getClass());

        // Reset the listener
        listener.resultChangedCnt = 0;

        // Delete the folders
        TestUtilities.deleteFile(getWorkDir(), "Tmp");

        assertEquals("Wrong number of events", 1, listener.resultChangedCnt);
        
        instances = lr.allInstances();
        assertEquals("Wrong number of instances", 0, instances.size());
    }
    
    private static final class L implements LookupListener {
        public int resultChangedCnt = 0;
        public void resultChanged(LookupEvent ev) {
            resultChangedCnt++;
        }
    }
}
