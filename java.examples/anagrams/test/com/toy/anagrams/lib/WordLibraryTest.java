/* Anagram Game Application - test
 *
 * WordLibraryTest
 *
 * Created on March 12, 2003, 11:26 AM
 */

package com.toy.anagrams.lib;

import java.util.Arrays;
import junit.framework.*;

public class WordLibraryTest extends TestCase {
    
    public WordLibraryTest(String testName) {
        super(testName);
    }
    
    /**
     * Test of isCorrect method, of class com.toy.anagrams.lib.WordLibrary.
     */
    public void testIsCorrect() {
        for (int i=0; i < WordLibrary.getSize(); i++) {
            String clearWord = WordLibrary.getWord(i);            
            String scrambledWord = WordLibrary.getScrambledWord(i);            
            assertTrue("Scrambled word \"" + scrambledWord + 
                       "\" at index: " + i +
                       " does not represent the word \"" + clearWord + "\"!", 
                       isAnagram(clearWord, scrambledWord));            
        }
    }
    
    
    /** Tests whether given anagram reperesents the word
     * @param clearWord The word in clear text
     * @param scrambledWord Scrambled version of the word
     * @return true if the scrambledWord is correct anagram of clearWord
     */
    private boolean isAnagram( String clearWord, String scrambledWord ) {
        
        char clearArray[] = clearWord.toCharArray();
        char scrambledArray[] = scrambledWord.toCharArray();
         
        Arrays.sort(clearArray);        
        Arrays.sort(scrambledArray);
        
        return Arrays.equals(clearArray, scrambledArray);
        
    }
    
    
}
