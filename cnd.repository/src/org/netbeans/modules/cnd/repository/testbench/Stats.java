/*
 * The contents of this file are subject to the terms of the Common Development
 * and Distribution License (the License). You may not use this file except in
 * compliance with the License.
 *
 * You can obtain a copy of the License at http://www.netbeans.org/cddl.html
 * or http://www.netbeans.org/cddl.txt.
 
 * When distributing Covered Code, include this CDDL Header Notice in each file
 * and include the License file at http://www.netbeans.org/cddl.txt.
 * If applicable, add the following below the CDDL Header, with the fields
 * enclosed by brackets [] replaced by your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.cnd.repository.testbench;

/**
 *
 * @author Sergey Grinev
 */
public class Stats {
    
    private Stats() {}
    
    public static int debugPut = 0;
    public static int debugGot = 0;
    public static int debugReadFromFile = 0;
    public static int debugNotFound = 0;
    public static int debugGotFromHardCache = 0;
    public static final boolean isDebug = getBoolean("cnd.repository.use.dev", false); //NOI18N
    public static final boolean verbosePut = getBoolean("cnd.repository.verbose.put", false); //NOI18N
    
    public static final boolean queueTiming = getBoolean("cnd.repository.queue.timing", false); //NOI18N
    public static final boolean queueTrace = getBoolean("cnd.repository.queue.trace", false); //NOI18N
    public static final boolean useThreading = getBoolean("cnd.repository.threading", false); //NOI18N
    
    public static final boolean writeToASingleFile = getBoolean("cnd.repository.1file", false); //NOI18N
            
    public static boolean getBoolean(String name, boolean result) {
        String text = System.getProperty(name);
        if( text != null ) {
            result = Boolean.parseBoolean(text);
        }
        return result;
    } 
    
    public static void report() {
        log(
            "Put: " + debugPut + //NOI18N
            "; Got: " + debugGot + //NOI18N
            "; Read: " + debugReadFromFile + //NOI18N
            "; N/A: " + debugNotFound //NOI18N
            + "; Hard: " + debugGotFromHardCache //NOI18N
            );
    }
   public static void log (String st)
   {
       if (isDebug) System.err.println("DEBUG [Repository] " + st); //NOI18N
   }
}
