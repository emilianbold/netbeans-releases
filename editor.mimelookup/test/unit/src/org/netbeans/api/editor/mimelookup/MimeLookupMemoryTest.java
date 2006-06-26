/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2005 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.api.editor.mimelookup;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.mimelookup.DefaultMimeDataProvider;
import org.netbeans.modules.editor.mimelookup.EditorTestLookup;
import org.netbeans.modules.editor.mimelookup.TestUtilities;
import org.openide.util.Lookup;

/** 
 * 
 * @author Martin Roskanin, Vita Stejskal
 */
public class MimeLookupMemoryTest extends NbTestCase {

    public MimeLookupMemoryTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws java.lang.Exception {
        // Set up the default lookup, repository, etc.
        EditorTestLookup.setLookup(
            new String[] {
                "Services/org-netbeans-modules-editor-mimelookup-DummyMimeDataProvider.instance"
            },
            getWorkDir(),
            new Object[] {},
            getClass().getClassLoader(), 
            new Class [] { 
                DefaultMimeDataProvider.class, 
            }
        );
    }
    
    public void testSimple1() {
        MimePath path = MimePath.get("text/x-java");
        Lookup lookupA = MimeLookup.getLookup(path);
        Lookup lookupB = MimeLookup.getLookup(path);
        assertSame("Lookups are not reused", lookupA, lookupB);
    }

    public void testSimple2() {
        MimePath path = MimePath.get("text/x-java");
        int idA = System.identityHashCode(MimeLookup.getLookup(path));
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idB = System.identityHashCode(MimeLookup.getLookup(path));
        assertEquals("Lookup instance was lost", idA, idB);
    }

    public void testSimple3() {
        int idA = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idB = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        assertEquals("Lookup instance was lost", idA, idB);
    }
    
    public void testLookupsRelease() {
        int idA = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idB = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        assertEquals("Lookup instance was lost", idA, idB);
        
        // Force the MimePath instance to be dropped from the list of recently used
        for (int i = 0; i < MimePath.MAX_LRU_SIZE; i++) {
            MimePath.get("text/x-nonsense-" + i);
        }
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idC = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        assertTrue("Lookup instance was not released", idA != idC);
    }

    public void testLookupResultHoldsTheLookup() {
        MimePath path = MimePath.get("text/x-java");
        Lookup lookup = MimeLookup.getLookup(path);
        Lookup.Result lr = lookup.lookupResult(Object.class);
        
        int pathIdA = System.identityHashCode(path);
        int lookupIdA = System.identityHashCode(lookup);
        
        path = null;
        lookup = null;
        
        // Force the MimePath instance to be dropped from the list of recently used
        for (int i = 0; i < MimePath.MAX_LRU_SIZE; i++) {
            MimePath.get("text/x-nonsense-" + i);
        }
        
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();

        int pathIdB = System.identityHashCode(MimePath.get("text/x-java"));
        int lookupIdB = System.identityHashCode(MimeLookup.getLookup(MimePath.get("text/x-java")));
        
        assertEquals("MimePath instance was lost", pathIdA, pathIdB);
        assertEquals("Lookup instance was lost", lookupIdA, lookupIdB);
    }
}
