/**
 * AnagramsMBean.java
 *
 * Created on Wed May 11 09:37:26 MEST 2005
 */
package com.toy.anagrams.mbeans;

/**
 * Interface AnagramsStatsMBean
 * Anagrams Description
 */
public interface AnagramsStatsMBean
{
   /**
    * Get The time it tooks for a user to resolve the last anagram
    */
    public int getLastThinkingTime();
    
   /**
    * Get The maximum time it tooks for a user to resolve an anagram
    */
    public int getMaxThinkingTime();

   /**
    * Get The minimum time it tooks for a user to resolve an anagram
    */
    public int getMinThinkingTime();

   /**
    * Get Number of resolved anagrams
    */
    public int getNumResolvedAnagrams();

   /**
    * Get The current anagram
    */
    public String getCurrentAnagram();

   /**
    * Resety all thinking related counters
    *
    */
    public void resetThinkingTimes();

}
