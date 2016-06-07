/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2005, 2016 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
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
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
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
 *
 * Contributor(s):
 */
/**
 * AnagramsStats.java
 *
 * Created on Wed May 11 09:37:26 MEST 2005
 */
package com.toy.anagrams.mbeans;

import javax.management.*;

/**
 * Class AnagramsStats
 * Anagrams MBean
 */
public class AnagramsStats implements AnagramsStatsMBean, NotificationEmitter
{
    /** Attribute : LastThinkingTime */
    private int lastThinkingTime = 0;

    /** Attribute : MaxThinkingTime */
    private int maxThinkingTime = 0;

    /** Attribute : MinThinkingTime */
    private int minThinkingTime = 0;

    /** Attribute : NumResolvedAnagrams */
    private int NumResolvedAnagrams = 0;

    /** Attribute : CurrentAnagram */
    private String currentAnagram = null;

   /* Creates a new instance of AnagramsStats */
    public AnagramsStats()
    {
    }

    /**
    * Get The time it tooks for a user to resolve the last anagram
    */
    public int getLastThinkingTime() {
        return lastThinkingTime;
    }

   /**
    * Get The maximum time it tooks for a user to resolve an anagram
    */
    public int getMaxThinkingTime()
    {
        return maxThinkingTime;
    }

   /**
    * Get The minimum time it tooks for a user to resolve an anagram
    */
    public int getMinThinkingTime()
    {
        return minThinkingTime;
    }

   /**
    * Get Number of resolved anagrams
    */
    public int getNumResolvedAnagrams()
    {
        return NumResolvedAnagrams;
    }

   /**
    * Get The current anagram
    */
    public String getCurrentAnagram()
    {
        return currentAnagram;
    }

   /**
    * Resety all thinking related counters
    */
    public void resetThinkingTimes()
    {
        //TODO Add the operation implementation
        minThinkingTime = 0;
        maxThinkingTime = 0;
        lastThinkingTime = 0;
    }

    /**
     * Methods exposed to Anagrams UI components to feed management with data.
     */
    
    /*
     * A new Anagram is porposed to the user. He starts thinking...
     */
    public void startThinking() {
        startTime = System.currentTimeMillis();
    }

    /*
     * An Anagram has been resolved.
     */
    public void stopThinking() {
        
        //Update the number of resolved anagrams
        NumResolvedAnagrams++;
        
        // Computes max and min
        long stopTime = System.currentTimeMillis();
        long oldMin = minThinkingTime;
        lastThinkingTime = (int) (stopTime - startTime) / 1000 ;
        minThinkingTime = (lastThinkingTime < minThinkingTime) || minThinkingTime == 0 ? lastThinkingTime : minThinkingTime;
        maxThinkingTime = lastThinkingTime > maxThinkingTime ? lastThinkingTime : maxThinkingTime;  
        
        //Create a JMX Notification
        Notification notification = new Notification(AttributeChangeNotification.ATTRIBUTE_CHANGE,
                this,
                getNextSeqNumber(),
                "Anagram resolved!");
        
        // Send a JMX notification.
        broadcaster.sendNotification(notification);
    }
    
    /**
     * Updates the current anagram coputed by the application
     */
    public void setCurrentAnagram(String currentAnagram) {
        this.currentAnagram = currentAnagram;
    }
    
   /**
    * MBean Notification support
    * You shouldn't update these methods
    */
   // <editor-fold defaultstate="collapsed" desc=" Generated Code ">
    public void addNotificationListener(NotificationListener listener,
       NotificationFilter filter, Object handback)
       throws IllegalArgumentException {
         broadcaster.addNotificationListener(listener, filter, handback);
    }

    public MBeanNotificationInfo[] getNotificationInfo() {
         return new MBeanNotificationInfo[] {
               new MBeanNotificationInfo(new String[] {
                      AttributeChangeNotification.ATTRIBUTE_CHANGE},
                      javax.management.Notification.class.getName(),
                      "When an anagram is resolved time")
                };
    }

    public void removeNotificationListener(NotificationListener listener)
       throws ListenerNotFoundException {
         broadcaster.removeNotificationListener(listener);
    }

    public void removeNotificationListener(NotificationListener listener,
       NotificationFilter filter, Object handback)
       throws ListenerNotFoundException {
         broadcaster.removeNotificationListener(listener, filter, handback);
    }
    
    private synchronized long getNextSeqNumber() {
        return seqNumber++;
    }
    
    private long seqNumber;
       // </editor-fold>
    
    private final NotificationBroadcasterSupport broadcaster =
               new NotificationBroadcasterSupport();

    //Stores the time a new anagram is proposed to the user.
    private long startTime;
}
