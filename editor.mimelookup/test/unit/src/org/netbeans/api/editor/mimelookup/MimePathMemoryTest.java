/*
 * DummyMemoryTest.java
 *
 * Created on June 13, 2006, 9:33 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.api.editor.mimelookup;

import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.editor.mimelookup.TestUtilities;

/**
 *
 * @author vita
 */
public class MimePathMemoryTest extends NbTestCase {
    
    /** Creates a new instance of DummyMemoryTest */
    public MimePathMemoryTest(String name) {
        super(name);
    }

    public void testSimple() {
        MimePath pathA = MimePath.get("text/x-java");
        MimePath pathB = MimePath.get("text/x-java");
        assertSame("MimePath instances are not cached and reused", pathA, pathB);
    }
    
    public void testListOfRecentlyUsed() {
        int idA = System.identityHashCode(MimePath.get("text/x-java"));

        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idB = System.identityHashCode(MimePath.get("text/x-java"));
        
        // The same instance of MimePath should still be in the cache
        assertEquals("The MimePath instance was lost", idA, idB);
        
        for (int i = 0; i < MimePath.MAX_LRU_SIZE; i++) {
            MimePath.get("text/x-nonsense-" + i);
        }
        
        // Now the original text/x-java MimePath should be discarded
        TestUtilities.consumeAllMemory();
        TestUtilities.gc();
        
        int idC = System.identityHashCode(MimePath.get("text/x-java"));
        assertTrue("The MimePath instance was not release", idA != idC);
    }
}
