/*
 * WordLibraryTest.java
 * JUnit based test
 *
 * Created on August 5, 2004, 3:07 PM
 */

package com.toy.anagrams.lib;

import junit.framework.*;


public class WordLibraryTest extends TestCase {
    
    public WordLibraryTest(java.lang.String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(WordLibraryTest.class);
        return suite;
    }
    

    /**
     * Test of isCorrect method, of class com.toy.anagrams.lib.WordLibrary.
     */
    public void testIsCorrect() {
        WordLibrary library = new WordLibrary ();
        for (int i=0; i < library.getSize(); i++) {
            String ow = library.getWord(i);
            String sw = library.getScrambledWord(i);
            assertTrue("Scrambled word: "+sw+" should be: "+ow, library.isCorrect(i, ow));
        }
    }        
    
}
