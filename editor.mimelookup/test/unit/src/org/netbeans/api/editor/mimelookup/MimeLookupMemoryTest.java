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
